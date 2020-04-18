import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(Deps.batikTranscoder)

    testImplementation(Deps.kotlinTest)
    testImplementation(Deps.testNG)
    testImplementation(Deps.mockitoCore)
    testImplementation(Deps.mockk)

    implementation(project(":module-common"))
    implementation(project(":module-engine"))
}

tasks {
    test {
        useTestNG()
    }
}
