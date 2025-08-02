package dev.supersam.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Configuration extension for the Compilugin Kotlin compiler plugin.
 *
 * This extension allows users to configure various aspects of the plugin behavior,
 * including enabling/disabling features and configuring the functions visitor functionality.
 *
 * Example usage in build.gradle.kts:
 * ```kotlin
 * compilugin {
 *     enabled.set(true)
 *     logging.set(true)
 *     functionsVisitorEnabled.set(true)
 *     functionsVisitorAnnotation.set("com.example.TrackIt")
 *     functionsVisitorPath.set("com.example.FunctionsVisitor.visit")
 * }
 * ```
 */
public open class CompiluginPluginExtension internal constructor(factory: ObjectFactory) {

    /** Whether the Compilugin plugin is enabled. Defaults to false. */
    public val enabled: Property<Boolean> = factory.property { set(false) }

    /** Whether to enable debug logging for the plugin. Defaults to false. */
    public val logging: Property<Boolean> = factory.property { set(false) }

    /** Whether the functions visitor feature is enabled. Defaults to false. */
    public val functionsVisitorEnabled: Property<Boolean> = factory.property { set(false) }

    /**
     * The fully qualified path to the visitor function that should be called.
     * Format: "package.ObjectName.functionName"
     * Example: "com.example.FunctionsVisitor.visit"
     */
    public val functionsVisitorPath: Property<String> = factory.property { set(empty) }

    /**
     * The fully qualified name of the annotation used to mark functions for visiting.
     * Example: "com.example.TrackIt"
     */
    public val functionsVisitorAnnotation: Property<String> = factory.property { set(empty) }
}
