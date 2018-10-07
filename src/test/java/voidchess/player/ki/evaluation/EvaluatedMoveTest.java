package voidchess.player.ki.evaluation;

import org.testng.annotations.Test;
import voidchess.helper.Move;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EvaluatedMoveTest {
    @Test
    public void testGetter() {
        final Move move = Move.byCode("a2-a3");
        final double primaryValue = -2.91;
        final double secondaryValue = 1.2;

        EvaluatedMove eMove = new EvaluatedMove(move, new Ongoing(primaryValue, secondaryValue));

        assertEquals(eMove.getMove(), move);
        assertEquals(((Ongoing) (eMove.getValue())).getCombinedEvaluation(), primaryValue+secondaryValue);
    }

    @Test
    public void testCompareTo() {
        EvaluatedMove e1_1 = new EvaluatedMove(Move.byCode("b2-b4"), new Ongoing(.1f));
        EvaluatedMove e1_2 = new EvaluatedMove(Move.byCode("c2-c4"), new Ongoing(.1f));
        EvaluatedMove e2 = new EvaluatedMove(Move.byCode("b2-b4"), new Ongoing(-.1f));
        EvaluatedMove e3 = new EvaluatedMove(Move.byCode("f2-f4"), new Ongoing(1.2f));

        assertTrue(e1_1.compareTo(e1_1) == 0);
        assertTrue(e1_1.compareTo(e1_2) == 0);
        assertTrue(e1_1.compareTo(e2) > 0);
        assertTrue(e1_1.compareTo(e3) < 0);
    }

    @Test
    public void testOrderingInSortedSet() {

        NavigableSet<EvaluatedMove> set = new TreeSet<>();

        set.add(new EvaluatedMove(Move.byCode("a2-a3"), new Ongoing(-1.2f)));//7.
        set.add(new EvaluatedMove(Move.byCode("b2-b3"), new Ongoing(-0.5f)));//6.
        set.add(new EvaluatedMove(Move.byCode("c2-c3"), new Ongoing(4.6f)));//3.
        set.add(new EvaluatedMove(Move.byCode("d2-d3"), new Ongoing(2.1f)));//4.
        set.add(new EvaluatedMove(Move.byCode("e2-e3"), Draw.INSTANCE));//5.
        set.add(new EvaluatedMove(Move.byCode("f2-f3"), new CheckmateSelf(1)));//9.
        set.add(new EvaluatedMove(Move.byCode("g2-g3"), new CheckmateSelf(2)));//8.
        set.add(new EvaluatedMove(Move.byCode("h2-h3"), new CheckmateOther(1)));//1.
        set.add(new EvaluatedMove(Move.byCode("h2-h4"), new CheckmateOther(2)));//2.

        Iterator<EvaluatedMove> iter = set.descendingIterator();

        assertEquals(iter.next().getMove(), Move.byCode("h2-h3"));
        assertEquals(iter.next().getMove(), Move.byCode("h2-h4"));
        assertEquals(iter.next().getMove(), Move.byCode("c2-c3"));
        assertEquals(iter.next().getMove(), Move.byCode("d2-d3"));
        assertEquals(iter.next().getMove(), Move.byCode("e2-e3"));
        assertEquals(iter.next().getMove(), Move.byCode("b2-b3"));
        assertEquals(iter.next().getMove(), Move.byCode("a2-a3"));
        assertEquals(iter.next().getMove(), Move.byCode("g2-g3"));
        assertEquals(iter.next().getMove(), Move.byCode("f2-f3"));
    }
}
