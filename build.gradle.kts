import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.gmx.simonvoid"
version = "13"

repositories {
    mavenCentral()
}

application {
    mainClass.set("voidchess.VoidchessAppKt")
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(21)
}

dependencies {
    val coroutinesVersion = "1.9.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:${coroutinesVersion}")

    implementation("org.apache.xmlgraphics:batik-transcoder:1.17")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.testng:testng:7.10.2")
    testImplementation("io.mockk:mockk:1.13.12")
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    withType<Test> {
        useTestNG()
    }

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
                destinationDir = File(projectDir, "build/installer").absolutePath,
                mainJar = "voidchess-$projectVersion-all.jar",
                addModules = listOf("java.desktop"),
                winIcoIconPath = "about/shortcut-icon2.ico",
                winShortcut = true,
                winMenu = true,
                winPackageType = WinPackageType.MSI,
                linuxPngIconPath = "about/shortcut-icon2.png",
                linuxShortcut = true,
                linuxMenuGroup = "Games",
                linuxPackageType = LinuxPackageType.DEB,
                macIcnsIconPath = "about/shortcut-icon2.icns"
            )
        }
    }
}
