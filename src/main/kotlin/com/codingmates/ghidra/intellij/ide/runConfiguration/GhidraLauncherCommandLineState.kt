package com.codingmates.ghidra.intellij.ide.runConfiguration

import com.intellij.execution.application.BaseJavaApplicationCommandLineState
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.util.JavaParametersUtil
import org.jetbrains.annotations.NotNull


class GhidraLauncherCommandLineState(
    environment: ExecutionEnvironment?,
    configuration: @NotNull GhidraLauncherConfiguration
) : BaseJavaApplicationCommandLineState<GhidraLauncherConfiguration>(environment, configuration) {
    override fun createJavaParameters(): JavaParameters {
        val project = configuration.project
        val javaParameters = JavaParameters()
        var jrePath: String? = null

        if (configuration.isAlternativeJrePathEnabled) {
            jrePath = configuration.alternativeJrePath
        }

        javaParameters.jdk = JavaParametersUtil.createProjectJdk(project, jrePath)
        javaParameters.mainClass = "ghidra.GhidraLauncher"
        if (configuration.getHeadless()) {
            javaParameters.programParametersList.add("ghidra.app.util.headless.AnalyzeHeadless")
        } else {
            javaParameters.programParametersList.add("ghidra.GhidraRun")
        }
        javaParameters.programParametersList.addParametersString(configuration.getArgs())
        javaParameters.vmParametersList.addAll(GHIDRA_CLI_OPTS)
        JavaParametersUtil.configureProject(
            project,
            javaParameters,
            JavaParameters.JDK_AND_CLASSES_AND_TESTS,
            jrePath
        )
        setupJavaParameters(javaParameters)

        return javaParameters
    }

    companion object {
        private val GHIDRA_CLI_OPTS = listOf(
            "-Djava.system.class.loader=ghidra.GhidraClassLoader"
        )
    }
}