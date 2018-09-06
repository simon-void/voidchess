package voidchess.board

enum class MoveResult {
    NO_END,
    MATT,
    PATT,
    DRAW,
    THREE_TIMES_SAME_POSITION,
    FIFTY_MOVES_NO_HIT,
    RESIGN
}