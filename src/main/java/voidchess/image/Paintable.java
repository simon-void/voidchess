package voidchess.image;

import java.awt.*;
import java.awt.image.ImageObserver;

/**
 * @author stephan
 */
public class Paintable {
    private final ImageObserver observer;
    private final Image image;

    public Paintable(ImageObserver observer, Image image) {
        this.observer = observer;
        this.image = image;
    }

    public void paintOn(Graphics g, int x_pos, int y_pos, int areaSize) {
        g.drawImage(image, x_pos, y_pos, areaSize, areaSize, observer);
    }
}
