package voidchess.common.board.check

import org.testng.annotations.Test
import voidchess.common.board.move.Position

import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import voidchess.common.board.move.PositionProgression
import voidchess.common.initChessBoard
import voidchess.common.toList
import kotlin.test.assertTrue

class AttackLinesTest {

    @Test(dataProvider = "getCheckAttackLinesData")
    fun testCheckAttackLines(gameDes: String, isWhite: Boolean, attackerGivingCheckPosCodes: Set<String>, boundFigurePosCodes: Set<String>) {
        val game = initChessBoard(gameDes)
        val attackLines = checkAttackLines(game, isWhite)

        // test check tests
        val actualChecks = attackLines.checkLines.size
        assertTrue( actualChecks in 0..2, "invalid number of checks! should be 0-2 but is $actualChecks")
        assertEquals(attackLines.noCheck, actualChecks==0, "noCheck")
        assertEquals(attackLines.isSingleCheck, actualChecks==1, "isSingleCheck")
        assertEquals(attackLines.isDoubleCheck, actualChecks==2, "isDoubleCheck")
        assertEquals(attackLines.isCheck, actualChecks!=0, "isCheck")

        // test checkLines
        assertEquals(attackLines.checkLines.size, attackerGivingCheckPosCodes.size, "number of checks")
        assertEquals(
                attackLines.checkLines.asSequence().map { checkLine -> checkLine.attackerPos.toString() }.toSet(),
                attackerGivingCheckPosCodes,
                "positions of attackers giving checks"
        )
        // test boundLines
        assertEquals(attackLines.boundLineByBoundFigurePos.size, boundFigurePosCodes.size, "number of bound lines")
        assertEquals(
                attackLines.boundLineByBoundFigurePos.keys.asSequence().map{it.toString()}.toSet(),
                boundFigurePosCodes,
                "positions of bound figures"
        )
    }

    @DataProvider
    fun getCheckAttackLinesData() :Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-white-e1-0 Knight-black-g3 King-black-e8-0", true, setOf<String>(), setOf<String>()),
            arrayOf("white 0 King-white-e1-0 Bishop-black-g3 King-black-e8-0", true, setOf("g3"), setOf<String>()),
            arrayOf("white 0 King-white-e1-0 Queen-black-e6 King-black-e8-0", true, setOf("e6"), setOf<String>()),
            arrayOf("white 0 King-white-e1-0 Bishop-black-g3 Queen-black-e6 King-black-e8-0", true, setOf("g3", "e6"), setOf<String>()),
            arrayOf("white 0 King-white-e1-0 Rook-white-e3-4 Queen-black-e6 King-black-e8-0", true, setOf<String>(), setOf("e3")),
            arrayOf("white 0 King-white-e1-0 Rook-white-e3-4 Bishop-black-g3 Queen-black-e6 King-black-e8-0", true, setOf("g3"), setOf("e3")),
            arrayOf("white 0 King-white-e1-0 Rook-white-f2-4 Bishop-black-g3 Queen-black-e6 King-black-e8-0", true, setOf("e6"), setOf("f2")),
            arrayOf("white 0 King-white-e1-0 Bishop-black-g3 Knight-black-d3 King-black-e8-0", true, setOf("g3", "d3"), setOf<String>())
    )

    @Test(dependsOnMethods = ["testCheckAttackLines"])
    fun testCheckAttackLinesIteratedOrder() {
        // the order of positions in checkLines and boundLines is important because it allows optimizations.
        // the question of if an attack can be intercepted or if a bound figure can move can in special case be decided
        // by checking if the first position in the iterator is reachable or not
        val gameDes = "white 0 King-white-e1-0 Rook-white-e5-4 Bishop-black-h4 Queen-black-e8 King-black-d8-1"
        val game = initChessBoard(gameDes)
        val attackLines = checkAttackLines(game, true)
        assertEquals(attackLines.checkLines.size, 1, "#checkLines")
        assertEquals(attackLines.boundLineByBoundFigurePos.size, 1, "#boundLines")

        fun assertIteratorOrder(positionProgression: PositionProgression, vararg posCodes: String) {
            assertEquals(positionProgression.toList(), posCodes.map { Position.byCode(it)}, "ordered")
        }

        // the attackIterator should start at the attacker
        val checkLine = attackLines.checkLines.first()
        assertIteratorOrder(checkLine.posProgression, "h4", "g3", "f2")

        // both boundLine iterator should start at the bound figure
        // (well, not the bound figure's position itself, but neighboring it)
        val boundLine = attackLines.boundLineByBoundFigurePos.entries.first().value
        assertIteratorOrder(boundLine.possibleMovesToAttacker, "e6", "e7", "e8")
        assertIteratorOrder(boundLine.possibleMovesToKing, "e4", "e3", "e2")
    }
}
