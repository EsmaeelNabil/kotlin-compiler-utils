package dev.supersam

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import dev.supersam.plugin.CompiluginCommandLineProcessor
import dev.supersam.plugin.CompiluginComponentRegistrar
import dev.supersam.plugin.cliOptions
import dev.supersam.util.*
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.JvmTarget
import org.junit.Rule
import org.junit.rules.TemporaryFolder

/**
 * Base class for compiler plugin tests, providing common setup and utilities.
 * This class handles the boilerplate of setting up the Kotlin compilation,
 * configuring the plugin, and providing helper methods for running compilations.
 */
@OptIn(ExperimentalCompilerApi::class)
abstract class BaseCompilerPluginTest {

    @Rule
    @JvmField
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

    /**
     * Prepares a Kotlin compilation with the given source files and plugin options.
     *
     * @param options A map of plugin options to configure the compilation.
     * @param sourceFiles The source files to be compiled.
     * @return A [KotlinCompilation] instance ready to be run.
     */
    protected fun prepareCompilation(
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
        sources = sourceFiles.asList()
        verbose = false
        jvmTarget = JvmTarget.fromString("11")!!.description
    }

    /**
     * Compiles the given source files with default plugin options.
     *
     * @param sourceFiles The source files to compile.
     * @return The result of the compilation.
     */
    protected fun compile(vararg sourceFiles: SourceFile) = prepareCompilation(emptyMap(), *sourceFiles).compile()

    /**
     * Compiles the given source files with additional specified plugin options.
     *
     * @param options Custom options for the plugin.
     * @param sourceFiles The source files to compile.
     * @return The result of the compilation.
     */
    protected fun compileWithOptions(options: Map<String, String>, vararg sourceFiles: SourceFile) =
        prepareCompilation(options, *sourceFiles).compile()

    /**
     * Helper function to create a [PluginOption] from a key-value pair.
     *
     * @param key The option name.
     * @param value The option value.
     * @return A [PluginOption] instance.
     */
    private fun CommandLineProcessor.option(key: String, value: String): PluginOption {
        val cliOption = cliOptions.find { it.optionName == key }
            ?: error("Unknown option: $key")
        return PluginOption(pluginId, cliOption.optionName, value)
    }
}
