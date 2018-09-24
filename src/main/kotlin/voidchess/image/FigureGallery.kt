package voidchess.image

import voidchess.figures.Figure
import voidchess.figures.FigureType
import java.awt.image.ImageObserver

class FigureGallery(private val imageObserver: ImageObserver) {
    private val whitePaintables: Array<Paintable>
    private val blackPaintables: Array<Paintable>

    init{
        whitePaintables = Array(FigureType.values().size) {
            Paintable(imageObserver, ImageLoader.getFigureImage(FigureType.values()[it], true))
        }
        blackPaintables = Array(FigureType.values().size) {
            Paintable(imageObserver, ImageLoader.getFigureImage(FigureType.values()[it], false))
        }
    }

    fun getPaintable(figure: Figure): Paintable {
        val paintableIndex = figure.type.ordinal
        return if(figure.isWhite) {
            whitePaintables[paintableIndex]
        }else{
            blackPaintables[paintableIndex]
        }
    }
}