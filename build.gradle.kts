import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Deps.kotlinVersion
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application {
    mainClass.set("voidchess.VoidchessAppKt")
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-ui"))
    implementation(project(":module-united"))
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

allprojects {
    group = "de.gmx.simonvoid"
    version = "5"

    repositories {
        mavenCentral()
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                kotlinOptions {
                    languageVersion = Deps.languageVersion
                    apiVersion = Deps.languageVersion
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
            if (JavaVersion.current() < JavaVersion.VERSION_16) {
                throw GradleException("Require JDK 16+ to run 'jpackage' (currently ${JavaVersion.current()})")
            }
            println("""
                these packaging tools have to be installed because they are required by jpackage:
                    - on Red Hat Linux: the rpm-build package
                    - on Ubuntu Linux: the fakeroot package
                    - on macOS: Xcode command line tools
                    - on Windows: WiX 3.0 or later
            """.trimIndent())
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
