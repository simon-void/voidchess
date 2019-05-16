package voidchess.player.ki.evaluation

import voidchess.helper.signAsInt
import java.text.DecimalFormat


sealed class Evaluation {
    abstract val type: EvaluationType
    abstract val msg: String
}

enum class EvaluationType {
    SelfCheckmate, OngoingNegative, FinalDraw, OngoingPositive, OtherCheckmate
}

sealed class NumericalEvaluation : Evaluation() {
    open val preliminaryEvaluation = .0
    open val fullEvaluation = .0
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
        assert(depth > 0)
    }

    fun compareWith(other: CheckmateOther) = other.depth - depth

    override val msg get() = "checkmate in $depth"
}

data class Ongoing(override val preliminaryEvaluation: Double, override val fullEvaluation: Double) : NumericalEvaluation() {
    override val type = if (fullEvaluation < 0) EvaluationType.OngoingNegative else EvaluationType.OngoingPositive

    fun compareWith(other: Ongoing) = (fullEvaluation - other.fullEvaluation).signAsInt

    override val msg get() = format(fullEvaluation)

    companion object {
        private val formatter = DecimalFormat().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        fun format(value: Double): String = formatter.format(value)
    }
}

object LowestEvaluationFirstComparator : Comparator<Evaluation> {
    override fun compare(e1: Evaluation, e2: Evaluation): Int {
        val typesCompared = e1.type.compareTo(e2.type)
        return if (typesCompared != 0) typesCompared
        else when (e1.type) {
            EvaluationType.OngoingPositive, EvaluationType.OngoingNegative -> (e1 as Ongoing).compareWith(e2 as Ongoing)
            EvaluationType.OtherCheckmate -> (e1 as CheckmateOther).compareWith(e2 as CheckmateOther)
            EvaluationType.SelfCheckmate -> (e1 as CheckmateSelf).compareWith(e2 as CheckmateSelf)
            EvaluationType.FinalDraw -> 0
        }
    }
}

object HighestEvaluationFirstComparator : Comparator<Evaluation> {
    override fun compare(e1: Evaluation, e2: Evaluation) =
            LowestEvaluationFirstComparator.compare(e2, e1)
}
