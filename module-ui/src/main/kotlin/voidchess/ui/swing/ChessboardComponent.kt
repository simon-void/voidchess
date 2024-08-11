package voidchess.ui.swing

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.forAllFigures
import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Position
import voidchess.ui.image.FigureGallery
import voidchess.ui.player.BoardUiListener
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.event.MouseEvent
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.event.MouseInputListener


internal class ChessboardComponent(
    private val game: StaticChessBoard
) : JComponent() {

    val areaSize: Int = 55
    val borderSize: Int = 25
    private var _isWhiteView = true
    val isWhiteView: Boolean get() = _isWhiteView
    private var isComputerMove = false
    private val adapter = ChessboardAdapter(this)
    private val figureGallery = FigureGallery(this, areaSize)
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
    private val lock: Lock = ReentrantLock()
    private var lastComputerMoveTo: Position? = null
    private var markedPositions: MarkedPositions = MarkedPositions.None

    init {
        preferredSize = Dimension(2 * borderSize + 8 * areaSize, 2 * borderSize + 8 * areaSize)
        border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.black),
                BorderFactory.createBevelBorder(0, Color.gray, Color.darkGray)
        )
        isDoubleBuffered = true
    }

    fun postConstruct(boardUiListener: BoardUiListener) {
        this.adapter.setPlayer(boardUiListener)
        // the adapter needs its HumanPlayerInterface before it can receive events (because of lateinit)
        addMouseListener(this.adapter)
        addMouseMotionListener(this.adapter)
    }

    fun startNewGame() {
        isComputerMove = !_isWhiteView
        repaintAtOnce()
    }

    fun repaintAfterMove(extendedMove: ExtendedMove) {
        synchronized(lock) {
            val move = if (extendedMove is ExtendedMove.Castling) extendedMove.kingMove else extendedMove.move
            run {
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

                isComputerMove = !isComputerMove
            }

            repaintPositionAtOnce(move.from)
            repaintPositionAtOnce(move.to)

            when (extendedMove) {
                is ExtendedMove.Enpassant -> {
                    repaintPositionAtOnce(Position[move.from.row, move.to.column])
                    repaintPositionAtOnce(Position[move.to.row, move.from.column])
                }
                is ExtendedMove.Castling -> {
                    repaintPositionAtOnce(extendedMove.rookMove.from)
                    repaintPositionAtOnce(extendedMove.rookMove.to)
                }
                else -> {
                }
            }
        }
    }

    private fun repaintPositionAtOnce(pos: Position) {
        val xPos = borderSize + areaSize * if (_isWhiteView) pos.column else 7 - pos.column
        val yPos = borderSize + areaSize * if (_isWhiteView) 7 - pos.row else pos.row

        val repaintSize = areaSize + 1
        paintImmediately(xPos, yPos, repaintSize, repaintSize)
    }

    fun repaintAtOnce() {
        synchronized(lock) {
            lastComputerMoveTo = null
            val (width, height) = size!!
            paintImmediately(0, 0, width, height)
        }
    }

    override fun paintComponent(g: Graphics) {
        synchronized(lock) {
            paintBoard(g)
            paintActiveAreas(g)
            paintFigures(g)
        }
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
            val xPos = borderSize + areaSize * if (_isWhiteView) pos.column else 7 - pos.column
            val yPos = borderSize + areaSize * if (_isWhiteView) 7 - pos.row else pos.row

            val paintableFigure = figureGallery.getPaintable(figure)
            paintableFigure.paintOn(g, xPos, yPos, areaSize)
        }
    }

    private fun paintActiveAreas(g: Graphics) {
        fun paintArea(pos: Position, evenColor: Color, oddColor: Color) {
            g.color = if (pos.isEven()) evenColor else oddColor
            val (xPos, yPos) = pos.boardCoordinates(_isWhiteView, borderSize, areaSize)
            val offset = areaSize/20
            val markedAreaSize = areaSize-(offset shl 1 )
            g.fillRect(xPos+offset, yPos+offset, markedAreaSize, markedAreaSize)
        }

        lastComputerMoveTo?.let {
            paintArea(it, evenFieldComputerMoveColor, oddFieldComputerMoveColor)
        }

        markedPositions.possibleFrom?.let {
            paintArea(it, evenFieldHoverColor, oddFieldHoverColor)
        }
        markedPositions.from?.let {
            paintArea(it, evenFieldHumanMoveColor, oddFieldHumanMoveColor)
        }
        markedPositions.possibleTo?.let {
            paintArea(it, evenFieldHumanMoveColor, oddFieldHumanMoveColor)
        }
    }

    fun setViewPoint(fromWhite: Boolean) {
        synchronized(lock) {
            _isWhiteView = fromWhite
            repaint()
        }
    }

    fun updateMarkedPositions(newMarkedPositions: MarkedPositions) {
        fun repaintIfMarkingChanged(oldMarkedPos: Position?, newMarkedPos: Position?) {
            if(oldMarkedPos!=newMarkedPos) {
                oldMarkedPos?.let { repaintPositionAtOnce(it) }
                newMarkedPos?.let { repaintPositionAtOnce(it) }
            }
        }
        synchronized(lock) {
            val oldMarkedPositions = markedPositions
            markedPositions = newMarkedPositions

            repaintIfMarkingChanged(oldMarkedPositions.possibleFrom, newMarkedPositions.possibleFrom)
            repaintIfMarkingChanged(oldMarkedPositions.from, newMarkedPositions.from)
            repaintIfMarkingChanged(oldMarkedPositions.possibleTo, newMarkedPositions.possibleTo)
        }
    }
}

