package dev.supersam.transformations

import dev.supersam.util.DebugLogger
import dev.supersam.util.hasAnnotation
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

public open class CompiluginTransformerVoid(
    private val context: IrPluginContext,
    private val logger: DebugLogger,
    private val enableFunctionAnnotationTransformer: Boolean,
    private val annotationClass: String,
    private val objectToInjectCallFor: String,
) : IrVisitorVoid() {

    override fun visitElement(element: IrElement) {
        when (element) {
            is IrDeclaration,
            is IrFile,
            is IrModuleFragment,
            -> element.acceptChildrenVoid(this)

            else -> {}
        }
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        if (
            enableFunctionAnnotationTransformer &&
            objectToInjectCallFor.isNotEmpty() &&
            annotationClass.isNotEmpty() &&
            declaration.hasAnnotation(annotationClass)
        ) {
            declaration.injectObjectCallInFunctionBody(
                context = context,
                logger = logger,
                objectToInjectCallFor = objectToInjectCallFor,
            )
        }
    }
}
