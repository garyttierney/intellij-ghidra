package com.codingmates.ghidra.intellij.ide.newProjectWizard

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.codingmates.ghidra.intellij.ide.runConfiguration.GhidraLauncherConfiguration
import com.codingmates.ghidra.intellij.ide.runConfiguration.GhidraLauncherConfigurationType
import com.codingmates.ghidra.intellij.ide.settings.GhidraSettings
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.projectWizard.generators.AssetsNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardBaseData.Companion.baseData
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.plugins.gradle.service.project.open.linkAndSyncGradleProject


class AssetsStep(private val parent: GhidraStep) : AssetsNewProjectWizardStep(parent) {
    override fun setupAssets(project: Project) {
        if (parent.type == GhidraProjectType.Module) setupModuleAssets(project) else setupScriptAssets()
    }

    override fun setupProject(project: Project) {
        super.setupProject(project)
        val ghidraSettings = requireNotNull(GhidraSettings.getInstance(project))
        val module = ModuleManager.getInstance(project).modules.firstOrNull()
        ApplicationManager.getApplication().invokeLater {
            WriteAction.runAndWait<Throwable> {
                ghidraSettings.type = parent.type
                if (parent.type == GhidraProjectType.Script) {
                    ghidraSettings.path = parent.ghidraPath  // This is not ran if project is a Module since it's set in
                                                       // gradle.properties in the setupModuleAssets method.
                    val ghidraLib = ghidraSettings.syncGhidraLibrary()
                    module?.let { ModuleRootModificationUtil.addDependency(it, ghidraLib) }
                }
            }
        }
        createRunConfigInstance(project)
    }

    private fun createRunConfigInstance(project: Project) {
        // Create run configuration entry for the project
        val runManager = RunManager.getInstance(project)

        val factory = ConfigurationTypeUtil
            .findConfigurationType(GhidraLauncherConfigurationType::class.java)
            .configurationFactories
            .firstOrNull()
            ?: error(GhidraBundle.message("ghidra.runconfig.type.error"))
        val settings = runManager.createConfiguration(
            "Ghidra",
            factory,
        )
        val configuration = settings.configuration as GhidraLauncherConfiguration
        configuration.apply {
            alternativeJrePath = ProjectRootManager.getInstance(project).projectSdk?.homePath
        }
        runManager.addConfiguration(settings)
        runManager.selectedConfiguration = settings
    }

    fun setupModuleAssets(project: Project) {
        val name = requireNotNull(baseData?.name) {
            "New project wizard base data must contain a non-null project name before creating module assets."
        }
        // Have to use this to inject the default props to the templates since apparently `addTemplateAsset`
        // doesn't do it.
        val props = FileTemplateManager.getInstance(project).defaultProperties.entries.associate {
            it.key.toString() to it.value
        }

        // Create relevant directories
        addEmptyDirectoryAsset("data")
        addEmptyDirectoryAsset("data/languages")
        addEmptyDirectoryAsset("ghidra_scripts")
        addEmptyDirectoryAsset("lib")
        addEmptyDirectoryAsset("os")
        addEmptyDirectoryAsset("src/main/java/$name")
        addEmptyDirectoryAsset("src/main/resources")

        // Create compulsory files
        addTemplateAsset("build.gradle", "build.gradle", buildMap {
            put(GhidraBundle.message("ghidra.gradle.path.key"), parent.ghidraPath)
        })
        addTemplateAsset("gradle.properties", "gradle.properties", buildMap {
            put(GhidraBundle.message("ghidra.gradle.path.key"), parent.ghidraPath)
        })
        addTemplateAsset("extension.properties", "extension.properties", emptyMap())
        addTemplateAsset("Module.manifest", "Module.manifest", emptyMap())
        addTemplateAsset("README.md", "README.md", emptyMap())

        if (parent.addSampleCode) {
            // data dir
            addTemplateAsset("data/buildLanguage.xml", "buildLanguage.xml", emptyMap())
            addTemplateAsset("data/README.txt", "data_README.txt", emptyMap())
            addTemplateAsset("data/sleighArgs.txt", "data__sleighArgs.txt", emptyMap())
            addTemplateAsset("data/languages/skel.cspec", "skel.cspec", emptyMap())
            addTemplateAsset("data/languages/skel.ldefs", "skel.ldefs", emptyMap())
            addTemplateAsset("data/languages/skel.opinion", "skel.opinion", emptyMap())
            addTemplateAsset("data/languages/skel.pspec", "skel.pspec", emptyMap())
            addTemplateAsset("data/languages/skel.sinc", "skel.sinc", emptyMap())
            addTemplateAsset("data/languages/skel.slaspec", "skel.slaspec", emptyMap())
            // ghidra_scripts dir
            addTemplateAsset("ghidra_scripts/README.txt", "ghidra_scripts_README.txt", emptyMap())
            addTemplateAsset("ghidra_scripts/sample_script.py", "sample_script.py", emptyMap())
            addTemplateAsset("ghidra_scripts/SampleScript.java", "SampleScript.java", emptyMap())
            // lib dir
            addTemplateAsset("lib/README.txt", "lib_README.txt", emptyMap())
            // os dir
            addTemplateAsset("os/linux_x86_64/README.txt", "linux_x86_64_README.txt", emptyMap())
            addTemplateAsset("os/mac_x86_64/README.txt", "mac_x86_64_README.txt", emptyMap())
            addTemplateAsset("os/win_x86_64/README.txt", "win_x86_64_README.txt", emptyMap())
            // src dir
            addTemplateAsset("src/main/help/help/topics/skeleton/help.html", "help.html", emptyMap())
            addTemplateAsset("src/main/help/help/TOC_Source.xml", "TOC_Source.xml", emptyMap())
            addTemplateAsset("src/main/resources/images/README.txt", "images_README.txt", emptyMap())
            addTemplateAsset("src/main/java/$name/${name}Analyzer.java", "nameAnalyzer.java", props)
            addTemplateAsset("src/main/java/$name/${name}Exporter.java", "nameExporter.java", props)
            addTemplateAsset("src/main/java/$name/${name}FileSystem.java", "nameFileSystem.java", props)
            addTemplateAsset("src/main/java/$name/${name}Loader.java", "nameLoader.java", props)
            addTemplateAsset("src/main/java/$name/${name}Plugin.java", "namePlugin.java", props)
        }

        // Load build.gradle
        val basePath = project.basePath ?: return
        currentThreadCoroutineScope().launch(Dispatchers.EDT) {
            linkAndSyncGradleProject(project, basePath)
        }
    }

    fun setupScriptAssets() {
        if (parent.addSampleCode) {
            addTemplateAsset("README.txt", "ghidra_scripts_README.txt", emptyMap())
            addTemplateAsset("sample_script.py", "sample_script.py", emptyMap())
            addTemplateAsset("SampleScript.java", "SampleScript.java", emptyMap())
        }
    }
}