package dev.supersam.plugin

import dev.supersam.transformations.CompiluginTransformer
import dev.supersam.util.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 * Main IR generation extension for the Compilugin plugin.
 *
 * This extension is responsible for performing code transformations during the IR generation phase
 * of Kotlin compilation. It delegates the actual transformation work to [CompiluginTransformer].
 *
 * @param debugLogger Logger for debug output
 * @param enableFunctionsVisitor Whether to enable the functions visitor feature
 * @param functionsVisitorAnnotation The annotation class name to look for
 * @param functionsVisitorPath The path to the visitor function
 */
internal class CompiluginGenerationExtension(
    private val debugLogger: DebugLogger,
    private val enableFunctionsVisitor: Boolean,
    private val functionsVisitorAnnotation: String,
    private val functionsVisitorPath: String,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        debugLogger.log("CompiluginGenerationExtension: generate")
        debugLogger.log("enableFunctionsVisitor: $enableFunctionsVisitor")
        debugLogger.log("functionsVisitorAnnotation: $functionsVisitorAnnotation")
        debugLogger.log("functionsVisitorPath: $functionsVisitorPath")
        moduleFragment.transform(
            transformer = CompiluginTransformer(
                pluginContext = pluginContext,
                logger = debugLogger,
                enableFunctionsVisitor = enableFunctionsVisitor,
                functionsVisitorAnnotation = functionsVisitorAnnotation,
                functionsVisitorPath = functionsVisitorPath,
            ),
            data = null,
        )
    }
}
