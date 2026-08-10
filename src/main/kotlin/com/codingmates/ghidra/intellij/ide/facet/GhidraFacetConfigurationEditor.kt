package com.codingmates.ghidra.intellij.ide.facet

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.codingmates.ghidra.intellij.ide.model.GhidraPathValidationException
import com.codingmates.ghidra.intellij.ide.model.validateGhidraPath
import com.intellij.facet.ui.*
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.observable.util.toUiPathProperty
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.withPathToTextConvertor
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.withTextToPathConvertor
import com.intellij.openapi.ui.getCanonicalPath
import com.intellij.openapi.ui.getPresentablePath
import com.intellij.openapi.ui.setEmptyState
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.annotations.Nls
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GhidraFacetConfigurationEditor(
    private val state: GhidraFacetSettings,
    private val context: FacetEditorContext,
    private val validator: FacetValidatorsManager
) : FacetEditorTab() {

    private val propertyGraph = PropertyGraph()
    private val installationDir = propertyGraph.property(state.installationPath)
    private val settingsDir = propertyGraph.property(state.settingsPath.orEmpty())
    private val version = propertyGraph.property(state.version.orEmpty())
    private val applied = propertyGraph.property(false)

    init {
        validator.registerValidator(GhidraInstallationValidator())
        propertyGraph.afterPropagation { validator.validate() }
    }

    override fun createComponent() = panel {
        group("Ghidra Settings") {
            row(GhidraBundle.message("ghidra.editor.path.label")) {
                val title = GhidraBundle.message("ghidra.editor.path.title")
                val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    .withPathToTextConvertor(::getPresentablePath)
                    .withTextToPathConvertor(::getCanonicalPath)
                    .withTitle(title)

                textFieldWithBrowseButton(fileChooserDescriptor, context.project)
                    .bindText(installationDir.toUiPathProperty())
                    .applyToComponent { setEmptyState(GhidraBundle.message("ghidra.editor.path.default")) }
                    .align(AlignX.FILL)
            }
        }

        group("Ghidra Installation Details") {
            row("Version") {
                textField()
                    .bindText(version)
                    .enabled(false)
                    .align(AlignX.FILL)
            }

            row("Settings") {
                textField()
                    .bindText(settingsDir)
                    .enabled(false)
                    .align(AlignX.FILL)

            }
        }.visibleIf(applied)
    }

    inner class GhidraInstallationValidator : FacetEditorValidator(), SlowFacetEditorValidator {
        override fun check(): ValidationResult {
            val ghidraInstallation = installationDir.get()
            try {
                validateGhidraPath(ghidraInstallation)
            } catch (e: GhidraPathValidationException) {
                return ValidationResult(e.message)
            }
            return ValidationResult.OK
        }
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String = GhidraFacetType.FACET_NAME

    override fun isModified(): Boolean = state.installationPath != installationDir.get()

    @Throws(ConfigurationException::class)
    override fun apply() {
        try {
            state.installationPath = installationDir.get()
            state.resolve()

            settingsDir.set(state.settingsPath.orEmpty())
            state.version?.let(version::set)
            applied.set(true)

            runWriteAction {
                val rootManager = ModuleRootManager.getInstance(context.module)
                val vfs = VirtualFileManager.getInstance()

                fun Path.toVfs(): VirtualFile? =
                    vfs.refreshAndFindFileByUrl(VfsUtil.getUrlForLibraryRoot(this))

                val classRoots: List<VirtualFile> = state.modules.orEmpty()
                    .values
                    .map { Paths.get(it, "lib") }
                    .filter(Files::isDirectory)
                    .map(Files::list)
                    .flatMap { it.use { stream -> stream.toList() }}
                    .filter {
                        val fileName = it.fileName.toString()
                        fileName.endsWith(".jar", ignoreCase = true) && !fileName.endsWith("-src.zip", ignoreCase = true)
                    }
                    .mapNotNull(Path::toVfs)


                val sourceRoots: List<VirtualFile> =
                    state.modules.orEmpty().asSequence()
                        .map { (moduleName, moduleRootStr) -> Paths.get(moduleRootStr, "lib", "${moduleName}-src.zip") }
                        .mapNotNull(Path::toVfs)
                        .toList()

                val registrar = LibraryTablesRegistrar.getInstance()
                val projectLibTable = registrar.getLibraryTable(context.project)
                val libModel = projectLibTable.modifiableModel
                val ghidraLib = projectLibTable.getLibraryByName("Ghidra")
                    ?: libModel.createLibrary("Ghidra")
                libModel.commit()

                ghidraLib.modifiableModel.apply {
                    listOf(OrderRootType.CLASSES, OrderRootType.SOURCES).forEach { rootType ->
                        getUrls(rootType).forEach { removeRoot(it, rootType) }
                    }

                    classRoots.forEach { addRoot(it, OrderRootType.CLASSES) }
                    sourceRoots.forEach { addRoot(it, OrderRootType.SOURCES) }
                }.commit()

                rootManager.modifiableModel.apply {
                    val alreadyAttached = orderEntries
                        .filterIsInstance<LibraryOrderEntry>()
                        .any { it.libraryName == "Ghidra" }
                    if (!alreadyAttached) addLibraryEntry(ghidraLib)
                }.commit()
            }
        } catch (e: ConfigurationException) {
            throw ConfigurationException(e.localizedMessage)
        }
    }
}