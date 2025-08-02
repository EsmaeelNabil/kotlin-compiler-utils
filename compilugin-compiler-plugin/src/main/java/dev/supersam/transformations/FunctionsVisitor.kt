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
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.statements

internal fun IrFunction.transformFunctionsVisitor(
    pluginContext: IrPluginContext,
    declaration: IrFunction,
    logger: DebugLogger,
    functionsVisitorPath: String,
    functionsVisitorAnnotation: String,
) {
    logger.log("transformFunctionsVisitor: ${declaration.name}")
    val hasAnnotation = declaration.annotations.any {
        it.dump().contains(functionsVisitorAnnotation)
    }
    if (!hasAnnotation) {
        return
    }

    val classPath = functionsVisitorPath.substringBeforeLast(".")
    val functionsVisitorObject: IrClassSymbol = pluginContext.findClass(classPath)
    val visitFunctionInsideObject = functionsVisitorObject.findSingleFunction(
        functionsVisitorPath.substringAfterLast("."),
    )
    val functionVisitorObjectInstance = functionsVisitorObject.getObjectValue()

    declaration.body?.let { originalBody ->
        declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
            logger.log("Building new body for ${declaration.name}")

            // Adding the function visitor call
            +irCall(visitFunctionInsideObject).apply {
                dispatchReceiver = functionVisitorObjectInstance

                arguments[1] = irString(declaration.name.asString())
                arguments[2] = irString(parent.kotlinFqName.asString())
                arguments[3] = irString(declaration.dumpKotlinLike())
                arguments[4] = buildMapOfParamsCall(declaration)
            }

            logger.log("Added function visitor call for ${declaration.name}")

            // Adding the original body statements
            originalBody.statements.forEach { statement ->
                +statement
            }

            logger.log("Added original body statements for ${declaration.name}")
            logger.log("------------------------------------------------------------")
        }
    }
}
