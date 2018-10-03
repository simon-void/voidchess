package voidchess.helper

import voidchess.board.BasicChessGameInterface
import voidchess.board.getFirstFigureInDir
import java.lang.Integer.signum
import java.util.*


object CheckSearch {
    fun analyseCheck(game: BasicChessGameInterface, whiteInCheck: Boolean): CheckStatus {
        val kingPos = game.getKingPosition(whiteInCheck)
        val attackPositions = ArrayList<Position>(2)

        isCheckByBishopOrQueen(game, kingPos, attackPositions, whiteInCheck)
        isCheckByRookOrQueen(game, kingPos, attackPositions, whiteInCheck)
        isCheckByKnight(game, kingPos, attackPositions, whiteInCheck)
        isCheckByKing(game, kingPos, attackPositions)
        isCheckByPawn(game, kingPos, attackPositions, whiteInCheck)

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
            kingPos.forEachMiddlePosInLine(attackerPos) {middlePos ->
                result.add(middlePos)
            }
            result.add(attackerPos)
            CheckStatus(result)
        } else {
            //Knight attacks
            CheckStatus(listOf(attackerPos))
        }
    }

    fun isCheck(game: BasicChessGameInterface, kingPos: Position): Boolean {
        val isWhite = game.getFigure(kingPos)!!.isWhite

        // TODO replace list with inline informOfAttackPositions closure
        val attackPositions = ArrayList<Position>(2)
        if (isCheckByBishopOrQueen(game, kingPos, attackPositions, isWhite)) return true
        if (isCheckByRookOrQueen(game, kingPos, attackPositions, isWhite)) return true
        if (isCheckByKnight(game, kingPos, attackPositions, isWhite)) return true
        return if (isCheckByKing(game, kingPos, attackPositions)) true
        else isCheckByPawn(game, kingPos, attackPositions, isWhite)
    }

    private fun isCheckByKing(game: BasicChessGameInterface, kingPos: Position, attackerPos: MutableList<Position>): Boolean {
        Direction.values().forEach {
            kingPos.step(it)?.let { pos ->
                game.getFigure(pos)?.let { figure ->
                    if (figure.isKing()) {
                        attackerPos.add(pos)
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun isCheckByPawn(game: BasicChessGameInterface, kingPos: Position, attackerPos: MutableList<Position>, isWhite: Boolean): Boolean {
        val forwardDir = if (isWhite) Direction.UP else Direction.DOWN

        kingPos.step(Direction.getDiagonal(forwardDir, Direction.RIGHT))?.let { pos ->
            game.getFigure(pos)?.let { figure ->
                if (figure.isPawn() && figure.isWhite != isWhite) {
                    attackerPos.add(pos)
                    return true
                }
            }
        }
        kingPos.step(Direction.getDiagonal(forwardDir, Direction.LEFT))?.let { pos ->
            game.getFigure(pos)?.let { figure ->
                if (figure.isPawn() && figure.isWhite != isWhite) {
                    attackerPos.add(pos)
                    return true
                }
            }
        }

        return false
    }

    private fun isCheckByKnight(game: BasicChessGameInterface, kingPos: Position, attackerPos: MutableList<Position>, isWhite: Boolean): Boolean {
        kingPos.forEachKnightPos { pos ->
            game.getFigure(pos)?.let { figure ->
                if (figure.isWhite != isWhite && figure.isKnight()) {
                    attackerPos.add(pos)
                    return true
                }
            }
        }

        return false
    }

    private fun isCheckByBishopOrQueen(game: BasicChessGameInterface, kingPos: Position, attackerPos: MutableList<Position>, isWhite: Boolean): Boolean {
        Direction.diagonalDirs.forEach { diagonal ->
            game.getFirstFigureInDir(diagonal, kingPos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure.isQueen() || figure.isBishop())) {
                    attackerPos.add(figure.position)
                    return true
                }
            }
        }
        return false
    }

    private fun isCheckByRookOrQueen(game: BasicChessGameInterface, kingPos: Position, attackerPos: MutableList<Position>, isWhite: Boolean): Boolean {

        if (isDoubleHorizontalCheckAfterPawnPromotion(game, kingPos, attackerPos, isWhite)) return true
        // now that a straight double attack after pawn promotion is no longer an issue,
        // we can stop looking for a second attacker from straight lines

        Direction.straightDirs.forEach { diagonal ->
            game.getFirstFigureInDir(diagonal, kingPos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                    attackerPos.add(figure.position)
                    return true
                }
            }
        }
        return false
    }

    private fun isDoubleHorizontalCheckAfterPawnPromotion(
            game: BasicChessGameInterface, kingPos: Position, attackerPos: MutableList<Position>, isWhite: Boolean): Boolean {
        //only possible if the king stod in the columnshadow of a pawn which transformed in the last move to Rook or queen
        val groundRow = if (isWhite) 0 else 7
        if (kingPos.row != groundRow) return false

        var kingSideAttackerPos: Position? = null

        kingPos.step(Direction.LEFT)?.let { sidePos->
            game.getFigure(sidePos)?.let { figure ->
                if(figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                    kingSideAttackerPos = sidePos
                }
            }
        }
        if(kingSideAttackerPos==null) {
            kingPos.step(Direction.RIGHT)?.let { sidePos ->
                game.getFigure(sidePos)?.let { figure ->
                    if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                        kingSideAttackerPos = sidePos
                    }
                }
            }
        }

        if (kingSideAttackerPos == null) return false

        val dirOfPossibleSecondStraightAttacker = if(isWhite) Direction.UP else Direction.DOWN

        game.getFirstFigureInDir(dirOfPossibleSecondStraightAttacker, kingPos)?.let { figure ->
            if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                attackerPos.add(kingSideAttackerPos!!)
                attackerPos.add(figure.position)
                return true
            }
        }

        return false
    }

    private fun getPassiveAttacker(game: BasicChessGameInterface,
                                   kingPos: Position,
                                   lastMovedFrom: Position): Position? {

        val direction = kingPos.getDirectionTo(lastMovedFrom)
        if(direction==null) return null

        game.getFirstFigureInDir(direction, kingPos)?.let { figure ->
            if (figure.isReachable(kingPos, game)) {
                return figure.position
            }
        }
        return null
    }
}
