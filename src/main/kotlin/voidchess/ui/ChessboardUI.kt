package voidchess.ui

import voidchess.board.BasicChessGameInterface
import voidchess.helper.Move
import voidchess.helper.Position
import voidchess.image.FigureGallery
import voidchess.player.HumanPlayerInterface

import javax.swing.*
import java.awt.*
import java.awt.image.ImageObserver


class ChessboardUI constructor(private val game: BasicChessGameInterface, imageObserver: ImageObserver) : JComponent() {
    val areaSize: Int = 50
    val borderSize: Int = 25
    private val adapter: ChessboardAdapter
    private val figureGallery = FigureGallery(imageObserver, areaSize)
    var isWhiteView: Boolean = true
        private set(value) { field = value }
    private var from: Position? = null
    private var to: Position? = null

    init {
        preferredSize = Dimension(2 * borderSize + 8 * areaSize, 2 * borderSize + 8 * areaSize)
        border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.black),
                BorderFactory.createBevelBorder(0, Color.gray, Color.darkGray)
        )
        adapter = ChessboardAdapter(this)
        addMouseListener(adapter)
        addMouseMotionListener(adapter)
        isDoubleBuffered = true
    }

    fun repaintAfterMove(move: Move) {
        repaintPositionAtOnce(move.from)
        repaintPositionAtOnce(move.to)

        val horizontalDiff = Math.abs(move.from.column - move.to.column)
        val verticalDiff = Math.abs(move.from.row - move.to.row)

        if (horizontalDiff == 1 && verticalDiff == 1) {                                //en passant?
            repaintPositionAtOnce(Position.get(move.from.row, move.to.column))
            repaintPositionAtOnce(Position.get(move.to.row, move.from.column))
        } else if (verticalDiff == 0 && (move.to.row == 0 || move.to.row == 7)) {      //castling? has to work for chess960 as well
            repaintRowAtOnce(move.from.row)
        }
    }

    private fun repaintPositionAtOnce(pos: Position) {
        val xPos = borderSize + areaSize * if (isWhiteView) pos.column else 7 - pos.column
        val yPos = borderSize + areaSize * if (isWhiteView) 7 - pos.row else pos.row

        val repaintSize = areaSize + 1
        paintImmediately(xPos, yPos, repaintSize, repaintSize)
    }

    private fun repaintRowAtOnce(row: Int) {
        val xPos = borderSize
        val yPos = borderSize + areaSize * if (isWhiteView) 7 - row else row

        val repaintSize = areaSize + 1
        paintImmediately(xPos, yPos, repaintSize * 8, repaintSize)
    }

    fun repaintAtOnce() {
        val dim = size
        paintImmediately(0, 0, dim.width, dim.height)
    }

    override fun paintComponent(g: Graphics) {
        paintBoard(g)
        paintActiveAreas(g)
        paintFigures(g)
    }

    private fun paintBoard(g: Graphics) {
        g.color = Color.white
        g.fillRect(0, 0, 2 * borderSize + 8 * areaSize, 2 * borderSize + 8 * areaSize)
        g.color = Color.lightGray
        g.drawRect(borderSize, borderSize, 8 * areaSize, 8 * areaSize)
        for (i in 0..6 step 2) {
            for (j in 1..7 step 2) {
                g.fillRect(borderSize + i * areaSize, borderSize + j * areaSize, areaSize, areaSize)
                g.fillRect(borderSize + (i+1) * areaSize, borderSize + (j-1) * areaSize, areaSize, areaSize)
            }
        }
    }

    private fun paintFigures(g: Graphics) {
        for( figure in game.getFigures()) {
            val pos = figure.position
            val xPos = borderSize + areaSize * if (isWhiteView) pos.column else 7 - pos.column
            val yPos = borderSize + areaSize * if (isWhiteView) 7 - pos.row else pos.row

            val paintableFigure = figureGallery.getPaintable(figure)
            paintableFigure.paintOn(g, xPos, yPos, areaSize)
        }
    }

    private fun paintActiveAreas(g: Graphics) {
        g.color = Color.darkGray
        if (from != null) {
            val xPos = borderSize + areaSize * if (isWhiteView) from!!.column else 7 - from!!.column
            val yPos = borderSize + areaSize * if (isWhiteView) 7 - from!!.row else from!!.row
            g.drawRect(xPos, yPos, areaSize, areaSize)
        }
        if (to != null) {
            val xPos = borderSize + areaSize * if (isWhiteView) to!!.column else 7 - to!!.column
            val yPos = borderSize + areaSize * if (isWhiteView) 7 - to!!.row else to!!.row
            g.drawRect(xPos, yPos, areaSize, areaSize)
        }
    }

    fun setViewPoint(fromWhite: Boolean) {
        isWhiteView = fromWhite
        repaint()
    }

    fun setPlayer(player: HumanPlayerInterface) {
        adapter.setPlayer(player)
    }

    fun markPosition(pos: Position?, isFromPosition: Boolean) {
        if (isFromPosition) {
            val temp = from
            if (from != null) {
                from = null
                if(temp!=null) {
                    repaintPositionAtOnce(temp)
                }
            }
            from = pos
        } else {
            val temp = to
            if (to != null) {
                to = null
                if(temp!=null) {
                    repaintPositionAtOnce(temp)
                }
            }
            to = pos
        }
        if (pos != null) repaintPositionAtOnce(pos)
    }
}
