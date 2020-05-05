package voidchess.united

import voidchess.united.board.CentralChessGame
import voidchess.united.board.CentralChessGameImpl
import voidchess.united.player.EnginePlayer
import voidchess.united.player.UiPlayer
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.other.StartConfig
import voidchess.united.player.Player
import voidchess.common.board.move.ExtendedMove
import voidchess.common.integration.ComputerPlayerUI
import voidchess.common.integration.HumanPlayer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TableImpl constructor(
    humanPlayer: HumanPlayer,
    computerPlayerUI: ComputerPlayerUI,
    private val uiPlayer: UiPlayer,
    private val enginePlayer: EnginePlayer
) : Table {

    private val game: CentralChessGame = CentralChessGameImpl()
    init {
        uiPlayer.postConstruct(this, humanPlayer)
        enginePlayer.postConstruct(this, computerPlayerUI, game)
    }

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var whitePlayer: Player
    private lateinit var blackPlayer: Player
    private var whitePlayersTurn: Boolean = false
    private var resign: Boolean = false

    override fun move(move: Move): ExtendedMove {
        val endOption = game.move(move)
        val extendedMove = game.getLatestExtendedMove()
        whitePlayersTurn = !whitePlayersTurn

        // this should probably by asynchronous
        executorService.submit {
            if (endOption === MoveResult.NO_END) {
                val gaveCheck = game.isCheck
                if (whitePlayersTurn) {
                    whitePlayer.makeAMove(extendedMove)
                    if (gaveCheck) {
                        blackPlayer.gaveCheck()
                    }
                } else {
                    blackPlayer.makeAMove(extendedMove)
                    if (gaveCheck) {
                        whitePlayer.gaveCheck()
                    }
                }
            } else {
                val computerDidFinalMove = whitePlayersTurn == (whitePlayer == uiPlayer)
                if(computerDidFinalMove) {
                    uiPlayer.makeAMove(extendedMove)
                }
                stopGame(endOption)
            }
        }

        return extendedMove
    }

    override fun startGame(startConfig: StartConfig, humanIsWhite: Boolean) {
        game.initGame(startConfig)
//        game.initGame(
//            StartConfig.ManualConfig(true, 0, listOf(
//                "King-white-e4-5", "Queen-black-b8", "King-black-e8-0"
//                "King-white-e4-5", "Bishop-black-a8", "Bishop-black-b8", "King-black-e8-0"
//                "King-white-e4-5", "Rook-black-a8-0", "King-black-e8-0"
//                  "King-white-h1-9", "Pawn-black-c7-false", "Pawn-black-e7-false", "King-black-g8-0"
//            ))
//        )

        enginePlayer.gameStarts(!humanIsWhite)

        resign = false
        whitePlayersTurn = true

        if (humanIsWhite) {
            whitePlayer = uiPlayer
            blackPlayer = enginePlayer
        } else {
            whitePlayer = enginePlayer
            blackPlayer = uiPlayer
        }

        whitePlayer.makeFirstMove()
    }

    override fun stopGame(endOption: MoveResult) {
        whitePlayer.gameEnds(endOption, !whitePlayersTurn)
        blackPlayer.gameEnds(endOption, !whitePlayersTurn)
    }
}
