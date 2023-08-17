package voidchess.common.board.check

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.getFirstFigureInDir
import voidchess.common.board.getKing
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Position
import voidchess.common.figures.Knight
import voidchess.common.figures.Pawn
import kotlin.collections.HashMap


data class AttackLines(val checkLines: List<CheckLine>, val boundLineByBoundFigurePos: Map<Position, BoundLine>) {
    init {
        assert(checkLines.size in 0..2) {"checkLines can only possibly contain between 0 and 2 checks but does contain ${checkLines.size}"}
    }

    val noCheck
        get() = checkLines.isEmpty()
    val isCheck
        get() = checkLines.isNotEmpty()
    val isSingleCheck
        get() = checkLines.size == 1
    val isDoubleCheck
        get() = checkLines.size == 2
}

fun checkAttackLines(game: StaticChessBoard, isWhite: Boolean): AttackLines {
    val king = game.getKing(isWhite)
    val kingPos = king.position

    val checkLines = ArrayList<CheckLine>()
    val boundLineByBoundFigurePos = HashMap<Position, BoundLine>(8)

    // check for check by bishop, rook or queen (the only figures that can also bind other figures)
    for(directionFromKingOutwards in Direction.entries) {
        val firstInLine = game.getFirstFigureInDir(directionFromKingOutwards, kingPos)
        if (firstInLine != null) {
            if (firstInLine.isWhite != isWhite) {
                val isAttackingKing = if (directionFromKingOutwards.isStraight) firstInLine.attacksStraightLine else firstInLine.attacksDiagonalLine
                if (isAttackingKing) {
                    checkLines.add(ActualCheckLine(kingPos, firstInLine.position, directionFromKingOutwards))
                }
            } else {
                val secondInLine = game.getFirstFigureInDir(directionFromKingOutwards, firstInLine.position)
                if (secondInLine != null) {
                    if (secondInLine.isWhite != isWhite) {
                        val isFirstInLineBound = if (directionFromKingOutwards.isStraight) secondInLine.attacksStraightLine else secondInLine.attacksDiagonalLine
                        if (isFirstInLineBound) {
                            val boundFigurePos = firstInLine.position
                            boundLineByBoundFigurePos[boundFigurePos] =
                                    BoundLine(kingPos, boundFigurePos, secondInLine.position, directionFromKingOutwards)
                        }
                    }
                }
            }
        }
    }

    // check for check by pawn
    val forwardDirection = Direction.getForward(king.isWhite)
    for(forwardDiagonalDir in sequenceOf(
            Direction.getDiagonal(forwardDirection, Direction.RIGHT),
            Direction.getDiagonal(forwardDirection, Direction.LEFT)
    )) {
        kingPos.step(forwardDiagonalDir)?.let { forwardDiagonalPos ->
            game.getFigureOrNull(forwardDiagonalPos)?.let { possiblyPawn ->
                if(possiblyPawn.isWhite!=isWhite && possiblyPawn is Pawn) {
                    checkLines.add(PawnCheck(forwardDiagonalPos, kingPos))
                }
            }
        }
    }

    kingPos.forEachKnightPos {possibleKnightPos->
        game.getFigureOrNull(possibleKnightPos)?.let { figure ->
            if(figure is Knight &&figure.isWhite!=isWhite) {
                checkLines.add(KnightCheck(possibleKnightPos, kingPos))
            }
        }
    }

    return AttackLines(checkLines, boundLineByBoundFigurePos)
}