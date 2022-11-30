plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    Deps.coroutineDeps.forEach { implementation(it) }
    Deps.testDeps.forEach { testImplementation(it) }

    implementation(project(":module-common"))
}