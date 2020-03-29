package voidchess.player.ki.evaluation

import org.testng.annotations.Test
import voidchess.board.move.Move
import java.util.TreeSet

import org.testng.Assert.assertEquals

internal class EvaluationMoveComparatorTest {
    @Test
    fun testHighestEvalFirstOrderingInSortedSet() {

        val set = TreeSet<EvaluatedMove>(HighestEvalFirst)

        set.add(EvaluatedMove(Move.byCode("a2-a3"), Ongoing(.0, -1.2)))//8.
        set.add(EvaluatedMove(Move.byCode("b2-b3"), Ongoing(.0, -0.5)))//7.
        set.add(EvaluatedMove(Move.byCode("c2-c3"), Ongoing(.0, 4.6)))//3.
        set.add(EvaluatedMove(Move.byCode("d2-d3"), Ongoing(.0, 2.1)))//4.
        set.add(EvaluatedMove(Move.byCode("d2-d4"), Ongoing(.0, .0)))//5.
        set.add(EvaluatedMove(Move.byCode("e2-e3"), Draw))//6.
        set.add(EvaluatedMove(Move.byCode("f2-f3"), CheckmateSelf(1, -1.0)))//10.
        set.add(EvaluatedMove(Move.byCode("f2-f3"), CheckmateSelf(1, -2.0)))//11.
        set.add(EvaluatedMove(Move.byCode("g2-g3"), CheckmateSelf(2, 5.0)))//9.
        set.add(EvaluatedMove(Move.byCode("h2-h3"), CheckmateOther(1)))//1.
        set.add(EvaluatedMove(Move.byCode("h2-h4"), CheckmateOther(2)))//2.

        val fromBestToWorst = set.iterator()

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
    }

    @Test
    fun testLowestEvalFirstOrderingInSortedSet() {

        val set = TreeSet<EvaluatedMove>(LowestEvalFirst)

        set.add(EvaluatedMove(Move.byCode("a2-a3"), Ongoing(.0, -1.2)))//8.
        set.add(EvaluatedMove(Move.byCode("b2-b3"), Ongoing(.0, -0.5)))//7.
        set.add(EvaluatedMove(Move.byCode("c2-c3"), Ongoing(.0, 4.6)))//3.
        set.add(EvaluatedMove(Move.byCode("d2-d3"), Ongoing(.0, 2.1)))//4.
        set.add(EvaluatedMove(Move.byCode("d2-d4"), Ongoing(.0, .0)))//5.
        set.add(EvaluatedMove(Move.byCode("e2-e3"), Draw))//6.
        set.add(EvaluatedMove(Move.byCode("f2-f3"), CheckmateSelf(1, -1.0)))//10.
        set.add(EvaluatedMove(Move.byCode("f2-f3"), CheckmateSelf(1, -2.0)))//11.
        set.add(EvaluatedMove(Move.byCode("g2-g3"), CheckmateSelf(2, 5.0)))//9.
        set.add(EvaluatedMove(Move.byCode("h2-h3"), CheckmateOther(1)))//1.
        set.add(EvaluatedMove(Move.byCode("h2-h4"), CheckmateOther(2)))//2.

        val fromBestToWorst = set.descendingIterator()

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
    }
}
