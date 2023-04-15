package voidchess.ui.image

import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.bridge.BridgeContext
import org.apache.batik.bridge.GVTBuilder
import org.apache.batik.bridge.UserAgentAdapter
import org.apache.batik.bridge.ViewBox
import org.apache.batik.gvt.GraphicsNode
import org.apache.batik.util.XMLResourceDescriptor
import org.w3c.dom.svg.SVGDocument
import voidchess.common.figures.FigureType
import voidchess.common.helper.getResourceStream
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.InputStream


internal object ImageLoader {
    private val whiteFigureImages = Array(FigureType.values().size) {
        val figureType = FigureType.values()[it]
        return@Array loadFigure(figureType, true)
    }
    private val blackFigureImages = Array(FigureType.values().size) {
        val figureType = FigureType.values()[it]
        return@Array loadFigure(figureType, false)
    }

    fun getFigureImage(figureType: FigureType, isWhite: Boolean, widthHeight: Int): Image {
        return if (isWhite) {
            whiteFigureImages[figureType.ordinal].getSquareImage(widthHeight)
        } else {
            blackFigureImages[figureType.ordinal].getSquareImage(widthHeight)
        }
    }

    /**
     * for svg images
     */
    private fun loadSvgImage(fileName: String): SvgImage {
        val svgInfo = SvgChessSetInfo("merida", 110)
        val relativePath = "image/${svgInfo.subfolder}/$fileName.svg"
        try {
            getResourceStream(javaClass, "module-ui", relativePath).use {
                return SvgImageFactory.createSquareImage(it, svgInfo.svgPageSquareWidth)
            }
        } catch (e: Exception) {
            throw IllegalStateException("couldn't find svg: $relativePath", e)
        }
    }



    private fun loadFigure(figureType: FigureType, isWhite: Boolean): SvgImage {
        val figureImgFile = "${if(isWhite) "w" else "b"}_${figureType.name.lowercase()}"
        return loadSvgImage(figureImgFile)
    }
}

private data class SvgChessSetInfo(
    val subfolder: String,
    val svgPageSquareWidth: Int
)

private object SvgImageFactory {
    private val factory = SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName())

    private fun createRectImage(inputStream: InputStream, svgPageDimension: Dimension): SvgImage {
        val svgDocument = factory.createSVGDocument(null, inputStream)
        return SvgImage(svgDocument, svgPageDimension)
    }

    fun createSquareImage(inputStream: InputStream, svgPageSquareWidth: Int): SvgImage {
        return createRectImage(inputStream, Dimension(svgPageSquareWidth, svgPageSquareWidth))
    }
}

/**
 * Immutable class to get the Image representation of a svg resource.
 */
private class SvgImage(
    private val svgDocument: SVGDocument,
    private val svgPageDimension: Dimension
) {
    private val bridgeContext: BridgeContext = BridgeContext(UserAgentAdapter())
    private val rootSvgNode: GraphicsNode = GVTBuilder().build(bridgeContext, svgDocument)

    /**
     * Renders and returns the svg based image.
     *
     * @param width desired width
     * @param height desired height
     * @return image of the rendered svg.
     */
    private fun getRectImage(width: Int, height: Int): Image {
        // Paint svg into image buffer
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = bufferedImage.graphics as Graphics2D

        // For a smooth graphic with no jagged edges or rastorized look.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

        // Scale image to desired size
        val elt = svgDocument.rootElement
        val usr2dev = ViewBox.getViewTransform(null, elt, width.toFloat(), height.toFloat(), bridgeContext)
        g2d.transform(usr2dev)
        g2d.scale(
            width.toDouble() / svgPageDimension.width.toDouble(),
            height.toDouble() / svgPageDimension.height.toDouble()
        )

        rootSvgNode.paint(g2d)

        // Cleanup and return image
        g2d.dispose()
        return bufferedImage
    }

    fun getSquareImage(widthHeight: Int): Image = getRectImage(widthHeight, widthHeight)
}
