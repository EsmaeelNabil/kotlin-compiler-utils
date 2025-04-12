package dev.supersam

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import dev.supersam.plugin.CompiluginCommandLineProcessor
import dev.supersam.plugin.CompiluginComponentRegistrar
import dev.supersam.plugin.cliOptions
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.JvmTarget
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import dev.supersam.util.COMPOSE_MODIFIER_WRAPPER_ENABLED
import dev.supersam.util.COMPOSE_MODIFIER_WRAPPER_PATH
import dev.supersam.util.ENABLED
import dev.supersam.util.FUNCTIONS_VISITOR_ENABLED
import dev.supersam.util.FUNCTIONS_VISITOR_ANNOTATION
import dev.supersam.util.FUNCTIONS_VISITOR_PATH
import dev.supersam.util.LOGGING
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@OptIn(ExperimentalCompilerApi::class)
class SimpleFunctionVisitorTest {

    @Rule
    @JvmField
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

    // Define the TrackIt annotation that will trigger our visitor
    private val trackFunctionAnnotation = kotlin(
        "TrackIt.kt",
        """
        package dev.supersam.test
        
        import kotlin.annotation.AnnotationRetention.RUNTIME
        import kotlin.annotation.AnnotationTarget.FUNCTION
        
        @Retention(RUNTIME)
        @Target(FUNCTION)
        annotation class TrackIt
        """
    )

    // Define our functions visitor object that will be called by the plugin
    // This version uses println for verification instead of trying to store state
    private val functionsVisitorImplementation = kotlin(
        "FunctionsVisitor.kt",
        """
        package dev.supersam.test
        
        object FunctionsVisitor {
            // This is the function our plugin will call
            fun visit(
                functionName: String,
                classPath: String,
                signature: String,
                parameters: Map<String, Any?>
            ) {
                println("VISITOR_LOG: Function called: " + classPath + "." + functionName)
                println("VISITOR_LOG: Parameters: " + parameters.keys.joinToString(", "))
            }
            
            fun clear() {
                // No state to clear
            }
        }
        """
    )

    // Define mock compose modifier wrapper
    private val mockModifierHelper = kotlin(
        "ModifierHelper.kt",
        """
        package dev.supersam.test
        
        object ModifierHelper {
            fun wrapModifier(functionName: String): Any {
                println("MODIFIER_LOG: Wrapping modifier for: " + functionName)
                return this // Just return anything, it's a mock
            }
            
            fun clear() {
                // No state to clear
            }
        }
        """
    )

    // Main test class with a function that will be transformed
    private val testClass = kotlin(
        "TestClass.kt",
        """
        package dev.supersam.test
        
        class TestClass {
            @TrackIt
            fun annotatedFunction(name: String, age: Int) {
                println("Hello, " + name + "! You are " + age + " years old.")
            }
            
            fun nonAnnotatedFunction(value: Double) {
                println("Value: " + value)
            }
        }
        """
    )

    // Complex test class with multiple parameters
    private val complexTestClass = kotlin(
        "ComplexTest.kt",
        """
        package dev.supersam.test
        
        class ComplexTest {
            @TrackIt
            fun complexFunction(
                stringParam: String,
                intParam: Int,
                doubleParam: Double,
                booleanParam: Boolean,
                nullableParam: String?,
                listParam: List<String>
            ) {
                println("Complex function called")
            }
        }
        """
    )

    // Test runner main class
    private val testRunner = kotlin(
        "TestRunner.kt",
        """
        package dev.supersam.test
        
        fun main() {
            // Test annotated function
            val testClass = TestClass()
            testClass.annotatedFunction("John", 30)
            
            // Test non-annotated function
            testClass.nonAnnotatedFunction(42.0)
            
            // Test complex function
            val complexTest = ComplexTest()
            complexTest.complexFunction("test", 42, 3.14, true, null, listOf("a", "b"))
            
            println("TEST_RUNNER: All tests completed")
        }
        """
    )

    // Test runner for disabled plugin
    private val disabledPluginRunner = kotlin(
        "DisabledPluginRunner.kt",
        """
        package dev.supersam.test
        
        fun main() {
            val disabledTest = DisabledTest()
            disabledTest.annotatedFunction("John")
            println("DISABLED_PLUGIN: Test completed")
        }
        """
    )

    // Class for disabled plugin test
    private val disabledTestClass = kotlin(
        "DisabledTest.kt",
        """
        package dev.supersam.test
        
        class DisabledTest {
            @TrackIt
            fun annotatedFunction(name: String) {
                println("Hello, " + name)
            }
        }
        """
    )

    // Test runner for disabled visitor
    private val disabledVisitorRunner = kotlin(
        "DisabledVisitorRunner.kt",
        """
        package dev.supersam.test
        
        fun main() {
            val visitorDisabledTest = VisitorDisabledTest()
            visitorDisabledTest.annotatedFunction("John")
            println("DISABLED_VISITOR: Test completed")
        }
        """
    )

    // Class for disabled visitor test
    private val visitorDisabledTestClass = kotlin(
        "VisitorDisabledTest.kt",
        """
        package dev.supersam.test
        
        class VisitorDisabledTest {
            @TrackIt
            fun annotatedFunction(name: String) {
                println("Hello, " + name)
            }
        }
        """
    )

