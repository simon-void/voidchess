package voidchess.helper;

import voidchess.board.BasicChessGameInterface;
import voidchess.board.SimpleChessBoardInterface;
import voidchess.figures.Figure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stephan
 */
public class CheckSearch {
    public static CheckStatus analyseCheck(SimpleChessBoardInterface game, boolean whiteInCheck) {
        final Position kingPos = game.getKingPosition(whiteInCheck);
        final boolean isWhite = game.getFigure(kingPos).isWhite();
        final List<Position> attackPositions = new ArrayList<Position>(2);

        isCheckByBishopOrQueen(game, kingPos, attackPositions, isWhite);
        isCheckByKing(game, kingPos, attackPositions);
        isCheckByKnight(game, kingPos, attackPositions, isWhite);
        isCheckByPawn(game, kingPos, attackPositions, isWhite);
        isCheckByRockOrQueen(game, kingPos, attackPositions, isWhite);

        switch (attackPositions.size()) {
            case 0:
                return CheckStatus.NO_CHECK;
            case 1:
                return getPossiblePositions(kingPos, attackPositions.get(0));
            case 2:
                return CheckStatus.DOUBLE_CHECK;
            default:
                throw new IllegalStateException("more than 2 attackers are impossible " + attackPositions.size());
        }
    }

    public static CheckStatus analyseCheck(SimpleChessBoardInterface game, boolean whiteInCheck, ExtendedMove lastMove) {
        if (lastMove == null) {
            return analyseCheck(game, whiteInCheck);
        }

        if (lastMove.isEnpassent()) return analyseCheckAfterEnpassent(game, whiteInCheck, lastMove);
        if (lastMove.isRochade()) return analyseCheckAfterRochade(game, whiteInCheck, lastMove);
        if (lastMove.pawnTransformed()) return analyseCheckAfterPawnTransform(game, whiteInCheck, lastMove);

        final Position kingPos = game.getKingPosition(whiteInCheck);
        final Figure movedFigure = game.getFigure(lastMove.to);
        final List<Position> attackPositions = new ArrayList<Position>(2);

        if (movedFigure.isReachable(kingPos, game)) {
            attackPositions.add(lastMove.to);
        }

        Position passiveAttacker = getPassiveAttacker(game, kingPos, lastMove.from);
        if (passiveAttacker != null) {
            attackPositions.add(passiveAttacker);
        }

        switch (attackPositions.size()) {
            case 0:
                return CheckStatus.NO_CHECK;
            case 1:
                return getPossiblePositions(kingPos, attackPositions.get(0));
            case 2:
                return CheckStatus.DOUBLE_CHECK;
            default:
                throw new IllegalStateException("more than 2 attackers are impossible " + attackPositions.size());
        }
    }

    private static CheckStatus analyseCheckAfterEnpassent(SimpleChessBoardInterface game, boolean whiteInCheck, Move lastMove) {
        final Position kingPos = game.getKingPosition(whiteInCheck);

        final List<Position> attackPositions = new ArrayList<Position>(2);
        final Figure attackFigure = game.getFigure(lastMove.to);

        Position passiveAttacker = getPassiveAttacker(game, kingPos, lastMove.from);
        if (passiveAttacker == null) {
            //an attackpath may have opend over the diagonal of the removed pawn
            Position removedPawnPos = Position.get(lastMove.from.row, lastMove.to.column);
            passiveAttacker = getPassiveAttacker(game, kingPos, removedPawnPos);
        }

        if (attackFigure.isReachable(kingPos, game)) {
            attackPositions.add(attackFigure.getPosition());
        }
        if (passiveAttacker != null) {
            attackPositions.add(passiveAttacker);
        }

        switch (attackPositions.size()) {
            case 0:
                return CheckStatus.NO_CHECK;
            case 1:
                return getPossiblePositions(kingPos, attackPositions.get(0));
            case 2:
                return CheckStatus.DOUBLE_CHECK;
            default:
                throw new IllegalStateException("more than 2 attackers are impossible " + attackPositions.size());
        }
    }

