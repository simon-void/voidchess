package voidchess.common.helper

import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.DataProvider
import voidchess.common.board.move.Move
import voidchess.common.board.other.Chess960Index

class EncodingKtTest {

    @Test(dataProvider = "getTestMovesToConvertToEncoding")
    fun convertMovesToUrlSafeEncoding(chess960Index: Int, concatenatedMoves: String, expectedEncoded: String) {
        fun String.deleteSpace(): String = this.replace(" ", "")
        fun String.toMoves(): List<Move> = this.split(",").filter { it.isNotBlank() }.map { Move.byCode(it.trim()) }
        val moves = concatenatedMoves.toMoves()
        val actualEncoded = moves.toUrlSafeEncoding(Chess960Index(chess960Index))
        assertEquals(actualEncoded, expectedEncoded.deleteSpace())
    }

    @DataProvider
    fun getTestMovesToConvertToEncoding(): Array<Array<Any>> {
        return arrayOf(
            //                                   | "no moves -> empty encoded String
            arrayOf(518 , "", ""),
            // KS                                | destination not unique target -> encoding needs two chars
            arrayOf(518 , "c2-c3", "KS"),
            // KS                                | in chess960 pos 900 can only the pawn move to c3 (knights are on the right side)
            arrayOf(900 , "c2-c3", "S"),
            // Ka                                | destination is unique target -> encoding needs one char
            arrayOf(518 , "c2-c4", "a"),
            // IY 3v Yg xh gp vn py nf OW fW y7Q | tests all pawn moves single-step, double-step, diagonal-capture, en-passant & promotion
            arrayOf(518 , "a2-a4,h7-h6,a4-a5,b7-b5,a5-b6,h6-h5,b6-c7,h5-h4,g2-g3,h4-g3,c7Qd8", "Y 3v g h p n y f W W 7Q"),
            // LT 2u CU 92 BS -t DL 8_ EA        | tests king- & queen-side castling
            arrayOf(518 , "d2-d3,g7-g6,c1-e3,f8-g7,b1-c3,g8-f6,d1-d2,e8-h8,e1-a1", "T u CU 2 BS -t DL 8_ EA"),
            // IY -t Yg xh gp 2u px 92 x4N 8_    | pawn promotion to knight is encoded as N
            arrayOf(518, "a2-a4,g8-f6,a4-a5,b7-b5,a5-b6,g7-g6,b6-b7,f8-g7,b7Ka8,e8-h8", "Y -t g h p u x 2 4N 8_"),
            // make sure king is counted when in check if only one figure can move to a target position
            arrayOf(518, "g2-g3,b7-b6,g1-f3,b6-b5,f1-g2,b5-b4,e1-h1,b4-b3,g1-h1,b3-a2,f1-g1", "WpGVhOZEHRGHIFG"),
            arrayOf(518, "e2-e4,c7-c5,g1-f3,e7-e6,c2-c3,f7-f5,e4-f5,e6-f5,d2-d4,f8-e7,d4-d5,g8-f6,c3-c4,e8-h8,d5-d6,a7-a6,d6-e7,d8-e7,f1-e2,b8-c6,b1-c3,d7-d6,e1-h1,c8-e6,b2-b3,c6-e5,c1-b2,b7-b6,f1-e1,a6-a5,d1-c2,f6-g4,h2-h3,g4-f6,a1-d1,a8-c8,c3-d5,e6-d5,c4-d5,f5-f4,f3-e5,d6-e5,d5-d6,e7-b7,e2-c4,g8-h8,b2-e5,f4-f3,g2-g3,b7-d7,g1-h2,b6-b5,c4-f1,a5-a4,e5-f6,f8-f6,e1-e7,a4-b3,a2-b3,d7-c6,d6-d7,c8-d8,e7-e8,d8-e8,d7Qd8,b5-b4,d8-d5,e8-f8,d5-c6,f6-c6,d1-d7,h8-g8,f1-c4,g8-h8,c2-e4,c6-h6,e4-e7,f8-g8,c4-g8,h6-g6,g8-f7,h7-h5,f7-g6,h5-h4,d7-d8", "ciGVsKSlllLb90j-tSa8_rwo00FMqSzrEH6sJRkJpFEgKteXetAD46SjsjajdVkrkrxMa_JkdVOWxzPphaFYt9t0RIRzqrz7887QZ7j89qqz_-Fa_cqvc09--vu-1nufz7")
        )
    }
}
