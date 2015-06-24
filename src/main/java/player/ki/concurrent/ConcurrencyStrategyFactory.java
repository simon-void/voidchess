package player.ki.concurrent;

import player.ki.AbstractComputerPlayerUI;

public class ConcurrencyStrategyFactory
{
	public static ConcurrencyStrategy getConcurrencyStrategy(AbstractComputerPlayerUI ui)
	{
		final int numberOfCores = getAvailableCores();
		if(numberOfCores==1) {
			return new SingleThreadStrategy(ui);
		}else{
			return new MultiThreadStrategy(ui, numberOfCores);
		}
	}
	
	private static int getAvailableCores()
	{
		return Runtime.getRuntime().availableProcessors();
	}
}
