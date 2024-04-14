package com.codingmates.ghidra.intellij.ide.facet

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.codingmates.ghidra.intellij.ide.facet.model.isGhidraInstallationPath
import com.codingmates.ghidra.intellij.ide.facet.model.isGhidraSourcesPath
import com.intellij.facet.ui.*
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.observable.util.toUiPathProperty
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.roots.ModuleRootManager
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
import java.nio.file.Path
import java.nio.file.Paths

class GhidraFacetConfigurationEditor(
    private val state: GhidraFacetSettings,
    private val context: FacetEditorContext,
    private val validator: FacetValidatorsManager
) : FacetEditorTab() {

    private val propertyGraph = PropertyGraph()
    private val installationDir = propertyGraph.property(state.installationPath)
    private val settingsDir = propertyGraph.property(state.settingsPath?.toString() ?: "")
    private val version = propertyGraph.property(state.version ?: "")
    private val applied = propertyGraph.property(false)

    init {
        validator.registerValidator(GhidraInstallationValidator())
        propertyGraph.afterPropagation { validator.validate() }
    }

    override fun createComponent() = panel {
        group("Ghidra Settings") {
            row(GhidraBundle.message("ghidra.facet.editor.installation")) {
                val title = GhidraBundle.message("ghidra.facet.editor.installation.dialog.title")
                val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    .withPathToTextConvertor(::getPresentablePath).withTextToPathConvertor(::getCanonicalPath)

                textFieldWithBrowseButton(title, context.project, fileChooserDescriptor)
                    .bindText(installationDir.toUiPathProperty())
                    .applyToComponent { setEmptyState(GhidraBundle.message("ghidra.facet.editor.installation.empty")) }
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

            if (!isGhidraInstallationPath(ghidraInstallation)) {
                return ValidationResult(GhidraBundle.message("ghidra.facet.editor.installation.error.no-properties"))
            }

            if (isGhidraSourcesPath(ghidraInstallation)) {
                return ValidationResult(GhidraBundle.message("ghidra.facet.editor.installation.error.sources"))
            }

            return ValidationResult.OK
        }
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName() = GhidraFacetType.FACET_NAME

    override fun isModified() = state.installationPath != installationDir.get()

    @Throws(ConfigurationException::class)
    override fun apply() {
        try {
            state.installationPath = installationDir.get()
            state.resolve()

            settingsDir.set(state.settingsPath.toString())
            version.set(state.version!!)
            applied.set(true)

            runWriteAction {
                val rootManager = ModuleRootManager.getInstance(context.module)
                val vfs = VirtualFileManager.getInstance()

                fun vfsPathForRoots(path: Path): VirtualFile? {
                    val url = VfsUtil.getUrlForLibraryRoot(path)
                    val file = vfs.refreshAndFindFileByUrl(url)

                    return file
                }

                val libraryRoots = state.modules
                    ?.map { Paths.get(it.value, "lib", "${it.key}.jar") }
                    ?.mapNotNull(::vfsPathForRoots) ?: emptyList()

                val sourceRoots = state.modules
                    ?.map { Paths.get(it.value, "lib", "${it.key}-src.zip") }
                    ?.mapNotNull(::vfsPathForRoots) ?: emptyList()

                val library = context.createProjectLibrary(
                    "Ghidra",
                    libraryRoots.toTypedArray(),
                    sourceRoots.toTypedArray()
                )

                val model = rootManager.modifiableModel
                model.addLibraryEntry(library)
                model.commit()
            }
        } catch (e: ConfigurationException) {
            throw ConfigurationException(e.localizedMessage)
        }
    }
}