package voidchess.player.ki;

import voidchess.board.BoardContent;
import voidchess.board.ChessGameInterface;
import voidchess.figures.Figure;
import voidchess.helper.Position;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.Ongoing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


class StaticSpaceEvaluation implements StaticEvaluationInterface {

    public Evaluated getPrimaryEvaluation(ChessGameInterface game, boolean forWhite) {
        final int BASIC_VALUE = 200;
        Position whiteKingPos = null;
        Position blackKingPos = null;
        boolean colorOfWinnerIsWhite = true;
        List<Position> figuresPos = new LinkedList<>();

        for (int index = 0; index < 64; index++) {
            Position pos = Position.byIndex(index);
            BoardContent content = game.getContent(pos);
            if (!content.isFreeArea()) {
                Figure figure = content.getFigure();
                if (figure.isKing()) {
                    if (figure.isWhite()) whiteKingPos = pos;
                    else blackKingPos = pos;
                } else {
                    colorOfWinnerIsWhite = figure.isWhite();
                    figuresPos.add(pos);
                }
            }
        }

        double value;
        Position kingPos;
        if (colorOfWinnerIsWhite) {
            figuresPos.add(whiteKingPos);
            kingPos = blackKingPos;
        } else {
            figuresPos.add(blackKingPos);
            kingPos = whiteKingPos;
        }

        value = countRestSpace(game, kingPos, figuresPos);
        value += 3 * borderMalus(kingPos);
        value += 5 * getKingDistance(whiteKingPos, blackKingPos);
        value -= BASIC_VALUE;

        if (colorOfWinnerIsWhite == forWhite) value = -value;

        //TODO is there is better secondary evaluation that a static 0?
        return new Ongoing(value / 10.0);
    }

    @Override
    public void addSecondaryEvaluation(ChessGameInterface game, boolean forWhite, Evaluated withPrimaryEvaluation) {
        //do nothing (keep the secondary value = 0)
        //TODO is there a good secondary matrix of success for space?
    }

    int getKingDistance(Position pos1, Position pos2) {
        //return	Math.max( Math.abs(pos1.row-pos2.row),Math.abs(pos1.column-pos2.column) );
        return Math.abs(pos1.row - pos2.row) + Math.abs(pos1.column - pos2.column);
    }

    double borderMalus(Position kingPos) {
        return -Math.max(Math.abs(kingPos.row - 3.5), Math.abs(kingPos.column - 3.5));
    }

    int countRestSpace(ChessGameInterface game,
                       Position kingPos,
                       List<Position> otherFiguresPos) {
        boolean figuresHaveWhiteColor = !game.getFigure(kingPos).isWhite();
        HashSet<Position> foundPositions = new HashSet<>(64);
        HashSet<Position> searchPositions = new HashSet<>(2);
        foundPositions.add(kingPos);
        searchPositions.add(kingPos);
        while (!searchPositions.isEmpty()) {
            HashSet<Position> newSearchPositions = new HashSet<>(10, 1);
            for (Position search_this : searchPositions) {
                Iterator newIter = getPossibleKingPositions(search_this);
                while (newIter.hasNext()) {
                    Position foundPos = (Position) newIter.next();
                    if (!foundPositions.contains(foundPos) &&
                            isNotReachableByFiguresInList(foundPos,
                                    otherFiguresPos,
                                    game,
                                    figuresHaveWhiteColor)
                    ) {
                        foundPositions.add(foundPos);
                        newSearchPositions.add(foundPos);
                    }
                }
            }
            searchPositions = newSearchPositions;
        }

        return foundPositions.size();
    }

    private boolean isNotReachableByFiguresInList(Position to,
                                                  List<Position> other,
                                                  ChessGameInterface game,
                                                  boolean figuresHaveWhiteColor) {
        for (Object anOther : other) {
            Position from = (Position) anOther;
            if (from.equalsPosition(to)) return false;
            if (game.isMovable(from, to, figuresHaveWhiteColor)) return false;
        }
        return true;
    }

    private Iterator getPossibleKingPositions(Position pos) {
        int minRow = Math.max(0, pos.row - 1);
        int maxRow = Math.min(7, pos.row + 1);
        int minColumn = Math.max(0, pos.column - 1);
        int maxColumn = Math.min(7, pos.column + 1);

        LinkedList<Position> positions = new LinkedList<>();
        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                Position newPos = Position.get(row, column);
                if (newPos.notEqualsPosition(pos)) {
                    positions.add(Position.get(row, column));
                }
            }
        }

        return positions.iterator();
    }

    static boolean shouldUseStaticSpaceEvaluation(ChessGameInterface game) {
        int whiteFigures = 0;
        int blackFigures = 0;
        for (int index = 0; index < 64; index++) {
            BoardContent content = game.getContent(Position.byIndex(index));
            if (!content.isFreeArea()) {
                Figure figure = content.getFigure();
                if (figure.isWhite()) {
                    whiteFigures++;
                } else {
                    blackFigures++;
                }
                if (figure.isPawn() || (whiteFigures > 1 && blackFigures > 1)) return false;
            }
        }
        return true;
    }
}