    private static CheckStatus analyseCheckAfterPawnTransform(SimpleChessBoardInterface game, boolean whiteInCheck, Move lastMove) {
        final Position kingPos = game.getKingPosition(whiteInCheck);
        final Figure transformedPawn = game.getFigure(lastMove.to);
        final List<Position> attackPositions = new ArrayList<Position>(2);
        final Position passiveAttacker = getPassiveAttacker(game, kingPos, lastMove.from);

        if (transformedPawn.isReachable(kingPos, game)) {
            attackPositions.add(lastMove.to);
        }
        if (passiveAttacker != null && !passiveAttacker.equalsPosition(lastMove.to)) {
            attackPositions.add(passiveAttacker);
        }

        switch (attackPositions.size()) {
            case 0:
                return CheckStatus.NO_CHECK;
            case 1:
                return getPossiblePositions(kingPos, attackPositions.get(0));
            case 2:
                return CheckStatus.DOUBLE_CHECK;
            default:
                throw new IllegalStateException("more than 2 attackers are impossible " + attackPositions.size());
        }
    }

    private static CheckStatus analyseCheckAfterRochade(SimpleChessBoardInterface game, boolean whiteInCheck, Move lastMove) {
        final Position kingPos = game.getKingPosition(whiteInCheck);

        final int rock_row = lastMove.to.row;
        final int rock_column = lastMove.to.column == 2 ? 3 : 5;
        final Position rockPos = Position.get(rock_row, rock_column);
        final Figure rock = game.getFigure(rockPos);

        if (rock.isReachable(kingPos, game)) return getPossiblePositions(kingPos, rockPos);
        return CheckStatus.NO_CHECK;
    }

    private static CheckStatus getPossiblePositions(Position kingPos, Position attackerPos) {
        List<Position> result;
        if (!areStraightPositions(kingPos, attackerPos) && !areDiagonalPositions(kingPos, attackerPos)) {
            //Knight attacks
            result = new ArrayList<Position>(1);
            result.add(attackerPos);
        } else {
            //diagonal or straight attack
            result = getInBetweenPositions(attackerPos, kingPos);
            result.add(attackerPos);
        }
        return new CheckStatus(result);
    }

    public static boolean isCheck(BasicChessGameInterface game, Position kingPos) {
        final boolean isWhite = game.getFigure(kingPos).isWhite();

        final List<Position> attackPositions = new ArrayList<Position>(2);
        if (isCheckByBishopOrQueen(game, kingPos, attackPositions, isWhite)) return true;
        if (isCheckByRockOrQueen(game, kingPos, attackPositions, isWhite)) return true;
        if (isCheckByKnight(game, kingPos, attackPositions, isWhite)) return true;
        if (isCheckByKing(game, kingPos, attackPositions)) return true;
        if (isCheckByPawn(game, kingPos, attackPositions, isWhite)) return true;
        return false;
    }

