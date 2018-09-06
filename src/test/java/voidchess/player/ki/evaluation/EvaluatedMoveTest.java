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
        final Move move = Move.Companion.byCode("a2-a3");
        final float value = -2.91f;

        EvaluatedMove eMove = new EvaluatedMove(move, new EvaluatedAsValue(value));

        assertEquals(eMove.getMove(), move);
        assertEquals(((EvaluatedAsValue) (eMove.getValue())).getCombinedEvaluation(), value);
    }

    @Test
    public void testCompareTo() {
        EvaluatedMove e1_1 = new EvaluatedMove(Move.Companion.byCode("b2-b4"), new EvaluatedAsValue(.1f));
        EvaluatedMove e1_2 = new EvaluatedMove(Move.Companion.byCode("c2-c4"), new EvaluatedAsValue(.1f));
        EvaluatedMove e2 = new EvaluatedMove(Move.Companion.byCode("b2-b4"), new EvaluatedAsValue(-.1f));
        EvaluatedMove e3 = new EvaluatedMove(Move.Companion.byCode("f2-f4"), new EvaluatedAsValue(1.2f));

        assertTrue(e1_1.compareTo(e1_1) == 0);
        assertTrue(e1_1.compareTo(e1_2) == 0);
        assertTrue(e1_1.compareTo(e2) > 0);
        assertTrue(e1_1.compareTo(e3) < 0);
    }

    @Test
    public void testOrderingInSortedSet() {

        NavigableSet<EvaluatedMove> set = new TreeSet<>();

        set.add(new EvaluatedMove(Move.Companion.byCode("a2-a3"), new EvaluatedAsValue(-1.2f)));//7.
        set.add(new EvaluatedMove(Move.Companion.byCode("b2-b3"), new EvaluatedAsValue(-0.5f)));//6.
        set.add(new EvaluatedMove(Move.Companion.byCode("c2-c3"), new EvaluatedAsValue(4.6f)));//3.
        set.add(new EvaluatedMove(Move.Companion.byCode("d2-d3"), new EvaluatedAsValue(2.1f)));//4.
        set.add(new EvaluatedMove(Move.Companion.byCode("e2-e3"), EvaluatedAsDraw.INSTANCE));//5.
        set.add(new EvaluatedMove(Move.Companion.byCode("f2-f3"), new EvaluatedAsMatt(1, false)));//9.
        set.add(new EvaluatedMove(Move.Companion.byCode("g2-g3"), new EvaluatedAsMatt(2, false)));//8.
        set.add(new EvaluatedMove(Move.Companion.byCode("h2-h3"), new EvaluatedAsMatt(1, true)));//1.
        set.add(new EvaluatedMove(Move.Companion.byCode("h2-h4"), new EvaluatedAsMatt(2, true)));//2.

        Iterator<EvaluatedMove> iter = set.descendingIterator();

        assertEquals(iter.next().getMove(), Move.Companion.byCode("h2-h3"));
        assertEquals(iter.next().getMove(), Move.Companion.byCode("h2-h4"));
        assertEquals(iter.next().getMove(), Move.Companion.byCode("c2-c3"));
        assertEquals(iter.next().getMove(), Move.Companion.byCode("d2-d3"));
        assertEquals(iter.next().getMove(), Move.Companion.byCode("e2-e3"));
        assertEquals(iter.next().getMove(), Move.Companion.byCode("b2-b3"));
        assertEquals(iter.next().getMove(), Move.Companion.byCode("a2-a3"));
        assertEquals(iter.next().getMove(), Move.Companion.byCode("g2-g3"));
        assertEquals(iter.next().getMove(), Move.Companion.byCode("f2-f3"));
    }
}
