package dev.supersam.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

public class CompiluginSubPlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create(EXTENSION_NAME, CompiluginPluginExtension::class.java)
    }

    override fun getCompilerPluginId(): String = COMPILUGIN_PLUGIN_ID

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = GROUP_ID,
        artifactId = ARTIFACT_ID,
        version = VERSION
    )


    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(CompiluginPluginExtension::class.java)
            ?: return kotlinCompilation.target.project.provider { emptyList() }

        val enabled = extension.enabled.get().toString()
        val logging = extension.logging.get().toString()

        val functionsVisitorEnabled = extension.functionsVisitorEnabled.get().toString()
        val functionsVisitorAnnotation = extension.functionsVisitorAnnotation.get()
        val functionsVisitorPath = extension.functionsVisitorPath.get()

        val composeModifierWrapperEnabled = extension.composeModifierWrapperEnabled.get().toString()
        val composeModifierWrapperPath = extension.composeModifierWrapperPath.get()


        return kotlinCompilation.target.project.provider {
            mutableListOf(
                SubpluginOption(ENABLED, enabled),
                SubpluginOption(LOGGING, logging),
                SubpluginOption(FUNCTIONS_VISITOR_ENABLED, functionsVisitorEnabled),
                SubpluginOption(FUNCTIONS_VISITOR_ANNOTATION, functionsVisitorAnnotation),
                SubpluginOption(FUNCTIONS_VISITOR_PATH, functionsVisitorPath),
                SubpluginOption(COMPOSE_MODIFIER_WRAPPER_ENABLED, composeModifierWrapperEnabled),
                SubpluginOption(COMPOSE_MODIFIER_WRAPPER_PATH, composeModifierWrapperPath)
            )
        }
    }

}