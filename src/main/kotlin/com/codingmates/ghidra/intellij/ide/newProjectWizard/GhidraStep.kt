package com.codingmates.ghidra.intellij.ide.newProjectWizard

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.codingmates.ghidra.intellij.ide.model.GhidraPathValidationException
import com.intellij.ide.projectWizard.generators.IntelliJNewProjectWizardStep
import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.wizard.GitNewProjectWizardStep
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.util.toUiPathProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.withPathToTextConvertor
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.withTextToPathConvertor
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.getCanonicalPath
import com.intellij.openapi.ui.getPresentablePath
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder


class GhidraStep(parent: GitNewProjectWizardStep) :
    IntelliJNewProjectWizardStep<GitNewProjectWizardStep>(parent),
    GhidraData {

    override val typeProperty = propertyGraph.property(GhidraProjectType.Module)
    override var type: GhidraProjectType by typeProperty

    override val ghidraPathProperty = propertyGraph.property("")
    override var ghidraPath: String by ghidraPathProperty

    override val ghidraModulesProperty = propertyGraph.property<Map<String, String>>(emptyMap())
    override var ghidraModules: Map<String, String> by ghidraModulesProperty

    init {
        data.putUserData(GhidraData.KEY, this)
        ghidraPathProperty.set(GhidraNewProjectWizardState.lastPath)
        moduleFileLocationProperty.set("${contentRoot}/.idea/modules")
        contentRootProperty.afterChange { root ->
            moduleFileLocationProperty.set("$root/.idea/modules")
        }
    }

    override fun setupSettingsUI(builder: Panel) {
        setupGhidraSettingsUI(builder)
        setupJavaSdkUI(builder)
        setupSampleCodeUI(builder)
    }

    fun setupGhidraSettingsUI(builder: Panel) {
        builder.row(GhidraBundle.message("ghidra.editor.path.label")) {
            val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withTitle(GhidraBundle.message("ghidra.editor.path.title"))
                .withPathToTextConvertor(::getPresentablePath)
                .withTextToPathConvertor(::getCanonicalPath)
            textFieldWithBrowseButton(fileChooserDescriptor, context.project)
                .bindText(ghidraPathProperty.toUiPathProperty())
                .align(AlignX.FILL)
                .validationOnInput { validateGhidraPath() }
                .validationOnApply { validateGhidraPath() }
        }
        builder.row("Project Type:") {
            comboBox(GhidraProjectType.entries)
                .bindItem(typeProperty)
        }
    }

    override fun setupProject(project: Project) {
        GhidraNewProjectWizardState.lastPath = ghidraPath
        val builder = JavaModuleBuilder()
        // Use the project root as the source root so no `src` directory is created.
        // JavaModuleBuilder creates a `src` subdirectory when no source paths are set.
        builder.addSourcePath(Pair.create(FileUtil.toSystemDependentName(contentRoot), ""))
        setupProject(project, builder)
    }

    private fun ValidationInfoBuilder.validateGhidraPath(): ValidationInfo? {
        try {
            com.codingmates.ghidra.intellij.ide.model.validateGhidraPath(ghidraPath)
        } catch (e: GhidraPathValidationException) {
            return error(e.message ?: "")
        }
        return null
    }
}