    @Test
    fun `test function visitor is called for annotated functions`() {
        // Capture System.out
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            // Compile and run the test
            val result = compile(testClass,complexTestClass, testRunner)
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            // Run the main method
            val mainClass = result.classLoader.loadClass("dev.supersam.test.TestRunnerKt")
            mainClass.getMethod("main").invoke(null)

            // Get the output
            val output = outputStream.toString()

            // Verify the test ran to completion
            assertThat(output).contains("TEST_RUNNER: All tests completed")

            // Verify the annotated function was tracked
            assertThat(output).contains("VISITOR_LOG: Function called: dev.supersam.test.TestClass.annotatedFunction")

            // Verify parameters were captured
            assertThat(output).contains("VISITOR_LOG: Parameters: name, age")

            // Verify the non-annotated function was not tracked
            assertThat(countOccurrences(output, "VISITOR_LOG: Function called:")).isEqualTo(2) // One for annotated function, one for complex function
        } finally {
            // Restore System.out
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test function visitor with multiple parameters`() {
        // Capture System.out
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            // Compile and run the test
            val result = compile(testClass, complexTestClass, testRunner)
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            // Run the main method
            val mainClass = result.classLoader.loadClass("dev.supersam.test.TestRunnerKt")
            mainClass.getMethod("main").invoke(null)

            // Get the output
            val output = outputStream.toString()

            // Verify the test ran to completion
            assertThat(output).contains("TEST_RUNNER: All tests completed")

            // Verify the complex function was tracked
            assertThat(output).contains("VISITOR_LOG: Function called: dev.supersam.test.ComplexTest.complexFunction")

            // Verify all parameters were captured
            assertThat(output).contains("VISITOR_LOG: Parameters: stringParam, intParam, doubleParam, booleanParam, nullableParam, listParam")
        } finally {
            // Restore System.out
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test plugin disabled does not transform functions`() {
        // Capture System.out
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            // Compile and run the test with plugin disabled
            val result = compileWithOptions(
                mapOf(ENABLED to "false"),
                disabledTestClass, disabledPluginRunner
            )
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            // Run the main method
            val mainClass = result.classLoader.loadClass("dev.supersam.test.DisabledPluginRunnerKt")
            mainClass.getMethod("main").invoke(null)

            // Get the output
            val output = outputStream.toString()

            // Verify the test ran to completion
            assertThat(output).contains("DISABLED_PLUGIN: Test completed")

            // Verify the visitor was not called (no visitor log output)
            assertThat(output).doesNotContain("VISITOR_LOG")
        } finally {
            // Restore System.out
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test visitor disabled does not transform functions`() {
        // Capture System.out
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            // Compile and run the test with visitor disabled
            val result = compileWithOptions(
                mapOf(FUNCTIONS_VISITOR_ENABLED to "false"),
                visitorDisabledTestClass, disabledVisitorRunner
            )
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            // Run the main method
            val mainClass = result.classLoader.loadClass("dev.supersam.test.DisabledVisitorRunnerKt")
            mainClass.getMethod("main").invoke(null)

            // Get the output
            val output = outputStream.toString()

            // Verify the test ran to completion
            assertThat(output).contains("DISABLED_VISITOR: Test completed")

            // Verify the visitor was not called (no visitor log output)
            assertThat(output).doesNotContain("VISITOR_LOG")
        } finally {
            // Restore System.out
            System.setOut(originalOut)
        }
    }

    private fun countOccurrences(text: String, subtext: String): Int {
        return text.split(subtext).size - 1
    }

    private fun prepareCompilation(
        options: Map<String, String> = emptyMap(),
        vararg sourceFiles: SourceFile
    ): KotlinCompilation {
        return KotlinCompilation().apply {
            workingDir = temporaryFolder.root
            compilerPluginRegistrars = listOf(CompiluginComponentRegistrar())
            val processor = CompiluginCommandLineProcessor()
            commandLineProcessors = listOf(processor)

            val defaultOptions = mapOf(
                ENABLED to "true",
                LOGGING to "true",
                FUNCTIONS_VISITOR_ENABLED to "true",
                FUNCTIONS_VISITOR_ANNOTATION to "dev.supersam.test.TrackIt",
                FUNCTIONS_VISITOR_PATH to "dev.supersam.test.FunctionsVisitor.visit",

                // Disabled by default for these tests
                COMPOSE_MODIFIER_WRAPPER_ENABLED to "false",
                COMPOSE_MODIFIER_WRAPPER_PATH to "dev.supersam.test.ModifierHelper.wrapModifier"
            )

            // Override with custom options
            val effectiveOptions = defaultOptions + options

            pluginOptions = effectiveOptions.map { (key, value) ->
                processor.option(key, value)
            }

            inheritClassPath = true
            sources = sourceFiles.asList() + listOf(
                trackFunctionAnnotation,
                functionsVisitorImplementation,
                mockModifierHelper
            )
            verbose = false
            jvmTarget = JvmTarget.fromString("11")!!.description
        }
    }

    private fun CommandLineProcessor.option(key: String, value: String): PluginOption {
        val cliOption = cliOptions.find { it.optionName == key }
            ?: error("Unknown option: $key")
        return PluginOption(pluginId, cliOption.optionName, value)
    }

    private fun compile(vararg sourceFiles: SourceFile) =
        prepareCompilation(emptyMap(), *sourceFiles).compile()

    private fun compileWithOptions(
        options: Map<String, String>,
        vararg sourceFiles: SourceFile
    ) = prepareCompilation(options, *sourceFiles).compile()
}