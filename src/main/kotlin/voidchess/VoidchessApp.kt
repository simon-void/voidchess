package voidchess

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import voidchess.common.board.ArrayChessBoard
import voidchess.common.board.ChessBoard
import voidchess.ui.compose.fullLayout
import voidchess.united.Table
import voidchess.united.EngineAdapter
import voidchess.ui.compose.leftSide
import voidchess.ui.compose.myCanvas
import voidchess.ui.initializeUI
import voidchess.ui.swing.ChessboardComponent
import voidchess.ui.swing.ComputerPlayerComponent
import javax.swing.JComponent
import voidchess.common.integration.ComputerPlayerUI as ComputerPlayerUI


fun main() = application {
    val chessPanel = chessPanel()
    val size = chessPanel.preferredSize
    val icon = BitmapPainter(useResource("icon.gif", ::loadImageBitmap))

    val computerPlayerComponent = ComputerPlayerComponent()
    val chessboardComponent = ChessboardComponent(ArrayChessBoard())

    Window(
        onCloseRequest = ::exitApplication,
        title = "VoidChess",
        state = rememberWindowState(
            width = (1100).dp,
            height = (1100).dp,
            position = WindowPosition.Aligned(Alignment.Center)
        ),
//        resizable = false,
        icon = icon,
    ) {
//        SwingPanel(
//            background = Color.White,
//            modifier = Modifier.size(size.width.dp, size.height.dp),
//            factory = {
//                chessPanel
//            },
//
//        )
//        fullLayout(
//            computerPlayerComponent,
//            chessboardComponent,
//        )
        myCanvas()
    }
    computerPlayerComponent.setProgress(5, 20)
    chessboardComponent.startNewGame()
}

fun chessPanel(): JComponent {
//    runCatching {
    val engineAdapter = EngineAdapter()
    val table = Table(engineAdapter)
    val (chessPanel, computerPlayerUI: ComputerPlayerUI) = initializeUI(
        engineAdapter.getEngineConfig(),
        table
    )
    table.postConstruct(computerPlayerUI)
    return chessPanel


//    }.getOrElse { exception ->
//        val errorMsg =
//            """
//            An exception occurred: $exception
//            Please consider logging an Issue here:
//            https://github.com/simon-void/voidchess/issues
//            """.trimIndent()
//        Text(errorMsg)
//    }
}