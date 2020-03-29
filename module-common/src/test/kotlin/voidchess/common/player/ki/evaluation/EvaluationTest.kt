package voidchess.common.player.ki.evaluation

import org.testng.Assert.*
import org.testng.annotations.Test
import voidchess.common.player.ki.evaluation.*

import java.util.*


internal class LowestEvaluationFirstComparatorTest {
    @Test
    fun `test general order created by compareTo`() {
        val selfMateIn1 = CheckmateSelf(1, 2.0)
        val selfMateIn2 = CheckmateSelf(2, 2.0)
        val negativeOngoingValueMinus2 = Ongoing(5.0, -2.0)
        val positiveOngoingValue0 = Ongoing(5.0, -0.0)
        val positiveOngoingValuePlus2 = Ongoing(5.0, 2.0)
        val draw = Draw
        val otherMateIn1 = CheckmateOther(1)
        val otherMateIn2 = CheckmateOther(2)

        val list = mutableListOf(
                otherMateIn1, selfMateIn1,
                otherMateIn2, selfMateIn2,
                draw, positiveOngoingValuePlus2,
                negativeOngoingValueMinus2, positiveOngoingValue0
        )
        Collections.sort(list, LowestEvaluationFirstComparator)

        val expectedOrder = listOf(
                selfMateIn1, selfMateIn2,
                negativeOngoingValueMinus2, draw,
                positiveOngoingValue0, positiveOngoingValuePlus2,
                otherMateIn2, otherMateIn1
        )

        assertEquals(list, expectedOrder, "from worst to best")
    }

    @Test
    fun `test self checkmate order created by compareTo`() {
        val selfMateIn1MatEval5 = CheckmateSelf(1, 5.0)
        val selfMateIn1MatEval3 = CheckmateSelf(1, 3.0)
        val selfMateIn2MatEval4 = CheckmateSelf(2, 4.0)
        val selfMateIn2MatEval6 = CheckmateSelf(2, 6.0)

        val list = mutableListOf(
                selfMateIn2MatEval6, selfMateIn2MatEval4,
                selfMateIn1MatEval5, selfMateIn1MatEval3
        )
        Collections.sort(list, LowestEvaluationFirstComparator)

        val expectedOrder = listOf(
                selfMateIn1MatEval3, selfMateIn1MatEval5, selfMateIn2MatEval4, selfMateIn2MatEval6
        )

        assertEquals(list, expectedOrder, "from worst to best")
    }
}

class OngoingTest {
    @Test
    fun `check that second parameter is completeEvaluation and not second summand next to preliminaryEvaluation`() {
        val ongoing = Ongoing(1.0, 2.0)
        assertEquals(ongoing.fullEvaluation, 2.0, "full evaluation is not the sum, but second parameter")
    }
}