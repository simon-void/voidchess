package voidchess.ui

import voidchess.player.ki.evaluation.*

import javax.swing.*
import java.awt.*


class ComputerPlayerComponent : JComponent(), ComputerPlayerUI {

    private var bubbleText: String? = "let's play"
    private var smileFactor = HappinessLevel.CONTENT
    private var value: Evaluated = Draw
    private var showThoughts = false
    private var showValue = false
    private var index: Int = 0
    private var total: Int = 1

    init {
        preferredSize = Dimension(200, 378)
    }

    override fun init() {
        bubbleText = "make your move"
        value = Draw
        smileFactor = HappinessLevel.CONTENT
        showThoughts = false
        showValue = false
        index = 0
        total = 1

        paintImmediately(0, 0, preferredSize.width, preferredSize.height)
    }

    override fun setBubbleText(msg: String?) {
        val oldBubbleText = bubbleText
        bubbleText = msg

        if(bubbleText!=oldBubbleText) {
            paintImmediately(SPEECH_BUBBLE_RECTANGLE)
        }
    }

    override fun showThoughts(show: Boolean) {
        val needsRepaint = show.xor(showThoughts)
        showThoughts = show
        if(needsRepaint) {
            paintImmediately(THOUGHT_RECTANGLE)
            paintImmediately(HAND_RECTANGLE)
        }
    }

    override fun setProgress(computedMoves: Int, totalMoves: Int) {
        showThoughts = true
        showValue = false
        index = computedMoves
        total = totalMoves
        setBubbleText(null)

        paintImmediately(THOUGHT_CONTENT_RECTANGLE)
    }

    override fun setValue(value: Evaluated) {
        showValue = true
        smileFactor = value.getHappinessLevel()
        this.value = value

        paintImmediately(HAND_RECTANGLE)
        paintImmediately(MOUTH_RECTANGLE)
        paintImmediately(THOUGHT_CONTENT_RECTANGLE)
    }

    override fun paintComponent(g: Graphics) {
        g.color = Color.WHITE
        val dim = size
        g.fillRect(0, 0, dim.width, dim.height)

        drawFace(g)
        drawMouth(g)
        drawHand(g)
        drawProgress(g)
        drawSpeechBubble(g)
    }

    private fun drawFace(g: Graphics) {
        g.color = Color.BLACK

        g.drawRect(HAT_START_X, HAT_START_Y, HAT_WIDTH, HAT_HEIGHT)    // hat
        g.drawLine(HAT_START_X + HAT_WIDTH / 2 - KIN_WIDTH / 2,    // chin
                HEAD_END_Y,
                HAT_START_X + HAT_WIDTH / 2 + KIN_WIDTH / 2,
                HEAD_END_Y)
        g.drawLine(HAT_START_X + HAT_WIDTH / 2 - KIN_WIDTH / 2,    // left face
                HEAD_END_Y,
                HAT_START_X,
                HAT_START_Y + HAT_HEIGHT)
        g.drawLine(HAT_START_X + HAT_WIDTH / 2 + KIN_WIDTH / 2,    // right face
                HEAD_END_Y,
                HEAD_END_X,
                HAT_START_Y + HAT_HEIGHT)

        g.color = Color.DARK_GRAY
        g.fillOval(FACE_MIDDLE - GLASS_DISTANCE / 2 - GLASS_DIAMETER, // left eye-glass
                EYE_HEIGHT - GLASS_DIAMETER / 2,
                GLASS_DIAMETER,
                GLASS_DIAMETER)
        g.fillOval(FACE_MIDDLE + GLASS_DISTANCE / 2,                  // right eye-glass
                EYE_HEIGHT - GLASS_DIAMETER / 2,
                GLASS_DIAMETER,
                GLASS_DIAMETER)

        g.color = Color.black
        g.drawOval(FACE_MIDDLE - GLASS_DISTANCE / 2 - GLASS_DIAMETER, // left eye-glass's frame
                EYE_HEIGHT - GLASS_DIAMETER / 2,
                GLASS_DIAMETER,
                GLASS_DIAMETER)
        g.drawOval(FACE_MIDDLE + GLASS_DISTANCE / 2,                  // right eye-glass's frame
                EYE_HEIGHT - GLASS_DIAMETER / 2,
                GLASS_DIAMETER,
                GLASS_DIAMETER)

        g.drawLine(FACE_MIDDLE - GLASS_DISTANCE / 2,                 // glasses bridge
                EYE_HEIGHT,
                FACE_MIDDLE + GLASS_DISTANCE / 2,
                EYE_HEIGHT)
        g.drawLine(FACE_MIDDLE,                                          // nose
                NOSE_START_Y,
                FACE_MIDDLE,
                NOSE_START_Y + NOSE_LENGTH)
    }

