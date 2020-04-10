package voidchess.common.helper

import java.text.NumberFormat

object RuntimeFacade {
    private val runtime = Runtime.getRuntime()

    fun collectGarbage() {
        runtime.runFinalization()
        runtime.gc()
        Thread.yield()
    }
}
