package voidchess.common.helper

import java.text.NumberFormat

object RuntimeFacade {
    private val runtime = Runtime.getRuntime()

    fun collectGarbage() {
        runtime.runFinalization()
        runtime.gc()
        Thread.yield()
    }

    fun printMemoryUsage(mark: String) {
        val numberFormat = NumberFormat.getPercentInstance()
        val total = runtime.totalMemory()
        val free = runtime.freeMemory()
        val used = total - free
        val usedPercentage = numberFormat.format(used / total.toDouble())

        println("$mark : $usedPercentage of ${total / 1000000}MB")
    }
}
