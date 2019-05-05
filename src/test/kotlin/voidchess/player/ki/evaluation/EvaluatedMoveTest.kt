package voidchess.player.ki.evaluation

import org.testng.annotations.Test
import voidchess.board.move.Move
import java.util.TreeSet

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue

class EvaluatedMoveTest {
    @Test
    fun testGetter() {
        val move = Move.byCode("a2-a3")
        val primaryValue = -2.91
        val secondaryValue = 1.2

        val eMove = EvaluatedMove(move, Ongoing(primaryValue, secondaryValue))

        assertEquals(eMove.move, move)
        assertEquals((eMove.value as Ongoing).getCombinedEvaluation(), primaryValue + secondaryValue)
    }

    @Test
    fun testCompareTo() {
        val e1 = EvaluatedMove(Move.byCode("b2-b4"), Ongoing(.1))
        val e2 = EvaluatedMove(Move.byCode("c2-c4"), Ongoing(.1))
        val e3 = EvaluatedMove(Move.byCode("b2-b4"), Ongoing(-.1))
        val e4 = EvaluatedMove(Move.byCode("f2-f4"), Ongoing(1.2))

        assertTrue(e1.compareTo(e1) == 0)
        assertTrue(e1.compareTo(e2) == 0)
        assertTrue(e1 > e3)
        assertTrue(e1 < e4)
    }

    @Test
    fun testOrderingInSortedSet() {

        val set = TreeSet<EvaluatedMove>()

        set.add(EvaluatedMove(Move.byCode("a2-a3"), Ongoing(-1.2)))//7.
        set.add(EvaluatedMove(Move.byCode("b2-b3"), Ongoing(-0.5)))//6.
        set.add(EvaluatedMove(Move.byCode("c2-c3"), Ongoing(4.6)))//3.
        set.add(EvaluatedMove(Move.byCode("d2-d3"), Ongoing(2.1)))//4.
        set.add(EvaluatedMove(Move.byCode("e2-e3"), Draw))//5.
        set.add(EvaluatedMove(Move.byCode("f2-f3"), CheckmateSelf(1)))//9.
        set.add(EvaluatedMove(Move.byCode("g2-g3"), CheckmateSelf(2)))//8.
        set.add(EvaluatedMove(Move.byCode("h2-h3"), CheckmateOther(1)))//1.
        set.add(EvaluatedMove(Move.byCode("h2-h4"), CheckmateOther(2)))//2.

        val iter = set.descendingIterator()

        assertEquals(iter.next().move, Move.byCode("h2-h3"))
        assertEquals(iter.next().move, Move.byCode("h2-h4"))
        assertEquals(iter.next().move, Move.byCode("c2-c3"))
        assertEquals(iter.next().move, Move.byCode("d2-d3"))
        assertEquals(iter.next().move, Move.byCode("e2-e3"))
        assertEquals(iter.next().move, Move.byCode("b2-b3"))
        assertEquals(iter.next().move, Move.byCode("a2-a3"))
        assertEquals(iter.next().move, Move.byCode("g2-g3"))
        assertEquals(iter.next().move, Move.byCode("f2-f3"))
    }
}
