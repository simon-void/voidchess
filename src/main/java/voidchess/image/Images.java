package voidchess.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;

import static voidchess.helper.ResourceFinderKt.getResourceStream;

public class Images {
    private final static Image[] images = new Image[ImageType.values().length];

    public static void loadImageResources() {
        for (ImageType imageType : ImageType.values()) {
            images[imageType.ordinal()] = readFromImageDir(imageType.getFileName());
        }
    }

    public static Image get(ImageType imageType) {
        assertNotNull(imageType, "iamgeType");
        final Image image = images[imageType.ordinal()];
        assertNotNull(image, imageType.toString());

        return image;
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
