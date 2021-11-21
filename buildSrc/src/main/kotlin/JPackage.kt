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
        fun MutableList<String>.add(paramName: String, paramValue: String) {
            add(paramName)
            if(paramValue.contains(' ')) {
                add(""""$paramValue"""")
            } else {
                add(paramValue)
            }
        }

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

        return execJpackageViaRuntime(arguments)
    }

    /**
     * switch to this method for invoking jpackage
     * TODO fix bug when executing on Ubuntu
     * executing: jpackage --name VoidChess --description "a chess program" --app-version 3.6 --input build/libs --dest build/installer --main-jar voidchess-3.6-all.jar --add-modules java.desktop --type deb --icon about/shortcut-icon2.png --linux-shortcut --linux-menu-group Games
     * java.io.IOException: Command [fakeroot, dpkg-deb, -b, /tmp/jdk.jpackage961355904914198194/images, /home/stephan/.gradle/daemon/7.0/build/installer/voidchess_3.6-1_amd64.deb] exited with 2 code
     */
    private fun execJpackageViaToolProvider(arguments: Array<String>): Int {
        val jpackageTool: ToolProvider = ToolProvider.findFirst("jpackage").orElseThrow {
            val javaVersion: String = System.getProperty("java.version")
            IllegalStateException("jpackage not found (expected JDK version: 16 or above, detected: $javaVersion)")
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
}

enum class OS {
    WIN, MAC, LINUX;
}

