import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.30"
    application
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("edu.sc.seis.launch4j") version "2.4.5"
}

repositories {
    mavenCentral()
    jcenter()
}

group = "de.gmx.simonvoid"
version = "3.0-beta"

application {
    mainClassName = "voidchess.ui.ChessFrameKt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.xmlgraphics:batik-transcoder:1.10")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.30")
    testImplementation("org.testng:testng:6.14.3")
    testImplementation("org.mockito:mockito-core:2.21.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useTestNG()
    }

    // launch4j has issues finding alternative (non-Oracle) JVMs
    // see https://sourceforge.net/p/launch4j/feature-requests/127/
    // TODO check out Distributing the ShadowJar:
    //      https://imperceptiblethoughts.com/shadow/application-plugin/#distributing-the-shadow-jar
    // TODO investigate using jlink to create a minimal JRE
    //      running only this project and system module 'java.desktop' (for swing/imageIO)
    //      e.g. via badass-runtime-plugin: https://badass-runtime-plugin.beryx.org/releases/latest/
    launch4j {
        mainClassName = "voidchess.ui.ChessFrameKt"
        icon = "${projectDir.canonicalPath}/about/exe-icon.ico"
        jar = shadowJar.get().archiveFileName.get()
        headerType = "gui"
        outfile = "${project.name}-${project.version}.exe"
        outputDir = "libs/"
        jreMinVersion = "1.8.0"
        initialHeapSize = 512
        maxHeapSize = 2048
        jvmOptions = setOf(
                "-Dfile.encoding=UTF8",
                "-Xmx${maxHeapSize}m",
                "-Xms${initialHeapSize}m"
        )
    }
}