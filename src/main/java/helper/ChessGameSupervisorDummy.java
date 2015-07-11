package helper;

/**
 * @author stephan
 */
public class ChessGameSupervisorDummy
        implements ChessGameSupervisor {
    final static public ChessGameSupervisor INSTANCE = new ChessGameSupervisorDummy();

    private ChessGameSupervisorDummy() {
    }

    public String askForPawnChange(Position pawnPosition) {
        return "Queen";
    }

    public void informOfEnd(int endoption) {
    }

    public void informOfMove(Move move) {
    }

}
