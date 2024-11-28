pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        maybeCreate("deps").apply {
            from(files("../gradle/libs.versions.toml"))
        }
    }
    repositories { mavenCentral() }
}