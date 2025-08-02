@file:OptIn(ExperimentalCompilerApi::class)

package dev.supersam.plugin

import com.google.auto.service.AutoService
import dev.supersam.util.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * Component registrar for the Compilugin Kotlin compiler plugin.
 *
 * This class is responsible for registering the plugin's extensions with the Kotlin compiler.
 * It reads configuration values from the compiler configuration and sets up the necessary
 * IR generation extensions.
 *
 * The registrar supports Kotlin K2 compiler and registers the following extensions:
 * - [CompiluginGenerationExtension]: Main IR generation extension for code transformations
 */
@AutoService(CompilerPluginRegistrar::class)
public class CompiluginComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[key_enabled_compiler_key] == false) {
            return
        }

        val messageCollector =
            configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        val loggingEnabled = configuration[key_logging_enabled_compiler_key] == true
        val enableFunctionsVisitor = configuration[enable_functions_visitor_compiler_key] == true
        val functionsVisitorAnnotation =
            configuration.get(functions_visitor_annotation_compiler_key).orEmpty()
        val functionsVisitorPath = configuration.get(functions_visitor_path_compiler_key).orEmpty()

        IrGenerationExtension.registerExtension(
            CompiluginGenerationExtension(
                debugLogger = DebugLogger(loggingEnabled, messageCollector),
                enableFunctionsVisitor = enableFunctionsVisitor,
                functionsVisitorAnnotation = functionsVisitorAnnotation,
                functionsVisitorPath = functionsVisitorPath,
            ),
        )
    }
}
