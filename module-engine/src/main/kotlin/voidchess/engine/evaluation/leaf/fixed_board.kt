package voidchess.engine.evaluation.leaf

import voidchess.common.board.move.Position
import voidchess.common.figures.*
import voidchess.engine.board.EngineChessGame
import kotlin.math.abs

internal data class FixedBoard(
    val board: IntArray,
    val whiteKingPosIndex: Int,
    val blackKingPosIndex: Int,
    val didWhiteKingCastle: Boolean,
    val didBlackKingCastle: Boolean,
    val isWhiteTurn: Colour,
) {
    init {
        assert(board.size == 64) {"board should have size of 64 but was ${board.size}"}
    }

    val whiteKingPos get() = Position.byIndex(whiteKingPosIndex)
    val blackKingPos get() = Position.byIndex(blackKingPosIndex)

    fun getContent(posIndex: Int) = BoardContent(board[posIndex])

    inline fun forAllFigures(crossinline iter: (figure: BoardContent, posIndex: Int)->Unit) {
        for(posIndex in 0 until 64) {
            val content = BoardContent(board[posIndex])
            if (!content.isEmpty()) {
                iter(
                    content,
                    posIndex,
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FixedBoard) return false

        return board.contentEquals(other.board) && isWhiteTurn == other.isWhiteTurn
    }

    override fun hashCode() = board.contentHashCode()

    companion object {
        @JvmStatic
        fun from(game: EngineChessGame) = FixedBoard(
            board = IntArray(64) { index ->
                boardContentBy(game.getFigureOrNull(Position.byIndex(index))).content
            },
            whiteKingPosIndex = game.whiteKing.position.index,
            blackKingPosIndex = game.blackKing.position.index,
            didWhiteKingCastle = game.whiteKing.didCastling,
            didBlackKingCastle = game.blackKing.didCastling,
            isWhiteTurn = Colour(game.isWhiteTurn),
        )
    }
}

@JvmInline
internal value class Colour(val isWhite: Boolean)

@JvmInline
internal value class BoardContent(val content: Int) {
    init {
        assert(abs(content) <= KING_CONTENT) {"value is supposed to be in range -6..6 but was $content"}
    }

    val colour get() = Colour(content>0)
    fun hasOppositeColour(otherColour: Colour) = otherColour!=colour
    fun hasSameColour(otherColour: Colour) = otherColour==colour

    fun isEmpty() = content == EMPTY_CONTENT
    fun isPawn() = abs(content) == PAWN_CONTENT
    fun isRook() = abs(content) == ROOK_CONTENT
    fun isKnight() = abs(content) == KNIGHT_CONTENT
    fun isBishop() = abs(content) == BISHOP_CONTENT
    fun isQueen() = abs(content) == QUEEN_CONTENT
    fun isKing() = abs(content) == KING_CONTENT
}

private const val EMPTY_CONTENT = 0
private const val PAWN_CONTENT = 1
private const val ROOK_CONTENT = 2
private const val KNIGHT_CONTENT = 3
private const val BISHOP_CONTENT = 4
private const val QUEEN_CONTENT = 5
private const val KING_CONTENT = 6

internal fun boardContentBy(figure: Figure?): BoardContent {
    val isWhite: Boolean = figure?.isWhite ?: return BoardContent(EMPTY_CONTENT)
    fun coloured(content: Int) = if(isWhite) content else -content
    return when(figure) {
        is Pawn -> BoardContent(coloured(PAWN_CONTENT))
        is Rook -> BoardContent(coloured(ROOK_CONTENT))
        is Knight -> BoardContent(coloured(KNIGHT_CONTENT))
        is Bishop -> BoardContent(coloured(BISHOP_CONTENT))
        is Queen -> BoardContent(coloured(QUEEN_CONTENT))
        is King -> BoardContent(coloured(KING_CONTENT))
    }
}