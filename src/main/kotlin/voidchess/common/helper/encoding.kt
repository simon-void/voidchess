package voidchess.common.helper

import voidchess.common.board.ArrayChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.forAllFiguresOfColor
import voidchess.common.board.getFigure
import voidchess.common.board.move.Move
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.common.board.other.Chess960Index
import voidchess.common.figures.FigureType

fun List<Move>.toUrlSafeEncoding(chess960Index: Chess960Index): String {
    fun ChessBoard.isCastlingMove(move: Move): Boolean {
        if (this.getFigure(move.from).type != FigureType.KING) return false
        val capturedFigure = this.getFigureOrNull(move.to) ?: return false
        return capturedFigure.isWhite == this.isWhiteTurn && capturedFigure.type == FigureType.ROOK
    }

    fun ChessBoard.onlySingleFigureCanReach(target: Position): Boolean {
        val board = this
        var nrOfFiguresThatCanMoveToTarget = 0
        this.forAllFiguresOfColor(board.isWhiteTurn) { figure ->
            if (figure.isReachable(target, board)) {
                nrOfFiguresThatCanMoveToTarget++
            }
            if (nrOfFiguresThatCanMoveToTarget == 2) {
                return false
            }
        }
        assert(nrOfFiguresThatCanMoveToTarget == 1) { "no figure can move to $target on board $board" }
        return true
    }

    fun ChessBoard.encodeOnlyToPos(move: Move): Boolean =
        !this.isCastlingMove(move) && this.onlySingleFigureCanReach(move.to)

    val board: ChessBoard =
        ArrayChessBoard(chess960Index.toStartConfig())

    return this.joinToString(separator = "") { move ->
        val encodedMove = if (board.encodeOnlyToPos(move)) {
            move.to.toUrlSafeEncoding().toString()
        } else {
            move.toUrlSafeEncoding()
        }.let { encodedFromTo ->
            move.pawnPromotionType?.let { promotionType ->
                encodedFromTo + promotionType.toUrlSafeEncoding()
            } ?: encodedFromTo
        }
        board.move(move)
        encodedMove
    }
}

// 0 A            17 R            34 i            51 z
// 1 B            18 S            35 j            52 0
// 2 C            19 T            36 k            53 1
// 3 D            20 U            37 l            54 2
// 4 E            21 V            38 m            55 3
// 5 F            22 W            39 n            56 4
// 6 G            23 X            40 o            57 5
// 7 H            24 Y            41 p            58 6
// 8 I            25 Z            42 q            59 7
// 9 J            26 a            43 r            60 8
//10 K            27 b            44 s            61 9
//11 L            28 c            45 t            62 - (minus)
//12 M            29 d            46 u            63 _ (underline)
//13 N            30 e            47 v
//14 O            31 f            48 w
//15 P            32 g            49 x
//16 Q            33 h            50 y         (pad) =
private val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray()

private fun Position.toUrlSafeEncoding(): Char = base64Chars[index]

private fun Move.toUrlSafeEncoding(): String = "${from.toUrlSafeEncoding()}${to.toUrlSafeEncoding()}"

private fun PawnPromotion.toUrlSafeEncoding(): Char = when (this) {
    PawnPromotion.QUEEN -> 'Q'
    PawnPromotion.ROOK -> 'R'
    PawnPromotion.KNIGHT -> 'N'
    PawnPromotion.BISHOP -> 'B'
}
