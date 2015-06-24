package helper;

/**
 * @author stephan
 */
public class ChessGameSupervisorDummy 
implements ChessGameSupervisor
{

	public String askForPawnChange( Position pawnPosition)
	{
		return "Queen";
	}

	public void informOfEnd(int endoption)
	{
	}
	
	public void informOfMove( Move move )
	{
	}

}
