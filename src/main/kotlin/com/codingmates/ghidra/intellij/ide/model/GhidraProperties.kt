package com.codingmates.ghidra.intellij.ide.model

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.intellij.util.lang.UrlClassLoader
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

fun Path.resolveGhidraModuleJar(category: String, name: String): Path {
    return resolve("Ghidra/${category}/${name}/lib/${name}.jar")
}

data class GhidraProperties(
    val installationPath: Path? = null,
    val version: String? = null,
    val settingsPath: Path? = null,
    val extensionInstallationPaths: List<Path>? = null,
    val modules: Map<String, Path>? = null,
) {
    companion object {
        /**
         * Discover the Ghidra installation by using the same approach used by Ghidra at runtime.
         * This requires loading the Framework Utility jar and creating a GhidraApplicationLayout proxy.
         */
        fun discoverFromRuntime(installationPath: Path): GhidraProperties {
            val utilsJar = installationPath.resolveGhidraModuleJar("Framework", "Utility")
            val utilsClassLoader = UrlClassLoader.build().files(listOf(utilsJar)).get()
            val layout = createApplicationLayoutProxy(utilsClassLoader, installationPath.toFile())

            return GhidraProperties(
                installationPath,
                version = layout.applicationProperties.applicationVersion,
                settingsPath = layout.settingsDir.toPath(),
                extensionInstallationPaths = layout.extensionInstallationDirs
                    .map { Paths.get(it.canonicalPath) }
                    .toList(),
                modules = layout.modules.mapValues { Paths.get(it.value.moduleRoot.canonicalPath) }
            )
        }
    }
}

fun validateGhidraPath(path: String) {
    val path = Paths.get(path)
    if (!path.resolve("Ghidra/application.properties").exists()) {
        throw GhidraPathValidationException(GhidraBundle.message("ghidra.editor.path.error.no-properties"))
    }
    if (path.resolve("Ghidra/certification.local.manifest").exists()) {
        throw GhidraPathValidationException(GhidraBundle.message("ghidra.editor.path.error.sources"))
    }

    if (!path.resolve("Ghidra/Framework/Utility/lib/Utility.jar").exists()) {
        throw GhidraPathValidationException(GhidraBundle.message("ghidra.editor.path.error.utility"))
    }

    val genericLib = path.resolve("Ghidra/Framework/Generic/lib")
    if (!genericLib.isDirectory()) {
        throw GhidraPathValidationException(GhidraBundle.message("ghidra.editor.path.error.generic-lib"))
    }

    val log4jRegex = Regex(GhidraBundle.message("ghidra.regex.log4j"))
    val hasLog4j = genericLib.listDirectoryEntries()
        .any { it.isRegularFile() && log4jRegex.matches(it.fileName.toString()) }
    if (!hasLog4j) {
        throw GhidraPathValidationException(GhidraBundle.message("ghidra.editor.path.error.log4j"))
    }
}

class GhidraPathValidationException(message: String) : Exception(message)
