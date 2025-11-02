@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package dev.supersam.transformations

import dev.supersam.util.DebugLogger
import dev.supersam.util.buildMapOfParamsCall
import dev.supersam.util.findClass
import dev.supersam.util.findSingleFunction
import dev.supersam.util.getObjectValue
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.statements

/**
 * Transforms a function to add functions visitor functionality.
 *
 * This function modifies the IR of functions that are annotated with the specified annotation
 * to call a visitor function at the beginning of their execution. The visitor function is called
 * with metadata about the function being executed.
 *
 * @param context The IR plugin context for accessing compiler services
 * @param [this] The function declaration to potentially transform
 * @param logger Debug logger for outputting transformation information
 * @param objectToInjectCallFor The full path to the visitor function (e.g., "com.example.Visitor.visit")
 */
internal fun IrSimpleFunction.injectObjectCallInFunctionBody(
    context: IrPluginContext,
    logger: DebugLogger,
    objectToInjectCallFor: String,
) {
    logger.log("transformFunctionsVisitor is called for fun : $name")

    val classPath = objectToInjectCallFor.substringBeforeLast(".")
    val functionsVisitorObject: IrClassSymbol = context.findClass(classPath)
    val visitFunctionInsideObject = functionsVisitorObject.findSingleFunction(
        objectToInjectCallFor.substringAfterLast("."),
    )
    val functionVisitorObjectInstance = functionsVisitorObject.getObjectValue()

    body = DeclarationIrBuilder(
        generatorContext = context,
        symbol = symbol,
    ).irBlockBody {
        logger.log("Building new body for $name")

        +irCall(visitFunctionInsideObject, visitFunctionInsideObject.owner.returnType).apply {
            dispatchReceiver = functionVisitorObjectInstance

            // Arguments array includes dispatch receiver at index 0, so value parameters start at index 1
            arguments[1] = irString(this@injectObjectCallInFunctionBody.name.asString())
            arguments[2] = irString(this@injectObjectCallInFunctionBody.parent.kotlinFqName.asString())
            arguments[3] = irString(this@injectObjectCallInFunctionBody.dumpKotlinLike())
            arguments[4] = buildMapOfParamsCall(this@injectObjectCallInFunctionBody)
        }

        logger.log("Added function visitor call for $name")

        body!!.statements.forEach { statement ->
            +statement
        }

        logger.log("Added original body statements for $name")
        logger.log("------------------------------------------------------------")
    }
}
