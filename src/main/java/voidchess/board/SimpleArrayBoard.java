package voidchess.board;

import voidchess.figures.Figure;
import voidchess.figures.FigureFactory;
import voidchess.helper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SimpleArrayBoard
        implements SimpleChessBoardInterface {
    final private Figure[] game;
    final private LastMoveProvider lastMoveProvider;
    final private FigureFactory figureFactory;

    private Position whiteKingPosition;
    private Position blackKingPosition;

    private boolean calculatedWhiteCheck;
    private boolean calculatedBlackCheck;
    private boolean isWhiteCheck;
    private boolean isBlackCheck;
    private CheckStatus whiteCheckStatus;
    private CheckStatus blackCheckStatus;

    SimpleArrayBoard(LastMoveProvider lastMoveProvider) {
        figureFactory = new FigureFactory();
        game = new Figure[64];
        this.lastMoveProvider = lastMoveProvider;
        init();
    }

    //für Testzwecke
    public SimpleArrayBoard(String des, LastMoveProvider lastMoveProvider) {
        this(lastMoveProvider);
        init(des);
    }

    private void clearCheckComputation() {
        calculatedWhiteCheck = false;
        calculatedBlackCheck = false;
        whiteCheckStatus = null;
        blackCheckStatus = null;
    }

    @Override
    public boolean isCheck(boolean isWhite) {
        if (isWhite) {
            if (!calculatedWhiteCheck) {
                isWhiteCheck = CheckSearch.isCheck(this, getKingPosition(isWhite));
                calculatedWhiteCheck = true;
            }
            return isWhiteCheck;
        } else {
            if (!calculatedBlackCheck) {
                isBlackCheck = CheckSearch.isCheck(this, getKingPosition(isWhite));
                calculatedBlackCheck = true;
            }
            return isBlackCheck;
        }
    }

    @Override
    public CheckStatus getCheckStatus(boolean isWhite) {
        final ExtendedMove lastMove = lastMoveProvider.getLastMove();
        if (isWhite) {
            if (whiteCheckStatus == null) {
                whiteCheckStatus = CheckSearch.analyseCheck(this, isWhite, lastMove);
            }
            return whiteCheckStatus;
        } else {
            if (blackCheckStatus == null) {
                blackCheckStatus = CheckSearch.analyseCheck(this, isWhite, lastMove);
            }
            return blackCheckStatus;
        }
    }

    private Position getPositionOfCodedFigure(String figure_description) {
        StringTokenizer st = new StringTokenizer(figure_description, "-", false);
        st.nextToken();
        st.nextToken();
        return Position.Companion.byCode(st.nextToken());
    }

    @Override
    public void init() {
        clear();
        Position pos;

        for (int i = 0; i < 8; i++) {
            pos = Position.Companion.get(1, i);
            setFigure(pos, figureFactory.getPawn(pos, true));
            pos = Position.Companion.get(6, i);
            setFigure(pos, figureFactory.getPawn(pos, false));
        }
        pos = Position.Companion.byCode("a1");
        setFigure(pos, figureFactory.getRook(pos, true));
        pos = Position.Companion.byCode("h1");
        setFigure(pos, figureFactory.getRook(pos, true));
        pos = Position.Companion.byCode("b1");
        setFigure(pos, figureFactory.getKnight(pos, true));
        pos = Position.Companion.byCode("g1");
        setFigure(pos, figureFactory.getKnight(pos, true));
        pos = Position.Companion.byCode("c1");
        setFigure(pos, figureFactory.getBishop(pos, true));
        pos = Position.Companion.byCode("f1");
        setFigure(pos, figureFactory.getBishop(pos, true));
        pos = Position.Companion.byCode("d1");
        setFigure(pos, figureFactory.getQueen(pos, true));
        pos = Position.Companion.byCode("e1");
        setFigure(pos, figureFactory.getKing(pos, true));
        whiteKingPosition = pos;

        pos = Position.Companion.byCode("a8");
        setFigure(pos, figureFactory.getRook(pos, false));
        pos = Position.Companion.byCode("h8");
        setFigure(pos, figureFactory.getRook(pos, false));
        pos = Position.Companion.byCode("b8");
        setFigure(pos, figureFactory.getKnight(pos, false));
        pos = Position.Companion.byCode("g8");
        setFigure(pos, figureFactory.getKnight(pos, false));
        pos = Position.Companion.byCode("c8");
        setFigure(pos, figureFactory.getBishop(pos, false));
        pos = Position.Companion.byCode("f8");
        setFigure(pos, figureFactory.getBishop(pos, false));
        pos = Position.Companion.byCode("d8");
        setFigure(pos, figureFactory.getQueen(pos, false));
        pos = Position.Companion.byCode("e8");
        setFigure(pos, figureFactory.getKing(pos, false));
        blackKingPosition = pos;
    }

    @Override
    public void init(String des) {
        clear();

        StringTokenizer st = new StringTokenizer(des, " ", false);
        st.nextToken();
        st.nextToken();

        while (st.hasMoreTokens()) {
            String figure_description = st.nextToken();
            Position pos = getPositionOfCodedFigure(figure_description);
            Figure figure = figureFactory.getFigureByString(figure_description);
            setFigure(pos, figure);
        }
    }

    @Override
    public void init(int chess960) {
        assert (chess960 >= 0 && chess960 < 960) : "chess960 out of bounds";

        clear();
        Position pos;

        //die Bauernpositionen verändern sich nicht
        for (int i = 0; i < 8; i++) {
            pos = Position.Companion.get(1, i);
            setFigure(pos, figureFactory.getPawn(pos, true));
            pos = Position.Companion.get(6, i);
            setFigure(pos, figureFactory.getPawn(pos, false));
        }

        /*erster Läufer*/
        int rest = chess960 % 4;
        int row = rest * 2 + 1;
        chess960 = chess960 / 4;

        pos = Position.Companion.get(0, row);
        setFigure(pos, figureFactory.getBishop(pos, true));
        pos = Position.Companion.get(7, row);
        setFigure(pos, figureFactory.getBishop(pos, false));

        /*zweiter Läufer*/
        rest = chess960 % 4;
        row = rest * 2;
        chess960 = chess960 / 4;

        pos = Position.Companion.get(0, row);
        setFigure(pos, figureFactory.getBishop(pos, true));
        pos = Position.Companion.get(7, row);
        setFigure(pos, figureFactory.getBishop(pos, false));

        /*zweiter Dame*/
        rest = chess960 % 6;
        row = getFreeRow(rest);
        chess960 = chess960 / 6;

        pos = Position.Companion.get(0, row);
        setFigure(pos, figureFactory.getQueen(pos, true));
        pos = Position.Companion.get(7, row);
        setFigure(pos, figureFactory.getQueen(pos, false));

        String[] otherFigures = getFigureArray(chess960);
        for (int i = 0; i < 5; i++) {
            //immer die erste noch freie Spalte
            row = getFreeRow(0);
            pos = Position.Companion.get(0, row);
            Figure figure = createFigure(otherFigures[i], true, pos);
            setFigure(pos, figure);
            if (figure.isKing()) {
                whiteKingPosition = pos;
            }
            pos = Position.Companion.get(7, row);
            figure = createFigure(otherFigures[i], false, pos);
            setFigure(pos, figure);
            if (figure.isKing()) {
                blackKingPosition = pos;
            }
        }
    }

    private String[] getFigureArray(int index) {
        assert index >= 0 && index < 10;

        switch (index) {
            case 0:
                return new String[]{"Springer", "Springer", "Turm", "König", "Turm"};
            case 1:
                return new String[]{"Springer", "Turm", "Springer", "König", "Turm"};
            case 2:
                return new String[]{"Springer", "Turm", "König", "Springer", "Turm"};
            case 3:
                return new String[]{"Springer", "Turm", "König", "Turm", "Springer"};
            case 4:
                return new String[]{"Turm", "Springer", "Springer", "König", "Turm"};
            case 5:
                return new String[]{"Turm", "Springer", "König", "Springer", "Turm"};
            case 6:
                return new String[]{"Turm", "Springer", "König", "Turm", "Springer"};
            case 7:
                return new String[]{"Turm", "König", "Springer", "Springer", "Turm"};
            case 8:
                return new String[]{"Turm", "König", "Springer", "Turm", "Springer"};
            case 9:
                return new String[]{"Turm", "König", "Turm", "Springer", "Springer"};
            default:
                throw new IllegalArgumentException("index should be between [0-9] but is "+ index);
        }
    }

    private Figure createFigure(String name, boolean isWhite, Position pos) {
        if (name.equals("Turm")) return figureFactory.getRook(pos, isWhite);
        if (name.equals("Springer")) return figureFactory.getKnight(pos, isWhite);
        if (name.equals("König")) return figureFactory.getKing(pos, isWhite);

        throw new IllegalStateException("unbekannte Figure:" + name);
    }

    private int getFreeRow(int index) {
        assert index >= 0 && index < 8;

        int counter = 0;
        for (int row = 0; row < 8; row++) {
            if (isFreeArea(Position.Companion.get(0, row))) {
                if (index == counter) return row;
                else counter++;
            }
        }
        throw new RuntimeException("No free Position with index " + index + " found");
    }

    private void clear() {
        clearCheckComputation();
        for (int linearIndex = 0; linearIndex < 64; linearIndex++) {
            game[linearIndex] = null;
        }
        whiteKingPosition = null;
        blackKingPosition = null;
    }

    @Override
    public void setFigure(Position pos, Figure figure) {
        clearCheckComputation();

        game[pos.getIndex()] = figure;
        if (figure != null && figure.isKing()) {
            if (figure.isWhite()) whiteKingPosition = pos;
            else blackKingPosition = pos;
        }
    }

    @Override
    public Figure move(Figure figure, Position to) {
        Move move = Move.Companion.get(figure.getPosition(), to);
        game[figure.getPosition().getIndex()] = null;
        Figure figureTaken = game[to.getIndex()];
        game[to.getIndex()] = figure;
        figure.figureMoved(move);
        return figureTaken;
    }

    @Override
    public void undoMove(Figure figure, Position from, Figure figureTaken) {
        game[figure.getPosition().getIndex()] = figureTaken;
        game[from.getIndex()] = figure;
        figure.undoMove(from);
    }

    @Override
    public Figure getFigure(Position pos) {
        return game[pos.getIndex()];
    }

    @Override
    public boolean isFreeArea(Position pos) {
        return game[pos.getIndex()] == null;
    }

    @Override
    public BoardContent getContent(Position pos) {
        return BoardContent.Companion.get(game[pos.getIndex()]);
    }

    @Override
    public List<Figure> getFigures() {
        List<Figure> figureIter = new ArrayList<>(16);

        for (int linearIndex = 0; linearIndex < 64; linearIndex++) {
            if (game[linearIndex] != null) {
                figureIter.add(game[linearIndex]);
            }
        }
        return figureIter;
    }

    @Override
    public Position getKingPosition(boolean whiteKing) {
        if (whiteKing) return whiteKingPosition;
        else return blackKingPosition;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(512);
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Position pos = Position.Companion.get(row, column);
                BoardContent content = getContent(pos);
                if (!content.isFreeArea()) {
                    buffer.append(content.getFigure().toString());
                    buffer.append(" ");
                }
            }
        }
        //delete the final space
        if (buffer.length() != 0) buffer.deleteCharAt(buffer.length() - 1);

        return buffer.toString();
    }
}
