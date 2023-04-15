import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Deps.kotlinVersion
    id("org.jetbrains.compose") version Deps.composeVersion
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":module-common"))
    implementation(project(":module-ui"))
    implementation(project(":module-united"))
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(Deps.jdkVersion)
}

compose.desktop {
    application {
        mainClass = "voidchess.VoidchessAppKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "voidchess"
            packageVersion = Deps.projectVersion
        }
    }
}

allprojects {
    group = "de.gmx.simonvoid"
    version = Deps.projectVersion

    repositories {
        mavenCentral()
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                kotlinOptions {
                    languageVersion = Deps.kotlinLangVersion
                    apiVersion = Deps.kotlinLangVersion
                }
            }
        }

        withType<Test> {
            useTestNG()
        }
    }
}
