package dev.supersam

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import dev.supersam.util.ENABLED
import dev.supersam.util.FUNCTIONS_VISITOR_ENABLED
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@OptIn(ExperimentalCompilerApi::class)
class FunctionVisitorTest : BaseCompilerPluginTest() {

    private val trackFunctionAnnotation = kotlin(
        "TrackIt.kt",
        """
        package dev.supersam.test

        import kotlin.annotation.AnnotationRetention.RUNTIME
        import kotlin.annotation.AnnotationTarget.FUNCTION

        @Retention(RUNTIME)
        @Target(FUNCTION)
        annotation class TrackIt
        """,
    )

    private val functionsVisitorImplementation = kotlin(
        "FunctionsVisitor.kt",
        """
        package dev.supersam.test

        object FunctionsVisitor {
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
            }
        }
        """,
    )

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
        """,
    )

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
        """,
    )

    private val testRunner = kotlin(
        "TestRunner.kt",
        """
        package dev.supersam.test

        fun main() {
            val testClass = TestClass()
            testClass.annotatedFunction("John", 30)

            testClass.nonAnnotatedFunction(42.0)

            val complexTest = ComplexTest()
            complexTest.complexFunction("test", 42, 3.14, true, null, listOf("a", "b"))

            println("TEST_RUNNER: All tests completed")
        }
        """,
    )

    private val disabledPluginRunner = kotlin(
        "DisabledPluginRunner.kt",
        """
        package dev.supersam.test

        fun main() {
            val disabledTest = DisabledTest()
            disabledTest.annotatedFunction("John")
            println("DISABLED_PLUGIN: Test completed")
        }
        """,
    )

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
        """,
    )

    private val disabledVisitorRunner = kotlin(
        "DisabledVisitorRunner.kt",
        """
        package dev.supersam.test

        fun main() {
            val visitorDisabledTest = VisitorDisabledTest()
            visitorDisabledTest.annotatedFunction("John")
            println("DISABLED_VISITOR: Test completed")
        }
        """,
    )

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
        """,
    )

    @Test
    fun `test function visitor is called for annotated functions`() {
        // Capture System.out
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val result = compile(
                trackFunctionAnnotation,
                functionsVisitorImplementation,
                testClass,
                complexTestClass,
                testRunner,
            )
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.TestRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()

            assertThat(output).contains("TEST_RUNNER: All tests completed")

            assertThat(output).contains("VISITOR_LOG: Function called: dev.supersam.test.TestClass.annotatedFunction")

            assertThat(output).contains("VISITOR_LOG: Parameters: ")

            assertThat(
                countOccurrences(
                    output,
                    "VISITOR_LOG: Function called:",
                ),
            ).isEqualTo(2) // One for annotated function, one for complex function
        } finally {
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
            val result = compile(
                trackFunctionAnnotation,
                functionsVisitorImplementation,
                testClass,
                complexTestClass,
                testRunner,
            )
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.TestRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()

            assertThat(output).contains("TEST_RUNNER: All tests completed")

            assertThat(output).contains("VISITOR_LOG: Function called: dev.supersam.test.ComplexTest.complexFunction")

            assertThat(output).contains("VISITOR_LOG: Parameters: ")
        } finally {
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
            val result = compileWithOptions(
                mapOf(ENABLED to "false"),
                trackFunctionAnnotation,
                functionsVisitorImplementation,
                disabledTestClass,
                disabledPluginRunner,
            )
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.DisabledPluginRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()

            assertThat(output).contains("DISABLED_PLUGIN: Test completed")

            assertThat(output).doesNotContain("VISITOR_LOG")
        } finally {
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
            val result = compileWithOptions(
                mapOf(FUNCTIONS_VISITOR_ENABLED to "false"),
                trackFunctionAnnotation,
                functionsVisitorImplementation,
                visitorDisabledTestClass,
                disabledVisitorRunner,
            )
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.DisabledVisitorRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()

            assertThat(output).contains("DISABLED_VISITOR: Test completed")

            assertThat(output).doesNotContain("VISITOR_LOG")
        } finally {
            System.setOut(originalOut)
        }
    }

    private fun countOccurrences(text: String, subtext: String): Int = text.split(subtext).size - 1
}
