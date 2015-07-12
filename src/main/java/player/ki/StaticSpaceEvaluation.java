package player.ki;

import board.ChessGameInterface;
import figures.Figure;
import helper.Position;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author stephan
 */
class StaticSpaceEvaluation implements StaticEvaluationInterface {

    public Evaluaded evaluate(ChessGameInterface game, boolean forWhite) {
        final int BASIC_VALUE = 200;
        Position whiteKingPos = null;
        Position blackKingPos = null;
        boolean colorOfWinnerIsWhite = true;
        List figuresPos = new LinkedList();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Position pos = Position.get(row, column);
                if (!game.isFreeArea(pos)) {
                    Figure figure = game.getFigure(pos);
                    if (figure.isKing()) {
                        if (figure.isWhite()) whiteKingPos = pos;
                        else blackKingPos = pos;
                    } else {
                        colorOfWinnerIsWhite = figure.isWhite();
                        figuresPos.add(pos);
                    }
                }
            }
        }

        float value = 0;
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
        return Evaluaded.fromValues(value / 10.0f, 0);
    }

    int getKingDistance(Position pos1, Position pos2) {
        //return	Math.max( Math.abs(pos1.row-pos2.row),Math.abs(pos1.column-pos2.column) );
        return Math.abs(pos1.row - pos2.row) + Math.abs(pos1.column - pos2.column);
    }

    float borderMalus(Position kingPos) {
        return -Math.max(Math.abs(kingPos.row - 3.5f), Math.abs(kingPos.column - 3.5f));
    }

    int countRestSpace(ChessGameInterface game,
                       Position kingPos,
                       List otherFiguresPos) {
        boolean figuresHaveWhiteColor = !game.getFigure(kingPos).isWhite();
        HashSet foundPositions = new HashSet(64);
        HashSet searchPositions = new HashSet(2);
        foundPositions.add(kingPos);
        searchPositions.add(kingPos);
        while (!searchPositions.isEmpty()) {
            HashSet newSearchPositions = new HashSet(10, 1);
            Iterator iter = searchPositions.iterator();
            while (iter.hasNext()) {
                Position search_this = (Position) iter.next();
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
                                                  List other,
                                                  ChessGameInterface game,
                                                  boolean figuresHaveWhiteColor) {
        Iterator iter = other.iterator();
        while (iter.hasNext()) {
            Position from = (Position) iter.next();
            if (from.equalsPosition(to)) return false;
            if (game.isMoveable(from, to, figuresHaveWhiteColor)) return false;
        }
        return true;
    }

    private Iterator getPossibleKingPositions(Position pos) {
        int minRow = Math.max(0, pos.row - 1);
        int maxRow = Math.min(7, pos.row + 1);
        int minColumn = Math.max(0, pos.column - 1);
        int maxColumn = Math.min(7, pos.column + 1);

        LinkedList positions = new LinkedList();
        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                Position newPos = Position.get(row, column);
                if (!newPos.equalsPosition(pos)) {
                    positions.add(Position.get(row, column));
                }
            }
        }

        return positions.iterator();
    }

    static boolean shouldUseStaticSpaceEvaluation(ChessGameInterface game) {
        int whiteFigures = 0;
        int blackFigures = 0;
        for (int column = 0; column < 8; column++) {
            for (int row = 0; row < 8; row++) {
                Position pos = Position.get(row, column);
                if (!game.isFreeArea(pos)) {
                    Figure figure = game.getFigure(pos);
                    if (figure.isWhite()) {
                        whiteFigures++;
                    } else {
                        blackFigures++;
                    }
                    if (figure.isPawn() || (whiteFigures > 1 && blackFigures > 1)) return false;
                }
            }
        }
        return true;
    }
}
