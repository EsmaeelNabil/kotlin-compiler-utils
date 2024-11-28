@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package dev.supersam.transformations

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private fun alreadyAppliedPrintLn(statement: IrFunction): Boolean {
    return statement.body?.statements?.firstOrNull()?.dump()?.contains("ENTERED") == true
}

internal fun injectPrintlnStatements(pluginContext: IrPluginContext, declaration: IrFunction) {

    if (alreadyAppliedPrintLn(declaration))
        return

    val typeNullableAny = pluginContext.irBuiltIns.anyNType

    val funPrintln = pluginContext.referenceFunctions(
        CallableId(
            FqName("kotlin.io"),
            Name.identifier("println")
        )
    ).single {
        val parameters = it.owner.valueParameters
        parameters.size == 1 && it.owner.valueParameters[0].type == typeNullableAny
    }

    declaration.body?.let { originalBody ->
        declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
            // Log ENTERED
            val enterPrintLn = irCall(funPrintln).apply {
                putValueArgument(0, irString("Composable : ${declaration.name} ENTERED"))
            }
            +enterPrintLn

            // Insert the original body statements
            originalBody.statements.forEach { statement ->
                +statement
            }

            // Log EXITED
            val exitPrintLn = irCall(funPrintln).apply {
                putValueArgument(
                    0,
                    irString("Composable : ${declaration.name} ${declaration.returnType.classFqName?.asString()} EXITED")
                )
            }
            +exitPrintLn
        }
    }
}
