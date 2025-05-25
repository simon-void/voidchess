plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(JDK.version)
}

dependencies {
    implementation(libs.bundles.coroutines)
    testImplementation(libs.bundles.test.testng.mockk)

    implementation(project(":module-common"))
    implementation(project(":module-engine"))
}