plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version Deps.composeVersion
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(Deps.jdkVersion)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(Deps.batikTranscoder)

    Deps.testDeps.forEach { testImplementation(it) }

    implementation(project(":module-common"))
}