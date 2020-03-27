import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.71"
    application
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

repositories {
    mavenCentral()
    jcenter()
}

group = "de.gmx.simonvoid"
version = "3.0.0"

application {
    mainClassName = "voidchess.ui.ChessFrameKt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.xmlgraphics:batik-transcoder:1.12")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.testng:testng:6.14.3")
    testImplementation("org.mockito:mockito-core:2.28.2")
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useTestNG()
    }

    // TODO Distrubution
    //    ----------------
    //   - check out Distributing the ShadowJar:
    //      https://imperceptiblethoughts.com/shadow/application-plugin/#distributing-the-shadow-jar
    //   - investigate using jlink to create a minimal JRE in a
    //      running only this project and system module 'java.desktop' (for swing/imageIO)
    //      e.g. via badass-runtime-plugin: https://badass-runtime-plugin.beryx.org/releases/latest/
    //   - and or the packaging tool that comes with Java 14 to create a native executable
    //      https://openjdk.java.net/jeps/343
}