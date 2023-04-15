plugins {
    kotlin("jvm")
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(Deps.jdkVersion)
}

dependencies {
    Deps.coroutineDeps.forEach { implementation(it) }
    Deps.testDeps.forEach { testImplementation(it) }

    implementation(project(":module-common"))
}