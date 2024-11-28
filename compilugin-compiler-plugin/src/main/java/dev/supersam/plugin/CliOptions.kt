package dev.supersam.plugin

import dev.supersam.util.COMPOSE_MODIFIER_WRAPPER_ENABLED
import dev.supersam.util.COMPOSE_MODIFIER_WRAPPER_PATH
import dev.supersam.util.ENABLED
import dev.supersam.util.FUNCTIONS_VISITOR_ANNOTATION
import dev.supersam.util.FUNCTIONS_VISITOR_ENABLED
import dev.supersam.util.FUNCTIONS_VISITOR_PATH
import dev.supersam.util.LOGGING
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val cliOptions: List<CliOption> = listOf(
    CliOption(
        optionName = ENABLED, valueDescription = "<true|false>",
        description = "whether to enable the plugin or not"
    ),
    CliOption(
        optionName = LOGGING,
        valueDescription = "<true|false>",
        description = "whether to enable logging or not"
    ),
    CliOption(
        optionName = FUNCTIONS_VISITOR_ENABLED, valueDescription = "<true|false>",
        description = "whether to enable the functions visitor or not"
    ),
    CliOption(
        optionName = FUNCTIONS_VISITOR_ANNOTATION, valueDescription = "package.Annotation",
        description = "the annotation to use for the functions visitor as a package.Annotation"
    ),
    CliOption(
        optionName = FUNCTIONS_VISITOR_PATH,
        valueDescription = "dev.supersam.android.FunctionsVisitor.visit",
        description = "the fully qualified name of the FunctionsVisitor object," +
                " package name + object name + function name with signature (signature:String, map : Map<String, Any?>)"
    ),
    CliOption(
        optionName = COMPOSE_MODIFIER_WRAPPER_ENABLED,
        valueDescription = "<true|false>",
        description = "whether to enable the compose modifier wrapper or not"
    ),
    CliOption(
        optionName = COMPOSE_MODIFIER_WRAPPER_PATH,
        valueDescription = "dev.supersam.android.ModifierBuilder",
        description = "the fully qualified name of the ModifierWrapper object, package name + object name + function name with signature (functionName:String)"
    )
)

public val enable_functions_visitor_compiler_key: CompilerConfigurationKey<Boolean> =
    CompilerConfigurationKey<Boolean>("whether the functions visitor is enabled or not")

public val functions_visitor_annotation_compiler_key: CompilerConfigurationKey<String> =
    CompilerConfigurationKey<String>("the annotation to use for the functions visitor as a package.Annotation")

public val key_enabled_compiler_key: CompilerConfigurationKey<Boolean> =
    CompilerConfigurationKey<Boolean>("whether the plugin is enabled")

public val key_logging_enabled_compiler_key: CompilerConfigurationKey<Boolean> =
    CompilerConfigurationKey<Boolean>("whether the logging is enabled")

public val compose_modifier_wrapper_enabled_compiler_key: CompilerConfigurationKey<Boolean> =
    CompilerConfigurationKey<Boolean>("whether the compose modifier wrapper is enabled")

public val compose_modifier_wrapper_path_compiler_key: CompilerConfigurationKey<String> =
    CompilerConfigurationKey<String>("the path to the compose modifier wrapper function")


public val functions_visitor_path_compiler_key: CompilerConfigurationKey<String> =
    CompilerConfigurationKey<String>("the path to the functions visitor function")