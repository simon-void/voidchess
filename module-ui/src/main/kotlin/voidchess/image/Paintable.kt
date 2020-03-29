package voidchess.image

import java.awt.*
import java.awt.image.ImageObserver


class Paintable(private val observer: ImageObserver, private val image: Image) {

    fun paintOn(g: Graphics, x_pos: Int, y_pos: Int, areaSize: Int) {
        g.drawImage(image, x_pos, y_pos, areaSize, areaSize, observer)
    }
}
