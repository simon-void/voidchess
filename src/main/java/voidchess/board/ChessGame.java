package voidchess.board;

import voidchess.figures.Figure;
import voidchess.figures.FigureFactory;
import voidchess.figures.King;
import voidchess.figures.Pawn;
import voidchess.figures.Rook;
import voidchess.helper.CheckSearch;
import voidchess.helper.ChessGameSupervisor;
import voidchess.helper.ChessGameSupervisorDummy;
import voidchess.helper.ExtendedMove;
import voidchess.helper.Move;
import voidchess.helper.PawnPromotion;
import voidchess.helper.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author stephan
 */
public class ChessGame implements ChessGameInterface, LastMoveProvider {
    private SimpleChessBoardInterface game;
    private FigureFactory figureFactory;
    private boolean whiteTurn;
    private MementoStack mementoStack;
    private ExtendedMoveStack extendedMoveStack;
    private NumberStack numberStack;
    private int numberOfMovesWithoutHit;
    private int figureCount;
    private boolean hasHitFigure;
    private ChessGameSupervisor supervisor;
    private boolean isStandardGame = false;

    /**
     * der normale Konstruktor, der von außerhalb verwendet werden sollte
     */
    public ChessGame(ChessGameSupervisor supervisor) {
        hasHitFigure = false;
        this.supervisor = supervisor;
        figureFactory = new FigureFactory();
        mementoStack = new MementoStack();
        extendedMoveStack = new ExtendedMoveStack();
        numberStack = new NumberStack();
        game = new SimpleArrayBoard(this);

        initGame();
    }

    /**
     * für CopyConstructor
     */
    public ChessGame(ChessGame other, String desc) {
        hasHitFigure = other.hasHitFigure;
        supervisor = ChessGameSupervisorDummy.INSTANCE;
        figureFactory = new FigureFactory();
        mementoStack = new MementoStack(other.mementoStack);
        extendedMoveStack = new ExtendedMoveStack(other.extendedMoveStack);
        numberStack = new NumberStack(other.numberStack);
        game = new SimpleArrayBoard(desc, this);

        whiteTurn = other.whiteTurn;
        numberOfMovesWithoutHit = other.numberOfMovesWithoutHit;
        figureCount = other.figureCount;
    }

    /**
     * für JUnit-TestKlassen
     */
    public ChessGame(String game_description) {
        this(ChessGameSupervisorDummy.INSTANCE, game_description);
    }

    /**
     * für JUnit-TestKlassen
     */
    public ChessGame(int initialPosition) {
        this(ChessGameSupervisorDummy.INSTANCE, initialPosition);
    }

    /**
     * für JUnit-TestKlassen: Standardaufstellung
     */
    public ChessGame() {
        this(518);
    }

    /**
     * wird nur implizit für JUnit-tests verwendet
     */
    private ChessGame(ChessGameSupervisor supervisor, String game_description) {
        this.supervisor = supervisor;
        figureFactory = new FigureFactory();
        mementoStack = new MementoStack();
        extendedMoveStack = new ExtendedMoveStack();
        numberStack = new NumberStack();

        StringTokenizer st = new StringTokenizer(game_description, " ", false);
        whiteTurn = st.nextToken().equals("white");
        numberOfMovesWithoutHit = Integer.parseInt(st.nextToken());
        for (int i = 0; i < numberOfMovesWithoutHit; i++) numberStack.noFigureHit();

        figureCount = 0;
        while (st.hasMoreTokens()) {
            figureCount++;
            st.nextToken();
        }

        game = new SimpleArrayBoard(this);
        game.init(game_description);

        memorizeGame();
        hasHitFigure = numberOfMovesWithoutHit == 0;
    }

    /**
     * wird nur implizit für JUnit-tests verwendet
     */
    private ChessGame(ChessGameSupervisor supervisor,
                      int initialPosition) {
        this.supervisor = supervisor;
        figureFactory = new FigureFactory();
        mementoStack = new MementoStack();
        extendedMoveStack = new ExtendedMoveStack();
        numberStack = new NumberStack();

        whiteTurn = true;
        hasHitFigure = false;
        numberOfMovesWithoutHit = 0;
        figureCount = 32;

        game = new SimpleArrayBoard(this);
        game.init(initialPosition);

        memorizeGame();
    }

