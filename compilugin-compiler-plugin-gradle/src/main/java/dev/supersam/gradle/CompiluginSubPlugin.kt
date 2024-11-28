package dev.supersam.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class CompiluginSubPlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create("compilugin", CompiluginPluginExtension::class.java)
    }

    override fun getCompilerPluginId(): String = "compiluginPlugin"

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "dev.supersam.compilugin",
        artifactId = "compilugin-compiler-plugin",
        version = "0.0.1"
    )


    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(CompiluginPluginExtension::class.java)

        val enabled = extension.enabled.get().toString()
        val logging = extension.logging.get().toString()

        val functionsVisitorEnabled = extension.functionsVisitorEnabled.get().toString()
        val functionsVisitorAnnotation = extension.functionsVisitorAnnotation.get()
        val functionsVisitorPath = extension.functionsVisitorPath.getOrElse("")

        val composeModifierWrapperEnabled = extension.composeModifierWrapperEnabled.get().toString()
        val composeModifierWrapperPath = extension.composeModifierWrapperPath.get()


        return kotlinCompilation.target.project.provider {
            mutableListOf(
                SubpluginOption("enabled", enabled),
                SubpluginOption("logging", logging),
                SubpluginOption("functionsVisitorEnabled", functionsVisitorEnabled),
                SubpluginOption("functionsVisitorAnnotation", functionsVisitorAnnotation),
                SubpluginOption("functionsVisitorPath", functionsVisitorPath),
                SubpluginOption("composeModifierWrapperEnabled", composeModifierWrapperEnabled),
                SubpluginOption("composeModifierWrapperPath", composeModifierWrapperPath)
            )
        }
    }

}
