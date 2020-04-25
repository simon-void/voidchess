package voidchess.ui

import voidchess.board.CentralChessGame
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.common.board.other.ChessGameSupervisor
import voidchess.common.board.other.StartConfig
import voidchess.player.PlayerInterface


class Table constructor(
    private val game: CentralChessGame,
    private val ui: ChessboardComponent,
    private val parent: ChessPanel,
    private val panel960: Chess960Panel
) : ChessGameSupervisor, TableInterface {

    private lateinit var whitePlayer: PlayerInterface
    private lateinit var blackPlayer: PlayerInterface
    private var whitePlayersTurn: Boolean = false
    private var resign: Boolean = false

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return if (whitePlayersTurn)
            whitePlayer.askForPawnPromotionType(pawnPosition)
        else
            blackPlayer.askForPawnPromotionType(pawnPosition)
    }

    override fun move(move: Move) {
        val endOption = synchronized(this) {
            if (resign) {
                stopGame(MoveResult.RESIGN)
                return
            }
            val endOption = game.move(move)
            val extendedMove = game.getLatestExtendedMove()
            ui.repaintAfterMove(extendedMove)

            whitePlayersTurn = !whitePlayersTurn
            endOption
        }

        if (endOption === MoveResult.NO_END) {
            val gaveCheck = game.isCheck
            if (whitePlayersTurn) {
                whitePlayer.play()
                if (gaveCheck) {
                    blackPlayer.gaveCheck()
                }
            } else {
                blackPlayer.play()
                if (gaveCheck) {
                    whitePlayer.gaveCheck()
                }
            }
        } else {
            stopGame(endOption)
        }
    }

    override fun setWhitePlayer(player: PlayerInterface) {
        whitePlayer = player
        player.setColor(true)
    }

    override fun setBlackPlayer(player: PlayerInterface) {
        blackPlayer = player
        player.setColor(false)
    }

    override fun switchPlayer() {
        val formerWhitePlayer = whitePlayer
        setWhitePlayer(blackPlayer)
        setBlackPlayer(formerWhitePlayer)
    }

    override fun startGame() {
        whitePlayersTurn = true
        resign = false
        game.initGame(StartConfig.Chess960Config(panel960.positionCode))
//        game.initGame(
//            StartConfig.ManualConfig(true, 0, listOf(
//                "King-white-e4-5", "Queen-black-b8", "King-black-e8-0"
//                "King-white-e4-5", "Bishop-black-a8", "Bishop-black-b8", "King-black-e8-0"
//                "King-white-e4-5", "Rook-black-a8-0", "King-black-e8-0"
//                  "King-white-h1-9", "Pawn-black-c7-false", "Pawn-black-e7-false", "King-black-g8-0"
//            ))
//        )
        ui.startNewGame()

        whitePlayer.gameStarts()
        blackPlayer.gameStarts()

        whitePlayer.play()
    }

    override fun stopGame(endOption: MoveResult) {
        whitePlayer.gameEnds(endOption, !whitePlayersTurn)
        blackPlayer.gameEnds(endOption, !whitePlayersTurn)
        parent.stop()
    }
}