    private static boolean isCheckByKing(
            final BasicChessGameInterface game, final Position kingPos, final List<Position> attackerPos) {
        final int minRow = Math.max(0, kingPos.row - 1);
        final int maxRow = Math.min(7, kingPos.row + 1);
        final int minColumn = Math.max(0, kingPos.column - 1);
        final int maxColumn = Math.min(7, kingPos.column + 1);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                if (row != kingPos.row || column != kingPos.column) {
                    final Position pos = Position.get(row, column);
                    final Figure figure = game.getFigure(pos);
                    if (figure != null && figure.isKing()) {
                        attackerPos.add(pos);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isCheckByPawn(
            final BasicChessGameInterface game, final Position kingPos, final List<Position> attackerPos, final boolean isWhite) {
        final int possiblePawnRow = isWhite ? kingPos.row + 1 : kingPos.row - 1;

        if (possiblePawnRow < 0 || possiblePawnRow > 7) return false;
        if (kingPos.column != 0) {
            final Position pos = Position.get(possiblePawnRow, kingPos.column - 1);
            final Figure figure = game.getFigure(pos);
            if (figure != null && figure.isWhite() != isWhite && figure.isPawn()) {
                attackerPos.add(pos);
                return true;
            }
        }

        if (kingPos.column != 7) {
            final Position pos = Position.get(possiblePawnRow, kingPos.column + 1);
            final Figure figure = game.getFigure(pos);
            if (figure != null && figure.isWhite() != isWhite && figure.isPawn()) {
                attackerPos.add(pos);
                return true;
            }
        }

        return false;
    }

    private static boolean isCheckByKnight(
            final BasicChessGameInterface game, final Position kingPos, final List<Position> attackerPos, final boolean isWhite) {
        final int minRow = Math.max(0, kingPos.row - 2);
        final int maxRow = Math.min(7, kingPos.row + 2);
        final int minColumn = Math.max(0, kingPos.column - 2);
        final int maxColumn = Math.min(7, kingPos.column + 2);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                final int vertical_dif = Math.abs(kingPos.row - row);
                final int horizontal_dif = Math.abs(kingPos.column - column);
                if (vertical_dif + horizontal_dif == 3) {
                    final Position pos = Position.get(row, column);
                    final Figure figure = game.getFigure(pos);
                    if (figure != null && figure.isWhite() != isWhite && figure.isKnight()) {
                        attackerPos.add(pos);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isCheckByBishopOrQueen(
            final BasicChessGameInterface game, final Position kingPos, final List<Position> attackerPos, final boolean isWhite) {
        int column, row;

        column = kingPos.column + 1;
        row = kingPos.row + 1;
        while (column < 8 && row < 8) {
            final Position pos = Position.get(row, column);
            row++;
            column++;

            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isBishop() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }

        column = kingPos.column - 1;
        row = kingPos.row + 1;
        while (column >= 0 && row < 8) {
            final Position pos = Position.get(row, column);
            row++;
            column--;

            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isBishop() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }

        column = kingPos.column + 1;
        row = kingPos.row - 1;
        while (column < 8 && row >= 0) {
            final Position pos = Position.get(row, column);
            row--;
            column++;

            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isBishop() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }

        column = kingPos.column - 1;
        row = kingPos.row - 1;
        while (column >= 0 && row >= 0) {
            final Position pos = Position.get(row, column);
            row--;
            column--;

            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isBishop() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }
        return false;
    }

    private static boolean isCheckByRockOrQueen(
            final BasicChessGameInterface game, final Position kingPos, final List<Position> attackerPos, final boolean isWhite) {
        if (isDoubleHorizontalCheckAfterPawnTransformation(game, kingPos, attackerPos, isWhite)) return true;

        //da die Bauerntransformation jetzt ausgeschlossen ist,
        //darf nach dem Fund eines Angreifer aufgehört werden
        for (int row = kingPos.row + 1; row < 8; row++) {                        //vertikale Reihe
            final Position pos = Position.get(row, kingPos.column);
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isRock() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }
        for (int row = kingPos.row - 1; row >= 0; row--) {                        //vertikale Reihe
            final Position pos = Position.get(row, kingPos.column);
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isRock() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }
        for (int column = kingPos.column + 1; column < 8; column++) {//horizontale Reihe
            final Position pos = Position.get(kingPos.row, column);
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isRock() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }
        for (int column = kingPos.column - 1; column >= 0; column--) {//horizontale Reihe
            final Position pos = Position.get(kingPos.row, column);
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isRock() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }
        return false;
    }

    private static boolean isDoubleHorizontalCheckAfterPawnTransformation(
            final BasicChessGameInterface game, final Position kingPos, final List<Position> attackerPos, final boolean isWhite) {
        //only possible if the king stod in the columnshadow of a pawn which transformed in the last move to rock or queen
        final int groundRow = isWhite ? 0 : 7;
        if (kingPos.row != groundRow) return false;

        Position kingSideAttackerPos = null;

        if (kingPos.column != 0) {
            final Position leftKingPos = Position.get(groundRow, kingPos.column - 1);
            final Figure leftFromKingFigure = game.getFigure(leftKingPos);
            if (leftFromKingFigure != null && leftFromKingFigure.isWhite() != isWhite &&
                    (leftFromKingFigure.isQueen() || leftFromKingFigure.isRock())) {
                kingSideAttackerPos = leftKingPos;
            }
        }

        if (kingPos.column != 7 && kingSideAttackerPos == null) {
            final Position rightKingPos = Position.get(groundRow, kingPos.column + 1);
            final Figure rightFromKingFigure = game.getFigure(rightKingPos);
            if (rightFromKingFigure != null && rightFromKingFigure.isWhite() != isWhite &&
                    (rightFromKingFigure.isQueen() || rightFromKingFigure.isRock())) {
                kingSideAttackerPos = rightKingPos;
            }
        }

        if (kingSideAttackerPos == null) return false;

        final int rowStep = isWhite ? 1 : -1;
        final int toRow = isWhite ? 7 : 0;

        int row = groundRow;
        do {
            row += rowStep;
            final Position pos = Position.get(row, kingPos.column);
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            //either figure says 'chess' or king is not in danger
            if (figure.isWhite() != isWhite && (figure.isRock() || figure.isQueen())) {
                attackerPos.add(kingSideAttackerPos);
                attackerPos.add(pos);
                return true;
            }
        } while (row != toRow);

        return false;
    }

    final public static int signum(int number) {
        if (number > 0) return 1;
        if (number < 0) return -1;
        return 0;
    }

    final public static boolean areStraightPositions(Position first, Position second) {
        return (first.row - second.row) == 0 || (first.column - second.column) == 0;
    }

    final public static boolean areDiagonalPositions(Position first, Position second) {
        return Math.abs(first.row - second.row) == Math.abs(first.column - second.column);
    }

    final static public boolean areInBetweenPositionsFree(BasicChessGameInterface game, Position first, Position second) {
        final int rowStep = signum(second.row - first.row);
        final int columnStep = signum(second.column - first.column);

        int row = first.row + rowStep;
        int column = first.column + columnStep;
        while (row != second.row || column != second.column) {
            if (!game.isFreeArea(Position.get(row, column))) {
                return false;
            }
            row += rowStep;
            column += columnStep;
        }

        return true;
    }

    final static private List<Position> getInBetweenPositions(Position first, Position second) {
        final int rowStep = signum(second.row - first.row);
        final int columnStep = signum(second.column - first.column);

        //this list might be added another Position later
        final int positionNumberPlusOne = Math.abs(second.row - first.row) + Math.abs(second.column - first.column);

        int row = first.row + rowStep;
        int column = first.column + columnStep;
        final List<Position> middlePositions = new ArrayList<Position>(positionNumberPlusOne);
        while (row != second.row || column != second.column) {
            middlePositions.add(Position.get(row, column));
            row += rowStep;
            column += columnStep;
        }

        return middlePositions;
    }

    final static private Position getPassiveAttacker(BasicChessGameInterface game,
                                                     Position kingPos,
                                                     Position lastMovedFrom) {
        final boolean straightAttack = areStraightPositions(kingPos, lastMovedFrom);
        final boolean diagonalAttack = areDiagonalPositions(kingPos, lastMovedFrom);
        if (!straightAttack && !diagonalAttack) {
            return null;
        }

        final int rowStep = signum(lastMovedFrom.row - kingPos.row);
        final int columnStep = signum(lastMovedFrom.column - kingPos.column);

        int row = lastMovedFrom.row + rowStep;
        int column = lastMovedFrom.column + columnStep;

        while (row >= 0 && column >= 0 && row < 8 && column < 8) {
            final Position pos = Position.get(row, column);
            final Figure figure = game.getFigure(pos);
            if (figure != null) {

                if (figure.isReachable(kingPos, game)) {
                    return pos;
                }
                break;
            }
            row += rowStep;
            column += columnStep;
        }
        return null;
    }
}
