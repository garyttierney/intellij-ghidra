package com.codingmates.ghidra.intellij.ide.settings

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.codingmates.ghidra.intellij.ide.model.createApplicationLayoutProxy
import com.codingmates.ghidra.intellij.ide.model.resolveGhidraModuleJar
import com.codingmates.ghidra.intellij.ide.newProjectWizard.GhidraProjectType
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.util.lang.UrlClassLoader
import org.jetbrains.plugins.gradle.settings.GradleSettings
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path


@Service(Service.Level.PROJECT)
@State(
    name = "com.codingmates.ghidra.intellij.ide.settings.GhidraSettings",
    storages = [Storage("GhidraSettings.xml")]
)
class GhidraSettings(private val project: Project): PersistentStateComponent<GhidraSettings.State> {
    data class State(
        var type: GhidraProjectType? = null,
        var path: String = ""
    )

    private var state = State()

    var type: GhidraProjectType?
        get() {
            if (state.type == null) {
                state.type = guessGhidraProjectType()
            }
            return state.type
        }
        set(value) { state.type = value }
    var path: String
        get() = if (type == GhidraProjectType.Module) {
            readGhidraPathFromGradleProperties()
        } else {
            state.path
        }
        set(value) {
            val newPath = FileUtil.toSystemIndependentName(value)
            if (type == GhidraProjectType.Module) {
                writeGhidraPathToGradleProperties(newPath)
            } else {
                state.path = newPath
            }
        }

    companion object {
        fun getInstance(project: Project): GhidraSettings =
            project.getService(GhidraSettings::class.java)
    }

    override fun getState(): GhidraSettings.State = state

    override fun loadState(newState: GhidraSettings.State) {
        state = newState
    }

    fun syncGhidraLibrary(): Library {
        val library = getGhidraLibrary()
        updateGhidraLibrary(library)
        return library
    }

    /**
     * Gets the Ghidra library. Creates it if it does not exist.
     */
    fun getGhidraLibrary(): Library {
        val registrar = LibraryTablesRegistrar.getInstance()
        val projectLibTable = registrar.getLibraryTable(project)
        projectLibTable.getLibraryByName("Ghidra")?.let { return it }
        val libModel = projectLibTable.modifiableModel
        val library = libModel.createLibrary("Ghidra")
        libModel.commit()
        return library
    }

    fun updateGhidraLibrary(library: Library) {
        library.modifiableModel.apply {
            val ghidraModules = getGhidraModules()
            val classRoots: List<VirtualFile> = getClassRoots(ghidraModules)
            val sourceRoots: List<VirtualFile> = getSourceRoots(ghidraModules)

            listOf(OrderRootType.CLASSES, OrderRootType.SOURCES).forEach { rootType ->
                getUrls(rootType).forEach { removeRoot(it, rootType) }
            }

            classRoots.forEach { addRoot(it, OrderRootType.CLASSES) }
            sourceRoots.forEach { addRoot(it, OrderRootType.SOURCES) }
        }.commit()
    }

    private fun getSourceRoots(ghidraModules: Map<String, String>): List<VirtualFile> {
        val vfs = VirtualFileManager.getInstance()
        fun Path.toVfs(): VirtualFile? =
            vfs.refreshAndFindFileByUrl(VfsUtil.getUrlForLibraryRoot(this))

        val sourceRoots: List<VirtualFile> =
            ghidraModules.asSequence()
                .map { (moduleName, moduleRootStr) -> Paths.get(moduleRootStr, "lib", "${moduleName}-src.zip") }
                .mapNotNull(Path::toVfs)
                .toList()
        return sourceRoots
    }

    private fun getClassRoots(ghidraModules: Map<String, String>): List<VirtualFile> {
        val vfs = VirtualFileManager.getInstance()
        fun Path.toVfs(): VirtualFile? =
            vfs.refreshAndFindFileByUrl(VfsUtil.getUrlForLibraryRoot(this))

        val classRoots: List<VirtualFile> = ghidraModules
            .values
            .map { Paths.get(it, "lib") }
            .filter(Files::isDirectory)
            .map(Files::list)
            .flatMap { it.use { stream -> stream.toList() } }
            .filter {
                val fileName = it.fileName.toString()
                fileName.endsWith(".jar", ignoreCase = true) && !fileName.endsWith("-src.zip", ignoreCase = true)
            }
            .mapNotNull(Path::toVfs)
        return classRoots
    }

    fun getGhidraModules(): Map<String, String> {
        val utilsJar = Path(path).resolveGhidraModuleJar("Framework", "Utility")
        val utilsClassLoader = UrlClassLoader.build().files(listOf(utilsJar)).get()
        val layout = createApplicationLayoutProxy(utilsClassLoader, File(path))
        return layout.modules.mapValues { it.value.moduleRoot.canonicalPath }
    }

    private fun guessGhidraProjectType(): GhidraProjectType {
        val isGradleProject = GradleSettings.getInstance(project).linkedProjectsSettings.isNotEmpty()
        return if (isGradleProject) GhidraProjectType.Module else GhidraProjectType.Script
    }

    private fun writeGhidraPathToGradleProperties(path: String) {
        val projectDir = project.guessProjectDir() ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            val gradlePropertiesFile = projectDir.findOrCreateChildData(this, "gradle.properties")
            val psiFile = PsiManager.getInstance(project)
                .findFile(gradlePropertiesFile) as? PropertiesFile ?: return@runWriteCommandAction
            val existingProperty = psiFile.findPropertyByKey(GhidraBundle.message("ghidra.gradle.path.key"))
            if (existingProperty != null) {
                existingProperty.setValue(path)
            } else {
                psiFile.addProperty(GhidraBundle.message("ghidra.gradle.path.key"), path)
            }
        }
    }

    private fun readGhidraPathFromGradleProperties(): String {
        val gradlePropertiesFile = project.guessProjectDir()
            ?.findChild("gradle.properties") ?: return ""

        val psiFile = PsiManager.getInstance(project)
            .findFile(gradlePropertiesFile) as? PropertiesFile ?: return ""

        val existingProperty = psiFile.findPropertyByKey(GhidraBundle.message("ghidra.gradle.path.key"))
        return existingProperty?.value ?:  ""
    }
}