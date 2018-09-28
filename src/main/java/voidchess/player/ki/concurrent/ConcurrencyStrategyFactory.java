package voidchess.player.ki.concurrent;

import voidchess.ui.ComputerPlayerUI;

public class ConcurrencyStrategyFactory {
    public static ConcurrencyStrategy getConcurrencyStrategy(ComputerPlayerUI ui, int numberOfCoresToUse) {
        if (numberOfCoresToUse == 1) {
            return new SingleThreadStrategy(ui);
        } else {
            return new MultiThreadStrategy(ui, numberOfCoresToUse);
        }
    }
}
