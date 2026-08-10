package com.codingmates.ghidra.intellij.ide.settings

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.codingmates.ghidra.intellij.ide.model.GhidraPathValidationException
import com.codingmates.ghidra.intellij.ide.model.validateGhidraPath
import com.codingmates.ghidra.intellij.ide.newProjectWizard.GhidraProjectType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.withPathToTextConvertor
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.withTextToPathConvertor
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.getCanonicalPath
import com.intellij.openapi.ui.getPresentablePath
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel


class GhidraSettingsConfigurable(var project: Project) : BoundConfigurable("GhidraSettingsComponent") {
    private val settings get() = GhidraSettings.getInstance(project)
    private lateinit var pathField: Cell<TextFieldWithBrowseButton>

    override fun apply() {
        if (settings.type == GhidraProjectType.Module) return
        try {
            validateGhidraPath(pathField.component.text)
        } catch (e: GhidraPathValidationException) {
            throw ConfigurationException(e.message)
        }
        super.apply()
        ApplicationManager.getApplication().runWriteAction {
            val ghidraLib = settings.syncGhidraLibrary()
            val module = ModuleManager.getInstance(project).modules.firstOrNull()
            module?.let {
                val hasGhidraLib = ModuleRootManager.getInstance(module)
                    .orderEntries
                    .filterIsInstance<LibraryOrderEntry>()
                    .any { entry -> entry.libraryName == "Ghidra" }
                if (!hasGhidraLib) {
                    ModuleRootModificationUtil.addDependency(it, ghidraLib)
                }
            }
        }
    }

    override fun createPanel(): DialogPanel = panel {
        val isGradleManaged = settings.type == GhidraProjectType.Module
        row(GhidraBundle.message("ghidra.editor.path.label")) {
            val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withTitle(GhidraBundle.message("ghidra.editor.path.title"))
                .withPathToTextConvertor(::getPresentablePath)
                .withTextToPathConvertor(::getCanonicalPath)
            pathField = textFieldWithBrowseButton(fileChooserDescriptor, project)
                .bindText(settings::path)
                .align(AlignX.FILL)
        }.enabled(!isGradleManaged)
        if (isGradleManaged) {
            row {
                label(GhidraBundle.message("ghidra.settings.panel.gradle-managed.error.label"))
                    .comment(GhidraBundle.message("ghidra.settings.panel.gradle-managed.error.comment"))
            }
        }
    }
}