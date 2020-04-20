package voidchess.common.player.ki.evaluation

import voidchess.common.helper.signAsInt
import java.text.DecimalFormat


sealed class Evaluation: Comparable<Evaluation> {
    abstract val type: EvaluationType
    abstract val msg: String

    override operator fun compareTo(other: Evaluation): Int {
        val typesCompared = type.compareTo(other.type)
        return if (typesCompared != 0) typesCompared
        else when (this) {
            is Ongoing -> compareWith(other as Ongoing)
            is CheckmateOther -> compareWith(other as CheckmateOther)
            is CheckmateSelf ->compareWith(other as CheckmateSelf)
            else -> 0
        }
    }
}

enum class EvaluationType {
    SelfCheckmate, OngoingNegative, FinalDraw, OngoingPositive, OtherCheckmate
}

sealed class NumericalEvaluation : Evaluation() {
    open val numericValue = .0
    override fun toString(): String = this::class.java.simpleName
}

object Draw : NumericalEvaluation() {
    override val type = EvaluationType.FinalDraw
    override val msg = "draw"
}

object Stalemate : NumericalEvaluation() {
    override val type = EvaluationType.FinalDraw
    override val msg = "stalemate"
}

object ThreeFoldRepetition : NumericalEvaluation() {
    override val type = EvaluationType.FinalDraw
    override val msg = "3-fold repetition"
}

// so why has CheckmateSelf a positional evaluation while CheckmateOther has not?
// because a human player might screw up the mating attack in which case the computer should be in a as decent position as possible.
data class CheckmateSelf(val depth: Int, val materialEvaluation: Double) : Evaluation() {
    override val type = EvaluationType.SelfCheckmate

    init {
        // TODO make depth a UInt when UInts no longer are experimental
        assert(depth > 0)
    }

    override val msg get() = "checkmate in $depth"

    fun compareWith(other: CheckmateSelf): Int {
        val depthDiff = depth - other.depth
        return if (depthDiff != 0) {
            depthDiff
        } else {
            materialEvaluation.compareTo(other.materialEvaluation)
        }
    }
}

data class CheckmateOther(val depth: Int) : Evaluation() {
    override val type = EvaluationType.OtherCheckmate

    init {
        // TODO make depth a UInt when UInts no longer are experimental
        assert(depth >= 0)
    }

    fun compareWith(other: CheckmateOther) = other.depth - depth

    override val msg get() = "checkmate in $depth"
}

data class Ongoing(override val numericValue: Double) : NumericalEvaluation() {
    override val type = if (numericValue < 0) EvaluationType.OngoingNegative else EvaluationType.OngoingPositive
    fun compareWith(other: Ongoing) = (numericValue - other.numericValue).signAsInt
    override val msg get() = format(numericValue)
    override fun toString(): String = "Ongoing($msg)"

    companion object {
        private val formatter = DecimalFormat().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        fun format(value: Double): String = formatter.format(value)
    }
}
