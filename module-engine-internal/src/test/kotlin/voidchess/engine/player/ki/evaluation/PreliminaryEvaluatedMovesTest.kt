package voidchess.engine.player.ki.evaluation

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.engine.board.ChessGameInterface
import voidchess.common.board.move.Move
import voidchess.common.player.ki.evaluation.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class MinPreliminaryEvaluatedMovesTest {
    private lateinit var minPreliminaryEvaluatedMovesSpy: MinPreliminaryEvaluatedMoves
    private val gameMock = mockk<ChessGameInterface>()

    @BeforeMethod
    fun setup() {
        minPreliminaryEvaluatedMovesSpy = spyk(MinPreliminaryEvaluatedMoves())
        clearMocks(gameMock)
    }

    @DataProvider
    fun minMoveProvider(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(-8.0, -8.5)
                                )
                        ),
                        listOf(
                            CheckmateSelf(1, -4.0)
                        ),
                    CheckmateSelf(1, -4.0)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(-8.0, -8.5)
                                )
                        ),
                        listOf(
                            Ongoing(-7.0, -8.6)
                        ),
                    Ongoing(-7.0, -8.6)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(-8.0, -8.5)
                                )
                        ),
                        listOf(
                            Ongoing(-7.0, -7.6)
                        ),
                    Ongoing(-8.0, -8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(-8.0, -8.5)
                                ),
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(8.0, 8.5)
                                )
                        ),
                        listOf(
                            Stalemate
                        ),
                    Ongoing(-8.0, -8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(8.0, 8.5)
                                ),
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(-8.0, -8.5)
                                )
                        ),
                        listOf(
                            Stalemate
                        ),
                    Ongoing(-8.0, -8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(-8.0, -8.5)
                                ),
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(-8.2, -8.4)
                                )
                        ),
                        listOf(
                            Stalemate
                        ),
                    Ongoing(-8.0, -8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(-8.2, -8.4)
                                ),
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(-8.0, -8.5)
                                )
                        ),
                        listOf(
                            Stalemate
                        ),
                    Ongoing(-8.0, -8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(-8.0, -8.5)
                                )
                        ),
                        listOf(
                            CheckmateSelf(3, -4.3),
                            CheckmateSelf(2, -3.3),
                            CheckmateSelf(2, -2.3)
                        ),
                    CheckmateSelf(2, -3.3)
                )
        )
    }

    @Test(dataProvider = "minMoveProvider")
    fun `test if minimum is found`(prelimEvalList: List<Pair<Move, Ongoing>>, selectorEvalList: List<Evaluation>, expectedMin: Evaluation) {
        //*setup
        val forWhite = true

        for((move, ongoing) in prelimEvalList) {
            val (prelimEval, _) = ongoing

            minPreliminaryEvaluatedMovesSpy.add(move, prelimEval)

            every {
                minPreliminaryEvaluatedMovesSpy["evaluateToOngoing"](any<ChessGameInterface>(), eq(move), eq(forWhite), eq(prelimEval), any<EvaluatingStatically>())
            } returns ongoing
        }

        val selector =
            Selector(HighestEvaluationFirstComparator)
        for(selectorEval in selectorEvalList) {
            selector.propose(selectorEval)
        }

        //* execute
        val actualMin = minPreliminaryEvaluatedMovesSpy.getMin(gameMock, forWhite, EvaluatingAsIsNow, selector)

        //* verify
        assertEquals(expectedMin.type, actualMin.type, "type (exp: $expectedMin, act: $actualMin)")
        assertTrue ("expectedMin: $expectedMin, actualMin: $actualMin") { LowestEvaluationFirstComparator.compare(expectedMin, actualMin) == 0 }
    }
}

class MaxPreliminaryEvaluatedMovesTest {
    private lateinit var maxPreliminaryEvaluatedMovesSpy: MaxPreliminaryEvaluatedMoves
    private val gameMock = mockk<ChessGameInterface>()

    @BeforeMethod
    fun setup() {
        maxPreliminaryEvaluatedMovesSpy = spyk(MaxPreliminaryEvaluatedMoves())
        clearMocks(gameMock)
    }

    @DataProvider
    fun minMoveProvider(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(8.0, 8.5)
                                )
                        ),
                        listOf(
                            CheckmateOther(1)
                        ),
                    CheckmateOther(1)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(8.0, 8.5)
                                )
                        ),
                        listOf(
                            Ongoing(7.0, 8.6)
                        ),
                    Ongoing(7.0, 8.6)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(8.0, 8.5)
                                )
                        ),
                        listOf(
                            Ongoing(7.0, 7.6)
                        ),
                    Ongoing(8.0, 8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(8.0, 8.5)
                                ),
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(-8.0, -8.5)
                                )
                        ),
                        listOf(
                            Stalemate
                        ),
                    Ongoing(8.0, 8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(-8.0, -8.5)
                                ),
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(8.0, 8.5)
                                )
                        ),
                        listOf(
                            Stalemate
                        ),
                    Ongoing(8.0, 8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(8.0, 8.5)
                                ),
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(8.2, 8.4)
                                )
                        ),
                        listOf(
                            Stalemate
                        ),
                    Ongoing(8.0, 8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a2"),
                                    Ongoing(8.2, 8.4)
                                ),
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(8.0, 8.5)
                                )
                        ),
                        listOf(
                            Stalemate
                        ),
                    Ongoing(8.0, 8.5)
                ),
                arrayOf(
                        listOf(
                                Pair(
                                    Move.byCode("a1-a3"),
                                    Ongoing(8.0, 8.5)
                                )
                        ),
                        listOf(
                            CheckmateOther(3),
                            CheckmateOther(2)
                        ),
                    CheckmateOther(2)
                )
        )
    }

    @Test(dataProvider = "minMoveProvider")
    fun `test if maximum is found`(prelimEvalList: List<Pair<Move, Ongoing>>, selectorEvalList: List<Evaluation>, expectedMax: Evaluation) {
        //*setup
        val forWhite = true

        for((move, ongoing) in prelimEvalList) {
            val (prelimEval, _) = ongoing

            maxPreliminaryEvaluatedMovesSpy.add(move, prelimEval)

            every {
                maxPreliminaryEvaluatedMovesSpy["evaluateToOngoing"](any<ChessGameInterface>(), eq(move), eq(forWhite), eq(prelimEval), any<EvaluatingStatically>())
            } returns ongoing
        }

        val selector =
            Selector(LowestEvaluationFirstComparator)
        for(selectorEval in selectorEvalList) {
            selector.propose(selectorEval)
        }

        //* execute
        val actualMax = maxPreliminaryEvaluatedMovesSpy.getMax(gameMock, forWhite, EvaluatingAsIsNow, selector)

        //* verify
        assertEquals(expectedMax.type, actualMax.type, "type (exp: $expectedMax, act: $actualMax)")
        assertTrue ("expectedMax: $expectedMax, actualMin: $actualMax") { LowestEvaluationFirstComparator.compare(expectedMax, actualMax) == 0 }
    }
}