plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Deps.kotlinVersion}")
    Deps.coroutineDeps.forEach { implementation(it) }
    Deps.testDeps.forEach { testImplementation(it) }

    implementation(project(":module-common"))
}