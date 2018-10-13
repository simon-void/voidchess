package voidchess.player.ki.openings;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import voidchess.board.move.Move;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by stephan on 12.07.2015.
 */
public class OpeningsLibraryTest {
    @Test(dataProvider = "validOpeningsProvider")
    public void testValidOpenings(String validOpeningSequence, int numberOfMoves) {
        List<String> moves = OpeningsLibrary.Companion.splitAndCheckOpeningSequence(validOpeningSequence);
        assertEquals(moves.size(), numberOfMoves, "number of moves in opening");
        for (String move : moves) {
            assertTrue(Move.isValid(move), "should be valid move: " + move);
        }
    }

    @Test(dataProvider = "invalidOpeningsProvider", expectedExceptions = IllegalArgumentException.class)
    public void testInValidOpenings(String invalidOpeningSequence) {
        OpeningsLibrary.Companion.splitAndCheckOpeningSequence(invalidOpeningSequence);
    }

    @DataProvider(name = "validOpeningsProvider")
    public Object[][] getValidOpeningsProvider() {
        return new Object[][]{
                new Object[]{"e2-e4,c7-c5,g1-f3,d7-d6,d2-d4,c5-d4,f3-d4,g8-f6,b1-c3,a7-a6", 10},
                new Object[]{"e2-e4, c7-c5,  b1-c3, b8-c6   ,g2-g3,g7-g6,f1-g2,f8-g7", 8},
                new Object[]{"e2-e4", 1},
                new Object[]{"", 0},
        };
    }

    @DataProvider(name = "invalidOpeningsProvider")
    public Object[][] getInvalidOpeningsProvider() {
        return new Object[][]{
                new Object[]{"e4,e5"},
                new Object[]{","},
                new Object[]{"e2_e4,e7_e5"},
                new Object[]{"e2e4,e7e5"},
                new Object[]{"e2-e4;e7-e5"},
                new Object[]{"e2-e4 e7-e5"},
                new Object[]{"e3-e4"},
                new Object[]{"e2-e5,e7-e5"},
                new Object[]{"e2-e4,d2-d4"},
                new Object[]{"e2-e4,e6-e5"},
                new Object[]{"e2-e4,e7-e4"},
                new Object[]{"e2-e4,e7-e5,d7-d5"},
                new Object[]{"e2-e4,e7-e5,g1-f3,d7-d6,d2-d4,e5-d4,b8-c6,f1-b5"},
        };
    }
}
