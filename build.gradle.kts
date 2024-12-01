import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mavenPublishPluginId = deps.plugins.mavenPublish.get().pluginId
val jvmTargetVersion = deps.versions.jvmTarget
val buildConfigPluginId = deps.plugins.buildConfig.get().pluginId

plugins {
    alias(deps.plugins.kotlin.jvm) apply false
    alias(deps.plugins.ksp) apply false
    alias(deps.plugins.kmp) apply false
    alias(deps.plugins.mavenPublish) apply false
    alias(deps.plugins.buildConfig) apply false
}

subprojects {
    group = project.property("GROUP") as String
    version = project.property("VERSION_NAME") as String

    // Apply the buildConfig plugin to all subprojects
    apply(plugin = buildConfigPluginId)
    // configure the buildConfig plugin to generate the fields we need
    extensions.configure<com.github.gmazzo.buildconfig.BuildConfigExtension> {
        packageName.set("dev.supersam.compilugin")
        documentation.set("Generated at build time")

        buildConfigField<String>("COMPILUGIN_PLUGIN_ID", "compiluginPlugin")
    }

    pluginManager.withPlugin("java") {
        configure<JavaPluginExtension> {
            toolchain { languageVersion.set(deps.versions.jdk.map(JavaLanguageVersion::of)) }
        }
        tasks.withType<JavaCompile>().configureEach {
            options.release.set(jvmTargetVersion.map(String::toInt))
        }
    }

    plugins.withType<KotlinBasePlugin> {
        project.tasks.withType<KotlinCompilationTask<*>>().configureEach {
            compilerOptions {
                progressiveMode.set(true)
                if (this is KotlinJvmCompilerOptions) {
                    jvmTarget.set(jvmTargetVersion.map(JvmTarget::fromTarget))
                    freeCompilerArgs.addAll("-Xjvm-default=all")
                }
            }
        }

        configure<KotlinProjectExtension> { explicitApi() }
    }

    plugins.withId(mavenPublishPluginId) {
        // configuration required to produce unique META-INF/*.kotlin_module file names
        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions { moduleName.set(project.property("POM_ARTIFACT_ID") as String) }
        }
    }
}


System.setProperty("kotlin.compiler.execution.strategy", "in-process")


