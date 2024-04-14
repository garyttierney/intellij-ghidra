package com.codingmates.ghidra.intellij.ide.facet.model

import com.intellij.util.lang.UrlClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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

fun isGhidraSourcesPath(path: String) = Files.exists(Path.of(path, "Ghidra", "certification.local.manifest"))

fun isGhidraInstallationPath(path: String) = Files.exists(Path.of(path, "Ghidra", "application.properties"))