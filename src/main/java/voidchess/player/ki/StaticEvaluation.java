package voidchess.player.ki;

import voidchess.board.ChessGameInterface;
import voidchess.figures.Figure;
import voidchess.figures.King;
import voidchess.figures.Pawn;
import voidchess.helper.Position;
import voidchess.player.ki.evaluation.CheckmateSelf;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.Ongoing;

import java.util.List;

/**
 * @author stephan
 */
public class StaticEvaluation implements StaticEvaluationInterface {
    final private static double PAWN_VALUE = 1.0;
    final private static double Rook_VALUE = 4.5;
    final private static double KNIGHT_VALUE = 3.0;
    final private static double BISHOP_VALUE = 3.0;
    final private static double QUEEN_VALUE = 9.0;

    @Override
    public Evaluated getPrimaryEvaluation(ChessGameInterface game, final boolean forWhite) {
        double primaryEvaluation = evaluateFigures(game, forWhite);
        return new Ongoing(primaryEvaluation);
    }

    @Override
    public void addSecondaryEvaluation(ChessGameInterface game, boolean forWhite, Evaluated withPrimaryEvaluation) {
        if (withPrimaryEvaluation.needsSecondaryEvaluation()) {
            if(withPrimaryEvaluation instanceof Ongoing) {
                withPrimaryEvaluation.setSecondaryEvaluation(getSecondaryEvaluation(game, forWhite));
            }else if(withPrimaryEvaluation instanceof CheckmateSelf) {
                withPrimaryEvaluation.setSecondaryEvaluation(
                        evaluateFigures(game, forWhite)
                        + getSecondaryEvaluation(game, forWhite)
                );
            }else{
                throw new IllegalStateException("unexpected class which requests a secondary evaluation: "+ withPrimaryEvaluation.getClass().getSimpleName());
            }
        }
    }

    private double getSecondaryEvaluation(ChessGameInterface game, final boolean forWhite) {
        return evaluateRuledArea(game, forWhite) + evaluatePosition(game, forWhite);
    }

    private double evaluateFigures(ChessGameInterface game, final boolean forWhite) {
        double whiteFigures = 0;
        double blackFigures = 0;

        for (Figure figure : game.getFigures()) {
            if (figure.isWhite()) {
                if (figure.isPawn()) {
                    whiteFigures += PAWN_VALUE;
                } else if (figure.isRook()) {
                    whiteFigures += Rook_VALUE;
                } else if (figure.isKnight()) {
                    whiteFigures += KNIGHT_VALUE;
                } else if (figure.isBishop()) {
                    whiteFigures += BISHOP_VALUE;
                } else if (figure.isQueen()) {
                    whiteFigures += QUEEN_VALUE;
                }
            } else {
                if (figure.isPawn()) {
                    blackFigures += PAWN_VALUE;
                } else if (figure.isRook()) {
                    blackFigures += Rook_VALUE;
                } else if (figure.isKnight()) {
                    blackFigures += KNIGHT_VALUE;
                } else if (figure.isBishop()) {
                    blackFigures += BISHOP_VALUE;
                } else if (figure.isQueen()) {
                    blackFigures += QUEEN_VALUE;
                }
            }
        }

        if (forWhite) return whiteFigures - blackFigures;
        else return blackFigures - whiteFigures;
    }

    private double evaluateRuledArea(ChessGameInterface game, final boolean forWhite) {
        final double VALUE_OF_AREA = 0.015;
        final double whiteMoves = game.countReachableMoves(true);
        final double blackMoves = game.countReachableMoves(false);

        double difference;
        if (forWhite) difference = whiteMoves - blackMoves;
        else difference = blackMoves - whiteMoves;

        return difference * VALUE_OF_AREA;
    }

    private double evaluatePosition(ChessGameInterface game, final boolean forWhite) {
        double whiteEvaluation = 0.0;
        double blackEvaluation = 0.0;

        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            Position pos = figure.getPosition();
            if (figure.isPawn()) {
                if (figure.isWhite()) whiteEvaluation += evaluatePawn(game, pos);
                else blackEvaluation += evaluatePawn(game, pos);
            } else if (figure.isKing()) {
                if (figure.isWhite()) whiteEvaluation += evaluateKing(game, pos);
                else blackEvaluation += evaluateKing(game, pos);
            } else if (figure.isKnight()) {
                if (figure.isWhite()) whiteEvaluation += evaluateKnight(game, pos);
                else blackEvaluation += evaluateKnight(game, pos);
            } else if (figure.isBishop()) {
                if (figure.isWhite()) whiteEvaluation += evaluateBishop(game, pos);
                else blackEvaluation += evaluateBishop(game, pos);
            }
        }

