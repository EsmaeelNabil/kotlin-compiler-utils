@file:OptIn(ExperimentalCompilerApi::class)

package dev.supersam.plugin

import com.google.auto.service.AutoService
import dev.supersam.util.*
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
public class CompiluginCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = COMPILUGIN_PLUGIN_ID

    override val pluginOptions: Collection<CliOption> = cliOptions

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration): Unit =
        when (option.optionName) {
            ENABLED -> configuration.put(key_enabled_compiler_key, value.toBoolean())
            LOGGING -> configuration.put(key_logging_enabled_compiler_key, value.toBoolean())

            FUNCTIONS_VISITOR_ENABLED -> configuration.put(
                enable_functions_visitor_compiler_key,
                value.toBoolean(),
            )

            FUNCTIONS_VISITOR_PATH -> configuration.put(functions_visitor_path_compiler_key, value)
            FUNCTIONS_VISITOR_ANNOTATION -> configuration.put(
                functions_visitor_annotation_compiler_key,
                value,
            )

            else -> configuration.put(key_enabled_compiler_key, true)
        }
}
