package dev.supersam.android.app

object FunctionsVisitor {
    fun visit(signature: String, map: Map<String, Any>) {
        println(signature)
        map.forEach { (key, value) ->
            println("$key: $value")
        }
    }
}