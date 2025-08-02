package dev.supersam.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

internal inline fun <reified T> ObjectFactory.setProperty(
    configuration: SetProperty<T>.() -> Unit = {},
): SetProperty<T> = setProperty(T::class.java).apply(configuration)

internal inline fun <reified T> ObjectFactory.property(configuration: Property<T>.() -> Unit = {}): Property<T> =
    property(T::class.java).apply(configuration)
