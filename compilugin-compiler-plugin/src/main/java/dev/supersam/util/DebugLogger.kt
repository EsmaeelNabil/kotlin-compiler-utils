package dev.supersam.util

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * Debug logger for the Compilugin plugin.
 *
 * Provides conditional logging functionality that only outputs messages when debug mode is enabled.
 * Messages are reported to the Kotlin compiler's message collector as strong warnings.
 *
 * @param debug Whether debug logging is enabled
 * @param messageCollector The compiler's message collector for output
 */
internal class DebugLogger(private val debug: Boolean, private val messageCollector: MessageCollector) {

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param message The message to log
     */
    internal fun log(message: String) {
        if (debug) {
            messageCollector.report(CompilerMessageSeverity.STRONG_WARNING, message)
        }
    }
}
