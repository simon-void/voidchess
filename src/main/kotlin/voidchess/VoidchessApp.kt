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
import voidchess.united.Table
import voidchess.united.EngineAdapter
import voidchess.common.integration.ComputerPlayerUI
import voidchess.ui.initializeUI
import javax.swing.JComponent


fun main() = application {
    val chessPanel = chessPanel()
    val size = chessPanel.preferredSize
    val icon = BitmapPainter(useResource("icon.gif", ::loadImageBitmap))

    Window(
        onCloseRequest = ::exitApplication,
        title = "VoidChess",
        state = rememberWindowState(
            width = (size.width + 20).dp,
            height = (size.height + 40).dp,
            position = WindowPosition.Aligned(Alignment.Center)
        ),
//        resizable = false,
        icon = icon,
    ) {
        SwingPanel(
            background = Color.White,
            modifier = Modifier.size(size.width.dp, size.height.dp),
            factory = {
                chessPanel
            },

        )
    }
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