    private fun drawMouth(g: Graphics) {
        g.color = Color.BLACK

        g.drawLine(FACE_MIDDLE - MOUTH_WIDTH / 2 + 10, // mouth
                MOUTH_HEIGHT,
                FACE_MIDDLE + MOUTH_WIDTH / 2 - 10,
                MOUTH_HEIGHT)
        g.drawLine(FACE_MIDDLE - MOUTH_WIDTH / 2,
                MOUTH_HEIGHT - smileFactor.mouthCornerEffect,
                FACE_MIDDLE - MOUTH_WIDTH / 2 + 10,    // left mouth corner
                MOUTH_HEIGHT)
        g.drawLine(FACE_MIDDLE + MOUTH_WIDTH / 2,
                MOUTH_HEIGHT - smileFactor.mouthCornerEffect,
                FACE_MIDDLE + MOUTH_WIDTH / 2 - 10,    // right mouth corner
                MOUTH_HEIGHT)
    }

    private fun drawHand(g: Graphics) {
        g.color = Color.BLACK

        if(smileFactor==HappinessLevel.BIG_GRIEF) {
            g.drawRect(HAND_START_X,                              // hand
                    HAND_START_Y,
                    HAND_WITH,
                    HAND_HEIGHT)
            g.drawRect(HAND_START_X + HAND_WITH - THUMB_WIDTH, // thumb
                    HAND_START_Y - THUMB_HEIGHT,
                    THUMB_WIDTH,
                    THUMB_HEIGHT)

            for (i in 1..3) {
                val lineHeight = i * HAND_HEIGHT / 4        // finger lines
                g.drawLine(HAND_START_X,
                        HAND_START_Y + lineHeight,
                        HAND_START_X + FINGER_LINE_LENGTH,
                        HAND_START_Y + lineHeight)
            }
        } else if(smileFactor==HappinessLevel.BIG_SMILE) {
            g.drawRect(HAND_START_X,                              // hand
                    HAND_START_Y,
                    HAND_WITH,
                    HAND_HEIGHT)
            g.drawRect(HAND_START_X,                              // thumbs
                    HAND_START_Y + HAND_HEIGHT,
                    THUMB_WIDTH,
                    THUMB_HEIGHT)

            for (i in 1..3) {                                // finger lines
                val lineHeight = i * HAND_HEIGHT / 4
                g.drawLine(HAND_START_X + HAND_WITH,
                        HAND_START_Y + lineHeight,
                        HAND_START_X + HAND_WITH - FINGER_LINE_LENGTH,
                        HAND_START_Y + lineHeight)
            }
        }
    }

    private fun drawProgress(g: Graphics) {
        if(showThoughts) {
            g.color = Color.BLACK
            drawRoundRect(g, 30, 30, 140, 50)
            g.drawOval(20, 80, 20, 20)
            g.drawOval(30, 110, 10, 10)

            if (showValue) {
                g.color = Color.WHITE
                g.fillRect(49, 46, 110, 20)
                g.color = Color.BLACK
                drawCenteredString(g, value.toString(), 100, 60)
            } else {
                g.color = Color.WHITE
                g.fillRect(49, 46, 110, 20)
                g.color = Color.BLACK
                g.drawRect(49, 46, 101, 18)
                g.color = Color.DARK_GRAY
                val progress = Math.ceil(100 * index / total.toDouble()).toInt()
                g.fillRect(50, 47, progress, 17)
            }
        }
    }

    private fun drawCenteredString(g: Graphics, text: String, centeredX: Int, y: Int): Int {
        val metric = g.fontMetrics
        val textHalfWidth = metric.stringWidth(text) / 2
        g.drawString(text, centeredX - textHalfWidth, y)
        return textHalfWidth
    }

    private fun drawRoundRect(g: Graphics, x: Int, y: Int, w: Int, h: Int) {
        g.drawArc(x, y, h, h, 90, 180)
        g.drawArc(x + w - h, y, h, h, 270, 180)
        g.drawLine(x + h / 2, y, x + w - h / 2, y)
        g.drawLine(x + h / 2, y + h, x + w - h / 2, y + h)
    }

