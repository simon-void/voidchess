package voidchess.common.board.move

enum class MoveResultType {
    NO_END,
    CHECKMATE,
    STALEMATE,
    DRAW,
    THREE_TIMES_SAME_POSITION,
    FIFTY_MOVES_NO_HIT,
    RESIGN
}