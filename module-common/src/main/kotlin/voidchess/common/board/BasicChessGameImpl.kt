package voidchess.common.board

import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import voidchess.common.board.other.ChessGameSupervisor
import voidchess.common.board.other.ChessGameSupervisorDummy
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.King


class BasicChessGameImpl private constructor(
    private val board: ChessBoard,
    private var supervisor: ChessGameSupervisor,
    startConfig: StartConfig
): BasicChessGame, StaticChessBoard by board {
    private var latestExtendedMove: ExtendedMove? = null
    private var isOngoing: Boolean = true
    private val isWhiteTurn get() = board.isWhiteTurn

    init {
        initGame(startConfig)
    }

    override fun initGame(startConfig: StartConfig) {
        isOngoing = true
        board.init(startConfig)
    }

    constructor(
        startConfig: StartConfig
    ): this(
        ArrayChessBoard(startConfig),
        ChessGameSupervisorDummy,
        startConfig
    )

    private fun isEnd(): Boolean {
        fun noMovesLeft(caseWhite: Boolean): Boolean {
            board.forAllFiguresOfColor(caseWhite) { figure ->
                if (figure !is King && figure.isSelectable(board)) {
                    return false
                }
            }
            return !board.getKing(caseWhite).isSelectable(board)
        }

        if (noMovesLeft(isWhiteTurn)) {
            return true
        }
        if (isDrawBecauseOfLowMaterial) {
            return true
        }
        return false
    }

    override fun useSupervisor(supervisor: ChessGameSupervisor) {
        this.supervisor = supervisor
    }

    override fun isSelectable(pos: Position): Boolean {
        val figure = getFigureOrNull(pos)
        return isOngoing && figure!=null && figure.isWhite == isWhiteTurn && figure.isSelectable(board)
    }

    override fun isMovable(from: Position, to: Position): Boolean {
        val figure = getFigureOrNull(from)
        return isOngoing && figure!=null && figure.isWhite == isWhiteTurn && figure.isMovable(to, board)
    }

    override fun move(move: Move): Boolean {
        if(isOngoing) {
            latestExtendedMove = board.move(move, supervisor)
            isOngoing = !isEnd()
        }
        return !isOngoing
    }

    override fun getLatestExtendedMove(): ExtendedMove {
        return latestExtendedMove ?: throw IllegalStateException("no move was been executed yet")
    }

    override fun toString() = "${if (isWhiteTurn) "white" else "black"} $board"
}