    @Override
    public void useSupervisor(ChessGameSupervisor supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public ChessGameSupervisor suspendInteractiveSupervisor() {
        ChessGameSupervisor normalSupervisor = supervisor;
        supervisor = ChessGameSupervisorDummy.INSTANCE;
        return normalSupervisor;
    }

    @Override
    public ExtendedMove getLastMove() {
        if (extendedMoveStack.isEmpty()) return null;
        return extendedMoveStack.topExtendedMove();
    }

    @Override
    public Position getKingPosition(boolean whiteKing) {
        return game.getKingPosition(whiteKing);
    }

    @Override
    public boolean isFreeArea(Position pos) {
        return game.isFreeArea(pos);
    }

    @Override
    public Figure getFigure(Position pos) {
        return game.getFigure(pos);
    }

    @Override
    public BoardContent getContent(Position pos) {
        return BoardContent.Companion.get(game.getFigure(pos));
    }

    private void setFigure(Position pos, Figure figure) {
        game.setFigure(pos, figure);
    }

    @Override
    public List<Figure> getFigures() {
        return game.getFigures();
    }

    @Override
    public boolean isSelectable(Position pos, boolean whitePlayer) {
        if (isFreeArea(pos)) return false;
        Figure figure = getFigure(pos);
        return figure.isWhite() == whitePlayer && figure.isSelectable(game);
    }

    @Override
    public boolean isMoveable(Position from, Position to, boolean whitePlayer) {
        if (isFreeArea(from)) return false;
        Figure figure = getFigure(from);
        return figure.isWhite() == whitePlayer && figure.isMovable(to, game);
    }

    @Override
    public int countFigures() {
        return figureCount;
    }

    @Override
    public MoveResult move(Move move) {
        assert !isFreeArea(move.getFrom())
                : "the move moves a null value:" + move.toString();
        assert (getFigure(move.getFrom()).isWhite() == whiteTurn)
                : "figure to be moved has wrong color";

        Rook castlingRook = extractCastlingRook(move);
        //im Fall der Castling wird der Zug jetzt so umgebogen,
        //das move.to dem Zielfeld des Königs entspricht
        //und nicht dem Feld des Castlingturms
        if (castlingRook != null) {
            final int row = move.getTo().getRow();
            final int column = move.getTo().getColumn() - move.getFrom().getColumn() > 0 ? 6 : 2;
            move = Move.Companion.get(move.getFrom(), Position.Companion.get(row, column));
        }

        Pawn hitPawn = handleEnpasent(move);
        Figure hitFigure = moveFigure(move);

        informFiguresOfMove(move);
        reinsertCastlingRook(castlingRook, move.getTo());
        boolean pawnTransformed = handlePawnTransformation(move);

        memorizeGame();
        memorizeMove(move, !whiteTurn, pawnTransformed, hitPawn, castlingRook, hitFigure);

        return isEnd();
    }

    private Figure moveFigure(Move move) {
        final boolean toNotEqualsFrom = move.getTo().notEqualsPosition(move.getFrom());//für manche Schach960castlingn true
        hasHitFigure = !isFreeArea(move.getTo()) && toNotEqualsFrom;  //Enpasent wird nicht beachtet
        Figure fromFigure = getFigure(move.getFrom());

        if (hasHitFigure) {
            numberStack.figureHit();
            numberOfMovesWithoutHit = 0;
            figureCount--;
        } else {
            numberStack.noFigureHit();
            numberOfMovesWithoutHit++;
        }

        Figure hitFigure = null;
        if (toNotEqualsFrom) {
            hitFigure = getFigure(move.getTo());
            setFigure(move.getTo(), fromFigure);
            setFigure(move.getFrom(), null);
        }

        whiteTurn = !whiteTurn;

        return hitFigure;
    }

    @Override
    public boolean hasHitFigure() {
        return hasHitFigure;
    }

    private Pawn handleEnpasent(Move move) {
        if (getFigure(move.getFrom()).isPawn()
                && move.getFrom().getColumn() != move.getTo().getColumn()
                && isFreeArea(move.getTo())
        ) {
            Position pawnToBeHit = Position.Companion.get(move.getFrom().getRow(), move.getTo().getColumn());
            Pawn pawn = (Pawn) getFigure(pawnToBeHit);
            setFigure(pawnToBeHit, null);
            figureCount--;
            numberOfMovesWithoutHit = -1;            //die Variable wird dann von move Figure auf 0 gesetzt
            return pawn;
        }
        return null;
    }

    private Rook extractCastlingRook(Move move) {
        final Figure movingFigure = getFigure(move.getFrom());
        if (!(movingFigure.isKing())) return null;

        final Figure castlingRook = getFigure(move.getTo());
        if (castlingRook != null && castlingRook.isWhite() == movingFigure.isWhite()) {
            setFigure(move.getTo(), null);    //der Turm wird kurzfristig vom Brett genommen
            ((King) movingFigure).performCastling();
            return (Rook) castlingRook;
        }
        return null;
    }

    private void reinsertCastlingRook(Rook castlingRook, Position moveTo) {
        if (castlingRook != null) {
            Position RookFrom = castlingRook.getPosition();
            Position RookTo = moveTo.getColumn() == 6 ?
                    Position.Companion.get(moveTo.getRow(), 5) :
                    Position.Companion.get(moveTo.getRow(), 3);
            castlingRook.figureMoved(Move.Companion.get(RookFrom, RookTo));
            setFigure(RookTo, castlingRook);
        }
    }

    private boolean handlePawnTransformation(Move move) {
        if (getFigure(move.getTo()).isPawn()) {
            if (move.getTo().getRow() == 0 || move.getTo().getRow() == 7) {
                PawnPromotion figure = supervisor.askForPawnChange(move.getTo());
                boolean isWhite = move.getTo().getRow() == 7;
                Figure newFigure;
                switch (figure) {
                    case QUEEN:
                        newFigure = figureFactory.getQueen(move.getTo(), isWhite);
                        break;
                    case ROOK:
                        newFigure = figureFactory.getRook(move.getTo(), isWhite);
                        break;
                    case KNIGHT:
                        newFigure = figureFactory.getKnight(move.getTo(), isWhite);
                        break;
                    case BISHOP:
                        newFigure = figureFactory.getBishop(move.getTo(), isWhite);
                        break;
                    default:
                        throw new NullPointerException("invalide pawn-transformation-string:" + figure);
                }
                setFigure(move.getTo(), newFigure);
                return true;
            }
        }
        return false;
    }

    private MoveResult isEnd() {
        if (noMovesLeft(whiteTurn)) {
            if (isCheck(whiteTurn)) {
                return MoveResult.CHECKMATE;
            } else {
                return MoveResult.STALEMATE;
            }
        }
        if (isDrawBecauseOfLowMaterial()) {
            return MoveResult.DRAW;
        }
        if (isDrawBecauseOfThreeTimesSamePosition()) {
            return MoveResult.THREE_TIMES_SAME_POSITION;
        }
        if (numberOfMovesWithoutHit == 100) {
            return MoveResult.FIFTY_MOVES_NO_HIT;
        }
        return MoveResult.NO_END;
    }

    private void informFiguresOfMove(Move move) {
        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            figure.figureMoved(move);
        }
    }

