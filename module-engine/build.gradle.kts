plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Deps.coroutinesVersion}")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Deps.coroutinesVersion}")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Deps.coroutinesVersion}")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${Deps.coroutinesVersion}")

    testImplementation(Deps.kotlinTest)
    testImplementation(Deps.testNG)
    testImplementation(Deps.mockitoCore)
    testImplementation(Deps.mockk)

    implementation(project(":module-common"))
}