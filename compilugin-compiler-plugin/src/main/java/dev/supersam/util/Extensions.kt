package dev.supersam.util

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.fir.backend.utils.defaultTypeWithoutArguments
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Returns true if this function/class/variable Has this annotation or not
 */

public fun IrDeclaration.hasAnnotation(annotationFqName: String): Boolean = annotations.any {
    it.dump().contains(annotationFqName)
}

/**
 * Finds a class by its fully qualified dotted path.
 *
 * @param classFullDottedPath The full dotted path of the class (e.g., "com.example.MyClass")
 * @return [IrClassSymbol] representing the found class
 * @throws IllegalStateException if the class is not found
 */
internal fun IrPluginContext.findClass(classFullDottedPath: String): IrClassSymbol = this.referenceClass(
    ClassId(
        FqName(classFullDottedPath.substringBeforeLast(".")),
        Name.identifier(classFullDottedPath.substringAfterLast(".")),
    ),
) ?: error("$classFullDottedPath was not found")

/**
 * Creates an [IrGetObjectValue] expression for accessing a Kotlin object instance.
 *
 * @return [IrGetObjectValue] expression that represents accessing the object instance
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrClassSymbol.getObjectValue(): IrGetObjectValue = IrGetObjectValueImpl(
    this.owner.startOffset,
    this.owner.endOffset,
    this.defaultTypeWithoutArguments,
    this,
)

/**
 * Finds a single function within a class by its name.
 *
 * @param functionName The name of the function to find
 * @return [IrSimpleFunctionSymbol] representing the found function
 * @throws IllegalStateException if the function is not found or if multiple functions with the same name exist
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrClassSymbol?.findSingleFunction(functionName: String): IrSimpleFunctionSymbol =
    this?.functions?.single { it.owner.name.asString() == functionName }
        ?: error("$functionName was not found inside class: ${this?.owner?.name?.asString()}")

/**
 * Builds an IR call to `mapOf()` containing function parameters as key-value pairs.
 *
 * This function creates a map where each parameter of the given function becomes a key-value pair,
 * with the parameter name as the key and the parameter value as the value.
 *
 * @param declaration The function whose parameters should be included in the map
 * @return [IrCall] representing a call to `mapOf()` with the function parameters
 */
@OptIn(UnsafeDuringIrConstructionAPI::class, InternalSymbolFinderAPI::class)
internal fun IrBuilderWithScope.buildMapOfParamsCall(declaration: IrSimpleFunction): IrCall {
    val mapOfFunction = context.irBuiltIns.symbolFinder.findFunctions(
        Name.identifier("mapOf"),
        FqName("kotlin.collections"),
    ).firstOrNull {
        it.owner.parameters.size == 1 &&
            it.owner.parameters[0].varargElementType != null
    } ?: error("mapOf(vararg pairs) function not found")

    return irCall(mapOfFunction).apply {
        val pairClass = context.irBuiltIns.symbolFinder.findClass(
            Name.identifier("Pair"),
            FqName("kotlin"),
        ) ?: error("Pair class not found")
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

        // Combine pairs into a vararg
        arguments[0] = irVararg(
            pairType,
            keyValuePairs,
        )
    }
}
