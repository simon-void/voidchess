package voidchess.common.board

import voidchess.common.board.check.AttackLines
import voidchess.common.board.check.checkAttackLines
import voidchess.common.board.move.Move
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.other.ChessGameSupervisor
import voidchess.common.board.other.boardInstanciator
import voidchess.common.figures.*
import java.util.*
import kotlin.math.abs


class ArrayChessBoard(startConfig: StartConfig = StartConfig.ClassicConfig) :
    ChessBoard {
    override var isWhiteTurn: Boolean = true

    private val board: Array<Figure?> = arrayOfNulls(64)
    // TODO use kotlin.ArrayDeque if that is no longer experimental
    private val extendedMoveStack = ArrayDeque<ExtendedMove>(16)

    // the alternative to creating dummy instances for white and black king is 'lateinit'
    // but that would come with a null-check on each access
    override var whiteKing: King = King(true, Position.byCode("a1"))
    override var blackKing: King = King(false, Position.byCode("a8"))

    private var cachedAttackLines: AttackLines? = null

    init {
        init(startConfig)
    }

    private fun clearCheckComputation() {
        cachedAttackLines = null
    }

    override fun getAttackLines(isWhite: Boolean): AttackLines {
        val scopedAttackLines: AttackLines = cachedAttackLines ?: checkAttackLines(this, isWhite)
        cachedAttackLines = scopedAttackLines
        return scopedAttackLines
    }

    override fun init(startConfig: StartConfig) {
        clearCheckComputation()
        extendedMoveStack.clear()
        isWhiteTurn = startConfig.doesWhitePlayerStart

        var foundWhiteKing = false
        var foundBlackKing = false
        val pawnsThatDoubleJumped = mutableListOf<Pawn>()
        startConfig.boardInstanciator().generateInitialSetup().forEach { (pos, figureOrNull)->
            board[pos.index] = figureOrNull

            if(figureOrNull is Pawn && figureOrNull.canBeHitEnpassant) {
                pawnsThatDoubleJumped.add(figureOrNull)
            }
            if(figureOrNull is King) {
                if(figureOrNull.isWhite) {
                    whiteKing = figureOrNull

                    require(!foundWhiteKing) {"more than one white king in config $startConfig"}
                    foundWhiteKing = true
                }else{
                    blackKing = figureOrNull

                    require(!foundBlackKing) {"more than one black king in config $startConfig"}
                    foundBlackKing = true
                }
            }
        }
        require(foundWhiteKing) {"no white king in config $startConfig"}
        require(foundBlackKing) {"no black king in config $startConfig"}
        require(pawnsThatDoubleJumped.size<2) {"it isn't possible that more than one pawn can be hit by enpassant but there are ${pawnsThatDoubleJumped.size} in $startConfig"}
    }

    private fun setFigure(pos: Position, figure: Figure) {
        board[pos.index] = figure
    }

    private fun clearFigure(pos: Position): Figure {
        val figure: Figure = board[pos.index] ?: throw IllegalStateException("position $pos doesn't contain a figure to clear")
        board[pos.index] = null
        return figure
    }

    private fun clearPos(pos: Position) {
        board[pos.index] = null
    }

    override fun simulateSimplifiedMove(
        figure: Figure,
        warpTo: Position,
        query: (BasicChessBoard) -> Boolean
    ): Boolean {
        fun move(figure: Figure, to: Position): Figure? {
            val move = Move[figure.position, to]
            board[figure.position.index] = null
            val figureTaken = board[to.index]
            board[to.index] = figure
            figure.figureMoved(move)
            return figureTaken
        }
        fun undoMove(figure: Figure, from: Position, figureTaken: Figure?) {
            board[figure.position.index] = figureTaken
            board[from.index] = figure
            figure.undoMove(from)
        }

        val fromPos = figure.position
        val figureTaken = move(figure, warpTo)
        // execute the query with figure on the new position
        val result = query(this)
        // move the figure back to it's original position
        undoMove(figure, fromPos, figureTaken)
        return result
    }

    override fun historyToString(numberOfHalfMoves: Int?) = extendedMoveStack.getLatestMoves(numberOfHalfMoves)

    /**
     * @return true if a figure got hit
     */
    override fun move(
        move: Move,
        supervisor: ChessGameSupervisor
    ): Boolean {
        clearCheckComputation()

        val movingFigure: Figure = clearFigure(move.from)
        val toFigure: Figure? = getFigureOrNull(move.to)

        val extendedMove: ExtendedMove = when(movingFigure.type) {
            FigureType.KING -> {
                if(toFigure!=null && toFigure.isWhite==movingFigure.isWhite) {
                    val isKingSideCastling = move.from.column<move.to.column
                    val kingToColumn = if (isKingSideCastling) 6 else 2
                    val rookToColumn = if (isKingSideCastling) 5 else 3
                    ExtendedMove.Castling(move, Move[move.from, Position[move.to.row, kingToColumn]], Move[move.to, Position[move.to.row, rookToColumn]])
                }else{
                    ExtendedMove.Normal(move, toFigure)
                }
            }
            FigureType.PAWN -> {
                if(move.to.row==0 || move.to.row==7) {
                    ExtendedMove.Promotion(move, movingFigure, getFigureOrNull(move.to))
                }else if(abs(move.from.row-move.to.row) ==2) {
                    ExtendedMove.PawnDoubleJump(move,  movingFigure as Pawn)
                }else if(move.from.column!=move.to.column && toFigure==null) {
                    val pawnTakenByEnpassant = getFigure(Position[move.from.row, move.to.column])
                    ExtendedMove.Enpassant(move, pawnTakenByEnpassant)
                }else{
                    ExtendedMove.Normal(move, toFigure)
                }
            }
            else -> ExtendedMove.Normal(move, toFigure)
        }

        when(extendedMove) {
            is ExtendedMove.Castling -> {
                val castlingRook = clearFigure(extendedMove.rookMove.from)
                setFigure(extendedMove.rookMove.to, castlingRook)
                setFigure(extendedMove.kingMove.to, movingFigure)
                // inform the involved figure(s) of the move
                movingFigure.figureMoved(extendedMove.kingMove)
                castlingRook.figureMoved(extendedMove.rookMove)
                (movingFigure as King).didCastling = true
            }
            is ExtendedMove.Promotion -> {
                val toPos: Position = extendedMove.move.to
                val promotedPawn: Figure = when (supervisor.askForPawnChange(toPos)) {
                    PawnPromotion.QUEEN -> Queen(movingFigure.isWhite, toPos)
                    PawnPromotion.ROOK -> Rook(movingFigure.isWhite, toPos)
                    PawnPromotion.KNIGHT -> Knight(movingFigure.isWhite, toPos)
                    PawnPromotion.BISHOP -> Bishop(movingFigure.isWhite, toPos)
                }
                setFigure(toPos, promotedPawn)
                // the newly created figure is already aware of its position and doesn't need to be informed
            }
            is ExtendedMove.Enpassant -> {
                setFigure(extendedMove.move.to, movingFigure)
                clearPos(extendedMove.pawnTaken.position)
                // inform the involved figure(s) of the move
                movingFigure.figureMoved(extendedMove.move)
            }
            is ExtendedMove.PawnDoubleJump -> {
                setFigure(extendedMove.move.to, movingFigure)
                // inform the involved figure(s) of the move
                extendedMove.pawn.let { pawn: Pawn ->
                    pawn.figureMoved(extendedMove.move)
                    pawn.canBeHitEnpassant = true
                }
            }
            is ExtendedMove.Normal -> {
                setFigure(extendedMove.move.to, movingFigure)
                // inform the involved figure(s) of the move
                movingFigure.figureMoved(extendedMove.move)
            }
        }.let {}

        // remove possible susceptibility to being hit by enpassant
        extendedMoveStack.lastOrNull()?.let { previousExtendedMove->
            if(previousExtendedMove is ExtendedMove.PawnDoubleJump) {
                previousExtendedMove.pawn.canBeHitEnpassant = false
            }
        }

        extendedMoveStack.addLast(extendedMove)
        isWhiteTurn = !isWhiteTurn

        return extendedMove.hasHitFigure
    }

    override fun undo(): Boolean {
        clearCheckComputation()

        val lastExtMove: ExtendedMove = extendedMoveStack.removeLast()
        when(lastExtMove) {
            is ExtendedMove.Castling -> {
                val king = clearFigure(lastExtMove.kingMove.to) as King
                val rook = clearFigure(lastExtMove.rookMove.to)
                setFigure(lastExtMove.kingMove.from, king)
                setFigure(lastExtMove.rookMove.from, rook)
                // inform the involved figure(s) of the undo
                king.undoMove(lastExtMove.kingMove.from)
                rook.undoMove(lastExtMove.rookMove.from)
                king.didCastling = false
            }
            is ExtendedMove.Promotion -> {
                clearPos(lastExtMove.move.to)
                lastExtMove.figureTaken?.let { figureTaken->
                    setFigure(figureTaken.position, figureTaken)
                }
                setFigure(lastExtMove.move.from, lastExtMove.pawnPromoted)
                // no undo necessary because pawn's position was never updated
            }
            is ExtendedMove.Enpassant -> {
                val pawnMoved = clearFigure(lastExtMove.move.to)
                setFigure(lastExtMove.move.from, pawnMoved)
                lastExtMove.pawnTaken.let { pawnTaken->
                    setFigure(pawnTaken.position, pawnTaken)
                }
                // inform the involved figure(s) of the undo
                pawnMoved.undoMove(lastExtMove.move.from)
            }
            is ExtendedMove.PawnDoubleJump -> {
                lastExtMove.pawn.let { pawn ->
                    clearPos(lastExtMove.move.to)
                    setFigure(lastExtMove.move.from, pawn)
                    // inform the involved figure(s) of the undo
                    pawn.undoMove(lastExtMove.move.from)
                    pawn.canBeHitEnpassant=false
                }
            }
            is ExtendedMove.Normal -> {
                val movingFigure = clearFigure(lastExtMove.move.to)
                setFigure(lastExtMove.move.from, movingFigure)
                lastExtMove.figureTaken?.let { setFigure(lastExtMove.move.to, it) }
                // inform the involved figure(s) of the undo
                movingFigure.undoMove(lastExtMove.move.from)
            }
        }.let {}

        extendedMoveStack.lastOrNull()?.let { preLastExtMove->
            if(preLastExtMove is ExtendedMove.PawnDoubleJump) {
                preLastExtMove.pawn.canBeHitEnpassant = true
            }
        }
        isWhiteTurn = !isWhiteTurn

        return lastExtMove.hasHitFigure
    }

    override fun movesPlayed(): List<Move> = extendedMoveStack.map { it.move }

    override fun getFigureOrNull(pos: Position) = board[pos.index]
    override fun isFreeArea(pos: Position) = board[pos.index] == null

    override fun toString(): String {
        val buffer = StringBuilder(512)
        for (row in 0..7) {
            for (column in 0..7) {
                val pos = Position[row, column]
                getFigureOrNull(pos)?.let {figure ->
                    buffer.append("$figure ")
                }
            }
        }
        //delete the final space
        if (buffer.isNotEmpty()) buffer.deleteCharAt(buffer.length - 1)

        return buffer.toString()
    }
}

private fun ArrayDeque<ExtendedMove>.getLatestMoves(count: Int?): String {
    val latestExtendedMoves = if (count == null) {
        this
    } else {
        assert(count > 0)
        val minIndex = (size - count).coerceAtLeast(0)
        filterIndexed { index, _ -> index >= minIndex }
    }

    return latestExtendedMoves.map { it.move }.joinToString(separator = ",") { it.toString() }
}
