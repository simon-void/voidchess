package voidchess.player.ki.concurrent;

import voidchess.helper.Move;
import org.testng.annotations.Test;
import voidchess.player.ki.Evaluaded;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EvaluatedMoveTest {
    @Test
    public void testGetter() {
        final Move move = Move.get("a2-a3");
        final float value = -2.91f;

        EvaluatedMove eMove = new EvaluatedMove(move, Evaluaded.fromValues(value,0));

        assertEquals(move, eMove.getMove());
        assertEquals(value, eMove.getValue().getCombinedEvaluation());
    }

    @Test
    public void testCompareTo() {
        EvaluatedMove e1_1 = new EvaluatedMove(Move.get("b2-b4"), Evaluaded.fromValues(.1f,0));
        EvaluatedMove e1_2 = new EvaluatedMove(Move.get("c2-c4"), Evaluaded.fromValues(.1f,0));
        EvaluatedMove e2 = new EvaluatedMove(Move.get("b2-b4"), Evaluaded.fromValues(-.1f,0));
        EvaluatedMove e3 = new EvaluatedMove(Move.get("f2-f4"), Evaluaded.fromValues(1.2f,0));

        assertEquals(0, e1_1.compareTo(e1_1));
        assertEquals(0, e1_1.compareTo(e1_2));
        assertEquals(-1, e1_1.compareTo(e2));
        assertEquals(1, e1_1.compareTo(e3));
    }

    @Test
    public void testOrderingInSortedSet() {

        SortedSet<EvaluatedMove> set = new TreeSet<EvaluatedMove>();

        set.add(new EvaluatedMove(Move.get("a2-a3"), Evaluaded.fromValues(-1.2f,0)));
        set.add(new EvaluatedMove(Move.get("b2-b3"), Evaluaded.fromValues(-0.5f,0)));
        set.add(new EvaluatedMove(Move.get("c2-c3"), Evaluaded.fromValues(4.6f, 0)));
        set.add(new EvaluatedMove(Move.get("d2-d3"), Evaluaded.fromValues(2.1f, 0)));
        set.add(new EvaluatedMove(Move.get("e2-e3"), Evaluaded.DRAW));
        set.add(new EvaluatedMove(Move.get("f2-f3"), Evaluaded.getThisComputerPlayerIsMatt(1)));
        set.add(new EvaluatedMove(Move.get("g2-g3"), Evaluaded.getThisComputerPlayerIsMatt(2)));
        set.add(new EvaluatedMove(Move.get("h2-h3"), Evaluaded.getOtherPlayerIsMatt(1)));
        set.add(new EvaluatedMove(Move.get("h2-h4"), Evaluaded.getOtherPlayerIsMatt(2)));

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
