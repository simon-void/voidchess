package voidchess.board;

import voidchess.helper.ExtendedMove;

public interface LastMoveProvider {
    ExtendedMove getLastMove();
}
