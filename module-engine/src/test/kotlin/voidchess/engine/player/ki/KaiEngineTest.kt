package voidchess.engine.player.ki

import org.testng.annotations.Test
import voidchess.common.board.other.StartConfig
import voidchess.common.player.ki.Engine
import voidchess.engine.KaiEngine

internal class KaiEngineTest {

//    private val playData: Array<Array<Any>>
//        @DataProvider
//        get() = arrayOf(
//            arrayOf(
//                "black 0 King-white-g1-2 Pawn-white-f2-false Bishop-black-e3 Pawn-white-g3-false Pawn-white-e4-false Bishop-black-b7 King-black-g8-2",
//                listOf("e3-f2")
//            ),
//            arrayOf("518", listOf("c2-c3")),
//            arrayOf("black 0 King-white-h1-3 Pawn-white-c7-false King-black-b7-3-false", emptyList<Any>()),
//            arrayOf(
//                "black 0 King-white-h1-3 Pawn-white-c7-false Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false Knight-black-a7 King-black-b7-3-false",
//                emptyList<Any>()
//            ),
//            arrayOf("621", listOf("f2-f3")),
//            arrayOf(
//                "black 0 Bishop-white-b1 King-white-h1-3 Pawn-black-f7-false King-black-e8-0 Rook-black-f8-0",
//                emptyList<Any>()
//            ),
//            arrayOf(
//                "white 0 Rook-black-e1-8 Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 Knight-white-g4 Queen-black-e5 King-black-g7-3",
//                emptyList<Any>()
//            ),
//            arrayOf("314", emptyList<Any>()),
//            arrayOf("621", listOf("g2-g3", "f7-f6", "c2-c3", "g8-b3", "d1-c2")),
//            arrayOf("707", listOf("e1-f3", "b7-b6", "f1-e3", "g7-g6"))
//        )

    @Test//(dataProvider = "getPlayData")
    fun `test evaluateMovesBestMoveFirst`() {

        val singleCoreKaiEngine: Engine = KaiEngine { _, _ -> }

        val movesSoFar = listOf("e2-e4", "e7-e5", "g1-f3", "b8-c6", "f1-b5", "g8-f6", "e1-g1", "d7-d5", "f1-e1")
        val startConfig = StartConfig.ClassicConfig

        singleCoreKaiEngine.evaluateMovesBestMoveFirst(movesSoFar, startConfig)
    }

    @Test//(dataProvider = "getPlayData")
    fun testTest() {

    }
}