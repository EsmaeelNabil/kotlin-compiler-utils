package dev.supersam.android.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

object ModifierBuilder {
    fun buildModifier(entryPoint: String): Modifier = Modifier
        .semantics {
            contentDescription = entryPoint
        }
        .background(Color.Red)
        .composed {
            this.clickable {
                println("Composable Function name: $entryPoint")
            }
        }
}