    private fun drawSpeechBubble(g: Graphics) {

//        HAND_RECTANGLE.apply {
//            g.color = Color.BLUE
//            g.drawRect(x, y, width, height)
//        }
//        SPEECH_BUBBLE_RECTANGLE.apply {
//            g.color = Color.RED
//            g.drawRect(x, y, width, height)
//        }
        bubbleText?.let {text ->
            g.color = Color.BLACK
            val centeredX = 80
            val textBorderWidth = 6
            val textBorderHeight = 4
            val bubbleY = 320
            val lineHight = 13
            val lines = text.split('\n')
            var maxHalfTextWidth = 0
            // print the text
            lines.forEachIndexed { i, line ->
                val halfTextWidth = drawCenteredString(g, line, centeredX, bubbleY+textBorderHeight+(i+1)*lineHight)
                if( halfTextWidth>maxHalfTextWidth) {
                    maxHalfTextWidth = halfTextWidth
                }
            }
            // paint the bubble
            val bubbleHeight = lines.size*lineHight+textBorderHeight*2+1
            val bubbleWidth = (maxHalfTextWidth+textBorderWidth)*2
            g.drawRect(centeredX-maxHalfTextWidth-textBorderWidth, bubbleY, bubbleWidth, bubbleHeight)
            g.drawLine(centeredX+5, bubbleY, centeredX-10, bubbleY-10)
            g.drawLine(centeredX-5, bubbleY, centeredX-10, bubbleY-10)
            g.color = Color.white
            g.drawLine(centeredX-4, bubbleY, centeredX+4, bubbleY)
        }
    }

    companion object {
        private const val HAT_HEIGHT = 40
        private const val HAT_WIDTH = 100
        private const val FACE_HEIGHT = 130
        private const val KIN_WIDTH = 60
        private const val GLASS_DIAMETER = 50
        private const val GLASS_DISTANCE = 10
        private const val HAT_START_X = 20
        private const val HAT_START_Y = 130
        private const val HEAD_END_X = HAT_START_X + HAT_WIDTH
        private const val HEAD_END_Y = HAT_START_Y + HAT_HEIGHT + FACE_HEIGHT
        private const val FACE_MIDDLE = HAT_START_X + HAT_WIDTH / 2
        private const val EYE_HEIGHT = HAT_START_Y + HAT_HEIGHT + FACE_HEIGHT / 4
        private const val NOSE_START_Y = EYE_HEIGHT + 20
        private const val NOSE_LENGTH = FACE_HEIGHT / 5
        private const val MOUTH_HEIGHT = HEAD_END_Y - FACE_HEIGHT / 4
        private const val MOUTH_WIDTH = 34

        private const val HAND_START_X = HEAD_END_X + 25
        private const val HAND_START_Y = HEAD_END_Y - 30
        private const val HAND_WITH = 50
        private const val HAND_HEIGHT = HAND_WITH * 4 / 3
        private const val THUMB_HEIGHT = HAND_WITH * 2 / 3
        private const val THUMB_WIDTH = HAND_WITH / 3
        private const val FINGER_LINE_LENGTH = HAND_WITH * 3 / 4

        private val MOUTH_RECTANGLE = Rectangle(FACE_MIDDLE - MOUTH_WIDTH / 2 - 1,
                MOUTH_HEIGHT - 8,
                MOUTH_WIDTH + 2,
                16)
        private val HAND_RECTANGLE = Rectangle(HAND_START_X - 1,
                HAND_START_Y - THUMB_HEIGHT - 1,
                HAND_WITH + 2,
                HAND_HEIGHT + 2 * THUMB_HEIGHT + 2)
        private val THOUGHT_CONTENT_RECTANGLE = Rectangle(49, 46, 102, 19)
        private val THOUGHT_RECTANGLE = Rectangle(14, 28, 160, 95)
        private val SPEECH_BUBBLE_RECTANGLE = Rectangle(17, 308, 126, 60)
    }
}

enum class HappinessLevel(val value: Int, val mouthCornerEffect: Int) {
    BIG_SMILE(8, 4),
    MEDIUM_SMILE(4, 3),
    LIGHT_SMILE(2,2 ),
    SLIGHT_SMILE(1,1),
    CONTENT(0, 0),
    SLIGHT_GRIEF(-1, -1),
    LIGHT_GRIEF(-2, -2),
    MEDIUM_GRIEF(-4, -3),
    BIG_GRIEF(-8, -4)
}

private fun Evaluated.getHappinessLevel(): HappinessLevel {
    when (this) {
        is CheckmateOther -> return HappinessLevel.BIG_SMILE
        is CheckmateSelf -> return HappinessLevel.BIG_GRIEF
        is Draw -> return HappinessLevel.CONTENT
        is Ongoing -> {
            val value = this.combinedEvaluation
            return when {
                value > HappinessLevel.MEDIUM_SMILE.value -> HappinessLevel.MEDIUM_SMILE
                value > HappinessLevel.LIGHT_SMILE.value -> HappinessLevel.LIGHT_SMILE
                value > HappinessLevel.SLIGHT_SMILE.value -> HappinessLevel.SLIGHT_SMILE
                value < HappinessLevel.MEDIUM_GRIEF.value -> HappinessLevel.MEDIUM_GRIEF
                value < HappinessLevel.LIGHT_GRIEF.value -> HappinessLevel.LIGHT_GRIEF
                value < HappinessLevel.SLIGHT_GRIEF.value -> HappinessLevel.SLIGHT_GRIEF
                else -> HappinessLevel.CONTENT
            }
        }
    }
}