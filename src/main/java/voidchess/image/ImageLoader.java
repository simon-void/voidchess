package voidchess.image;

import voidchess.figures.FigureType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;

import static voidchess.helper.ResourceFinderKt.getResourceStream;

public class ImageLoader {
    private final static Image[] whiteFigureImages = new Image[FigureType.values().length];
    private final static Image[] blackFigureImages = new Image[FigureType.values().length];
    private static Image icon;

    public static void loadImageResources() {
        for (FigureType figureType : FigureType.values()) {
            whiteFigureImages[figureType.ordinal()] = readFromImageDir("w_"+figureType.name().toLowerCase()+".gif");
            blackFigureImages[figureType.ordinal()] = readFromImageDir("b_"+figureType.name().toLowerCase()+".gif");
        }
        icon = readFromImageDir("icon.gif");
    }

    public static Image getIcon() {
        return icon;
    }

    public static Image getFigureImage(FigureType figureType, boolean isWhite) {
        if(isWhite) {
            return whiteFigureImages[figureType.ordinal()];
        }else{
            return blackFigureImages[figureType.ordinal()];
        }
    }

    private static void assertNotNull(Object o, String msg) {
        if (o == null) {
            throw new AssertionError("object was null: " + msg);
        }
    }

    private static Image readFromImageDir(String fileName) {
        final String relativePath = "image/" + fileName;
        try {
            InputStream imageStream = getResourceStream(relativePath);
            final Image img = ImageIO.read(imageStream);
            return img;
        } catch (Exception e) {
            throw new IllegalStateException("couldn't find image: " + relativePath);
        }
    }
}
