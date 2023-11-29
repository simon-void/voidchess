package voidchess.common.helper

object RuntimeFacade {
    private val runtime = Runtime.getRuntime()

    fun collectGarbage() {
        runtime.gc()
        Thread.yield()
    }
}
