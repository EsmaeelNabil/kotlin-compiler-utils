package dev.supersam.android.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trackMePleaseFunction(
            name = "Sam",
            age = "25",
            life = true,
            height = 2000.0,
            moreInfo = MoreInfo("Sam"),
            list = listOf(
                SomeMoreInformation(
                    name = "Rosalie Hester",
                    age = "option",
                    life = false,
                    height = 2.3,
                    list = listOf("Sam", "Rosalie"),
                ),
                SomeMoreInformation(
                    name = "Rosalie Hester",
                    age = "option",
                    life = false,
                    height = 2.3,
                    list = listOf("Sam", "Rosalie"),
                ),
                SomeMoreInformation(
                    name = "Rosalie Hester",
                    age = "option",
                    life = false,
                    height = 2.3,
                    list = listOf("Sam", "Rosalie"),
                ),
            ),
        )
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                App(Modifier)
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App(Modifier)
}
