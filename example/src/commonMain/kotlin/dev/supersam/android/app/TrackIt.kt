package dev.supersam.android.app

@Retention(AnnotationRetention.BINARY)
@Target(
    // function declarations
    // @TrackIt fun Foo() { ... }
    // lambda expressions
    // val foo = @TrackIt { ... }
    AnnotationTarget.FUNCTION,
)
annotation class TrackIt