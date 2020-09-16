import java.util.concurrent.TimeUnit
import java.util.spi.ToolProvider

object JPackage {
    fun buildInstaller(
        name: String,
        description: String,
        appVersion: String,
        inputDir: String,
        destinationDir: String,
        mainJar: String,
        addModules: List<String>,
        winIcoIconPath: String? = null,
        winShortcut: Boolean = false,
        winMenu: Boolean = false,
        winPackageType: String = "msi", //or "exe"
        linuxPngIconPath: String? = null,
        linuxShortcut: Boolean = false,
        linuxMenuGroup: String? = null,
        linuxPackageType: String = "deb", //or "rpm"
        macIcnsIconPath: String? = null
    ): Int {
        val currentOS: OS = getCurrentOS()
        val arguments: Array<String> = ArrayList<String>(16).let {args->
            args.add("--name", name)
            args.add("--description", description)
            args.add("--app-version", appVersion)
            args.add("--input", inputDir)
            args.add("--dest", destinationDir)
            args.add("--main-jar", mainJar)
            if(addModules.isNotEmpty()) {
                args.add("--add-modules", addModules.joinToString(separator = ",") { it.replace(" ", "") })
            }
            when(currentOS) {
                OS.WIN -> {
                    args.add("--type", winPackageType)
                    winIcoIconPath?.let { args.add("--icon", winIcoIconPath) }
                    if(winShortcut) args.add("--win-shortcut")
                    if(winMenu) args.add("--win-menu")
                }
                OS.LINUX -> {
                    args.add("--type", linuxPackageType)
                    linuxPngIconPath?.let { args.add("--icon", linuxPngIconPath) }
                    if(linuxShortcut) args.add("--linux-shortcut")
                    linuxMenuGroup?.let { args.add("--linux-menu-group", linuxMenuGroup) }
                }
                OS.MAC -> {
                    args.add("--type", "pkg")
                    macIcnsIconPath?.let { args.add("--icon", macIcnsIconPath) }
                }
            }
            args.toTypedArray()
        }

        return execJpackageViaToolProvider(arguments)
    }

    /**
     * Unfortunately this approach doesn't work until jpackage leaves the incubator status
     * More background here: https://stackoverflow.com/a/61310708/4515050
     * outdated: to do: use this method, when jpackage leaves incubator
     *
     * After adding the jpackage incubator module via gradle.properties (using org.gradle.jvmargs),
     * the jpackage tool is found but it throws an exception in Ubuntu stating:
     * java.io.IOException: Command [fakeroot, dpkg-deb, ... exited with 2 code
     * I found a bug ticket that claimed this would be fixed in OpenJDK 15
     * TODO wait for Java 15
     */
    private fun execJpackageViaToolProvider(arguments: Array<String>): Int {
        val jpackageTool: ToolProvider = ToolProvider.findFirst("jpackage").orElseThrow {
            val javaVersion: String = System.getProperty("java.version")
            IllegalStateException("jpackage not found (expected JDK version: 14 or above, detected: $javaVersion)")
        }
        println("executing: jpackage " + arguments.joinToString(separator = " "))
        return jpackageTool.run(System.out, System.err, *arguments)
    }

    private fun execJpackageViaRuntime(arguments: Array<String>): Int {
        val cmdAndArgs = ArrayList<String>(arguments.size+1).let {
            it.add("jpackage")
            it.addAll(arguments)
            it.toTypedArray()
        }
        return try {
            val minutesToAwait = 3L
            println("executing: " + cmdAndArgs.joinToString(separator = " "))
            println("(timeout: $minutesToAwait minutes)")
            val process: Process = Runtime.getRuntime().exec(cmdAndArgs)
            process.waitFor(minutesToAwait, TimeUnit.MINUTES)
            return process.exitValue()
        } catch (e: Exception) {
            println("failed to execute jpackage. $e")
            1
        }
    }
}

enum class OS {
    WIN, MAC, LINUX;
}
private fun getCurrentOS(): OS {
    val osName: String = System.getProperty("os.name")
    val normalizedOsName = osName.trim().replace(" ", "").toLowerCase()
    return when {
        normalizedOsName.contains("windows") -> OS.WIN
        normalizedOsName.contains("linux") -> OS.LINUX
        normalizedOsName.contains("mac") -> OS.MAC
        else -> throw IllegalStateException("unknown OS: $osName")
    }
}

private fun ArrayList<String>.add(paramName: String, paramValue: String) {
    add(paramName)
    add(paramValue)
}
