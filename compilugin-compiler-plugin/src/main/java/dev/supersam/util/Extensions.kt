package dev.supersam.util

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.types.classFqName

internal fun IrFunction.hasModifierParam() = valueParameters.any {
    it.type.classFqName?.asString() == "androidx.compose.ui.Modifier"
}

internal fun IrFunction.isComposable() = annotations.any {
    it.type.classFqName?.asString() == "androidx.compose.runtime.Composable"
}