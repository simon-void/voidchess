import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.3.71"
    application
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

application {
    mainClassName = "voidchess.VoidchessAppKt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":module-ui"))
}


allprojects {
    group = "de.gmx.simonvoid"
    version = "3.2.0"

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "11"
            }
        }
    }
}

// TODO Distribution
//    ----------------
//   - check out Distributing the ShadowJar:
//      https://imperceptiblethoughts.com/shadow/application-plugin/#distributing-the-shadow-jar
//   - investigate using jlink to create a minimal JRE in a
//      running only this project and system module 'java.desktop' (for swing/imageIO)
//      e.g. via badass-runtime-plugin: https://badass-runtime-plugin.beryx.org/releases/latest/
//   - and or the packaging tool that comes with Java 14 to create a native executable
//      https://openjdk.java.net/jeps/343
