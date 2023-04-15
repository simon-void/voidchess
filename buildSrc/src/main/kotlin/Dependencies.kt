object Deps {
    const val projectVersion = "9.0.0"
    const val jdkVersion = 17
    const val kotlinLangVersion = "1.9"
    const val kotlinVersion = "1.8.20"
    const val composeVersion = "1.4.0"

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
        "io.mockk:mockk:1.13.5",
    )
}


