package voidchess.helper

/**
 * @author stephan
 */

object ChessGameSupervisorDummy : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): String {
        return "Queen"
    }

}
