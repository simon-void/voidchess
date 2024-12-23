package voidchess

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoImportDeclaration
import com.lemonappdev.konsist.api.provider.modifier.KoVisibilityModifierProvider
import com.lemonappdev.konsist.api.provider.KoNameProvider
import com.lemonappdev.konsist.api.provider.KoPackageProvider
import org.testng.annotations.Test
import java.io.File
import kotlin.test.fail

class ArchitectureTest {
    @Test
    fun `clean architecture layers have correct dependencies`() {
        Konsist
            .scopeFromProject() // Define the scope containing all Kotlin files present in project
            .assertArchitecture { // Assert architecture
                // Define layers
                val common = Layer("Common", "voidchess.common..")
                val engine = Layer("Engine", "voidchess.engine..")
                val ui = Layer("UI", "voidchess.ui..")
                val app = Layer("App", "voidchess.app..")

                // Define architecture assertions
                common.dependsOnNothing()
                engine.dependsOn(common)
                ui.dependsOn(common)
                app.dependsOn(common, engine, ui)
            }
    }

    @Test
    fun `test that no files contain imports from another layer if that import is from an inner package`() {

        val filePathsWithIllegalImports = Konsist.scopeFromProject().getFilesWithIllegalImports(
            listOf(
                "voidchess.common.",
                "voidchess.engine.",
                "voidchess.ui.",
                "voidchess.app.",
            )
        )

        if (filePathsWithIllegalImports.isNotEmpty()) {
            filePathsWithIllegalImports.forEach { (filePaths, illegalImports) ->
                println("files with illegal imports:")
                println("- file $filePaths has illegal imports:")
                illegalImports.forEach { println("  - $it") }
            }
            fail("found ${filePathsWithIllegalImports.size} files with illegal imports")
        }
    }
}

fun KoScope.getFilesWithIllegalImports(
    layerBasePackages: List<String>,
    srcPath: String = "src/main/kotlin",
): List<Pair<String, List<String>>> {
    val fileSeparatorChar = File.separatorChar
    val effectiveSrcPath = buildString(srcPath.length + 2) {
        val localized = when {
            srcPath.contains('/') -> if (fileSeparatorChar == '/') srcPath else srcPath.replace('/', '\\')
            srcPath.contains('\\') -> if (fileSeparatorChar == '\\') srcPath else srcPath.replace('\\', '/')
            else -> srcPath
        }
        if (!srcPath.startsWith(fileSeparatorChar)) append(fileSeparatorChar)
        append(localized)
        if (!srcPath.endsWith(fileSeparatorChar)) append(fileSeparatorChar)
    }

    // returns a pair of path of the file and all there illegal imports (imports from a different layer from an inner package)
    fun getImportsOfOtherLayersInnerPackages(fileImportPath: String, importDecs: List<KoImportDeclaration>): Pair<String, List<String>>? {
        println("nr of imports: ${importDecs.size}")
        fun String.getBasePackage(): String? = layerBasePackages.find { it.startsWith(this) }
        val layerBasePackage = fileImportPath.getBasePackage() ?: return null

        println("look for illegal imports")

        val illegalImports = importDecs.mapNotNull { oneImport ->
            val importPath = oneImport.path
            println("import Path: $importPath")
            if (!importPath.startsWith(layerBasePackage) && importPath.contains(".inner.")) {
                importPath
            } else null
        }

        if (illegalImports.isEmpty()) return null

        return fileImportPath to illegalImports
    }

    return this.files.filter {
        it.projectPath.startsWith(effectiveSrcPath)
    }.mapNotNull { fileDec ->
        val fileImportPath = fileDec.projectPath.substring(effectiveSrcPath.length).replace(fileSeparatorChar, '.')
        println("testing $fileImportPath")
        getImportsOfOtherLayersInnerPackages(fileImportPath, fileDec.imports)
    }
}