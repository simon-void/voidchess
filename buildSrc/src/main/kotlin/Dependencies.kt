object Deps {
    const val kotlinVersion = "1.6.10"
    const val batikTranscoder = "org.apache.xmlgraphics:batik-transcoder:1.14"
    private const val coroutinesVersion = "1.6.0"

    val coroutineDeps = listOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${coroutinesVersion}",
    )

    val testDeps = listOf(
        "org.jetbrains.kotlin:kotlin-test:$kotlinVersion",
        "org.testng:testng:7.4.0",
        "io.mockk:mockk:1.12.1",
    )
}


