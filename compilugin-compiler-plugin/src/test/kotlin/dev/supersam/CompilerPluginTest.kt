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
import dev.supersam.util.*
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.JvmTarget
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Basic tests to ensure core functionality works reliably.
 * These tests focus on fundamental scenarios that must always pass.
 */
@OptIn(ExperimentalCompilerApi::class)
class CompilerPluginTest {

    @Rule
    @JvmField
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

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
            private val calls = mutableListOf<String>()

            fun visit(
                functionName: String,
                classPath: String,
                signature: String,
                parameters: Map<String, Any?>
            ) {
                val entry = "${'$'}classPath.${'$'}functionName(${'$'}{parameters.size} params)"
                calls.add(entry)
                println("VISITOR_LOG: ${'$'}entry")
            }

            fun clear() {
                calls.clear()
            }

            fun getCalls(): List<String> = calls.toList()
            fun getCallCount(): Int = calls.size
        }
        """,
    )

    @Test
    fun `test basic function tracking works`() {
        val basicTest = kotlin(
            "BasicTest.kt",
            """
            package dev.supersam.test

            class BasicTestClass {
                @TrackIt
                fun trackedMethod(name: String): String {
                    return "Hello, ${'$'}name"
                }

                fun untrackedMethod(name: String): String {
                    return "Hi, ${'$'}name"
                }
            }
            """,
        )

        val runner = kotlin(
            "BasicRunner.kt",
            """
            package dev.supersam.test

            fun main() {
                val test = BasicTestClass()
                test.trackedMethod("World")
                test.untrackedMethod("Universe")
                println("Basic test completed")
            }
            """,
        )

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val result = compile(basicTest, runner)
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.BasicRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()
            assertThat(output).contains("VISITOR_LOG: dev.supersam.test.BasicTestClass.trackedMethod(2 params)")
            assertThat(output).doesNotContain("VISITOR_LOG: dev.supersam.test.BasicTestClass.untrackedMethod")
            assertThat(output).contains("Basic test completed")
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test multiple parameter types are handled correctly`() {
        val parameterTest = kotlin(
            "ParameterTest.kt",
            """
            package dev.supersam.test

            class ParameterTestClass {
                @TrackIt
                fun methodWithDifferentParams(
                    str: String,
                    num: Int,
                    flag: Boolean,
                    optional: String? = null
                ): String {
                    return "Method called with: ${'$'}str, ${'$'}num, ${'$'}flag, ${'$'}optional"
                }
            }
            """,
        )

        val runner = kotlin(
            "ParameterRunner.kt",
            """
            package dev.supersam.test

            fun main() {
                val test = ParameterTestClass()
                test.methodWithDifferentParams("test", 42, true, "optional")
                test.methodWithDifferentParams("test2", 24, false)
                println("Parameter test completed")
            }
            """,
        )

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val result = compile(parameterTest, runner)
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.ParameterRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()
            assertThat(
                output,
            ).contains("VISITOR_LOG: dev.supersam.test.ParameterTestClass.methodWithDifferentParams(5 params)")

            // Should be called twice
            val visitorCallCount = output.split("VISITOR_LOG:").size - 1
            assertThat(visitorCallCount).isEqualTo(2)
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test plugin disabled configuration`() {
        val disabledTest = kotlin(
            "DisabledTest.kt",
            """
            package dev.supersam.test

            class DisabledTestClass {
                @TrackIt
                fun shouldNotBeTracked(): String {
                    return "This should not be tracked"
                }
            }
            """,
        )

        val runner = kotlin(
            "DisabledRunner.kt",
            """
            package dev.supersam.test

            fun main() {
                val test = DisabledTestClass()
                test.shouldNotBeTracked()
                println("Disabled test completed")
            }
            """,
        )

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val result = compileWithOptions(
                mapOf(ENABLED to "false"),
                disabledTest,
                runner,
            )
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.DisabledRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()
            assertThat(output).contains("Disabled test completed")
            assertThat(output).doesNotContain("VISITOR_LOG")
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test object methods are tracked`() {
        val objectTest = kotlin(
            "ObjectTest.kt",
            """
            package dev.supersam.test

            object TestObject {
                @TrackIt
                fun objectMethod(value: String): String {
                    return "Object method: ${'$'}value"
                }
            }
            """,
        )

        val runner = kotlin(
            "ObjectRunner.kt",
            """
            package dev.supersam.test

            fun main() {
                TestObject.objectMethod("test")
                println("Object test completed")
            }
            """,
        )

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val result = compile(objectTest, runner)
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.ObjectRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()
            assertThat(output).contains("VISITOR_LOG: dev.supersam.test.TestObject.objectMethod(2 params)")
            assertThat(output).contains("Object test completed")
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test top-level functions are tracked`() {
        val topLevelTest = kotlin(
            "TopLevelTest.kt",
            """
            package dev.supersam.test

            @TrackIt
            fun topLevelFunction(message: String): String {
                return "Top level: ${'$'}message"
            }

            fun nonTrackedTopLevel(): String {
                return "Not tracked"
            }
            """,
        )

        val runner = kotlin(
            "TopLevelRunner.kt",
            """
            package dev.supersam.test

            fun main() {
                topLevelFunction("hello")
                nonTrackedTopLevel()
                println("Top level test completed")
            }
            """,
        )

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val result = compile(topLevelTest, runner)
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.TopLevelRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()
            assertThat(output).contains("VISITOR_LOG: dev.supersam.test.topLevelFunction(1 params)")
            assertThat(output).doesNotContain("VISITOR_LOG: dev.supersam.test.nonTrackedTopLevel")
            assertThat(output).contains("Top level test completed")
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test repeated function calls are all tracked`() {
        val repeatedTest = kotlin(
            "RepeatedTest.kt",
            """
            package dev.supersam.test

            class RepeatedTestClass {
                @TrackIt
                fun repeatedMethod(index: Int): Int {
                    return index * 2
                }
            }
            """,
        )

        val runner = kotlin(
            "RepeatedRunner.kt",
            """
            package dev.supersam.test

            fun main() {
                val test = RepeatedTestClass()
                repeat(5) { index ->
                    test.repeatedMethod(index)
                }
                println("Repeated test completed")
            }
            """,
        )

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val result = compile(repeatedTest, runner)
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.RepeatedRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()

            // Should have 5 calls to the same method
            val visitorCallCount =
                output.split("VISITOR_LOG: dev.supersam.test.RepeatedTestClass.repeatedMethod").size - 1
            assertThat(visitorCallCount).isEqualTo(5)
            assertThat(output).contains("Repeated test completed")
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test inheritance tracking works correctly`() {
        val inheritanceTest = kotlin(
            "InheritanceTest.kt",
            """
            package dev.supersam.test

            open class BaseClass {
                @TrackIt
                open fun baseMethod(): String {
                    return "Base method"
                }
            }

            class DerivedClass : BaseClass() {
                @TrackIt
                override fun baseMethod(): String {
                    return "Derived method"
                }

                @TrackIt
                fun derivedMethod(): String {
                    return "Derived only"
                }
            }
            """,
        )

        val runner = kotlin(
            "InheritanceRunner.kt",
            """
            package dev.supersam.test

            fun main() {
                val base = BaseClass()
                base.baseMethod()

                val derived = DerivedClass()
                derived.baseMethod()
                derived.derivedMethod()

                println("Inheritance test completed")
            }
            """,
        )

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val result = compile(inheritanceTest, runner)
            assertThat(result.exitCode).isEqualTo(ExitCode.OK)

            val mainClass = result.classLoader.loadClass("dev.supersam.test.InheritanceRunnerKt")
            mainClass.getMethod("main").invoke(null)

            val output = outputStream.toString()
            assertThat(output).contains("VISITOR_LOG: dev.supersam.test.BaseClass.baseMethod(1 params)")
            assertThat(output).contains("VISITOR_LOG: dev.supersam.test.DerivedClass.baseMethod(1 params)")
            assertThat(output).contains("VISITOR_LOG: dev.supersam.test.DerivedClass.derivedMethod(1 params)")

            // Should have 3 total calls
            val visitorCallCount = output.split("VISITOR_LOG:").size - 1
            assertThat(visitorCallCount).isEqualTo(3)
        } finally {
            System.setOut(originalOut)
        }
    }

    private fun compile(vararg sourceFiles: SourceFile) = prepareCompilation(emptyMap(), *sourceFiles).compile()

    private fun compileWithOptions(options: Map<String, String>, vararg sourceFiles: SourceFile) =
        prepareCompilation(options, *sourceFiles).compile()

    private fun prepareCompilation(
        options: Map<String, String> = emptyMap(),
        vararg sourceFiles: SourceFile,
    ): KotlinCompilation = KotlinCompilation().apply {
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
        )

        val effectiveOptions = defaultOptions + options

        pluginOptions = effectiveOptions.map { (key, value) ->
            processor.option(key, value)
        }

        inheritClassPath = true
        sources = sourceFiles.asList() +
            listOf(
                trackFunctionAnnotation,
                functionsVisitorImplementation,
            )
        verbose = false
        jvmTarget = JvmTarget.fromString("11")!!.description
    }

    private fun CommandLineProcessor.option(key: String, value: String): PluginOption {
        val cliOption = cliOptions.find { it.optionName == key }
            ?: error("Unknown option: $key")
        return PluginOption(pluginId, cliOption.optionName, value)
    }
}
