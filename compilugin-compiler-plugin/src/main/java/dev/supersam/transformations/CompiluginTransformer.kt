package dev.supersam.transformations

import dev.supersam.util.DebugLogger
import dev.supersam.util.isComposable
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

internal class CompiluginTransformer(
    private val pluginContext: IrPluginContext,
    private val logger: DebugLogger,
    private val composeModifierWrapperEnabled: Boolean,
    private val composeModifierWrapperPath: String,
    private val enableFunctionsVisitor: Boolean,
    private val functionsVisitorAnnotation: String,
    private val functionsVisitorPath: String
) : IrElementTransformerVoid() {


    override fun visitFunction(declaration: IrFunction): IrStatement {

        // functions visitor
        if (enableFunctionsVisitor
            && functionsVisitorPath.isNotEmpty()
            && functionsVisitorAnnotation.isNotEmpty()
        ) {
            declaration.transformFunctionsVisitor(
                pluginContext = pluginContext,
                declaration = declaration,
                logger = logger,
                functionsVisitorPath = functionsVisitorPath,
                functionsVisitorAnnotation = functionsVisitorAnnotation
            )
        }

        // modifier builder
        if (composeModifierWrapperEnabled && composeModifierWrapperPath.isNotEmpty()) {
            if (declaration.isComposable()) {
                declaration.transformModifierCompanionObjectAccess(
                    pluginContext = pluginContext,
                    declaration = declaration,
                    composeModifierWrapperPath = composeModifierWrapperPath
                )
            }
        }

        return super.visitFunction(declaration)
    }
}