package voidchess.ui

import voidchess.common.board.BasicChessGameImpl
import voidchess.common.engine.EngineConfig
import voidchess.common.integration.ComputerPlayerUI
import voidchess.common.integration.TableAdapter
import voidchess.ui.inner.player.SwingPlayerImpl
import voidchess.ui.inner.swing.Chess960Panel
import voidchess.ui.inner.swing.ChessFrame
import voidchess.ui.inner.swing.ChessPanel
import voidchess.ui.inner.swing.ChessboardComponent
import voidchess.ui.inner.swing.ComputerPlayerComponent
import voidchess.ui.inner.swing.CoresPanel
import voidchess.ui.inner.swing.DifficultyPanel
import voidchess.ui.inner.swing.ShareGamePanel
import javax.swing.SwingUtilities

fun initializeUI(
    engineConfig: EngineConfig,
    tableAdapter: TableAdapter
): ComputerPlayerUI {

    val game = BasicChessGameImpl()
    val chessboardComponent = ChessboardComponent(game)
    val panel960 = Chess960Panel(game, chessboardComponent)
    val shareGamePanel = ShareGamePanel(
        getMovesPlayed = { game.movesPlayed() },
        getCurrentChess960Index = { panel960.chess960Index },
    )
    val humanPlayer = SwingPlayerImpl(chessboardComponent, game, tableAdapter).also {
        chessboardComponent.postConstruct(it)
    }
    val computerPlayerUI = ComputerPlayerComponent()
    val difficultyPanel = DifficultyPanel(engineConfig)
    val coresPanel = CoresPanel(engineConfig)
    val chessPanel = ChessPanel(
        humanPlayer,
        panel960,
        difficultyPanel,
        coresPanel,
        shareGamePanel,
        chessboardComponent,
        computerPlayerUI,
    ).also {
        humanPlayer.postConstruct(it::stop, it::enableResign)
    }
    //Swing UI updates have to come from the SwingHandler or something
    SwingUtilities.invokeLater {
        ChessFrame(chessPanel)
    }

    return computerPlayerUI
}