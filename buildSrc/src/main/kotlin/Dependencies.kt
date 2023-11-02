object Deps {
    const val projectVersion = "10"
    const val jdkVersion = 17
    const val kotlinLangVersion = "2.0"
    const val kotlinVersion = "1.9.20"

    const val shadowPluginVersion = "7.1.2"
    const val batikTranscoder = "org.apache.xmlgraphics:batik-transcoder:1.17"

    private const val coroutinesVersion = "1.7.3"
    val coroutineDeps = listOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${coroutinesVersion}",
    )

    val testDeps = listOf(
        "org.jetbrains.kotlin:kotlin-test:$kotlinVersion",
        "org.testng:testng:7.8.0",
        "io.mockk:mockk:1.13.8",
    )
}


