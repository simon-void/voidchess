
plugins {
    kotlin("jvm") version Deps.kotlinVersion
    application
    id("com.github.johnrengelman.shadow") version Deps.shadowPluginVersion
}

application {
    mainClass = "voidchess.VoidchessAppKt"
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-ui"))
    implementation(project(":module-united"))
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(Deps.jdkVersion)
}

allprojects {
    group = "de.gmx.simonvoid"
    version = Deps.projectVersion

    repositories {
        mavenCentral()
    }

    tasks {
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
