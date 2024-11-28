@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package dev.supersam.transformations

import dev.supersam.util.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.transformStatement
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


internal fun IrFunction.transformModifierCompanionObjectAccess(
    pluginContext: IrPluginContext,
    declaration: IrFunction,
    logger: DebugLogger,
    composeModifierWrapperPath: String,
) {

    transformStatement(
        object : IrElementTransformerVoid() {

            override fun visitGetObjectValue(expression: IrGetObjectValue): IrExpression {

                val isModifierCompanion = expression.isCompanionModifier()
                logger.log("isModifierCompanion: $isModifierCompanion")

                if (isModifierCompanion) {
                    logger.log("Injecting ModifierBuilder function call")

                    val classPath = composeModifierWrapperPath.substringBeforeLast(".")
                    logger.log("classPath: $classPath")
                    val kcpModifiersClass: IrClassSymbol = pluginContext.findClass(classPath)
                    logger.log("kcpModifiersClass: ${kcpModifiersClass.owner.name.asString()}")


                    val contentDescriptionModifierFunction = kcpModifiersClass.findSingleFunction(
                        composeModifierWrapperPath.substringAfterLast(
                            "."
                        )
                    )
                    logger.log("contentDescriptionModifierFunction: ${contentDescriptionModifierFunction.owner.name.asString()}")


                    val modifierBuilderObject = kcpModifiersClass.getObjectValue()
                    logger.log("modifierBuilderObject: ${modifierBuilderObject.symbol.owner.name.asString()}")


                    val entryPoint = declaration.name.asString()
                    logger.log("entryPoint: $entryPoint")


                    return DeclarationIrBuilder(pluginContext, expression.symbol).run {
                        irCall(contentDescriptionModifierFunction).apply {
                            dispatchReceiver = modifierBuilderObject
                            putValueArgument(
                                0,
                                irString("ComposableFunction:$entryPoint")
                            )
                        }
                    }
                }

                return super.visitGetObjectValue(expression)
            }
        }
    )
}

/**
 * Checks if the [IrGetObjectValue] is a companion object of the Modifier class.
 * which means it is a Modifier.Companion object being accessed.
 */
internal fun IrGetObjectValue.isCompanionModifier() =
    this.type.classFqName?.asString() == "androidx.compose.ui.Modifier.Companion"

/**
 * Returns an [IrClassSymbol] for the given class full dotted path.
 * classFullDottedPath: String - full dotted path of the class: "com.example.MyClass"
 */
internal fun IrPluginContext.findClass(classFullDottedPath: String): IrClassSymbol =
    this.referenceClass(
        ClassId(
            FqName(classFullDottedPath.substringBeforeLast(".")),
            Name.identifier(classFullDottedPath.substringAfterLast("."))
        )
    ) ?: error("findClass: class was not found: $classFullDottedPath")


/**
 * Returns an [IrGetObjectValue] for the given [IrClassSymbol].
 * returns a Kotlin Object instance.
 */
internal fun IrClassSymbol.getObjectValue(): IrGetObjectValue = IrGetObjectValueImpl(
    this.owner.startOffset,
    this.owner.endOffset,
    this.defaultType,
    this
)


internal fun IrClassSymbol?.findSingleFunction(functionName: String): IrSimpleFunctionSymbol =
    this?.functions?.single { it.owner.name.asString() == functionName }
        ?: error("findModifierBuilderFunction: function was not found inside class: ${this?.owner?.name?.asString()}")