    @Override
    public void undo() {
        whiteTurn = !whiteTurn;
        numberOfMovesWithoutHit = numberStack.undo();
        mementoStack.popMemento();

        ExtendedMove lastExtMove = extendedMoveStack.popExtendedMove();
        Move lastMove = lastExtMove.getMove();
        final boolean wasCastling = lastExtMove.isCastling();
        Figure activeFigure = getFigure(lastMove.getTo());
        setFigure(lastMove.getFrom(), activeFigure);
        if (!wasCastling || lastMove.getFrom().notEqualsPosition(lastMove.getTo())) {
            setFigure(lastMove.getTo(), lastExtMove.getFigureTaken());
        }
        activeFigure.undoMove(lastMove.getFrom());

        if (lastExtMove.wasFigureTaken()) {
            figureCount++;
        }

        if (wasCastling) undoCastling(lastExtMove);
        if (lastExtMove.isEnpassent()) undoEnpassent(lastExtMove);
        if (lastExtMove.isPawnTransformation()) undoPawnTransformation(lastExtMove);
        rebuildPawnEnpassentCapability();
    }

    private void undoCastling(ExtendedMove lastExtMove) {
        Rook rook = (Rook) lastExtMove.getEnpassentPawnOrCastlingRook();
        Position RookStartPos = rook.getInitialPosition();
        Position RookCurrentPos = rook.getPosition();

        setFigure(RookStartPos, rook);
        if (RookStartPos.notEqualsPosition(RookCurrentPos) && lastExtMove.getMove().getFrom().notEqualsPosition(RookCurrentPos)) {
            setFigure(RookCurrentPos, null);
        }
        rook.undoMove(RookStartPos);
    }

