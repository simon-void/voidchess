package voidchess.helper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;

/**
 * Created by stephan on 12.07.2015.
 */
public class ResourceFinder {
    /**
     * finds a file in the filesystem or in a jar
     * @param relativePath path relative to "/src/main/resources/", without starting "/"
     * @return an input stream to the file
     */
    public static InputStream getResourceStream(String relativePath)
            throws IOException {
        final String path = "src/main/resources/" + relativePath;
        File file = new File(path);
        if(file.exists()) {
            //should find file in file system
            return new FileInputStream(file);
        }else{
            //should find file in jar
            return ResourceFinder.class.getResourceAsStream("/" + relativePath);
        }
    }
}