private class ChessboardAdapter(
    private val ui: ChessboardComponent
) : MouseInputListener {
    private lateinit var adapter: BoardUiListener
    private var lastMouseMovedPos: Position? = null

    override fun mousePressed(e: MouseEvent) {}
    override fun mouseReleased(e: MouseEvent) {}
    override fun mouseEntered(e: MouseEvent) {}
    override fun mouseExited(e: MouseEvent) {}
    override fun mouseDragged(e: MouseEvent) {}


    fun setPlayer(boardUiListener: BoardUiListener) {
        adapter = boardUiListener
    }

    fun resendLatestMousePos() = adapter.mouseMovedOver(lastMouseMovedPos)

    override fun mouseMoved(e: MouseEvent) {
        val pos = getPositionFromPoint(e.point)
        if(lastMouseMovedPos !== pos){
            lastMouseMovedPos = pos
            adapter.mouseMovedOver(pos)
        }
    }

    override fun mouseClicked(e: MouseEvent) {
        getPositionFromPoint(e.point)?.let { pos ->
            adapter.mouseClickedOn(pos)
        }
    }

    private fun getPositionFromPoint(p: Point): Position? {
        val borderSize = ui.borderSize
        val areaSize = ui.areaSize
        val isWhiteView = ui.isWhiteView

        val onFieldsX = p.x - borderSize
        val onFieldsY = p.y - borderSize
        if (onFieldsX < 0 || onFieldsY < 0) return null

        var x = onFieldsX / areaSize
        var y = onFieldsY / areaSize

        if (x > 7 || y > 7) return null

        if (isWhiteView) {
            y = 7 - y
        } else {
            x = 7 - x
        }

        return Position[y, x]
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

internal sealed class MarkedPositions(
    val possibleFrom: Position? = null,
    val from: Position? = null,
    val possibleTo: Position? = null,
) {
    data object None: MarkedPositions()
    class PossibleFrom(possibleFrom: Position): MarkedPositions(possibleFrom = possibleFrom)
    class From(from: Position): MarkedPositions(from = from)
    class FromAndPossibleTo(from: Position, possibleTo: Position): MarkedPositions(from = from, possibleTo = possibleTo)
    class FromAndPossibleFrom(from: Position, possibleFrom: Position): MarkedPositions(from = from, possibleFrom = possibleFrom)
}