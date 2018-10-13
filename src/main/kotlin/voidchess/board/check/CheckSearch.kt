package voidchess.board.check

import voidchess.board.BasicChessGameInterface
import voidchess.board.getFirstFigureInDir
import voidchess.board.move.Direction
import voidchess.board.move.ExtendedMove
import voidchess.board.move.Move
import voidchess.board.move.Position
import java.util.*


object CheckSearch {
    fun analyseCheck(game: BasicChessGameInterface, whiteInCheck: Boolean): CheckStatus {
        val kingPos = game.getKingPosition(whiteInCheck)
        val attackPositions = LinkedList<Position>()
        val collectAttackPositions: (pos: Position) -> Unit = {
            attackPositions.add(it)
        }

        isCheckByBishopOrQueen(game, kingPos, whiteInCheck, collectAttackPositions)
        isCheckByRookOrQueen(game, kingPos, whiteInCheck, collectAttackPositions)
        isCheckByKnight(game, kingPos, whiteInCheck, collectAttackPositions)
        isCheckByKing(game, kingPos, collectAttackPositions)
        isCheckByPawn(game, kingPos, whiteInCheck, collectAttackPositions)

        return when (attackPositions.size) {
            0 -> CheckStatus.NO_CHECK
            1 -> getPossiblePositions(kingPos, attackPositions[0])
            2 -> CheckStatus.DOUBLE_CHECK
            else -> throw IllegalStateException("more than 2 attackers are impossible " + attackPositions.size)
        }
    }

    fun analyseCheck(game: BasicChessGameInterface, whiteInCheck: Boolean, lastExtMove: ExtendedMove?): CheckStatus {
        if (lastExtMove == null) {
            return analyseCheck(game, whiteInCheck)
        }

        val lastMove = lastExtMove.move
        if (lastExtMove.isEnpassent) return analyseCheckAfterEnpassent(game, whiteInCheck, lastMove)
        if (lastExtMove.isCastling) return analyseCheckAfterCastling(game, whiteInCheck, lastMove)
        if (lastExtMove.isPawnTransformation) return analyseCheckAfterPawnTransform(game, whiteInCheck, lastMove)

        val kingPos = game.getKingPosition(whiteInCheck)
        val attackPositions = ArrayList<Position>(2)
        val movedFigure = game.getFigure(lastMove.to)!!

        if (movedFigure.isReachable(kingPos, game)) {
            attackPositions.add(lastMove.to)
        }

        getPassiveAttacker(game, kingPos, lastMove.from)?.let { attackPositions.add(it) }

        return when (attackPositions.size) {
            0 -> CheckStatus.NO_CHECK
            1 -> getPossiblePositions(kingPos, attackPositions[0])
            2 -> CheckStatus.DOUBLE_CHECK
            else -> throw IllegalStateException("more than 2 attackers are impossible " + attackPositions.size)
        }
    }

    private fun analyseCheckAfterEnpassent(game: BasicChessGameInterface, whiteInCheck: Boolean, lastMove: Move): CheckStatus {
        val kingPos = game.getKingPosition(whiteInCheck)

        val attackPositions = ArrayList<Position>(2)
        val attackFigure = game.getFigure(lastMove.to)!!

        var passiveAttacker = getPassiveAttacker(game, kingPos, lastMove.from)
        if (passiveAttacker == null) {
            //an attackpath may have opend over the diagonal of the removed pawn
            val removedPawnPos = Position[lastMove.from.row, lastMove.to.column]
            passiveAttacker = getPassiveAttacker(game, kingPos, removedPawnPos)
        }

        if (attackFigure.isReachable(kingPos, game)) {
            attackPositions.add(attackFigure.position)
        }
        passiveAttacker?.let { attackPositions.add(it) }

        return when (attackPositions.size) {
            0 -> CheckStatus.NO_CHECK
            1 -> getPossiblePositions(kingPos, attackPositions[0])
            2 -> CheckStatus.DOUBLE_CHECK
            else -> throw IllegalStateException("more than 2 attackers are impossible " + attackPositions.size)
        }
    }

    private fun analyseCheckAfterPawnTransform(game: BasicChessGameInterface, whiteInCheck: Boolean, lastMove: Move): CheckStatus {
        val kingPos = game.getKingPosition(whiteInCheck)
        val transformedPawn = game.getFigure(lastMove.to)!!
        val attackPositions = ArrayList<Position>(2)
        val passiveAttacker = getPassiveAttacker(game, kingPos, lastMove.from)

        if (transformedPawn.isReachable(kingPos, game)) {
            attackPositions.add(lastMove.to)
        }
        if (passiveAttacker != null && passiveAttacker.notEqualsPosition(lastMove.to)) {
            attackPositions.add(passiveAttacker)
        }

        return when (attackPositions.size) {
            0 -> CheckStatus.NO_CHECK
            1 -> getPossiblePositions(kingPos, attackPositions[0])
            2 -> CheckStatus.DOUBLE_CHECK
            else -> throw IllegalStateException("more than 2 attackers are impossible " + attackPositions.size)
        }
    }

    private fun analyseCheckAfterCastling(game: BasicChessGameInterface, whiteInCheck: Boolean, lastMove: Move): CheckStatus {
        val kingPos = game.getKingPosition(whiteInCheck)

        val rookRow = lastMove.to.row
        val rookColumn = if (lastMove.to.column == 2) 3 else 5
        val rookPos = Position[rookRow, rookColumn]
        val rook = game.getFigure(rookPos)!!

        return if (rook.isReachable(kingPos, game)) getPossiblePositions(kingPos, rookPos) else CheckStatus.NO_CHECK
    }

