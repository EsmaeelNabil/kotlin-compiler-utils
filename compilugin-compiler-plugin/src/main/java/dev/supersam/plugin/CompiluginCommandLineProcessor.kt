@file:OptIn(ExperimentalCompilerApi::class)

package dev.supersam.plugin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class)
public class CompiluginCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "compiluginPlugin"

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = "enabled", valueDescription = "<true|false>",
            description = "whether to enable the plugin or not"
        ),
        CliOption(
            optionName = "logging",
            valueDescription = "<true|false>",
            description = "whether to enable logging or not"
        ),
        CliOption(
            optionName = "functionsVisitorEnabled", valueDescription = "<true|false>",
            description = "whether to enable the functions visitor or not"
        ),
        CliOption(
            optionName = "functionsVisitorAnnotation", valueDescription = "package.Annotation",
            description = "the annotation to use for the functions visitor as a package.Annotation"
        ),
        CliOption(
            optionName = "functionsVisitorPath",
            valueDescription = "dev.supersam.android.FunctionsVisitor.visit",
            description = "the fully qualified name of the FunctionsVisitor object, package name + object name + function name"
        ),
        CliOption(
            optionName = "composeModifierWrapperEnabled",
            valueDescription = "<true|false>",
            description = "whether to enable the compose modifier wrapper or not"
        ),
        CliOption(
            optionName = "composeModifierWrapperPath",
            valueDescription = "dev.supersam.android.ModifierBuilder",
            description = "the fully qualified name of the ModifierWrapper object, package name + object name + function name"
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ): Unit = when (option.optionName) {
        "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
        "logging" -> configuration.put(KEY_LOGGING_ENABLED, value.toBoolean())

        "functionsVisitorEnabled" -> configuration.put(ENABLE_FUNCTIONS_VISITOR, value.toBoolean())
        "functionsVisitorPath" -> configuration.put(FUNCTIONS_VISITOR_PATH, value)
        "functionsVisitorAnnotation" -> configuration.put(FUNCTIONS_VISITOR_ANNOTATION, value)

        "composeModifierWrapperEnabled" -> configuration.put(
            COMPOSE_MODIFIER_WRAPPER_ENABLED,
            value.toBoolean()
        )

        "composeModifierWrapperPath" -> configuration.put(COMPOSE_MODIFIER_WRAPPER_PATH, value)

        else -> configuration.put(KEY_ENABLED, true)
    }
}

public val ENABLE_FUNCTIONS_VISITOR: CompilerConfigurationKey<Boolean> =
    CompilerConfigurationKey<Boolean>("whether the functions visitor is enabled")

public val FUNCTIONS_VISITOR_ANNOTATION: CompilerConfigurationKey<String> =
    CompilerConfigurationKey<String>("the annotation to use for the functions visitor as a package.Annotation")

public val KEY_ENABLED: CompilerConfigurationKey<Boolean> =
    CompilerConfigurationKey<Boolean>("whether the plugin is enabled")

public val KEY_LOGGING_ENABLED: CompilerConfigurationKey<Boolean> =
    CompilerConfigurationKey<Boolean>("whether the logging is enabled")

public val COMPOSE_MODIFIER_WRAPPER_ENABLED: CompilerConfigurationKey<Boolean> =
    CompilerConfigurationKey<Boolean>("whether the compose modifier wrapper is enabled")

public val COMPOSE_MODIFIER_WRAPPER_PATH: CompilerConfigurationKey<String> =
    CompilerConfigurationKey<String>("the path to the compose modifier wrapper function")


public val FUNCTIONS_VISITOR_PATH: CompilerConfigurationKey<String> =
    CompilerConfigurationKey<String>("the path to the functions visitor function")