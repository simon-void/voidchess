package voidchess.engine.concurrent

import voidchess.common.engine.*

internal abstract class ConcurrencyStrategy {

    // TODO when Kotlin contracts come out: result==null <=> currentMaxEvaluation==null
    fun getOkEval(
        currentMaxEvaluation: Evaluation?,
        numericEvalOkRadius: Double
    ): Evaluation? = currentMaxEvaluation?.let { currentMax ->
        if (currentMax is NumericalEvaluation) {
            Ongoing(currentMax.numericValue - numericEvalOkRadius)
        } else {
            currentMax
        }
    }
}