        if (forWhite) return whiteEvaluation - blackEvaluation;
        else return blackEvaluation - whiteEvaluation;
    }

    final static double BISHOP_ON_STARTPOSITION_PUNISHMENT = -0.45;
    final static double BISHOP_BLOCKS_MIDDLE_PAWN_PUNISHMENT = -0.2;

    private double evaluateBishop(ChessGameInterface game, Position pos) {


        final boolean isWhite = game.getFigure(pos).isWhite();
        final int startRow = isWhite ? 0 : 7;

        if (pos.getRow() == startRow &&
                (pos.getColumn() == 2 || pos.getColumn() == 5)) {
            return BISHOP_ON_STARTPOSITION_PUNISHMENT;
        }


        final int blockingRow = isWhite ? 2 : 5;
        final int blockedPawnRow = isWhite ? 1 : 6;
        Position possiblePawn = Position.get(blockedPawnRow, pos.getColumn());

        if (pos.getRow() == blockingRow &&
                (pos.getColumn() == 3 || pos.getColumn() == 4) &&
                containsFigureOfColor(game, Pawn.class, possiblePawn, isWhite)) {
            return BISHOP_BLOCKS_MIDDLE_PAWN_PUNISHMENT;
        }

        return 0;
    }

    final static double BORDER_KNIGHT_PUNISHMENT = -0.45;

    private double evaluateKnight(ChessGameInterface game, Position pos) {

        if (pos.getRow() == 0 || pos.getRow() == 7 || pos.getColumn() == 0 || pos.getColumn() == 7) {
            return BORDER_KNIGHT_PUNISHMENT;
        }
        return 0;
    }

    private double evaluatePawn(ChessGameInterface game, Position pos) {
        double pawnValue = evaluatePawnPosition(game, pos);
        pawnValue += evaluatePawnDefense(game, pos);

        return pawnValue;
    }


    final static double MOVES_GONE_VALUE = 0.2;

    private double evaluatePawnPosition(ChessGameInterface game, Position pos) {
        double additionalPawnValue = 0;

        Figure pawn = game.getFigure(pos);
        int startRow = pawn.isWhite() ? 1 : 6;
        int movesGone = Math.abs(pos.getRow() - startRow);
        additionalPawnValue += movesGone * MOVES_GONE_VALUE;

        return additionalPawnValue;
    }

    final static double DEFENSE_VALUE = 0.10;                 //Bonuswert, wenn Bauer anderen Bauern deckt,bzw.gedeckt wird
    final static double NEXT_TO_VALUE = 0.06;                 //Bonuswert, wenn Bauer einen Nachbarn hat
    final static double UNPROTECTED_BORDER_PAWN_VALUE = -0.2; //Bestrafung für ungedeckte Bauern am Rand

    private double evaluatePawnDefense(ChessGameInterface game, Position pos) {


        Figure pawn = game.getFigure(pos);
        final boolean isWhite = pawn.isWhite();

        int forwardRow = isWhite ? pos.getRow() + 1 : pos.getRow() - 1;
        int backwardRow = isWhite ? pos.getRow() - 1 : pos.getRow() + 1;

        if (pos.getColumn() != 0) {
            Position leftForwardPosition = Position.get(forwardRow, pos.getColumn() - 1);
            Position leftBackwardPosition = Position.get(backwardRow, pos.getColumn() - 1);
            Position leftPosition = Position.get(pos.getRow(), pos.getColumn() - 1);
            if (containsFigureOfColor(game, Pawn.class, leftForwardPosition, isWhite)) {
                return DEFENSE_VALUE;
            }
            if (containsFigureOfColor(game, Pawn.class, leftBackwardPosition, isWhite)) {
                return DEFENSE_VALUE;
            }
            if (containsFigureOfColor(game, Pawn.class, leftPosition, isWhite)) {
                return NEXT_TO_VALUE;
            }
        } else {
            Position rightPosition = Position.get(pos.getRow(), 1);
            Position rightForwardPosition = Position.get(forwardRow, 1);
            Position rightBackwardPosition = Position.get(backwardRow, 1);
            if (!(containsFigureOfColor(game, Pawn.class, rightPosition, isWhite)
                    || containsFigureOfColor(game, Pawn.class, rightForwardPosition, isWhite)
                    || containsFigureOfColor(game, Pawn.class, rightBackwardPosition, isWhite)
            )) {
                return UNPROTECTED_BORDER_PAWN_VALUE;
            }
        }

        if (pos.getColumn() != 7) {
            Position rightForwardPosition = Position.get(forwardRow, pos.getColumn() + 1);
            Position rightBackwardPosition = Position.get(backwardRow, pos.getColumn() + 1);
            Position rightPosition = Position.get(pos.getRow(), pos.getColumn() + 1);
            if (containsFigureOfColor(game, Pawn.class, rightForwardPosition, isWhite)) {
                return DEFENSE_VALUE;
            }
            if (containsFigureOfColor(game, Pawn.class, rightBackwardPosition, isWhite)) {
                return DEFENSE_VALUE;
            }
            if (containsFigureOfColor(game, Pawn.class, rightPosition, isWhite)) {
                return NEXT_TO_VALUE;
            }
        } else {
            Position leftPosition = Position.get(pos.getRow(), 6);
            Position leftForwardPosition = Position.get(forwardRow, 6);
            Position leftBackwardPosition = Position.get(backwardRow, 6);
            if (!(containsFigureOfColor(game, Pawn.class, leftPosition, isWhite)
                    || containsFigureOfColor(game, Pawn.class, leftForwardPosition, isWhite)
                    || containsFigureOfColor(game, Pawn.class, leftBackwardPosition, isWhite)
            )) {
                return UNPROTECTED_BORDER_PAWN_VALUE;
            }
        }

        return 0;
    }

    private boolean containsFigureOfColor(ChessGameInterface game, Class clazz, Position pos, final boolean white) {
        if (game.isFreeArea(pos)) return false;
        Figure figure = game.getFigure(pos);
        return figure.isWhite() == white && clazz.isInstance(figure);
    }


    private double evaluateKing(ChessGameInterface game, Position pos) {
        double value = evaluateCastling(game, pos);
        value += evaluateKingDefense(game, pos);
        return value;
    }

    final static double NO_ROCHADE_PUNISHMENT = -0.4;

    private double evaluateCastling(ChessGameInterface game, Position pos) {

        King king = (King) game.getFigure(pos);

        if (gameStillContainsQueenOfColor(game, !king.isWhite())
                && !king.didCastling()) {
            return NO_ROCHADE_PUNISHMENT;
        }
        return 0.0;
    }

    final static double BIG_KING_DEFENSE_VALUE = 0.6;
    final static double SMALL_KING_DEFENSE_VALUE = 0.2;

    private double evaluateKingDefense(ChessGameInterface game, Position pos) {


        King king = (King) game.getFigure(pos);
        final boolean isWhite = king.isWhite();
        double value = 0.0;
        int groundRow = isWhite ? 0 : 7;
        int secondRow = isWhite ? 1 : 6;

        if (king.didCastling() && pos.getRow() == groundRow) {
            int minColumn = Math.max(0, pos.getRow() - 1);
            int maxColumn = Math.min(7, pos.getRow() + 1);
            for (int column = minColumn; column <= maxColumn; column++) {
                Position infrontOfKingPos = Position.get(secondRow, column);
                if (containsFigureOfColor(game, Pawn.class, infrontOfKingPos, isWhite)) {
                    if (gameStillContainsQueenOfColor(game, !isWhite)) {
                        value += BIG_KING_DEFENSE_VALUE;
                    } else {
                        value += SMALL_KING_DEFENSE_VALUE;
                    }
                }
            }
        }

        return value;
    }

    private boolean gameStillContainsQueenOfColor(ChessGameInterface game, final boolean white) {
        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            if (figure.isWhite() == white && figure.isQueen()) {
                return true;
            }
        }

        return false;
    }
}
