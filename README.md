## Compilugin

##### Installing

```kotlin
plugins {
    //...
    id("dev.supersam.compilugin") version "latest_version"
}

```

---

### How to use the : `Functions Visitor`

#### build.gradle.kts

- configure the plugin in your `build.gradle.kts` file

```kotlin
compilugin {
    enabled.set(true)
    functionsVisitorEnabled.set(true)
    functionsVisitorAnnotation.set("TrackIt")
    functionsVisitorPath.set("dev.supersam.android.app.FunctionsVisitor.visit")
}
```

- Create your own `Annotation` and annotate your functions with it
- Create an `Object` that will be called when the function which is annotated with your `Annotation`
  is called.

#### Annotation

```kotlin
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class TrackIt
```

#### Object

```kotlin

package any.packagename.you.need

object AnyObjectNameYouWant {
    // parameter names are not important, but the types or the order are important.
    fun anyFunctionName(
        name: String,
        parent: String,
        body: String,
        args: Map<String, Any?>,
    ) {
        // your code here, logging/tracking events, etc.
    }
}
```

> [!WARNING]
> Parameter names are not important, but the types or the order are important.

#### In your code

```kotlin
@TrackIt
fun anyFunctionName(
    anyParameterName: Any,
) {
    // your code here
}
```

> [!IMPORTANT]
> All arguments are supported as long as they give meaningful information when .toString() is called
> on them.
> If you want to use a custom object, make sure to override the `toString()` method for that object.

---