    private void undoEnpassent(ExtendedMove lastExtMove) {
        Pawn hitPawn = (Pawn) lastExtMove.getEnpassentPawnOrCastlingRook();
        Position pawnPos = Position.Companion.get(lastExtMove.getMove().getFrom().getRow(), lastExtMove.getMove().getTo().getColumn());
        setFigure(pawnPos, hitPawn);
        hitPawn.setCanBeHitByEnpasent();
    }

    private void undoPawnTransformation(ExtendedMove lastExtMove) {
        Position pawnPos = lastExtMove.getMove().getFrom();
        Figure pawn = figureFactory.getPawn(pawnPos, lastExtMove.getColorOfMove());
        setFigure(pawnPos, pawn);
    }

    private void rebuildPawnEnpassentCapability() {
        if (extendedMoveStack.isEmpty()) return;

        ExtendedMove newLatestMove = extendedMoveStack.topExtendedMove();
        Figure figure = getFigure(newLatestMove.getMove().getTo());
        if (figure.isPawn() &&
                Math.abs(newLatestMove.getMove().getFrom().getRow() - newLatestMove.getMove().getTo().getRow()) == 2) {
            ((Pawn) figure).setCanBeHitByEnpasent();
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(512);

        if (whiteTurn) buffer.append("white ");
        else buffer.append("black ");

        buffer.append(numberOfMovesWithoutHit);
        buffer.append(" ");
        buffer.append(game.toString());

        return buffer.toString();
    }

    public String getHistory() {
        return getHistory(4);
    }

    public String getHistory(int numberOfHalvMoves) {
        return extendedMoveStack.getLatestMoves(numberOfHalvMoves);
    }

    @Override
    public String getCompleteHistory() {
        final int numberOfMovesSaved = extendedMoveStack.size();
        return extendedMoveStack.getLatestMoves(numberOfMovesSaved);
    }

    private void initGame() {
        initGame(518);    //normale Schachposition
    }

    @Override
    public void initGame(int chess960) {
        isStandardGame = chess960 == 518;
        whiteTurn = true;
        numberOfMovesWithoutHit = 0;
        figureCount = 32;
        mementoStack.clear();
        extendedMoveStack.clear();
        numberStack.init();

        game.init(chess960);

        memorizeGame();
    }

    public boolean equals(ChessGame other) {
        if (whiteTurn != other.whiteTurn) return false;

        for (int index = 0; index < 64; index++) {
            Position pos = Position.Companion.byIndex(index);
            BoardContent content = getContent(pos);
            BoardContent otherContent = other.getContent(pos);
            if (content.isFreeArea() != otherContent.isFreeArea()) return false;
            if (!content.isFreeArea()) {
                Figure figure1 = content.getFigure();
                Figure figure2 = otherContent.getFigure();
                if (!figure1.equals(figure2)) return false;
            }
        }

        return true;
    }

    public boolean isStandardGame() {
        return isStandardGame;
    }

    private boolean noMovesLeft(boolean caseWhite) {
        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            if (figure.isWhite() == caseWhite &&
                    figure.isSelectable(game)) {
                return false;
            }
        }
        return true;
    }

    private boolean isDrawBecauseOfLowMaterial() {
        int numberOfWhiteBishops = 0;
        int numberOfBlackBishops = 0;
        int numberOfWhiteKnights = 0;
        int numberOfBlackKnights = 0;

        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            if (figure.isPawn()
                    || figure.isRook()
                    || figure.isQueen()) {
                return false;
            } else if (figure.isBishop()) {
                if (figure.isWhite()) numberOfWhiteBishops++;
                else numberOfBlackBishops++;
            } else if (figure.isKnight()) {
                if (figure.isWhite()) numberOfWhiteKnights++;
                else numberOfBlackKnights++;
            }
        }

