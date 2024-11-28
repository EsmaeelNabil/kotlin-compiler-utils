pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    versionCatalogs {
        create("deps") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}


include(
    ":compilugin-compiler-plugin",
    ":example",
)

includeBuild("compilugin-compiler-plugin-gradle") {
    dependencySubstitution {
        substitute(module("dev.supersam.compilugin:compilugin-compiler-plugin-gradle")).using(project(":"))
    }
}

rootProject.name = "compilugin-compiler-plugin"