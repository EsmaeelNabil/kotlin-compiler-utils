package dev.supersam.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

public open class CompiluginPluginExtension internal constructor(factory: ObjectFactory) {
    public val enabled: Property<Boolean> = factory.property { set(false) }
    public val logging: Property<Boolean> = factory.property { set(false) }
    public val functionsVisitorEnabled: Property<Boolean> = factory.property { set(false) }
    public val functionsVisitorPath: Property<String> = factory.property { set(empty) }
    public val functionsVisitorAnnotation: Property<String> = factory.property { set(empty) }
}
