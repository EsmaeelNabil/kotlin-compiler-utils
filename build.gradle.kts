import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        // Include our included build
        classpath(libs.compiluginCompilerPluginGradle)
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kmp) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
    alias(deps.plugins.mavenPublish) apply false
}

subprojects {
    group = project.property("GROUP") as String
    version = project.property("VERSION_NAME") as String

    pluginManager.withPlugin("java") {
        configure<JavaPluginExtension> {
            toolchain { languageVersion.set(libs.versions.jdk.map(JavaLanguageVersion::of)) }
        }
        tasks.withType<JavaCompile>().configureEach {
            options.release.set(libs.versions.jvmTarget.map(String::toInt))
        }
    }

    plugins.withType<KotlinBasePlugin> {
        project.tasks.withType<KotlinCompilationTask<*>>().configureEach {
            compilerOptions {
                progressiveMode.set(true)
                if (this is KotlinJvmCompilerOptions) {
                    if (project.name != "example") {
                        jvmTarget.set(libs.versions.jvmTarget.map(JvmTarget::fromTarget))
                    }
                    freeCompilerArgs.addAll("-Xjvm-default=all")
                }
            }
        }
        if ("example" !in project.path) {
            configure<KotlinProjectExtension> { explicitApi() }
        }
    }

    plugins.withId("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> { publishToMavenCentral(automaticRelease = true) }

        // configuration required to produce unique META-INF/*.kotlin_module file names
        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions { moduleName.set(project.property("POM_ARTIFACT_ID") as String) }
        }
    }
}


System.setProperty("kotlin.compiler.execution.strategy", "in-process")


