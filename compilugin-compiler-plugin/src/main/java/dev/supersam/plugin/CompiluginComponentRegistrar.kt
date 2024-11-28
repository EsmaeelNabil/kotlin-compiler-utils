@file:OptIn(ExperimentalCompilerApi::class)

package dev.supersam.plugin

import com.google.auto.service.AutoService
import dev.supersam.util.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
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
        if (configuration[KEY_ENABLED] == false) {
            return
        }

        val messageCollector =
            configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)


        // logging
        val loggingEnabled = configuration[KEY_LOGGING_ENABLED] == true

        // functions visitor
        val enableFunctionsVisitor = configuration[ENABLE_FUNCTIONS_VISITOR] == true
        // functions visitor annotation
        val functionsVisitorAnnotation = configuration.get(FUNCTIONS_VISITOR_ANNOTATION)
        // functions visitor path
        val functionsVisitorPath = configuration.get(FUNCTIONS_VISITOR_PATH)

        // modifier builder
        val composeModifierWrapperEnabled = configuration[COMPOSE_MODIFIER_WRAPPER_ENABLED] == true
        val composeModifierWrapperPath: String? =
            configuration.get(COMPOSE_MODIFIER_WRAPPER_PATH)

        IrGenerationExtension.registerExtension(
            CompiluginGenerationExtension(
                debugLogger = DebugLogger(loggingEnabled, messageCollector),
                composeModifierWrapperEnabled = composeModifierWrapperEnabled,
                composeModifierWrapperPath = composeModifierWrapperPath,
                enableFunctionsVisitor = enableFunctionsVisitor,
                functionsVisitorAnnotation = functionsVisitorAnnotation,
                functionsVisitorPath = functionsVisitorPath
            )
        )
    }
}
