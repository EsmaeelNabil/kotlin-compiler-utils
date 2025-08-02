plugins {
    alias(deps.plugins.kotlin.jvm)
    alias(deps.plugins.mavenPublish)
    alias(deps.plugins.ksp)
}

dependencies {
    implementation(deps.autoService)
    ksp(deps.autoService.ksp)
    compileOnly(deps.kotlinCompilerEmbeddable)
    compileOnly(deps.kotlin.stdlib)

    testImplementation(deps.kotlin.reflect)
    testImplementation(deps.kotlin.stdlib)
    testImplementation(deps.kotlinCompilerEmbeddable)
    testImplementation(libs.kotlin.aptEmbeddable)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(deps.kotlinCompileTesting)
    testImplementation(deps.junit)
    testImplementation(deps.truth)
}
