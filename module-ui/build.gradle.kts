plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(Deps.jdkVerion)
}

dependencies {
    implementation(Deps.batikTranscoder)

    Deps.testDeps.forEach { testImplementation(it) }

    implementation(project(":module-common"))
}