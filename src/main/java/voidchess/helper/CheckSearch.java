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
        isCheckByRookOrQueen(game, kingPos, attackPositions, isWhite);

        switch (attackPositions.size()) {
            case 0:
                return CheckStatus.Companion.getNO_CHECK();
            case 1:
                return getPossiblePositions(kingPos, attackPositions.get(0));
            case 2:
                return CheckStatus.Companion.getDOUBLE_CHECK();
            default:
                throw new IllegalStateException("more than 2 attackers are impossible " + attackPositions.size());
        }
    }

    public static CheckStatus analyseCheck(SimpleChessBoardInterface game, boolean whiteInCheck, ExtendedMove lastExtMove) {
        if (lastExtMove == null) {
            return analyseCheck(game, whiteInCheck);
        }

        final Move lastMove = lastExtMove.getMove();
        if (lastExtMove.isEnpassent()) return analyseCheckAfterEnpassent(game, whiteInCheck, lastMove);
        if (lastExtMove.isCastling()) return analyseCheckAfterCastling(game, whiteInCheck, lastMove);
        if (lastExtMove.isPawnTransformation()) return analyseCheckAfterPawnTransform(game, whiteInCheck, lastMove);

        final Position kingPos = game.getKingPosition(whiteInCheck);
        final Figure movedFigure = game.getFigure(lastMove.getTo());
        final List<Position> attackPositions = new ArrayList<Position>(2);

        if (movedFigure.isReachable(kingPos, game)) {
            attackPositions.add(lastMove.getTo());
        }

        Position passiveAttacker = getPassiveAttacker(game, kingPos, lastMove.getFrom());
        if (passiveAttacker != null) {
            attackPositions.add(passiveAttacker);
        }

        switch (attackPositions.size()) {
            case 0:
                return CheckStatus.Companion.getNO_CHECK();
            case 1:
                return getPossiblePositions(kingPos, attackPositions.get(0));
            case 2:
                return CheckStatus.Companion.getDOUBLE_CHECK();
            default:
                throw new IllegalStateException("more than 2 attackers are impossible " + attackPositions.size());
        }
    }

    private static CheckStatus analyseCheckAfterEnpassent(SimpleChessBoardInterface game, boolean whiteInCheck, Move lastMove) {
        final Position kingPos = game.getKingPosition(whiteInCheck);

        final List<Position> attackPositions = new ArrayList<Position>(2);
        final Figure attackFigure = game.getFigure(lastMove.getTo());

        Position passiveAttacker = getPassiveAttacker(game, kingPos, lastMove.getFrom());
        if (passiveAttacker == null) {
            //an attackpath may have opend over the diagonal of the removed pawn
            Position removedPawnPos = Position.Companion.get(lastMove.getFrom().getRow(), lastMove.getTo().getColumn());
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
                return CheckStatus.Companion.getNO_CHECK();
            case 1:
                return getPossiblePositions(kingPos, attackPositions.get(0));
            case 2:
                return CheckStatus.Companion.getDOUBLE_CHECK();
            default:
                throw new IllegalStateException("more than 2 attackers are impossible " + attackPositions.size());
        }
    }

    private static CheckStatus analyseCheckAfterPawnTransform(SimpleChessBoardInterface game, boolean whiteInCheck, Move lastMove) {
        final Position kingPos = game.getKingPosition(whiteInCheck);
        final Figure transformedPawn = game.getFigure(lastMove.getTo());
        final List<Position> attackPositions = new ArrayList<Position>(2);
        final Position passiveAttacker = getPassiveAttacker(game, kingPos, lastMove.getFrom());

        if (transformedPawn.isReachable(kingPos, game)) {
            attackPositions.add(lastMove.getTo());
        }
        if (passiveAttacker != null && !passiveAttacker.equalsPosition(lastMove.getTo())) {
            attackPositions.add(passiveAttacker);
        }

        switch (attackPositions.size()) {
            case 0:
                return CheckStatus.Companion.getNO_CHECK();
            case 1:
                return getPossiblePositions(kingPos, attackPositions.get(0));
            case 2:
                return CheckStatus.Companion.getDOUBLE_CHECK();
            default:
                throw new IllegalStateException("more than 2 attackers are impossible " + attackPositions.size());
        }
    }

    private static CheckStatus analyseCheckAfterCastling(SimpleChessBoardInterface game, boolean whiteInCheck, Move lastMove) {
        final Position kingPos = game.getKingPosition(whiteInCheck);

        final int Rook_row = lastMove.getTo().getRow();
        final int Rook_column = lastMove.getTo().getColumn() == 2 ? 3 : 5;
        final Position RookPos = Position.Companion.get(Rook_row, Rook_column);
        final Figure Rook = game.getFigure(RookPos);

        if (Rook.isReachable(kingPos, game)) return getPossiblePositions(kingPos, RookPos);
        return CheckStatus.Companion.getNO_CHECK();
    }

    private static CheckStatus getPossiblePositions(Position kingPos, Position attackerPos) {
        List<Position> result;
        if (kingPos.isStraightOrDiagonalTo(attackerPos)) {
            //diagonal or straight attack
            result = getInBetweenPositions(attackerPos, kingPos);
            result.add(attackerPos);
        } else {
            //Knight attacks
            result = new ArrayList<Position>(1);
            result.add(attackerPos);
        }
        return new CheckStatus(result);
    }

    public static boolean isCheck(BasicChessGameInterface game, Position kingPos) {
        final boolean isWhite = game.getFigure(kingPos).isWhite();

        final List<Position> attackPositions = new ArrayList<Position>(2);
        if (isCheckByBishopOrQueen(game, kingPos, attackPositions, isWhite)) return true;
        if (isCheckByRookOrQueen(game, kingPos, attackPositions, isWhite)) return true;
        if (isCheckByKnight(game, kingPos, attackPositions, isWhite)) return true;
        if (isCheckByKing(game, kingPos, attackPositions)) return true;
        return isCheckByPawn(game, kingPos, attackPositions, isWhite);
    }

    private static boolean isCheckByKing(
            final BasicChessGameInterface game, final Position kingPos, final List<Position> attackerPos) {
        final int minRow = Math.max(0, kingPos.getRow() - 1);
        final int maxRow = Math.min(7, kingPos.getRow() + 1);
        final int minColumn = Math.max(0, kingPos.getColumn() - 1);
        final int maxColumn = Math.min(7, kingPos.getColumn() + 1);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                if (row != kingPos.getRow() || column != kingPos.getColumn()) {
                    final Position pos = Position.Companion.get(row, column);
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
        final int possiblePawnRow = isWhite ? kingPos.getRow() + 1 : kingPos.getRow() - 1;

        if (possiblePawnRow < 0 || possiblePawnRow > 7) return false;
        if (kingPos.getColumn() != 0) {
            final Position pos = Position.Companion.get(possiblePawnRow, kingPos.getColumn() - 1);
            final Figure figure = game.getFigure(pos);
            if (figure != null && figure.isWhite() != isWhite && figure.isPawn()) {
                attackerPos.add(pos);
                return true;
            }
        }

        if (kingPos.getColumn() != 7) {
            final Position pos = Position.Companion.get(possiblePawnRow, kingPos.getColumn() + 1);
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
        final int minRow = Math.max(0, kingPos.getRow() - 2);
        final int maxRow = Math.min(7, kingPos.getRow() + 2);
        final int minColumn = Math.max(0, kingPos.getColumn() - 2);
        final int maxColumn = Math.min(7, kingPos.getColumn() + 2);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                final int vertical_dif = Math.abs(kingPos.getRow() - row);
                final int horizontal_dif = Math.abs(kingPos.getColumn() - column);
                if (vertical_dif + horizontal_dif == 3) {
                    final Position pos = Position.Companion.get(row, column);
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

        column = kingPos.getColumn() + 1;
        row = kingPos.getRow() + 1;
        while (column < 8 && row < 8) {
            final Position pos = Position.Companion.get(row, column);
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

        column = kingPos.getColumn() - 1;
        row = kingPos.getRow() + 1;
        while (column >= 0 && row < 8) {
            final Position pos = Position.Companion.get(row, column);
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

        column = kingPos.getColumn() + 1;
        row = kingPos.getRow() - 1;
        while (column < 8 && row >= 0) {
            final Position pos = Position.Companion.get(row, column);
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

        column = kingPos.getColumn() - 1;
        row = kingPos.getRow() - 1;
        while (column >= 0 && row >= 0) {
            final Position pos = Position.Companion.get(row, column);
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

    private static boolean isCheckByRookOrQueen(
            final BasicChessGameInterface game, final Position kingPos, final List<Position> attackerPos, final boolean isWhite) {
        if (isDoubleHorizontalCheckAfterPawnTransformation(game, kingPos, attackerPos, isWhite)) return true;

        //da die Bauerntransformation jetzt ausgeschlossen ist,
        //darf nach dem Fund eines Angreifer aufgehört werden
        for (int row = kingPos.getRow() + 1; row < 8; row++) {                        //vertikale Reihe
            final Position pos = Position.Companion.get(row, kingPos.getColumn());
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isRook() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }
        for (int row = kingPos.getRow() - 1; row >= 0; row--) {                        //vertikale Reihe
            final Position pos = Position.Companion.get(row, kingPos.getColumn());
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isRook() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }
        for (int column = kingPos.getColumn() + 1; column < 8; column++) {//horizontale Reihe
            final Position pos = Position.Companion.get(kingPos.getRow(), column);
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isRook() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }
        for (int column = kingPos.getColumn() - 1; column >= 0; column--) {//horizontale Reihe
            final Position pos = Position.Companion.get(kingPos.getRow(), column);
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            if (figure.isWhite() == isWhite) break;
            if (figure.isRook() || figure.isQueen()) {
                attackerPos.add(pos);
                return true;
            }
            break;
        }
        return false;
    }

    private static boolean isDoubleHorizontalCheckAfterPawnTransformation(
            final BasicChessGameInterface game, final Position kingPos, final List<Position> attackerPos, final boolean isWhite) {
        //only possible if the king stod in the columnshadow of a pawn which transformed in the last move to Rook or queen
        final int groundRow = isWhite ? 0 : 7;
        if (kingPos.getRow() != groundRow) return false;

        Position kingSideAttackerPos = null;

        if (kingPos.getColumn() != 0) {
            final Position leftKingPos = Position.Companion.get(groundRow, kingPos.getColumn() - 1);
            final Figure leftFromKingFigure = game.getFigure(leftKingPos);
            if (leftFromKingFigure != null && leftFromKingFigure.isWhite() != isWhite &&
                    (leftFromKingFigure.isQueen() || leftFromKingFigure.isRook())) {
                kingSideAttackerPos = leftKingPos;
            }
        }

        if (kingPos.getColumn() != 7 && kingSideAttackerPos == null) {
            final Position rightKingPos = Position.Companion.get(groundRow, kingPos.getColumn() + 1);
            final Figure rightFromKingFigure = game.getFigure(rightKingPos);
            if (rightFromKingFigure != null && rightFromKingFigure.isWhite() != isWhite &&
                    (rightFromKingFigure.isQueen() || rightFromKingFigure.isRook())) {
                kingSideAttackerPos = rightKingPos;
            }
        }

        if (kingSideAttackerPos == null) return false;

        final int rowStep = isWhite ? 1 : -1;
        final int toRow = isWhite ? 7 : 0;

        int row = groundRow;
        do {
            row += rowStep;
            final Position pos = Position.Companion.get(row, kingPos.getColumn());
            final Figure figure = game.getFigure(pos);
            if (figure == null) continue;
            //either figure says 'chess' or king is not in danger
            if (figure.isWhite() != isWhite && (figure.isRook() || figure.isQueen())) {
                attackerPos.add(kingSideAttackerPos);
                attackerPos.add(pos);
                return true;
            }
        } while (row != toRow);

        return false;
    }

    public static int signum(int number) {
        return Integer.compare(number, 0);
    }

    static private List<Position> getInBetweenPositions(Position first, Position second) {
        final int rowStep = signum(second.getRow() - first.getRow());
        final int columnStep = signum(second.getColumn() - first.getColumn());

        //this list might be added another Position later
        final int positionNumberPlusOne = Math.abs(second.getRow() - first.getRow()) + Math.abs(second.getColumn() - first.getColumn());

        int row = first.getRow() + rowStep;
        int column = first.getColumn() + columnStep;
        final List<Position> middlePositions = new ArrayList<Position>(positionNumberPlusOne);
        while (row != second.getRow() || column != second.getColumn()) {
            middlePositions.add(Position.Companion.get(row, column));
            row += rowStep;
            column += columnStep;
        }

        return middlePositions;
    }

    static private Position getPassiveAttacker(BasicChessGameInterface game,
                                                     Position kingPos,
                                                     Position lastMovedFrom) {
        final boolean straightAttack = kingPos.isStraightTo(lastMovedFrom);
        final boolean diagonalAttack = kingPos.isDiagonalTo(lastMovedFrom);
        if (!straightAttack && !diagonalAttack) {
            return null;
        }

        final int rowStep = signum(lastMovedFrom.getRow() - kingPos.getRow());
        final int columnStep = signum(lastMovedFrom.getColumn() - kingPos.getColumn());

        int row = lastMovedFrom.getRow() + rowStep;
        int column = lastMovedFrom.getColumn() + columnStep;

        while (row >= 0 && column >= 0 && row < 8 && column < 8) {
            final Position pos = Position.Companion.get(row, column);
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