    private fun getPossiblePositions(kingPos: Position, attackerPos: Position): CheckStatus {
        return if (kingPos.isStraightOrDiagonalTo(attackerPos)) {
            //diagonal or straight attack
            val result = LinkedList<Position>()
            kingPos.forEachMiddlePosInLine(attackerPos) { middlePos ->
                result.add(middlePos)
            }
            result.add(attackerPos)
            CheckStatus(result)
        } else {
            //Knight attacks
            CheckStatus(listOf(attackerPos))
        }
    }

    private val doNotCollectPositions: (pos: Position) -> Unit = {}

    fun isCheck(game: BasicChessGameInterface, kingPos: Position): Boolean {
        val isWhite = game.getFigure(kingPos)!!.isWhite

        if (isCheckByBishopOrQueen(game, kingPos, isWhite, doNotCollectPositions)) return true
        if (isCheckByRookOrQueen(game, kingPos, isWhite, doNotCollectPositions)) return true
        if (isCheckByKnight(game, kingPos, isWhite, doNotCollectPositions)) return true
        return if (isCheckByKing(game, kingPos, doNotCollectPositions)) true
        else isCheckByPawn(game, kingPos, isWhite, doNotCollectPositions)
    }

    private inline fun isCheckByKing(game: BasicChessGameInterface, kingPos: Position, informOfAttackerPos: (Position) -> Unit): Boolean {
        Direction.values().forEach {
            kingPos.step(it)?.let { pos ->
                game.getFigure(pos)?.let { figure ->
                    if (figure.isKing()) {
                        informOfAttackerPos(pos)
                        return true
                    }
                }
            }
        }

        return false
    }

    private inline fun isCheckByPawn(game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {
        val forwardDir = if (isWhite) Direction.UP else Direction.DOWN

        kingPos.step(Direction.getDiagonal(forwardDir, Direction.RIGHT))?.let { pos ->
            game.getFigure(pos)?.let { figure ->
                if (figure.isPawn() && figure.isWhite != isWhite) {
                    informOfAttackerPos(pos)
                    return true
                }
            }
        }
        kingPos.step(Direction.getDiagonal(forwardDir, Direction.LEFT))?.let { pos ->
            game.getFigure(pos)?.let { figure ->
                if (figure.isPawn() && figure.isWhite != isWhite) {
                    informOfAttackerPos(pos)
                    return true
                }
            }
        }

        return false
    }

    private inline fun isCheckByKnight(game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {
        kingPos.forEachKnightPos { pos ->
            game.getFigure(pos)?.let { figure ->
                if (figure.isWhite != isWhite && figure.isKnight()) {
                    informOfAttackerPos(pos)
                    return true
                }
            }
        }

        return false
    }

    private inline fun isCheckByBishopOrQueen(game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {
        Direction.diagonalDirs.forEach { diagonal ->
            game.getFirstFigureInDir(diagonal, kingPos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure.isQueen() || figure.isBishop())) {
                    informOfAttackerPos(figure.position)
                    return true
                }
            }
        }
        return false
    }

    private inline fun isCheckByRookOrQueen(game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {

        if (isDoubleHorizontalCheckAfterPawnPromotion(game, kingPos, isWhite, informOfAttackerPos)) return true
        // now that a straight double attack after pawn promotion is no longer an issue,
        // we can stop looking for a second attacker from straight lines

        Direction.straightDirs.forEach { diagonal ->
            game.getFirstFigureInDir(diagonal, kingPos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                    informOfAttackerPos(figure.position)
                    return true
                }
            }
        }
        return false
    }

    private inline fun isDoubleHorizontalCheckAfterPawnPromotion(
            game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {
        //only possible if the king stod in the columnshadow of a pawn which transformed in the last move to Rook or queen
        val groundRow = if (isWhite) 0 else 7
        if (kingPos.row != groundRow) return false

        var kingSideAttackerPos: Position? = null

        kingPos.step(Direction.LEFT)?.let { sidePos ->
            game.getFigure(sidePos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                    kingSideAttackerPos = sidePos
                }
            }
        }
        if (kingSideAttackerPos == null) {
            kingPos.step(Direction.RIGHT)?.let { sidePos ->
                game.getFigure(sidePos)?.let { figure ->
                    if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                        kingSideAttackerPos = sidePos
                    }
                }
            }
        }

        if (kingSideAttackerPos == null) return false

        val dirOfPossibleSecondStraightAttacker = if (isWhite) Direction.UP else Direction.DOWN

        game.getFirstFigureInDir(dirOfPossibleSecondStraightAttacker, kingPos)?.let { figure ->
            if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                informOfAttackerPos(kingSideAttackerPos!!)
                informOfAttackerPos(figure.position)
                return true
            }
        }

        return false
    }

    private fun getPassiveAttacker(game: BasicChessGameInterface,
                                   kingPos: Position,
                                   lastMovedFrom: Position): Position? {

        val direction = kingPos.getDirectionTo(lastMovedFrom)
        if (direction == null) return null

        game.getFirstFigureInDir(direction, lastMovedFrom)?.let { figure ->
            if (figure.isReachable(kingPos, game)) {
                return figure.position
            }
        }
        return null
    }
}
