@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package dev.supersam.transformations

import dev.supersam.util.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.createType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


internal fun IrFunction.transformFunctionsVisitor(
    pluginContext: IrPluginContext,
    declaration: IrFunction,
    logger: DebugLogger,
    functionsVisitorPath: String,
    functionsVisitorAnnotation: String?,
) {
    val hasAnnotation = declaration.annotations.any {
        it.dump().contains(functionsVisitorAnnotation!!)
    }
    if (!hasAnnotation)
        return

    logger.log("Transforming function has annotation track it : ${declaration.name}")

    val classPath = functionsVisitorPath.substringBeforeLast(".")
    val functionsVisitorObject: IrClassSymbol = pluginContext.findClass(classPath)
    val visitFunctionInsideObject = functionsVisitorObject.findSingleFunction(
        functionsVisitorPath.substringAfterLast(".")
    )
    val functionVisitorObjectInstance = functionsVisitorObject.getObjectValue()
    val functionSignature = declaration.getFunctionSignature()


    declaration.body?.let { originalBody ->
        declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
            +irCall(visitFunctionInsideObject).apply {

                // function inside object
                dispatchReceiver = functionVisitorObjectInstance
                //first parameter : String
                putValueArgument(0, irString(functionSignature))


                val mapOfFunction = context.irBuiltIns.findFunctions(
                    Name.identifier("mapOf"),
                    FqName("kotlin.collections")
                ).firstOrNull {
                    it.owner.valueParameters.size == 1 &&
                            it.owner.valueParameters[0].varargElementType != null
                } ?: error("mapOf(vararg pairs) function not found")

                val argsMap = irCall(mapOfFunction).apply {
                    val pairClass =
                        context.irBuiltIns.findClass(Name.identifier("Pair"), FqName("kotlin"))
                            ?: error("Pair class not found")
                    val pairConstructor = pairClass.owner.constructors.firstOrNull()
                        ?: error("Pair constructor not found")

                    val pairType =
                        pairClass.owner.symbol.defaultType // This represents Pair<Any?, Any?>


                    val keyValuePairs = declaration.valueParameters.map { param ->
                        irCall(pairConstructor).apply {
                            putValueArgument(0, irString(param.name.asString())) // Key
                            putValueArgument(1, irGet(param)) // Value
                        }
                    }
                    putValueArgument(
                        0,
                        irVararg(
                            pairType,
                            keyValuePairs
                        ) // Combine pairs into a vararg
                    )
                }

                putValueArgument(
                    1,
                    argsMap // Second argument
                )
            }


            // Insert the original body statements
            originalBody.statements.forEach { statement ->
                +statement
            }
        }
    }
}

private fun IrFunction.getFunctionSignature(
): String {

    val functionName = this.name.asString()
    val annotations = this.annotations.joinToString(separator = "\n") { "@" + it.dumpKotlinLike() }
    val arguments = this.valueParameters.joinToString {
        " ${it.name.asString()} : ${it.type.classFqName?.asString()}"
    }
    val returnType = this.returnType.classFqName?.asString()
    val isSuspend = if (this.isSuspend) "suspend " else ""

    return """
        
        $annotations
        ${isSuspend}fun $functionName($arguments): $returnType
        
        
    """.trimIndent()
}