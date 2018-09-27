package voidchess.ui

import voidchess.helper.Position
import voidchess.player.HumanPlayerInterface
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.event.MouseInputListener


class ChessboardAdapter internal constructor(private val ui: ChessboardUI) : MouseInputListener {
    private var player: HumanPlayerInterface? = null
    private var lastMouseMovedPos: Position? = null

    override fun mousePressed(e: MouseEvent) {}
    override fun mouseReleased(e: MouseEvent) {}
    override fun mouseEntered(e: MouseEvent) {}
    override fun mouseExited(e: MouseEvent) {}
    override fun mouseDragged(e: MouseEvent) {}


    override fun mouseMoved(e: MouseEvent) {
        getPositionFromPoint(e.point).let { pos ->
            player?.let { human ->
                if(lastMouseMovedPos!==pos){
                    lastMouseMovedPos = pos
                    human.mouseMovedOver(pos)
                }
            }
        }
    }

    override fun mouseClicked(e: MouseEvent) {
        getPositionFromPoint(e.point)?.let { pos ->
            player?.let { human ->
                human.mouseClickedOn(pos)
            }
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

        return Position.get(y, x)
    }

    fun setPlayer(human: HumanPlayerInterface) {
        player = human
    }
}
