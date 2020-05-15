plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(Deps.batikTranscoder)

    Deps.testDeps.forEach { testImplementation(it) }

    implementation(project(":module-common"))
}