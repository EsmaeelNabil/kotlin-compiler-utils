package dev.supersam.android.example

object Tracker {
    // parameter names are not important, but the types or the order are important.
    fun track(
        name: String,
        parent: String,
        body: String,
        args: Map<String, Any?>,
    ) {
        println("Tracking happening for : $name with $parent at $body")
    }
}