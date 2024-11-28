import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(deps.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(deps.plugins.ksp)
    alias(deps.plugins.mavenPublish)
}

dependencies {
    compileOnly(deps.kotlin.gradlePlugin)
}

java { toolchain { languageVersion.set(deps.versions.jdk.map(JavaLanguageVersion::of)) } }

tasks.withType<JavaCompile>().configureEach {
    options.release.set(deps.versions.jvmTarget.map(String::toInt))
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(deps.versions.jvmTarget.map(JvmTarget::fromTarget))

        // Lower version for Gradle compat
        languageVersion.set(KotlinVersion.KOTLIN_1_8)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_8)
    }
}

gradlePlugin {
    plugins {
        create("compiluginPlugin") {
            id = "dev.supersam.compilugin"
            implementationClass = "dev.supersam.gradle.CompiluginSubPlugin"
        }
    }
}


mavenPublishing {
    configure(GradlePlugin(sourcesJar = true, javadocJar = JavadocJar.Javadoc()))
    publishToMavenCentral(automaticRelease = true)
}

// configuration required to produce unique META-INF/*.kotlin_module file names
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions { moduleName.set(project.property("POM_ARTIFACT_ID") as String) }
}