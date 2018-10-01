package voidchess.player.ki.evaluation

import java.text.DecimalFormat


sealed class Evaluated: Comparable<Evaluated> {
    abstract fun setSecondaryEvaluation(secondaryEvaluation: Float)
    abstract fun needsSecondaryEvaluation(): Boolean
    abstract fun isCloseToByPrimary(other: Evaluated): Boolean
    abstract fun isCloseToByCombined(other: Evaluated): Boolean

    override fun equals(other: Any?) =
            if (other is Evaluated) compareTo(other) == 0
            else false

    override fun hashCode(): Int {
        return when(this) {
            is Draw -> 0
            is CheckmateOther -> 1000 + depth
            is CheckmateSelf -> 2000 + depth
            is Ongoing -> Math.abs(primaryEvaluation.toInt())
        }
    }
}

private const val PRIMARY_EQUALITY_CUTOFF_RADIUS = 1f
private const val FINAL_EQUALITY_CUTOFF_RADIUS = 0.5f

object Draw: Evaluated() {

    override fun setSecondaryEvaluation(secondaryEvaluation: Float) =throw UnsupportedOperationException("DrawValue has no secondaryEvaluation")

    override fun needsSecondaryEvaluation() = false

    override fun isCloseToByPrimary(other: Evaluated) =
            if (other is Draw) true
            else other.isCloseToByPrimary(this)

    override fun isCloseToByCombined(other: Evaluated) =
            if (other is Draw) true
            else other.isCloseToByCombined(this)

    override fun compareTo(other: Evaluated) =
            if (other is Draw) 0
            else -other.compareTo(this)

    override fun toString() = Ongoing.format(0f)
}

class CheckmateSelf(val depth: Int) : Evaluated() {

    init {
        assert(depth > 0)
    }

    override fun isCloseToByPrimary(other: Evaluated): Boolean {
        if (other is CheckmateSelf) {
            return depth == other.depth
        }
        return false
    }

    override fun isCloseToByCombined(other: Evaluated) = isCloseToByPrimary(other)

    override fun compareTo(other: Evaluated) =
            if (other is CheckmateSelf) compareWith(other)
            else -1

    private fun compareWith(other: CheckmateSelf) = depth - other.depth

    override fun setSecondaryEvaluation(secondaryEvaluation: Float) = throw UnsupportedOperationException("MattValue has no secondaryEvaluation")

    override fun needsSecondaryEvaluation() = false

    override fun toString() = "checkmate in $depth"
}

class CheckmateOther(val depth: Int) : Evaluated() {

    init {
        assert(depth > 0)
    }

    override fun isCloseToByPrimary(other: Evaluated): Boolean {
        if (other is CheckmateOther) {
            return depth == other.depth
        }
        return false
    }

    override fun isCloseToByCombined(other: Evaluated) = isCloseToByPrimary(other)

    override fun compareTo(other: Evaluated) =
            if (other is CheckmateOther) compareWith(other)
            else 1

    private fun compareWith(other: CheckmateOther) = other.depth - depth

    override fun setSecondaryEvaluation(secondaryEvaluation: Float) = throw UnsupportedOperationException("MattValue has no secondaryEvaluation")

    override fun needsSecondaryEvaluation() = false

    override fun toString() = "checkmate in $depth"
}

class Ongoing(val primaryEvaluation: Float) : Evaluated() {

    private var needsSecondaryEvaluation = true
    private var secondaryEvaluation: Float = 0.toFloat()

    val combinedEvaluation: Float
        get() = primaryEvaluation + secondaryEvaluation

    override fun setSecondaryEvaluation(secondaryEvaluation: Float) {
        assert(needsSecondaryEvaluation)

        this.secondaryEvaluation = secondaryEvaluation
        needsSecondaryEvaluation = false
    }

    override fun needsSecondaryEvaluation() = needsSecondaryEvaluation

    override fun isCloseToByPrimary(other: Evaluated): Boolean {
        return when (other) {
            is Ongoing -> isAbsValueEqualOrLess(primaryEvaluation - other.primaryEvaluation, PRIMARY_EQUALITY_CUTOFF_RADIUS)
            is Draw -> isAbsValueEqualOrLess(primaryEvaluation, PRIMARY_EQUALITY_CUTOFF_RADIUS)
            else -> false
        }
    }

    override fun isCloseToByCombined(other: Evaluated): Boolean {
        return when (other) {
            is Ongoing -> isAbsValueEqualOrLess(combinedEvaluation - other.combinedEvaluation, FINAL_EQUALITY_CUTOFF_RADIUS)
            is Draw -> isAbsValueEqualOrLess(combinedEvaluation, FINAL_EQUALITY_CUTOFF_RADIUS)
            else -> false
        }
    }

    private fun isAbsValueEqualOrLess(value: Float, okRadius: Float) = Math.abs(value) <= okRadius

    override fun compareTo(other: Evaluated): Int {
        return when (other) {
            is Draw -> if(combinedEvaluation>=0) 1 else -1 // prefer Ongoing(0.0) to Draw
            is Ongoing -> compareWith(other)
            else -> -other.compareTo(this)
        }
    }

    private fun compareWith(other: Ongoing) = Math.signum(combinedEvaluation - other.combinedEvaluation).toInt()

    override fun toString(): String {
        return format(combinedEvaluation)
    }

    companion object {
        private val formatter = DecimalFormat().apply{
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        fun format(value: Float): String = formatter.format(value.toDouble())
    }
}
