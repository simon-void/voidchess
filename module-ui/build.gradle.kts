plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Deps.kotlinVersion}")
    implementation(Deps.batikTranscoder)

    Deps.testDeps.forEach { testImplementation(it) }

    implementation(project(":module-common"))
}