        if (numberOfWhiteBishops > 1 || numberOfBlackBishops > 1) {
            return false;
        }
        if (numberOfWhiteKnights > 2 || numberOfBlackKnights > 2) {
            return false;
        }
        if (numberOfWhiteBishops == 1 && numberOfWhiteKnights > 0) {
            return false;
        }
        if (numberOfBlackBishops == 1 && numberOfBlackKnights > 0) {
            return false;
        }
        return true;
    }

    private boolean isDrawBecauseOfThreeTimesSamePosition() {
        int occurencesOfMemento = mementoStack.countOccurencesOfLastMemento();
        return occurencesOfMemento >= 3;
    }

    @Override
    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    @Override
    public boolean isCheck(boolean isWhiteInCheck) {
        return CheckSearch.isCheck(game, game.getKingPosition(isWhiteInCheck));
    }

    @Override
    public void getPossibleMoves(List<Move> possibleMoves) {
        final Position kingPos = game.getKingPosition(whiteTurn);

        getFigure(kingPos).getPossibleMoves(game, possibleMoves);

        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            if (figure.isWhite() == whiteTurn && !(figure.isKing())) {
                figure.getPossibleMoves(game, possibleMoves);
            }
        }
    }

    @Override
    public int countReachableMoves(boolean forWhite) {
        int count = 0;

        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            if (figure.isWhite() == forWhite) {
                count += figure.countReachableMoves(game);
            }
        }

        return count;
    }

    @Override
    public List<ChessGameInterface> copyGame(int neededInstances) {
        final List<ChessGameInterface> gameInstances = new ArrayList<ChessGameInterface>(neededInstances);
        gameInstances.add(this);

        if (neededInstances > 1) {
            final String gameDes = toString();
            for (int i = 1; i < neededInstances; i++) {
                ChessGame copy = new ChessGame(this, gameDes);
                gameInstances.add(copy);
            }
        }
        return gameInstances;
    }

    private void memorizeGame() {
        mementoStack.putMemento(new Memento(game, whiteTurn));
    }

    private void memorizeMove(Move move,
                              boolean whiteMove,
                              boolean pawnTransformed,
                              Pawn hitPawn,
                              Rook castlingRook,
                              Figure hitFigure) {
        boolean hitsEnpassent = hitPawn != null;
        boolean isCastling = castlingRook != null;
        Figure castlingRookOrEnpassentPawn = hitPawn;
        if (isCastling) {
            castlingRookOrEnpassentPawn = castlingRook;
        }
        ExtendedMove extendedMove = new ExtendedMove(
                move,
                hitFigure,
                castlingRookOrEnpassentPawn,
                whiteMove,
                isCastling,
                hitsEnpassent,
                pawnTransformed);
        extendedMoveStack.putExtendedMove(extendedMove);
    }

    public ExtendedMove getLastExtendedMove() {
        return extendedMoveStack.topExtendedMove();
    }

    private class Memento {
        final private int figureCount;
        final private boolean isWhite;
        final private long[] compressedBoard;

        private Memento(BasicChessGameInterface game, boolean isWhite) {
            int count = 0;
            int[] board = new int[64];
            for (int index = 0; index < 64; index++) {
                Figure figure = game.getFigure(Position.Companion.byIndex(index));
                if (figure != null) {
                    board[index] = figure.getTypeInfo();
                    count++;
                }
            }

            // compress the board by exploiting that typeInfo is smaller than 16
            // and therefore only 4 bits are needed -> pack 15 typeInfos into 1 long
            compressedBoard = new long[]{
                    compressBoardSlicesToLong(board, 0, 15),
                    compressBoardSlicesToLong(board, 15, 30),
                    compressBoardSlicesToLong(board, 30, 45),
                    compressBoardSlicesToLong(board, 45, 60),
                    compressBoardSlicesToLong(board, 60, 64)
            };

            this.isWhite = isWhite;
            figureCount = count;
        }

        private boolean hasDifferentNumberOfFiguresAs(Memento other) {
            return figureCount != other.figureCount;
        }

        private boolean equals(Memento other) {
            return isWhite == other.isWhite && Arrays.equals(compressedBoard, other.compressedBoard);
        }

        private long compressBoardSlicesToLong(int[] board, int startIndex, int endIndex) {
            assert endIndex-startIndex < 16;

            final int endIndexMinusOne = endIndex-1;
            long compressedValue = 0;
            for(int i=startIndex; i<endIndexMinusOne; i++ ) {
                assert board[i]>=0 && board[i]<16; // board[i] (=figure==null?0:figure.typeInfo) out of Bounds, it has to fit into 4 bits with 0->no figure!
                compressedValue+=board[i];
                compressedValue <<=4;
            }
            compressedValue+=board[endIndexMinusOne];
            return compressedValue;
        }
    }

    private class MementoStack {
        private Memento[] mementoArray;
        private int index;

        MementoStack() {
            mementoArray = new Memento[200];
            index = 0;
        }

        //Copy-Constructor
        MementoStack(MementoStack other) {
            mementoArray = new Memento[other.mementoArray.length];
            System.arraycopy(other.mementoArray, 0, mementoArray, 0, mementoArray.length);
            index = other.index;
        }

        void putMemento(Memento memento) {
            ensureCapacity();
            mementoArray[index++] = memento;
        }

        void popMemento() {
            if (index > 0) index--;
        }

        int countOccurencesOfLastMemento() {
            assert index > 0;
            int count = 1;

            Memento lastMemento = mementoArray[index - 1];
            for (int i = index - 3; i >= 0; i = i - 2) {
                Memento previousMemento = mementoArray[i];
                if (previousMemento.hasDifferentNumberOfFiguresAs(lastMemento)) {
                    break;
                }
                if (lastMemento.equals(previousMemento)) {
                    count++;
                }
            }

            return count;
        }

        void clear() {
            index = 0;
        }

        private void ensureCapacity() {
            if (index == mementoArray.length) {
                Memento[] newMementoArray = new Memento[mementoArray.length * 2];
                System.arraycopy(mementoArray, 0, newMementoArray, 0, index);
                mementoArray = newMementoArray;
            }
        }
    }

    private class ExtendedMoveStack {
        private ExtendedMove[] extendedMoveArray;
        private int index;

        ExtendedMoveStack() {
            extendedMoveArray = new ExtendedMove[100];
            index = 0;
        }

        ExtendedMoveStack(ExtendedMoveStack other) {
            extendedMoveArray = new ExtendedMove[other.extendedMoveArray.length];
            System.arraycopy(other.extendedMoveArray, 0, extendedMoveArray, 0, extendedMoveArray.length);
            index = other.index;
        }

        void putExtendedMove(ExtendedMove extendedMove) {
            ensureCapacity();
            extendedMoveArray[index++] = extendedMove;
        }

        ExtendedMove popExtendedMove() {
            assert index > 0;
            return extendedMoveArray[--index];
        }

        ExtendedMove topExtendedMove() {
            assert index > 0;
            return extendedMoveArray[index - 1];
        }

        boolean isEmpty() {
            return index == 0;
        }

        void clear() {
            index = 0;
        }

        int size() {
            return index;
        }

        String getLatestMoves(int count) {
            assert count > 0;

            final int MIN_INDEX = Math.max(0, index - count);
            StringBuilder sb = new StringBuilder(24);

//            for (int i = index - 1; i >= MIN_INDEX; i--) {
            for (int i = MIN_INDEX; i < index; i++) {
                sb.append(extendedMoveArray[i].toString());
                sb.append(",");
            }
            final int deleteCharAt = sb.length() - 1;
            if (deleteCharAt < 0) {
                return "";
            }
            sb.deleteCharAt(deleteCharAt);

            return sb.toString();
        }

        private void ensureCapacity() {
            if (index == extendedMoveArray.length) {
                ExtendedMove[] newExtendedMoveArray = new ExtendedMove[extendedMoveArray.length * 2];
                System.arraycopy(extendedMoveArray, 0, newExtendedMoveArray, 0, index);
                extendedMoveArray = newExtendedMoveArray;
            }
        }
    }

    private class NumberStack {
        private int[] numberStack;
        private int index;

        NumberStack() {
            numberStack = new int[50];
            init();
        }

        //copy-Constructor
        NumberStack(NumberStack other) {
            numberStack = new int[other.numberStack.length];
            System.arraycopy(other.numberStack, 0, numberStack, 0, numberStack.length);
            index = other.index;
        }

        void init() {
            for (int i = 0; i < numberStack.length; i++) numberStack[i] = 0;
            index = 0;
        }

        void noFigureHit() {
            numberStack[index]++;
        }

        void figureHit() {
            ensureCapacity();
            index++;
        }

        int undo() {
            if (numberStack[index] == 0) {
                index--;
            } else {
                numberStack[index]--;
            }
            return numberStack[index];
        }

        private void ensureCapacity() {
            if (index + 1 == numberStack.length) {
                int[] newNumberStack = new int[numberStack.length * 2];
                System.arraycopy(numberStack, 0, newNumberStack, 0, numberStack.length);
                numberStack = newNumberStack;
            }
        }
    }
}
