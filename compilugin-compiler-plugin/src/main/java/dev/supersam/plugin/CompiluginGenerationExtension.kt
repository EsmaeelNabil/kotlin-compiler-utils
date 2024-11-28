package dev.supersam.plugin

import dev.supersam.util.DebugLogger
import dev.supersam.transformations.CompiluginTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class CompiluginGenerationExtension(
    private val debugLogger: DebugLogger,
    private val composeModifierWrapperEnabled: Boolean,
    private val composeModifierWrapperPath: String,
    private val enableFunctionsVisitor: Boolean,
    private val functionsVisitorAnnotation: String,
    private val functionsVisitorPath: String
) : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        debugLogger.log("CompiluginGenerationExtension: generate")
        debugLogger.log("composeModifierWrapperEnabled: $composeModifierWrapperEnabled")
        debugLogger.log("composeModifierWrapperPath: $composeModifierWrapperPath")
        debugLogger.log("enableFunctionsVisitor: $enableFunctionsVisitor")
        debugLogger.log("functionsVisitorAnnotation: $functionsVisitorAnnotation")
        debugLogger.log("functionsVisitorPath: $functionsVisitorPath")
        moduleFragment.transform(
            transformer = CompiluginTransformer(
                pluginContext = pluginContext,
                logger = debugLogger,
                composeModifierWrapperEnabled = composeModifierWrapperEnabled,
                composeModifierWrapperPath = composeModifierWrapperPath,
                enableFunctionsVisitor = enableFunctionsVisitor,
                functionsVisitorAnnotation = functionsVisitorAnnotation,
                functionsVisitorPath = functionsVisitorPath
            ),
            data = null
        )
    }
}