package image;

import java.awt.*;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

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
    assertNotNull(imageType, "iamgeType");
    final Image image = images[imageType.ordinal()];
    assertNotNull(image, imageType.toString());
    
    return image; 
  }
  
  private static void assertNotNull(Object o, String msg )
  {
    if(o==null) {
      throw new AssertionError("object was null: "+msg);
    }
  }
}
