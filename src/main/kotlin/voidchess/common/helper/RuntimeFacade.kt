package voidchess.common.helper

data object RuntimeFacade {
    private val runtime = Runtime.getRuntime()

    fun collectGarbage() {
        runtime.gc()
        Thread.yield()
    }
}
