package voidchess.helper

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by stephan on 12.07.2015.
 */

/**
 * finds a file in the filesystem or in a jar
 *
 * @param relativePath path relative to "/src/main/resources/", without starting "/"
 * @return an input stream to the file
 */
@Throws(IOException::class)
fun getResourceStream(relativePath: String): InputStream {
    val path = "src/main/resources/$relativePath"
    val file = File(path)
    return if (file.exists()) {
        //should find file in file system
        FileInputStream(file)
    } else {
        //should find file in jar
        File::class.java.getResourceAsStream("/$relativePath")
    }
}
