package organisation;

import helper.Position;
import player.HumanPlayerInterface;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author stephan
 */
public class ChessGameAdapter implements MouseInputListener {
    private ChessGameUI ui;
    private List players;

    ChessGameAdapter(ChessGameUI ui) {
        this.ui = ui;
        players = new LinkedList();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }


    public void mouseMoved(MouseEvent e) {
        Position pos = getPositionFromPoint(e.getPoint());
        if (pos != null) {
            Iterator iter = players.iterator();
            while (iter.hasNext()) {
                HumanPlayerInterface player = (HumanPlayerInterface) iter.next();
                player.mouseMovedOver(pos);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        Position pos = getPositionFromPoint(e.getPoint());
        if (pos != null) {
            Iterator iter = players.iterator();
            while (iter.hasNext()) {
                HumanPlayerInterface player = (HumanPlayerInterface) iter.next();
                player.mouseClickedOn(pos);
            }
        }
    }

    private Position getPositionFromPoint(Point p) {
        int borderSize = ui.getBorderSize();
        int areaSize = ui.getAreaSize();
        boolean isWhiteView = ui.isWhiteView();

        int x = (p.x - borderSize) / areaSize;
        int y = (p.y - borderSize) / areaSize;

        if (x > 7 || x < 0 || y > 7 || y < 0) return null;

        if (isWhiteView) {
            y = 7 - y;
        } else {
            x = 7 - x;
        }

        return Position.get(y, x);
    }

    void addPlayer(HumanPlayerInterface player) {
        players.add(player);
    }

    void removePlayer(HumanPlayerInterface player) {
        players.remove(player);
    }

}
