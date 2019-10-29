package voidchess.ui

import voidchess.board.BasicChessBoard
import voidchess.board.forAllFigures
import voidchess.board.move.ExtendedMove
import voidchess.board.move.Position
import voidchess.image.FigureGallery
import voidchess.player.human.HumanPlayerInterface
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.image.ImageObserver
import javax.swing.BorderFactory
import javax.swing.JComponent


class ChessboardComponent constructor(private val game: BasicChessBoard, imageObserver: ImageObserver) : JComponent() {
    val areaSize: Int = 50
    val borderSize: Int = 25
    private val adapter: ChessboardAdapter
    private val figureGallery = FigureGallery(imageObserver, areaSize)
    var isWhiteView: Boolean = true
    private val evenFieldColor = Color.lightGray
    private val oddFieldColor = Color.white
    private val lessDark = 20
    private val darker = 30
    private val darkest = 45
    private val evenFieldComputerMoveColor = evenFieldColor.darken(minusRed = darker, minusGreen = lessDark, minusBlue = darker)
    private val oddFieldComputerMoveColor = oddFieldColor.darken(minusRed = darker, minusGreen = lessDark, minusBlue = darker)
    private val evenFieldHoverColor = evenFieldColor.darken(minusRed = darker, minusGreen = darker, minusBlue = darker)
    private val oddFieldHoverColor = oddFieldColor.darken(minusRed = darkest, minusGreen = darkest, minusBlue = darkest)
    private val evenFieldHumanMoveColor = evenFieldColor.darken(minusRed = darker, minusGreen = lessDark, minusBlue = 10)
    private val oddFieldHumanMoveColor = oddFieldColor.darken(minusRed = darker, minusGreen = lessDark, minusBlue = 10)
    private var lastComputerMoveTo: Position? = null
    private var hover: Position? = null
    private var from: Position? = null
    private var to: Position? = null

    init {
        preferredSize = Dimension(2 * borderSize + 8 * areaSize, 2 * borderSize + 8 * areaSize)
        border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.black),
                BorderFactory.createBevelBorder(0, Color.gray, Color.darkGray)
        )
        adapter = ChessboardAdapter(this)
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

        if (extendedMove.isEnPassant) {
            repaintPositionAtOnce(Position[move.from.row, move.to.column])
            repaintPositionAtOnce(Position[move.to.row, move.from.column])
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
        game.forAllFigures {figure->
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

        val lockedHoverPos = hover
        if (lockedHoverPos != null) {
            paintArea(lockedHoverPos, evenFieldHoverColor, oddFieldHoverColor)
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
        // the adapter needs its HumanPlayerInterface before it can receive events (because of lateinit)
        addMouseListener(adapter)
        addMouseMotionListener(adapter)
    }

    fun markPosition(pos: Position, posType: PosType) {
        when (posType) {
            PosType.HOVER_FROM -> hover = pos
            PosType.SELECT_FROM -> from = pos
            PosType.HOVER_TO -> to = pos
        }
        repaintPositionAtOnce(pos)
    }

    fun unmarkPosition(posType: PosType) {
        when (posType) {
            PosType.HOVER_FROM -> {
                val lockedHoverPos = hover
                hover = null
                lockedHoverPos?.let { repaintPositionAtOnce(it) }
            }
            PosType.SELECT_FROM -> {
                val lockedFromPos = from
                from = null
                lockedFromPos?.let { repaintPositionAtOnce(it) }
            }
            PosType.HOVER_TO -> {
                val lockedToPos = to
                to = null
                lockedToPos?.let { repaintPositionAtOnce(it) }
            }
        }
    }
}

enum class PosType {
    HOVER_FROM, SELECT_FROM, HOVER_TO
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
