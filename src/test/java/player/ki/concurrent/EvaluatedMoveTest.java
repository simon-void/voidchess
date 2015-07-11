package player.ki.concurrent;

import helper.Move;
import org.testng.annotations.Test;
import player.ki.ChessValue;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EvaluatedMoveTest {
    @Test
    public void testGetter() {
        final Move move = Move.get("a2-a3");
        final float value = -2.9123f;

        EvaluatedMove eMove = new EvaluatedMove(move, value);

        assertEquals(move, eMove.getMove());
        assertEquals(value, eMove.getValue());
    }

    @Test
    public void testCompareTo() {
        EvaluatedMove e1_1 = new EvaluatedMove(Move.get("b2-b4"), .1f);
        EvaluatedMove e1_2 = new EvaluatedMove(Move.get("c2-c4"), .1f);
        EvaluatedMove e2 = new EvaluatedMove(Move.get("b2-b4"), -.1f);
        EvaluatedMove e3 = new EvaluatedMove(Move.get("f2-f4"), 1.2f);

        assertEquals(0, e1_1.compareTo(e1_1));
        assertEquals(0, e1_1.compareTo(e1_2));
        assertEquals(-1, e1_1.compareTo(e2));
        assertEquals(1, e1_1.compareTo(e3));
    }

    @Test
    public void testOrderingInSortedSet() {
        ChessValue wrapper = ChessValue.getInstance();

        SortedSet<EvaluatedMove> set = new TreeSet<EvaluatedMove>();

        set.add(new EvaluatedMove(Move.get("a2-a3"), wrapper.getFloatValue(-1.2f)));
        set.add(new EvaluatedMove(Move.get("b2-b3"), wrapper.getFloatValue(-0.5f)));
        set.add(new EvaluatedMove(Move.get("c2-c3"), wrapper.getFloatValue(4.6f)));
        set.add(new EvaluatedMove(Move.get("d2-d3"), wrapper.getFloatValue(2.1f)));
        set.add(new EvaluatedMove(Move.get("e2-e3"), wrapper.getDrawValue()));
        set.add(new EvaluatedMove(Move.get("f2-f3"), wrapper.getThisComputerPlayerIsMatt(1)));
        set.add(new EvaluatedMove(Move.get("g2-g3"), wrapper.getThisComputerPlayerIsMatt(2)));
        set.add(new EvaluatedMove(Move.get("h2-h3"), wrapper.getOtherPlayerIsMatt(1)));
        set.add(new EvaluatedMove(Move.get("h2-h4"), wrapper.getOtherPlayerIsMatt(2)));

        Iterator<EvaluatedMove> iter = set.iterator();

        assertTrue(iter.next().getMove().equalsMove(Move.get("f2-f3")));
        assertTrue(iter.next().getMove().equalsMove(Move.get("g2-g3")));
        assertTrue(iter.next().getMove().equalsMove(Move.get("c2-c3")));
        assertTrue(iter.next().getMove().equalsMove(Move.get("d2-d3")));
        assertTrue(iter.next().getMove().equalsMove(Move.get("e2-e3")));
        assertTrue(iter.next().getMove().equalsMove(Move.get("b2-b3")));
        assertTrue(iter.next().getMove().equalsMove(Move.get("a2-a3")));
        assertTrue(iter.next().getMove().equalsMove(Move.get("h2-h4")));
        assertTrue(iter.next().getMove().equalsMove(Move.get("h2-h3")));
    }
}
