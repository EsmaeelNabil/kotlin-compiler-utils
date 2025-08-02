package dev.supersam.util

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Returns an [IrClassSymbol] for the given class full dotted path.
 * classFullDottedPath: String - full dotted path of the class: "com.example.MyClass"
 */
internal fun IrPluginContext.findClass(classFullDottedPath: String): IrClassSymbol = this.referenceClass(
    ClassId(
        FqName(classFullDottedPath.substringBeforeLast(".")),
        Name.identifier(classFullDottedPath.substringAfterLast(".")),
    ),
) ?: error("$classFullDottedPath was not found")

/**
 * Returns an [IrGetObjectValue] for the given [IrClassSymbol].
 * returns a Kotlin Object instance.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrClassSymbol.getObjectValue(): IrGetObjectValue = IrGetObjectValueImpl(
    this.owner.startOffset,
    this.owner.endOffset,
    this.defaultType,
    this,
)

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrClassSymbol?.findSingleFunction(functionName: String): IrSimpleFunctionSymbol =
    this?.functions?.single { it.owner.name.asString() == functionName }
        ?: error("$functionName was not found inside class: ${this?.owner?.name?.asString()}")

@OptIn(UnsafeDuringIrConstructionAPI::class, InternalSymbolFinderAPI::class)
internal fun IrBuilderWithScope.buildMapOfParamsCall(declaration: IrFunction): IrCall {
    val mapOfFunction = context.irBuiltIns.symbolFinder.findFunctions(
        Name.identifier("mapOf"),
        FqName("kotlin.collections"),
    ).firstOrNull {
        it.owner.parameters.size == 1 &&
            it.owner.parameters[0].varargElementType != null
    } ?: error("mapOf(vararg pairs) function not found")

    return irCall(mapOfFunction).apply {
        val pairClass =
            context.irBuiltIns.symbolFinder.findClass(Name.identifier("Pair"), FqName("kotlin"))
                ?: error("Pair class not found")
        val pairConstructor = pairClass.owner.constructors.firstOrNull()
            ?: error("Pair constructor not found")

        val pairType =
            pairClass.owner.symbol.defaultType // This represents Pair<Any?, Any?>

        val keyValuePairs = declaration.parameters.map { param ->
            irCall(pairConstructor).apply {
                arguments[0] = irString(param.name.asString())
                arguments[1] = irGet(param)
            }
        }
        arguments[0] = irVararg(
            pairType,
            keyValuePairs,
        ) // Combine pairs into a vararg
    }
}
