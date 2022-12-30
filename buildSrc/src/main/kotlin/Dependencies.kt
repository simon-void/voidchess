object Deps {
    const val projectVersion = "7"
    const val jdkVerion = 17
    const val kotlinLangVersion = "1.8"
    const val kotlinVersion = "1.8.0"

    const val shadowPluginVersion = "7.1.2"
    const val batikTranscoder = "org.apache.xmlgraphics:batik-transcoder:1.16"

    private const val coroutinesVersion = "1.6.4"
    val coroutineDeps = listOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${coroutinesVersion}",
    )

    val testDeps = listOf(
        "org.jetbrains.kotlin:kotlin-test:$kotlinVersion",
        "org.testng:testng:7.7.1",
        "io.mockk:mockk:1.13.3",
    )
}


