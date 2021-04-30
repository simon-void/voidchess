object Deps {
    const val kotlinVersion = "1.5.0"
    const val batikTranscoder = "org.apache.xmlgraphics:batik-transcoder:1.14"
    private const val coroutinesVersion = "1.4.3"

    val coroutineDeps = listOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${coroutinesVersion}",
    )

    val testDeps = listOf(
        "org.jetbrains.kotlin:kotlin-test:$kotlinVersion",
        "org.testng:testng:7.4.0",
        "io.mockk:mockk:1.11.0",
    )
}


