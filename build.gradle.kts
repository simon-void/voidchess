import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.4.10"
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

application {
    mainClassName = "voidchess.VoidchessAppKt"
// this is the replacement for the deprecated mainClassName in Gradle 8.0
// but currently ShadowJar still requires `mainClassName`
//    mainClass.set("voidchess.VoidchessAppKt")
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-ui"))
    implementation(project(":module-united"))
}


allprojects {
    group = "de.gmx.simonvoid"
    version = "3.5"

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "14"
                kotlinOptions {
                    languageVersion = "1.4"
                }
            }
        }

        withType<Test> {
            useTestNG()
        }
    }
}

tasks {
    register("buildInstaller") {
        dependsOn("build")

        doLast {
            if (JavaVersion.current() < JavaVersion.VERSION_14) {
                throw GradleException("Require Java 14+ to run 'jpackage' (currently ${JavaVersion.current()})")
            }
            val projectVersion = project.version.toString()
            JPackage.buildInstaller(
                name = "VoidChess",
                description = "a chess program",
                appVersion = projectVersion,
                inputDir = "build/libs",
                destinationDir = "build/installer",
                mainJar = "voidchess-$projectVersion-all.jar",
                addModules = listOf("java.desktop"),
                winIcoIconPath = "about/shortcut-icon2.ico",
                winShortcut = true,
                winMenu = true,
                linuxPngIconPath = "about/shortcut-icon2.png",
                linuxShortcut = true,
                linuxMenuGroup = "Games",
                macIcnsIconPath = "about/shortcut-icon2.icns"
            )
        }
    }
}
