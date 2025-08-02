package dev.supersam.android.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun App(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HomeScreen(Modifier)
    }
}

@Composable
fun HomeScreen(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Home Screen!")
        ScreenContent(Modifier)
    }
}

@Composable
fun ScreenContent(modifier: Modifier) {
    Column(modifier.fillMaxWidth()) {
        Text("Hello, from screen content")
        CRAZY_FUNCTION_NAME()
    }
}

@Composable
fun CRAZY_FUNCTION_NAME() {
    Column(Modifier.fillMaxWidth()) {
        Text("Hello, from CRAZY_FUNCTION_NAME")
    }
}

data class MoreInfo(val name: String) {
    val age: String = "Age"
    val life: Boolean = true
    val height: Double = 2000.0
    val list: List<String> = listOf("Sam", "Rosalie")

    override fun toString(): String = "MoreInfo(name='$name', age='$age', life=$life, height=$height, list=$list)"
}

data class SomeMoreInformation(
    val name: String,
    val age: String,
    val life: Boolean,
    val height: Double,
    val list: List<String>,
)

@TrackIt
fun trackMePleaseFunction(
    name: String,
    age: String,
    life: Boolean,
    height: Double,
    moreInfo: MoreInfo,
    list: List<SomeMoreInformation>,
): String = "Hello, $name, $age, $life, $height, $list"
