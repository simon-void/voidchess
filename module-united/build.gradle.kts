plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(Deps.kotlinTest)
    testImplementation(Deps.testNG)
    testImplementation(Deps.mockitoCore)
    testImplementation(Deps.mockk)

    implementation(project(":module-common"))
    implementation(project(":module-engine"))
}