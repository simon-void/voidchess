plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(Deps.batikTranscoder)

    Deps.testDeps.forEach { testImplementation(it) }

    implementation(project(":module-common"))
}