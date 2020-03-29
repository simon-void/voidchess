package voidchess.common.helper

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream


/**
 * finds a file in the filesystem or in a jar
 *
 * @param relativePath path relative to "module-ui/src/main/resources/", without starting "/"
 * @return an input stream to the file
 */
@Throws(IOException::class)
fun getResourceStream(relativePath: String): InputStream {
    val file = File("module-ui/src/main/resources/$relativePath")
    return if (file.exists()) {
        //should find file in file system
        FileInputStream(file)
    } else {
        //should find file in jar
        file.javaClass.getResourceAsStream(relativePath)
    }
}
