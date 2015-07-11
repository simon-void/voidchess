package organisation;

import board.BasicChessGameInterface;
import helper.Move;
import helper.Position;
import image.FigureImage;
import image.ImageType;
import image.Images;
import player.HumanPlayerInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Map;

/**
 * @author stephan
 */
public class ChessGameUI
        extends JComponent {
    private int areaSize, borderSize;
    private boolean whiteView;
    private Position from, to;
    private BasicChessGameInterface game;
    private Map<ImageType, FigureImage> imageTypeToImage;
    ChessGameAdapter adapter;

    public ChessGameUI(BasicChessGameInterface game, ImageObserver imageObserver) {
        this.game = game;
        areaSize = 50;
        borderSize = 25;
        whiteView = true;
        setPreferredSize(new Dimension(2 * borderSize + 8 * areaSize, 2 * borderSize + 8 * areaSize));
        setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.black),
                        BorderFactory.createBevelBorder(0, Color.gray, Color.darkGray)
                )
        );
        adapter = new ChessGameAdapter(this);
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        setDoubleBuffered(true);

        imageTypeToImage = new HashMap<>(20);
        for (ImageType imageType : ImageType.values()) {
            if (imageType.isFigure) {
                FigureImage figureImage = new FigureImage(imageObserver, Images.get(imageType));
                imageTypeToImage.put(imageType, figureImage);
            }
        }
    }

    public void repaintAfterMove(Move move) {
        repaintPositionAtOnce(move.from);
        repaintPositionAtOnce(move.to);

        int horizontal_dif = Math.abs(move.from.column - move.to.column);
        int vertical_dif = Math.abs(move.from.row - move.to.row);

        if (horizontal_dif == 1 && vertical_dif == 1) {                                //enpassant?
            repaintPositionAtOnce(Position.get(move.from.row, move.to.column));
            repaintPositionAtOnce(Position.get(move.to.row, move.from.column));
        } else if (vertical_dif == 0 && (move.to.row == 0 || move.to.row == 7)) {            //Rochade?muss auch für Schach960 funktionieren
            repaintRowAtOnce(move.from.row);
        }
    }

    private void repaintPositionAtOnce(Position pos) {
        int x_pos = borderSize + areaSize * (whiteView ? pos.column : 7 - pos.column);
        int y_pos = borderSize + areaSize * (whiteView ? 7 - pos.row : pos.row);

        int repaintSize = areaSize + 1;
        paintImmediately(x_pos, y_pos, repaintSize, repaintSize);
    }

    private void repaintRowAtOnce(int row) {
        int x_pos = borderSize;
        int y_pos = borderSize + areaSize * (whiteView ? 7 - row : row);

        int repaintSize = areaSize + 1;
        paintImmediately(x_pos, y_pos, repaintSize * 8, repaintSize);
    }

    public void repaintAtOnce() {
        Dimension dim = getSize();
        paintImmediately(0, 0, dim.width, dim.height);
    }

    protected void paintComponent(Graphics g) {
        if (game == null) return;
        paintBoard(g);
        paintFigures(g);
        paintActivAreas(g);
    }

    private void paintBoard(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, 2 * borderSize + 8 * areaSize, 2 * borderSize + 8 * areaSize);
        g.setColor(Color.lightGray);
        g.drawRect(borderSize, borderSize, 8 * areaSize, 8 * areaSize);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (((i + j) % 2) == 1) {
                    g.fillRect(borderSize + i * areaSize, borderSize + j * areaSize, areaSize, areaSize);
                }
            }
        }
    }

    private void paintFigures(Graphics g) {
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Position pos = Position.get(row, column);
                if (!game.isFreeArea(pos)) {
                    int x_pos = borderSize + areaSize * (whiteView ? column : 7 - column);
                    int y_pos = borderSize + areaSize * (whiteView ? 7 - row : row);

                    ImageType imageType = game.getFigure(pos).getImageType();
                    FigureImage figureImage = imageTypeToImage.get(imageType);
                    if (figureImage != null) {
                        figureImage.paint(g, x_pos, y_pos, areaSize);
                    } else {
                        throw new IllegalArgumentException("unknown imageType: " + imageType);
                    }
                }
            }
        }
    }

    private void paintActivAreas(Graphics g) {
        g.setColor(Color.darkGray);
        if (from != null) {
            int x_pos = borderSize + areaSize * (whiteView ? from.column : 7 - from.column);
            int y_pos = borderSize + areaSize * (whiteView ? 7 - from.row : from.row);
            g.drawRect(x_pos, y_pos, areaSize, areaSize);
        }
        if (to != null) {
            int x_pos = borderSize + areaSize * (whiteView ? to.column : 7 - to.column);
            int y_pos = borderSize + areaSize * (whiteView ? 7 - to.row : to.row);
            g.drawRect(x_pos, y_pos, areaSize, areaSize);
        }
    }

    int getBorderSize() {
        return borderSize;
    }

    int getAreaSize() {
        return areaSize;
    }

    boolean isWhiteView() {
        return whiteView;
    }

    void setView(boolean fromWhite) {
        whiteView = fromWhite;
        repaint();
    }

    public void addPlayer(HumanPlayerInterface player) {
        adapter.addPlayer(player);
    }

    public void removePlayer(HumanPlayerInterface player) {
        adapter.removePlayer(player);
    }

    public void markPosition(Position pos, boolean isFromPosition) {
        if (isFromPosition) {
            Position temp = from;
            if (from != null) {
                from = null;
                repaintPositionAtOnce(temp);
            }
            from = pos;
        } else {
            Position temp = to;
            if (to != null) {
                to = null;
                repaintPositionAtOnce(temp);
            }
            to = pos;
        }
        if (pos != null) repaintPositionAtOnce(pos);
    }
}
