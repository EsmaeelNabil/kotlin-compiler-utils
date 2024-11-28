package dev.supersam.transformations

import dev.supersam.util.findClass
import dev.supersam.util.findSingleFunction
import dev.supersam.util.getObjectValue
import dev.supersam.util.isCompanionModifier
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.transformStatement
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid


internal fun IrFunction.transformModifierCompanionObjectAccess(
    pluginContext: IrPluginContext,
    declaration: IrFunction,
    composeModifierWrapperPath: String,
) {
    transformStatement(
        object : IrElementTransformerVoid() {

            override fun visitGetObjectValue(expression: IrGetObjectValue): IrExpression {
                val isModifierCompanion = expression.isCompanionModifier()

                if (isModifierCompanion) {
                    val classPath = composeModifierWrapperPath.substringBeforeLast(".")
                    val kcpModifiersClass: IrClassSymbol = pluginContext.findClass(classPath)
                    val contentDescriptionModifierFunction = kcpModifiersClass.findSingleFunction(
                        composeModifierWrapperPath.substringAfterLast(".")
                    )

                    val modifierBuilderObject = kcpModifiersClass.getObjectValue()
                    return DeclarationIrBuilder(pluginContext, expression.symbol).run {
                        irCall(contentDescriptionModifierFunction).apply {
                            dispatchReceiver = modifierBuilderObject
                            putValueArgument(
                                0,
                                irString(declaration.name.asString())
                            )
                        }
                    }
                }

                return super.visitGetObjectValue(expression)
            }
        }
    )
}

