object Deps {
    const val batikTranscoder = "org.apache.xmlgraphics:batik-transcoder:1.12"
    private const val coroutinesVersion = "1.3.6"

    val coroutineDeps = listOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${coroutinesVersion}"
    )

    val testDeps = listOf(
        "org.jetbrains.kotlin:kotlin-test",
        "org.testng:testng:6.14.3",
        "org.mockito:mockito-core:2.28.2",
        "io.mockk:mockk:1.9.3"
    )
}


