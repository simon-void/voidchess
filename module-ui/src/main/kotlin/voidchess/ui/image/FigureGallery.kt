package voidchess.ui.image

import voidchess.common.figures.Figure
import voidchess.common.figures.FigureType
import java.awt.Graphics
import java.awt.Image
import java.awt.image.ImageObserver

internal class FigureGallery(
    imageObserver: ImageObserver,
    imageWidthHeight: Int
) {
    private val whitePaintables: Array<Paintable> = FigureType.entries.map { figureType ->
        Paintable(imageObserver, ImageLoader.getFigureImage(figureType, true, imageWidthHeight))
    }.toTypedArray()
    private val blackPaintables: Array<Paintable> = FigureType.entries.map { figureType ->
        Paintable(imageObserver, ImageLoader.getFigureImage(figureType, false, imageWidthHeight))
    }.toTypedArray()

    fun getPaintable(figure: Figure): Paintable {
        val paintableIndex = figure.type.ordinal
        return if (figure.isWhite) {
            whitePaintables[paintableIndex]
        } else {
            blackPaintables[paintableIndex]
        }
    }
}

internal class Paintable(
    private val observer: ImageObserver,
    private val image: Image
) {
    fun paintOn(g: Graphics, xPos: Int, yPos: Int, areaSize: Int) {
        g.drawImage(image, xPos, yPos, areaSize, areaSize, observer)
    }
}
