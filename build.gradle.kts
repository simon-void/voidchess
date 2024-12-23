import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.gmx.simonvoid"
version = "13"

repositories {
    mavenCentral()
}

application {
    mainClass.set("voidchess.app.VoidchessAppKt")
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        extraWarnings.set(true)
        freeCompilerArgs.add("-Xsuppress-warning=UNUSED_ANONYMOUS_PARAMETER")
    }
}

dependencies {
    val coroutinesVersion = "1.10.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:${coroutinesVersion}")

    implementation("org.apache.xmlgraphics:batik-transcoder:1.18")

    testImplementation(kotlin("test"))
    testImplementation("org.testng:testng:7.10.2")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("com.lemonappdev:konsist:0.17.3")
}

tasks {
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
