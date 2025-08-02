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

        // logging
        val loggingEnabled = configuration[key_logging_enabled_compiler_key] == true

        // functions visitor
        val enableFunctionsVisitor = configuration[enable_functions_visitor_compiler_key] == true
        // functions visitor annotation
        val functionsVisitorAnnotation =
            configuration.get(functions_visitor_annotation_compiler_key).orEmpty()
        // functions visitor path
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
