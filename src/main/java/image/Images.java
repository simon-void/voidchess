package image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Images{
  private final static Image[] images = new Image[ImageType.values().length];

  public static void loadImageResources()
  throws IOException
  {
    final ClassLoader cl = Images.class.getClassLoader();
    for( ImageType imageType: ImageType.values() ) {
//      final URL url = Images.class.getResource( imageType.getFileName() );
//      final String path = "image/"+imageType.getFileName();
//      final URL url = cl.getResource(path);
//      final Image img = ImageIO.read(url);

//      final Image img = Toolkit.getDefaultToolkit().getImage(url);
      images[imageType.ordinal()] = readFromImageDir(imageType.getFileName());
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

  private static Image readFromImageDir(String fileName) {
    final String path = "src/main/resources/image/"+fileName;
    File file = new File(path);
    boolean fileE = file.exists();

    InputStream imageStream = null;

    try{
      imageStream = new FileInputStream(path);
    }catch (FileNotFoundException e) {
      //we're probably inside a jar
      imageStream = Images.class.getResourceAsStream( "/image/"+fileName);
    }

    try {
      final Image img = ImageIO.read(imageStream);
      return img;
    }catch (Exception e) {
      throw new IllegalStateException("couldn't find image: "+path);
    }
  }
}
