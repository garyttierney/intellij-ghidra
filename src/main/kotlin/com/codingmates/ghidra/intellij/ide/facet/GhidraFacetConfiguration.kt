package com.codingmates.ghidra.intellij.ide.facet

import com.codingmates.ghidra.intellij.ide.facet.model.GhidraInstallation
import com.codingmates.ghidra.intellij.ide.facet.model.GhidraVersion
import com.intellij.facet.FacetConfiguration
import com.intellij.facet.ui.FacetEditorContext
import com.intellij.facet.ui.FacetEditorTab
import com.intellij.facet.ui.FacetValidatorsManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.io.inputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class GhidraFacetConfiguration : FacetConfiguration, PersistentStateComponent<GhidraFacetState> {

    var ghidraState = GhidraFacetState("")

    private fun searchGhidraRoots(root: Path, filter: (VirtualFile) -> Boolean): MutableList<VirtualFile> {
        val roots = mutableListOf<VirtualFile>()
        val vfs = VirtualFileManager.getInstance()
        vfs.findFileByNioPath(root)?.let { vfsRoot ->
            VfsUtil.iterateChildrenRecursively(vfsRoot, { !it.path.contains("GhidraServer/data") }) {
                if (filter(it)) {
                    val uri = VfsUtil.getUrlForLibraryRoot(it.toNioPath().toFile())
                    val archiveFile = vfs.findFileByUrl(uri) ?: return@iterateChildrenRecursively true
                    roots.add(archiveFile)
                }

                true
            }
        }

        return roots
    }

    fun loadGhidraInstallation(): GhidraInstallation {
        val propertyFile = Paths.get(state.installationPath, "Ghidra", "application.properties")
        val properties = propertyFile.inputStream().use {
            val properties = Properties()
            properties.load(it)

            properties
        }

        val version = GhidraVersion(
            properties.getProperty("application.name"),
            properties.getProperty("application.release.name"),
            properties.getProperty("application.version")
        )

        val userDataKey = ".${version.name.toLowerCase()}_${version.version}_${version.releaseName}"
        val extensionRoot = Paths.get(System.getProperty("user.home"), ".ghidra", userDataKey, "Extensions")
        val installationRoot = Paths.get(state.installationPath)

        fun isBinaries(vf: VirtualFile) = vf.extension.equals("jar")
        fun isSources(vf: VirtualFile) = vf.extension.equals("zip") && vf.path.contains(Regex("src|sources"))

        val sources = searchGhidraRoots(installationRoot, ::isSources) + searchGhidraRoots(extensionRoot, ::isSources)
        val binaries =
            searchGhidraRoots(installationRoot, ::isBinaries) + searchGhidraRoots(extensionRoot, ::isBinaries)

        return GhidraInstallation(state.installationPath, version, sources, binaries)
    }

    override fun createEditorTabs(
        editorContext: FacetEditorContext,
        validatorsManager: FacetValidatorsManager
    ): Array<FacetEditorTab> {
        return arrayOf(GhidraFacetConfigurationEditor(ghidraState, editorContext, validatorsManager))
    }


    override fun getState(): GhidraFacetState = ghidraState

    override fun loadState(state: GhidraFacetState) {
        ghidraState = state
    }

}