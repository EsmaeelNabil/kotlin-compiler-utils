package dev.supersam.android.app

object FunctionsVisitor {
    fun visit(
        name: String,
        parent: String,
        body: String,
        params: Map<String, Any>
    ) {
        println(name)
        println(parent)
        println(body)
        params.forEach { (key, value) ->
            println("$key: $value")
        }
    }
}