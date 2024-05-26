object Deps {
    const val projectVersion = "12"
    const val jdkVersion = 21
    const val kotlinLangVersion = "2.0"
    const val kotlinVersion = "2.0.0"

    const val shadowPluginVersion = "8.1.1"
    const val batikTranscoder = "org.apache.xmlgraphics:batik-transcoder:1.17"

    private const val coroutinesVersion = "1.8.1"
    val coroutineDeps = listOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${coroutinesVersion}",
    )

    val testDeps = listOf(
        "org.jetbrains.kotlin:kotlin-test:$kotlinVersion",
        "org.testng:testng:7.10.2",
        "io.mockk:mockk:1.13.10",
    )
}


