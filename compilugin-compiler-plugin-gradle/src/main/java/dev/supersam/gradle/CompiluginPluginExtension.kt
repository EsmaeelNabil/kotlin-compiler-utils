package dev.supersam.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class CompiluginPluginExtension internal constructor(factory: ObjectFactory) {
    val enabled: Property<Boolean> = factory.property { set(false) }
    val logging: Property<Boolean> = factory.property { set(false) }
    val functionsVisitorEnabled: Property<Boolean> = factory.property { set(false) }
    val functionsVisitorPath: Property<String> = factory.property { set(empty) }
    val functionsVisitorAnnotation: Property<String> = factory.property { set(empty) }
    val composeModifierWrapperEnabled: Property<Boolean> = factory.property { set(false) }
    val composeModifierWrapperPath: Property<String> = factory.property { set(empty) }
}