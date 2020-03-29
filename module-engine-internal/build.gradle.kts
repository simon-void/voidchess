import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(Deps.kotlinTest)
    testImplementation(Deps.testNG)
    testImplementation(Deps.mockitoCore)
    testImplementation(Deps.mockk)

    implementation(project(":module-common"))
}

tasks {
    test {
        useTestNG()
    }
}