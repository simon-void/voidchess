package voidchess.image

import voidchess.figures.FigureType

import javax.imageio.ImageIO
import java.awt.*

import voidchess.helper.getResourceStream


/**
 * for gif, png, jpg images
 */
private fun loadImage(fileName: String, fileType: String): Image {
    val relativePath = "image/$fileName.$fileType"
    try {
        getResourceStream(relativePath).use {
            return ImageIO.read(it)
        }
    } catch (e: Exception) {
        throw IllegalStateException("couldn't find image: $relativePath")
    }
}

/**
 * for svg images
 */
private fun loadSvgImage(svgInfo: SvgChessSetInfo, fileName: String): SvgImage {
    val relativePath = "image/${svgInfo.subfolder}/$fileName.svg"
    try {
        getResourceStream(relativePath).use {
            return SvgImageFactory.createSquareImage(it, svgInfo.svgPageSquareWidth)
        }
    } catch (e: Exception) {
        throw IllegalStateException("couldn't find image: $relativePath")
    }
}



private fun loadFigure(figureType:FigureType, isWhite: Boolean, svgInfo: SvgChessSetInfo): SvgImage {
    val figureImgFile = "${if(isWhite) "w" else "b"}_${figureType.name.toLowerCase()}"
    return loadSvgImage(svgInfo, figureImgFile)
}

object ImageLoader {
    private val svgInfo = SvgChessSetInfo("merida", 110)
    private val whiteFigureImages = Array(FigureType.values().size) {
        val figureType = FigureType.values()[it]
        return@Array loadFigure(figureType, true, svgInfo)
    }
    private val blackFigureImages = Array(FigureType.values().size) {
        val figureType = FigureType.values()[it]
        return@Array loadFigure(figureType, false, svgInfo)
    }

    val icon = loadImage("icon", "gif")

    fun getFigureImage(figureType: FigureType, isWhite: Boolean, widthHeight: Int): Image {
        return if (isWhite) {
            whiteFigureImages[figureType.ordinal].getSquareImage(widthHeight)
        } else {
            blackFigureImages[figureType.ordinal].getSquareImage(widthHeight)
        }
    }
}

data class SvgChessSetInfo constructor(val subfolder: String, val svgPageSquareWidth: Int)
