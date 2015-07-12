package voidchess.player.ki;

import voidchess.helper.Move;

import java.awt.*;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * @author stephan
 */
public class ComputerPlayerUI extends AbstractComputerPlayerUI {
    final static private int HAT_HEIGHT = 40;
    final static private int HAT_WIDTH = 100;
    final static private int FACE_HEIGHT = 130;
    final static private int KIN_WIDTH = 60;
    final static private int GLASS_DIAMETER = 50;
    final static private int GLASS_DISTANCE = 10;
    final static private int HAT_START_X = 20;
    final static private int HAT_START_Y = 130;
    final static private int HEAD_END_X = HAT_START_X + HAT_WIDTH;
    final static private int HEAD_END_Y = HAT_START_Y + HAT_HEIGHT + FACE_HEIGHT;
    final static private int FACE_MIDDLE = HAT_START_X + HAT_WIDTH / 2;
    final static private int EYE_HEIGHT = HAT_START_Y + HAT_HEIGHT + FACE_HEIGHT / 4;
    final static private int NOSE_START_Y = EYE_HEIGHT + 20;
    final static private int NOSE_LENGHT = FACE_HEIGHT / 5;
    final static private int MOUTH_HEIGHT = HEAD_END_Y - FACE_HEIGHT / 4;
    final static private int MOUTH_WIDTH = 34;

    final static private int HAND_START_X = HEAD_END_X;
    final static private int HAND_START_Y = HEAD_END_Y - 30;
    final static private int HAND_WITH = 50;
    final static private int HAND_HEIGHT = (HAND_WITH * 4) / 3;
    final static private int THUMB_HEIGHT = (HAND_WITH * 2) / 3;
    final static private int THUMB_WIDTH = HAND_WITH / 3;
    final static private int FINGER_LINE_LENGTH = (HAND_WITH * 3) / 4;

    final static private int BIG_SMILE = 4;
    final static private int LIGHT_SMILE = 2;
    final static private int SLIGHT_SMILE = 1;
    final static private int CONTENT = 0;
    final static private int SLIGHT_GRIEF = -1;
    final static private int LIGHT_GRIEF = -2;
    final static private int BIG_GRIEF = -4;

    final static private Rectangle MOUTH_RECTANGLE
            = new Rectangle(FACE_MIDDLE - MOUTH_WIDTH / 2 - 1,
            MOUTH_HEIGHT - 8,
            MOUTH_WIDTH + 2,
            16);
    final static private Rectangle HAND_RECTANGLE
            = new Rectangle(HAND_START_X - 1,
            HAND_START_Y - THUMB_HEIGHT - 1,
            HAND_WITH + 2,
            HAND_HEIGHT + 2 * THUMB_HEIGHT + 2);
    final static private Rectangle THOUGHT_RECTANGLE
            = new Rectangle(49, 46, 102, 19);

    private TreeSet<Float> referenceSet;
    private Evaluaded value;
    private int smileFactor;
    private boolean showValue;
    private int index;
    private int total;

    public ComputerPlayerUI() {
        showValue = true;
        index = 1;
        total = 1;
        smileFactor = CONTENT;
        value = Evaluaded.INITAL;

        referenceSet = new TreeSet(new InverseValueComperator());
        referenceSet.add(Evaluaded.getOtherPlayerIsMatt(20).getCombinedEvaluation());
        referenceSet.add(-4f);
        referenceSet.add(-1f);
        referenceSet.add(1f);
        referenceSet.add(4f);
        referenceSet.add(Evaluaded.getThisComputerPlayerIsMatt(20).getCombinedEvaluation());

        setPreferredSize(new Dimension(200, 378));
    }

    @Override
    public void setProgress(int index, int total) {
        showValue = false;
        this.index = index;
        this.total = total;

        paintImmediately(THOUGHT_RECTANGLE);
    }

