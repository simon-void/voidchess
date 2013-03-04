package helper;

/**
 * @author stephan
 */
public class CheckStatus
{
	final public static CheckStatus NO_CHECK     = new CheckStatus( false,false);
	final public static CheckStatus DOUBLE_CHECK = new CheckStatus(  true,true );
	
	private boolean isCheck;
	private boolean onlyKingCanMove;
	private PositionIterator possiblePositions;
	
	private CheckStatus(boolean isCheck,boolean onlyKingCanMove )
	{
		this.isCheck         = isCheck;
		this.onlyKingCanMove = onlyKingCanMove;
		possiblePositions    = null;
	}
	
	CheckStatus( PositionIterator possiblePositions )
	{
		isCheck                = true;
		onlyKingCanMove        = false;
		this.possiblePositions = possiblePositions;
	}

	public boolean isCheck()
	{
		return isCheck;
	}
	
	public boolean onlyKingCanMove()
	{
		return onlyKingCanMove;
	}
	
	public PositionIterator getPossiblePositions()
	{
		return possiblePositions;
	}
}
