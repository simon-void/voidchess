object Deps {
    const val projectVersion = "14"
    const val jdkVersion = 21
    const val kotlinVersion = "2.2.0-RC"

    const val shadowPluginVersion = "8.1.1"
    const val batikTranscoder = "org.apache.xmlgraphics:batik-transcoder:1.19"

    private const val coroutinesVersion = "1.10.2"
    val coroutineDeps = listOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${coroutinesVersion}",
    )

    val testDeps = listOf(
        "org.jetbrains.kotlin:kotlin-test:$kotlinVersion",
        "org.testng:testng:7.11.0",
        "io.mockk:mockk:1.14.2",
    )
}


