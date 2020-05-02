package voidchess.common.player.ki.evaluation

import org.testng.annotations.Test
import voidchess.common.board.move.Move
import org.testng.Assert.assertEquals
import voidchess.common.engine.*
import kotlin.test.assertFalse

internal class EvaluationMoveComparatorTest {
    @Test
    fun testHighestEvalFirstOrderingInSortedSet() {

        val set = mutableSetOf<EvaluatedMove>()

        set.add(
                EvaluatedMove(
                        Move.byCode("a2-a3"),
                        Ongoing(-1.2)
                )
        )//8.
        set.add(
                EvaluatedMove(
                        Move.byCode("b2-b3"),
                        Ongoing(-0.5)
                )
        )//7.
        set.add(
                EvaluatedMove(
                        Move.byCode("c2-c3"),
                        Ongoing(4.6)
                )
        )//3.
        set.add(
                EvaluatedMove(
                        Move.byCode("d2-d3"),
                        Ongoing(2.1)
                )
        )//4.
        set.add(
                EvaluatedMove(
                        Move.byCode("d2-d4"),
                        Ongoing(.0)
                )
        )//5.
        set.add(
                EvaluatedMove(
                        Move.byCode("e2-e3"),
                        Draw
                )
        )//6.
        set.add(
                EvaluatedMove(
                        Move.byCode("f2-f3"),
                        CheckmateSelf(1, -1.0)
                )
        )//10.
        set.add(
                EvaluatedMove(
                        Move.byCode("f2-f4"),
                        CheckmateSelf(1, -2.0)
                )
        )//11.
        set.add(
                EvaluatedMove(
                        Move.byCode("g2-g3"),
                        CheckmateSelf(2, 5.0)
                )
        )//9.
        set.add(
                EvaluatedMove(
                        Move.byCode("h2-h3"),
                        CheckmateOther(1)
                )
        )//1.
        set.add(
                EvaluatedMove(
                        Move.byCode("h2-h4"),
                        CheckmateOther(2)
                )
        )//2.

        val fromBestToWorst: Iterator<EvaluatedMove> = set.sortedWith(HighestEvalFirst).iterator()

        assertEquals(fromBestToWorst.next().move, Move.byCode("h2-h3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("h2-h4"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("c2-c3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("d2-d3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("d2-d4"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("e2-e3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("b2-b3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("a2-a3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("g2-g3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("f2-f3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("f2-f4"))
        assertFalse(fromBestToWorst.hasNext())
    }

    @Test
    fun testLowestEvalFirstOrderingInSortedSet() {

        val set = mutableSetOf<EvaluatedMove>()

        set.add(
                EvaluatedMove(
                        Move.byCode("a2-a3"),
                        Ongoing(-1.2)
                )
        )//8.
        set.add(
                EvaluatedMove(
                        Move.byCode("b2-b3"),
                        Ongoing(-0.5)
                )
        )//7.
        set.add(
                EvaluatedMove(
                        Move.byCode("c2-c3"),
                        Ongoing(4.6)
                )
        )//3.
        set.add(
                EvaluatedMove(
                        Move.byCode("d2-d3"),
                        Ongoing(2.1)
                )
        )//4.
        set.add(
                EvaluatedMove(
                        Move.byCode("d2-d4"),
                        Ongoing(.0)
                )
        )//5.
        set.add(
                EvaluatedMove(
                        Move.byCode("e2-e3"),
                        Draw
                )
        )//6.
        set.add(
                EvaluatedMove(
                        Move.byCode("f2-f3"),
                        CheckmateSelf(1, -1.0)
                )
        )//10.
        set.add(
                EvaluatedMove(
                        Move.byCode("f2-f4"),
                        CheckmateSelf(1, -2.0)
                )
        )//11.
        set.add(
                EvaluatedMove(
                        Move.byCode("g2-g3"),
                        CheckmateSelf(2, 5.0)
                )
        )//9.
        set.add(
                EvaluatedMove(
                        Move.byCode("h2-h3"),
                        CheckmateOther(1)
                )
        )//1.
        set.add(
                EvaluatedMove(
                        Move.byCode("h2-h4"),
                        CheckmateOther(2)
                )
        )//2.

        val fromBestToWorst = set.sortedWith(LowestEvalFirst).iterator()

        assertEquals(fromBestToWorst.next().move, Move.byCode("f2-f4"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("f2-f3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("g2-g3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("a2-a3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("b2-b3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("e2-e3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("d2-d4"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("d2-d3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("c2-c3"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("h2-h4"))
        assertEquals(fromBestToWorst.next().move, Move.byCode("h2-h3"))
        assertFalse(fromBestToWorst.hasNext())
    }
}
