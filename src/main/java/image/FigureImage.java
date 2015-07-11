package image;

import java.awt.*;
import java.awt.image.ImageObserver;

/**
 * @author stephan
 */
public class FigureImage {
    private final ImageObserver observer;
    private final Image image;

    public FigureImage(ImageObserver observer, Image image) {
        this.observer = observer;
        this.image = image;
    }

    public void paint(Graphics g, int x_pos, int y_pos, int areaSize) {
        g.drawImage(image, x_pos, y_pos, areaSize, areaSize, observer);
    }
}