    @Override
    public void setValue(Evaluaded value, Move move) {
        showValue = true;
        setSmileFactor(value.getCombinedEvaluation());
        this.value = value;
        paintImmediately(HAND_RECTANGLE);
        paintImmediately(MOUTH_RECTANGLE);
        paintImmediately(THOUGHT_RECTANGLE);
    }

    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        Dimension dim = getSize();
        g.fillRect(0, 0, dim.width, dim.height);

        drawFace(g);
        drawMouth(g);
        drawHand(g);
        drawProgress(g);
    }

    private void drawFace(Graphics g) {
        g.setColor(Color.BLACK);

        g.drawRect(HAT_START_X, HAT_START_Y, HAT_WIDTH, HAT_HEIGHT);    //Hut
        g.drawLine(HAT_START_X + HAT_WIDTH / 2 - KIN_WIDTH / 2,                        //Kinn
                HEAD_END_Y,
                HAT_START_X + HAT_WIDTH / 2 + KIN_WIDTH / 2,
                HEAD_END_Y);
        g.drawLine(HAT_START_X + HAT_WIDTH / 2 - KIN_WIDTH / 2,                        //linke Gesichtsseite
                HEAD_END_Y,
                HAT_START_X,
                HAT_START_Y + HAT_HEIGHT);
        g.drawLine(HAT_START_X + HAT_WIDTH / 2 + KIN_WIDTH / 2,                        //rechteGesichtsseite
                HEAD_END_Y,
                HEAD_END_X,
                HAT_START_Y + HAT_HEIGHT);

        g.setColor(Color.DARK_GRAY);
        g.fillOval(FACE_MIDDLE - GLASS_DISTANCE / 2 - GLASS_DIAMETER,        //linkes Auge
                EYE_HEIGHT - GLASS_DIAMETER / 2,
                GLASS_DIAMETER,
                GLASS_DIAMETER);
        g.fillOval(FACE_MIDDLE + GLASS_DISTANCE / 2,                                        //rechtes Auge
                EYE_HEIGHT - GLASS_DIAMETER / 2,
                GLASS_DIAMETER,
                GLASS_DIAMETER);

        g.setColor(Color.black);
        g.drawOval(FACE_MIDDLE - GLASS_DISTANCE / 2 - GLASS_DIAMETER,        //linkes Auge
                EYE_HEIGHT - GLASS_DIAMETER / 2,
                GLASS_DIAMETER,
                GLASS_DIAMETER);
        g.drawOval(FACE_MIDDLE + GLASS_DISTANCE / 2,                                        //rechtes Auge
                EYE_HEIGHT - GLASS_DIAMETER / 2,
                GLASS_DIAMETER,
                GLASS_DIAMETER);

        g.drawLine(FACE_MIDDLE - GLASS_DISTANCE / 2,                                        //BrillenBügel
                EYE_HEIGHT,
                FACE_MIDDLE + GLASS_DISTANCE / 2,
                EYE_HEIGHT);
        g.drawLine(FACE_MIDDLE,                                                                        //Nase
                NOSE_START_Y,
                FACE_MIDDLE,
                NOSE_START_Y + NOSE_LENGHT);
    }

    private void setSmileFactor(float value) {
        Float floatvalue = new Float(value);
        referenceSet.add(floatvalue);
        Iterator iter = referenceSet.iterator();
        if (iter.next().equals(floatvalue)) {
            smileFactor = BIG_SMILE;
        } else if (iter.next().equals(floatvalue)) {
            smileFactor = LIGHT_SMILE;
        } else if (iter.next().equals(floatvalue)) {
            smileFactor = SLIGHT_SMILE;
        } else if (iter.next().equals(floatvalue)) {
            smileFactor = CONTENT;
        } else if (iter.next().equals(floatvalue)) {
            smileFactor = SLIGHT_GRIEF;
        } else if (iter.next().equals(floatvalue)) {
            smileFactor = LIGHT_GRIEF;
        } else {
            smileFactor = BIG_GRIEF;
        }

        referenceSet.remove(floatvalue);
    }

    private void drawMouth(Graphics g) {
        g.setColor(Color.BLACK);

        g.drawLine(FACE_MIDDLE - MOUTH_WIDTH / 2 + 10,                                            //Mund
                MOUTH_HEIGHT,
                FACE_MIDDLE + MOUTH_WIDTH / 2 - 10,
                MOUTH_HEIGHT);
        g.drawLine(FACE_MIDDLE - MOUTH_WIDTH / 2,
                MOUTH_HEIGHT - smileFactor,
                FACE_MIDDLE - MOUTH_WIDTH / 2 + 10,                                            //Mundwinkel1
                MOUTH_HEIGHT);
        g.drawLine(FACE_MIDDLE + MOUTH_WIDTH / 2,
                MOUTH_HEIGHT - smileFactor,
                FACE_MIDDLE + MOUTH_WIDTH / 2 - 10,                                            //Mundwinkel2
                MOUTH_HEIGHT);
    }

    private void drawHand(Graphics g) {
        g.setColor(Color.BLACK);

        if (smileFactor == BIG_GRIEF) {
            g.drawRect(HAND_START_X,                                                                        //Hand
                    HAND_START_Y,
                    HAND_WITH,
                    HAND_HEIGHT);
            g.drawRect(HAND_START_X + HAND_WITH - THUMB_WIDTH,                            //DAUMEN
                    HAND_START_Y - THUMB_HEIGHT,
                    THUMB_WIDTH,
                    THUMB_HEIGHT);

            for (int i = 1; i < 4; i++) {
                int line_height = i * HAND_HEIGHT / 4;                                            //Fingerlinien
                g.drawLine(HAND_START_X,
                        HAND_START_Y + line_height,
                        HAND_START_X + FINGER_LINE_LENGTH,
                        HAND_START_Y + line_height);
            }
        } else if (smileFactor == BIG_SMILE) {
            g.drawRect(HAND_START_X,                                                                        //Hand
                    HAND_START_Y,
                    HAND_WITH,
                    HAND_HEIGHT);
            g.drawRect(HAND_START_X,                            //DAUMEN
                    HAND_START_Y + HAND_HEIGHT,
                    THUMB_WIDTH,
                    THUMB_HEIGHT);

            for (int i = 1; i < 4; i++) {                                                                        //Fingerlinien
                int line_height = i * HAND_HEIGHT / 4;
                g.drawLine(HAND_START_X + HAND_WITH,
                        HAND_START_Y + line_height,
                        HAND_START_X + HAND_WITH - FINGER_LINE_LENGTH,
                        HAND_START_Y + line_height);
            }
        }
    }

    private void drawProgress(Graphics g) {
        g.setColor(Color.BLACK);
        drawRoundRect(g, 30, 30, 140, 50);
        g.drawOval(20, 80, 20, 20);
        g.drawOval(30, 110, 10, 10);

        if (showValue) {
            g.setColor(Color.WHITE);
            g.fillRect(49, 46, 110, 20);
            g.setColor(Color.BLACK);

            if (value != Evaluaded.INITAL) {
                FontMetrics metric = g.getFontMetrics();
                String news = value.toString();
                g.drawString(news, 100 - metric.stringWidth(news) / 2, 60);
            }
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(49, 46, 110, 20);
            g.setColor(Color.BLACK);
            g.drawRect(49, 46, 101, 18);
            g.setColor(Color.DARK_GRAY);
            int progress = (int) Math.ceil(100 * index / (double) total);
            g.fillRect(50, 47, progress, 17);
        }
    }

    private void drawRoundRect(Graphics g, int x, int y, int w, int h) {
        g.drawArc(x, y, h, h, 90, 180);
        g.drawArc(x + w - h, y, h, h, 270, 180);
        g.drawLine(x + h / 2, y, x + w - h / 2, y);
        g.drawLine(x + h / 2, y + h, x + w - h / 2, y + h);
    }

}
