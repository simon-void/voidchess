package image;

import java.awt.*;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import junit.framework.Assert;

public class Images{
  private final static Image[] images = new Image[ImageType.values().length];

  public static void loadImageResources()
  throws IOException
  {
    for( ImageType imageType: ImageType.values() ) {
      final URL url = Images.class.getResource( imageType.getFileName() );
      final Image img = ImageIO.read(url);
      images[imageType.ordinal()] = img;
    }
  }

  public static Image get( ImageType imageType )
  {
    Assert.assertNotNull("iamgeType", imageType);
    final Image image = images[imageType.ordinal()];
    Assert.assertNotNull(imageType.toString(), imageType);
    
    return image; 
  }
}
