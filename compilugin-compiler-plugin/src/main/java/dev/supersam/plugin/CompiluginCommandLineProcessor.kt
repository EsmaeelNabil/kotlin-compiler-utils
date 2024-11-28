@file:OptIn(ExperimentalCompilerApi::class)

package dev.supersam.plugin

import com.google.auto.service.AutoService
import dev.supersam.util.COMPILUGIN_PLUGIN_ID
import dev.supersam.util.COMPOSE_MODIFIER_WRAPPER_ENABLED
import dev.supersam.util.COMPOSE_MODIFIER_WRAPPER_PATH
import dev.supersam.util.ENABLED
import dev.supersam.util.FUNCTIONS_VISITOR_ANNOTATION
import dev.supersam.util.FUNCTIONS_VISITOR_ENABLED
import dev.supersam.util.FUNCTIONS_VISITOR_PATH
import dev.supersam.util.LOGGING
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
public class CompiluginCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = COMPILUGIN_PLUGIN_ID

    override val pluginOptions: Collection<CliOption> = cliOptions

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ): Unit = when (option.optionName) {
        ENABLED -> configuration.put(key_enabled_compiler_key, value.toBoolean())
        LOGGING -> configuration.put(key_logging_enabled_compiler_key, value.toBoolean())

        FUNCTIONS_VISITOR_ENABLED -> configuration.put(
            enable_functions_visitor_compiler_key,
            value.toBoolean()
        )

        FUNCTIONS_VISITOR_PATH -> configuration.put(functions_visitor_path_compiler_key, value)
        FUNCTIONS_VISITOR_ANNOTATION -> configuration.put(
            functions_visitor_annotation_compiler_key,
            value
        )

        COMPOSE_MODIFIER_WRAPPER_ENABLED -> configuration.put(
            compose_modifier_wrapper_enabled_compiler_key,
            value.toBoolean()
        )

        COMPOSE_MODIFIER_WRAPPER_PATH -> configuration.put(
            compose_modifier_wrapper_path_compiler_key,
            value
        )

        else -> configuration.put(key_enabled_compiler_key, true)
    }
}


