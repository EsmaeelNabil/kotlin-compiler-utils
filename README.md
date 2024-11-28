## Compilugin

##### Installing

```kotlin
plugins {
    //...
    id("dev.supersam.compilugin") version "0.0.2"
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


License
-------
Copyright 2024 Esmaeel Moustafa

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
