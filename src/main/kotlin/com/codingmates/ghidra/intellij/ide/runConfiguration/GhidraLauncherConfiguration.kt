package com.codingmates.ghidra.intellij.ide.runConfiguration

import com.codingmates.ghidra.intellij.ide.facet.GhidraFacet
import com.intellij.diagnostic.logging.LogConfigurationPanel
import com.intellij.execution.*
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.util.JavaParametersUtil
import com.intellij.execution.util.ProgramParametersUtil
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.xmlb.XmlSerializer
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jdom.Element


class GhidraLauncherConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String?
) :
    LocatableConfigurationBase<Any?>(project, factory, name),
    CommonJavaRunConfigurationParameters,
    ConfigurationWithCommandLineShortener,
    SearchScopeProvidingRunProfile,
    RunProfileWithCompileBeforeLaunchOption {

    override fun checkConfiguration() {
        JavaParametersUtil.checkAlternativeJRE(this)
        ProgramParametersUtil.checkWorkingDirectoryExist(this, project, null)
        JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this)
    }

    override fun clone(): RunConfiguration {
        val clone = super.clone() as GhidraLauncherConfiguration
        clone.state = XmlSerializerUtil.createCopy(state)
        val configurationModule = JavaRunConfigurationModule(project, true)
        clone.backingRunConfigurationModule = configurationModule
        clone.envs = LinkedHashMap(clone.envs)
        return clone
    }

    override fun getAlternativeJrePath(): String? {
        return state.alternativeJrePath
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        val group = SettingsEditorGroup<GhidraLauncherConfiguration>()
        val title = ExecutionBundle.message("run.configuration.configuration.tab.title")
        group.addEditor(title, GhidraLauncherConfigurationEditor(project))

        val javaRunConfigurationExtensionManager = JavaRunConfigurationExtensionManager.instance
        javaRunConfigurationExtensionManager.appendEditors(this, group)
        group.addEditor(ExecutionBundle.message("logs.tab.title"), LogConfigurationPanel())

        return group
    }

    override fun getEnvs(): Map<String, String> {
        return _envs
    }

    override fun getPackage(): String? {
        return null
    }

    override fun getProgramParameters(): String? {
        return null
    }

    override fun getRunClass(): String? {
        return null
    }

    override fun getSearchScope(): GlobalSearchScope {
        return GlobalSearchScope.allScope(project)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return GhidraLauncherCommandLineState(environment, this)
    }

    override fun getVMParameters(): String {
        return state.vmParameters
    }

    override fun getWorkingDirectory(): String? {
        return null
    }

    override fun isAlternativeJrePathEnabled(): Boolean {
        return state.alternativeJrePathEnabled
    }

    override fun isPassParentEnvs(): Boolean {
        return state.passParentEnvironments
    }

    override fun onNewConfigurationCreated() {
        if (StringUtil.isEmpty(workingDirectory)) {
            val installationPath = GhidraFacet.findAnyInProject(project).installationPath
            workingDirectory = FileUtil.toSystemIndependentName(StringUtil.notNullize(installationPath))
        }
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)

        val javaRunConfigurationExtensionManager = JavaRunConfigurationExtensionManager.instance
        javaRunConfigurationExtensionManager.readExternal(this, element)
        XmlSerializer.deserializeInto(state, element)

        EnvironmentVariablesComponent.readExternal(element, envs)
        backingRunConfigurationModule.readExternal(element)
    }

    override fun setAlternativeJrePath(path: String?) {
        state.alternativeJrePath = path
    }

    override fun setAlternativeJrePathEnabled(enabled: Boolean) {
        state.alternativeJrePathEnabled = enabled
    }

    override fun setEnvs(envs: Map<String, String>) {
        _envs.clear()
        _envs.putAll(envs)
    }

    override fun setPassParentEnvs(passParentEnvs: Boolean) {
        state.passParentEnvironments = passParentEnvs
    }

    override fun setProgramParameters(value: String?) {}
    override fun setVMParameters(value: String?) {
        state.vmParameters = value!!
    }

    override fun setWorkingDirectory(value: String?) {}

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        val javaRunConfigurationExtensionManager = JavaRunConfigurationExtensionManager.instance

        javaRunConfigurationExtensionManager.writeExternal(this, element)
        XmlSerializer.serializeInto(state, element, null)
        EnvironmentVariablesComponent.writeExternal(element, envs)

        if (backingRunConfigurationModule.module != null) {
            backingRunConfigurationModule.writeExternal(element)
        }
    }

    private val _envs: MutableMap<String, String> = LinkedHashMap()
    private var backingRunConfigurationModule: JavaRunConfigurationModule = JavaRunConfigurationModule(project, true)
    private var state = GhidraLauncherConfigurationBean()

    private data class GhidraLauncherConfigurationBean(
        var alternativeJrePath: String? = "",
        var alternativeJrePathEnabled: Boolean = true,
        var passParentEnvironments: Boolean = true,
        var shortenCommandLine: ShortenCommandLine? = null,
        var vmParameters: String = ""
    )

    init {
        state.vmParameters = "-Xmx2560m"
    }

    override fun getShortenCommandLine(): ShortenCommandLine? {
        return state.shortenCommandLine
    }

    override fun setShortenCommandLine(mode: ShortenCommandLine?) {
        state.shortenCommandLine = mode
    }
}
