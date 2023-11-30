package voidchess.ui.swing

import voidchess.common.board.BasicChessGame
import voidchess.common.board.move.Move
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import java.awt.Color
import java.awt.Dimension
import java.lang.Math.min
import javax.swing.*
import javax.swing.table.DefaultTableModel
import kotlin.math.max


internal class HistoryPanel(
    private val game: BasicChessGame,
) : JPanel() {
    private var historyButton: JButton = JButton("moves played")

    init {
        designLayout()
    }

    private fun designLayout() {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        background = Color.WHITE

        historyButton.addActionListener { displayMovesPlayed() }

        add(historyButton)
    }

    fun JTable.resizeColumnWidth() {
        val table = this
        val columnModel = table.columnModel
        for (column in 0 until table.columnCount) {
            var width = 15 // Min width
            for (row in 0 until table.getRowCount()) {
                val renderer = table.getCellRenderer(row, column)
                val comp = table.prepareRenderer(renderer, row, column)
                width = max((comp.preferredSize.width + 1).toDouble(), width.toDouble()).toInt()
            }
            if (width > 300) width = 300
            columnModel.getColumn(column).setPreferredWidth(width)
        }
    }

    private fun displayMovesPlayed() {
        val movesPlayed = game.movesPlayed()
        if(movesPlayed.isNotEmpty()) {
            val historyPanelLocation = locationOnScreen
            val rows: List<List<Move>> = movesPlayed.chunked(2)
            val table = JTable(
                DefaultTableModel(rows.size, 3).apply {
                    rows.forEachIndexed { index, moves ->
                        val whiteMove = moves[0].toString()
                        val blackMove = moves.getOrNull(1)?.toString() ?: ""
                        setValueAt("${index+1}.", index, 0)
                        setValueAt(whiteMove, index, 1)
                        setValueAt(blackMove, index, 2)
                    }
                }
            ).apply {
                tableHeader = null

                autoResizeMode = JTable.AUTO_RESIZE_OFF
                val table = this
                val columnModel = table.columnModel
                for (column in 0 until table.columnCount) {
                    var width = 15 // Min width
                    for (row in 0 until table.getRowCount()) {
                        val renderer = table.getCellRenderer(row, column)
                        val comp = table.prepareRenderer(renderer, row, column)
                        width = max((comp.preferredSize.width + 1).toDouble(), width.toDouble()).toInt()
                    }
                    if (width > 300) width = 300
                    columnModel.getColumn(column).setPreferredWidth(width)
                }
            }
            JDialog().apply {
                add(JScrollPane(JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                    add(JTextField(movesPlayed.joinToString("") { it.toBase64() }))
                    add(table)
                }).apply {
                    preferredSize = table.preferredSize.let {
                        Dimension(it.width+20, min(it.height+4, 400))
                    }
                    pack()
                })
                pack()
                location = historyPanelLocation
//                isModal = true
                isVisible = true
            }
        }
    }
}

// 0 A            17 R            34 i            51 z
// 1 B            18 S            35 j            52 0
// 2 C            19 T            36 k            53 1
// 3 D            20 U            37 l            54 2
// 4 E            21 V            38 m            55 3
// 5 F            22 W            39 n            56 4
// 6 G            23 X            40 o            57 5
// 7 H            24 Y            41 p            58 6
// 8 I            25 Z            42 q            59 7
// 9 J            26 a            43 r            60 8
//10 K            27 b            44 s            61 9
//11 L            28 c            45 t            62 - (minus)
//12 M            29 d            46 u            63 _ (underline)
//13 N            30 e            47 v
//14 O            31 f            48 w
//15 P            32 g            49 x
//16 Q            33 h            50 y         (pad) =
private val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray()

internal fun Position.toBase64(): Char = base64Chars[index]

internal fun Move.toBase64(): String = "${from.toBase64()}${to.toBase64()}".let { basicMoveCode ->
    val pawnPromotionBase64Char = when(pawnPromotionType) {
        null -> return basicMoveCode
        PawnPromotion.QUEEN -> 'Q'
        PawnPromotion.ROOK -> 'R'
        PawnPromotion.KNIGHT -> 'K'
        PawnPromotion.BISHOP -> 'B'
    }
    "$basicMoveCode$pawnPromotionBase64Char"
}