package dev.supersam.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class CompiluginPluginExtension @Inject constructor(objects: ObjectFactory) {

    public val enabled: Property<Boolean> =
        objects.property(Boolean::class.javaObjectType).convention(true)

    public val logging: Property<Boolean> =
        objects.property(Boolean::class.javaObjectType).convention(false)

    public val functionsVisitorEnabled: Property<Boolean> =
        objects.property(Boolean::class.javaObjectType).convention(false)

    public val functionsVisitorPath: Property<String> = objects.property(String::class.java)

    public val functionsVisitorAnnotation: Property<String> = objects.property(String::class.java)

    public val composeModifierWrapperEnabled: Property<Boolean> =
        objects.property(Boolean::class.javaObjectType).convention(false)

    public val composeModifierWrapperPath: Property<String> = objects.property(String::class.java)

}