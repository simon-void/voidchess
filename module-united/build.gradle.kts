plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    Deps.coroutineDeps.forEach { implementation(it) }
    Deps.testDeps.forEach { testImplementation(it) }

    implementation(project(":module-common"))
    implementation(project(":module-engine"))
}