package com.codingmates.ghidra.intellij.ide.facet

import com.codingmates.ghidra.intellij.ide.facet.model.createApplicationLayoutProxy
import com.codingmates.ghidra.intellij.ide.facet.model.resolveGhidraModuleJar
import com.intellij.util.lang.UrlClassLoader
import java.io.File
import java.nio.file.Paths

data class GhidraFacetSettings(
    var installationPath: String = "",
    var version: String? = null,
    var settingsPath: String? = null,
    var extensionInstallationPaths: List<String>? = null,
    var modules: Map<String, String>? = null,
) {
    fun resolve() {
        val utilsJar = Paths.get(installationPath).resolveGhidraModuleJar("Framework", "Utility")
        val utilsClassLoader = UrlClassLoader.build().files(listOf(utilsJar)).get()
        val layout = createApplicationLayoutProxy(utilsClassLoader, File(installationPath))

        version = layout.applicationProperties.applicationVersion
        settingsPath = layout.settingsDir.canonicalPath
        extensionInstallationPaths = layout.extensionInstallationDirs
            .map { it.canonicalPath }
            .toList()
        modules = layout.modules.mapValues { it.value.moduleRoot.canonicalPath }
    }
}
