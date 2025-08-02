package dev.supersam.util

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

internal class DebugLogger(private val debug: Boolean, private val messageCollector: MessageCollector) {
    internal fun log(message: String) {
        if (debug) {
            messageCollector.report(CompilerMessageSeverity.STRONG_WARNING, message)
        }
    }
}
