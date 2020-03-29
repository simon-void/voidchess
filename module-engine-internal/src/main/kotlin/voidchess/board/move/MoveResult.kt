package voidchess.board.move

internal enum class MoveResult {
    NO_END,
    CHECKMATE,
    STALEMATE,
    DRAW,
    THREE_TIMES_SAME_POSITION,
    FIFTY_MOVES_NO_HIT,
    RESIGN
}