package voidchess.ui

import voidchess.board.ChessGameInterface
import voidchess.board.MoveResult
import voidchess.helper.*
import voidchess.player.PlayerInterface


class Table constructor(
        private val game: ChessGameInterface,
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
            val extendedMove = game.getLastExtendedMove()
            ui.repaintAfterMove(extendedMove)

            whitePlayersTurn = !whitePlayersTurn
            endOption
        }

        if (endOption === MoveResult.NO_END) {
            val gaveCheck = game.isCheck(whitePlayersTurn)
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
        game.initGame(panel960.positionCode)
        ui.repaintAtOnce()

        whitePlayer.gameStarts()
        blackPlayer.gameStarts()

        whitePlayer.play()
    }

    override fun stopGame(endoption: MoveResult) {
        whitePlayer.gameEnds(endoption, !whitePlayersTurn)
        blackPlayer.gameEnds(endoption, !whitePlayersTurn)
        parent.gameover()
    }
}
