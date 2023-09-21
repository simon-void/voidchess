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
        winPackageType: WinPackageType,
        linuxPngIconPath: String? = null,
        linuxShortcut: Boolean = false,
        linuxMenuGroup: String? = null,
        linuxPackageType: LinuxPackageType,
        macIcnsIconPath: String? = null,
        verbose: Boolean = false,
    ): Int {
        fun MutableList<String>.add(paramName: String, paramValue: String) {
            add(paramName)
            if (paramValue.contains(' ')) {
                add(""""$paramValue"""")
            } else {
                add(paramValue)
            }
        }

        val currentOS: OS = getCurrentOS()
        val arguments: Array<String> = ArrayList<String>(16).let { args ->
            if (verbose) args.add("--verbose")
            args.add("--name", name)
            args.add("--description", description)
            args.add("--app-version", appVersion)
            args.add("--input", inputDir)
            args.add("--dest", destinationDir)
            args.add("--main-jar", mainJar)
            if (addModules.isNotEmpty()) {
                args.add("--add-modules", addModules.joinToString(separator = ",") { it.replace(" ", "") })
            }
            when (currentOS) {
                OS.WIN -> {
                    args.add("--type", winPackageType.toArg())
                    winIcoIconPath?.let { args.add("--icon", winIcoIconPath) }
                    if (winShortcut) args.add("--win-shortcut")
                    if (winMenu) args.add("--win-menu")
                }

                OS.LINUX -> {
                    args.add("--type", linuxPackageType.toArg())
                    linuxPngIconPath?.let { args.add("--icon", linuxPngIconPath) }
                    if (linuxShortcut) args.add("--linux-shortcut")
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

    private fun execJpackageViaToolProvider(arguments: Array<String>): Int {
        val jpackageTool: ToolProvider = ToolProvider.findFirst("jpackage").orElseThrow {
            val javaVersion: String = System.getProperty("java.version")
            IllegalStateException("jpackage not found (expected JDK version: 16 or above, detected: $javaVersion)")
        }
        println("executing: jpackage " + arguments.joinToString(separator = " "))
        return jpackageTool.run(System.out, System.err, *arguments)
    }

    private fun getCurrentOS(): OS {
        val osName: String = System.getProperty("os.name")
        val normalizedOsName = osName.trim().replace(" ", "").lowercase()
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

enum class LinuxPackageType {
    DEB, RPM;
}

enum class WinPackageType {
    MSI, EXE;
}

private fun Enum<*>.toArg(): String = this.name.lowercase()
