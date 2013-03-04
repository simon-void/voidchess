package helper;

/**
 * @author stephan
 */
public class MockChessGameSupervisor implements ChessGameSupervisor
{
	private boolean allOK  = false;
	private String  defaultPawnTransform = "nofigure";
	
	public MockChessGameSupervisor( String defaultPawnTransform )
	{
		this.defaultPawnTransform = defaultPawnTransform;
	}

	public String askForPawnChange( Position pawnPosition)
	{
		return defaultPawnTransform;
	}

	public boolean everythingWentAsExpected()
	{
		return allOK;
	}

}
