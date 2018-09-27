package voidchess.ui

import voidchess.board.BasicChessGameInterface
import voidchess.helper.ExtendedMove
import voidchess.helper.Position
import voidchess.image.FigureGallery
import voidchess.player.HumanPlayerInterface
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.image.ImageObserver
import javax.swing.BorderFactory
import javax.swing.JComponent


class ChessboardUI constructor(private val game: BasicChessGameInterface, imageObserver: ImageObserver) : JComponent() {
    val areaSize: Int = 50
    val borderSize: Int = 25
    private val adapter: ChessboardAdapter
    private val figureGallery = FigureGallery(imageObserver, areaSize)
    var isWhiteView: Boolean = true
        private set(value) {
            field = value
        }
    private val evenFieldColor = Color.lightGray
    private val oddFieldColor = Color.white
    private val lessDark = 20
    private val darker = 30
    private val darkest = 45
    private val evenFieldComputerMoveColor = evenFieldColor.darken(minusRed = darker, minusGreen = lessDark, minusBlue = darker)
    private val oddFieldComputerMoveColor = oddFieldColor.darken(minusRed = darker, minusGreen = lessDark, minusBlue = darker)
    private val evenFieldHumanMoveColor = evenFieldColor.darken(minusRed = darker, minusGreen = darker, minusBlue = darker)
    private val oddFieldHumanMoveColor = oddFieldColor.darken(minusRed = darkest, minusGreen = darkest, minusBlue = darkest)
    private var lastComputerMoveTo: Position? = null
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

    fun repaintAfterMove(extendedMove: ExtendedMove) {
        val move = extendedMove.move
        run {
            val isComputerMove = extendedMove.colorOfMove != isWhiteView

            if (isComputerMove) {
                lastComputerMoveTo = move.to
            } else {
                val computerMoveToMemory = lastComputerMoveTo
                lastComputerMoveTo = null
                computerMoveToMemory?.let {
                    repaintPositionAtOnce(it)
                }
            }

            if (isComputerMove) {
                adapter.resendLatestMousePos()
            }
        }

        repaintPositionAtOnce(move.from)
        repaintPositionAtOnce(move.to)

        if (extendedMove.isEnpassent) {
            repaintPositionAtOnce(Position.get(move.from.row, move.to.column))
            repaintPositionAtOnce(Position.get(move.to.row, move.from.column))
        } else if (extendedMove.isCastling) {
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
        lastComputerMoveTo = null
        val (width, height) = size!!
        paintImmediately(0, 0, width, height)
    }

    override fun paintComponent(g: Graphics) {
        paintBoard(g)
        paintActiveAreas(g)
        paintFigures(g)
    }

    private fun paintBoard(g: Graphics) {
        g.color = oddFieldColor
        g.fillRect(0, 0, 2 * borderSize + 8 * areaSize, 2 * borderSize + 8 * areaSize)
        g.color = evenFieldColor
        g.drawRect(borderSize, borderSize, 8 * areaSize, 8 * areaSize)
        for (i in 0..6 step 2) {
            for (j in 1..7 step 2) {
                g.fillRect(borderSize + i * areaSize, borderSize + j * areaSize, areaSize, areaSize)
                g.fillRect(borderSize + (i + 1) * areaSize, borderSize + (j - 1) * areaSize, areaSize, areaSize)
            }
        }
    }

    private fun paintFigures(g: Graphics) {
        for (figure in game.getFigures()) {
            val pos = figure.position
            val xPos = borderSize + areaSize * if (isWhiteView) pos.column else 7 - pos.column
            val yPos = borderSize + areaSize * if (isWhiteView) 7 - pos.row else pos.row

            val paintableFigure = figureGallery.getPaintable(figure)
            paintableFigure.paintOn(g, xPos, yPos, areaSize)
        }
    }

    private fun paintActiveAreas(g: Graphics) {
        fun paintArea(pos: Position, evenColor: Color, oddColor: Color) {
            g.color = if (pos.isEven()) evenColor else oddColor
            val (xPos, yPos) = pos.boardCoordinates(isWhiteView, borderSize, areaSize)
            val offset = areaSize/20
            val markedAreaSize = areaSize-(offset shl 1 )
            g.fillRect(xPos+offset, yPos+offset, markedAreaSize, markedAreaSize)
        }

        lastComputerMoveTo?.let {
            paintArea(it, evenFieldComputerMoveColor, oddFieldComputerMoveColor)
        }

        val lockedFromPos = from
        if (lockedFromPos != null) {
            paintArea(lockedFromPos, evenFieldHumanMoveColor, oddFieldHumanMoveColor)
        }
        val lockedToPos = to
        if (lockedToPos != null) {
            paintArea(lockedToPos, evenFieldHumanMoveColor, oddFieldHumanMoveColor)
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
            val lockedFromPos = from
            if (lockedFromPos != null) {
                from = null
                repaintPositionAtOnce(lockedFromPos)
            }
            from = pos
        } else {
            val lockedToPos = to
            if (lockedToPos != null) {
                to = null
                repaintPositionAtOnce(lockedToPos)
            }
            to = pos
        }
        if (pos != null) {
            repaintPositionAtOnce(pos)
        }
    }
}

private fun Color.darken(minusRed: Int = 0, minusGreen: Int = 0, minusBlue: Int = 0) = Color((red - minusRed).coerceIn(0, 255), (green - minusGreen).coerceIn(0, 255), (blue - minusBlue).coerceIn(0, 255))

private fun Position.isEven() = (row + column) % 2 == 0

private fun Position.boardCoordinates(isWhiteView: Boolean, borderSize: Int, areaSize: Int): Point {
    val xPos = borderSize + areaSize * if (isWhiteView) column else 7 - column
    val yPos = borderSize + areaSize * if (isWhiteView) 7 - row else row
    return Point(xPos, yPos)
}

operator fun Point.component1() = x
operator fun Point.component2() = y

operator fun Dimension.component1() = width
operator fun Dimension.component2() = height
