pluginManagement {
    includeBuild("..")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

includeBuild("..") {
    dependencySubstitution {
        substitute(module("dev.supersam:compilugin-compiler-plugin")).using(project(":compilugin-compiler-plugin"))
        substitute(
            module("dev.supersam:compilugin-compiler-plugin-gradle"),
        ).using(project(":compilugin-compiler-plugin-gradle"))
    }
}

rootProject.name = "android-example"
include(":app")
