package com.codingmates.ghidra.intellij.ide.runConfiguration

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.codingmates.ghidra.intellij.ide.model.GhidraPathValidationException
import com.codingmates.ghidra.intellij.ide.model.validateGhidraPath
import com.codingmates.ghidra.intellij.ide.settings.GhidraSettings
import com.intellij.execution.ExecutionException
import com.intellij.execution.application.BaseJavaApplicationCommandLineState
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.util.JavaParametersUtil
import org.jetbrains.annotations.NotNull
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries


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
        project.basePath?.let { basePath ->
            javaParameters.vmParametersList.add(
                "-Dghidra.external.modules=$basePath"
            )  // Attach current module to Ghidra
        }
        JavaParametersUtil.configureProject(
            project,
            javaParameters,
            JavaParameters.JDK_AND_CLASSES,
            jrePath
        )

        try {
            validateGhidraPath(GhidraSettings.getInstance(project).path)
        } catch (e: GhidraPathValidationException) {
            throw ExecutionException(e.message)
        }
        val ghidraHome = Path(GhidraSettings.getInstance(project).path)
        javaParameters.classPath.add(ghidraHome.resolve("Ghidra/Framework/Utility/lib/Utility.jar").toFile())
        val regex = Regex(GhidraBundle.message("ghidra.regex.log4j"))
        val genericLib = ghidraHome.resolve("Ghidra/Framework/Generic/lib")
        try {
            genericLib.listDirectoryEntries()
                .filter { path -> path.isRegularFile() && regex.matches(path.fileName.toString()) }
                .forEach { path -> javaParameters.classPath.add(path.toAbsolutePath().toString()) }
        } catch (e: IOException) {
            throw ExecutionException("Failed to scan Ghidra classpath directory '$genericLib': ${e.message}", e)
        }
        setupJavaParameters(javaParameters)
        return javaParameters
    }

    companion object {
        private val GHIDRA_CLI_OPTS = listOf(
            "-Djava.system.class.loader=ghidra.GhidraClassLoader",
            "-Dfile.encoding=UTF8",
            "-Duser.country=US",
            "-Duser.language=en",
            "-Duser.variant=",
            "-Dsun.java2d.opengl=false",
            "-Djdk.tls.client.protocols=TLSv1.2,TLSv1.3",
            "-Dcpu.core.limit=",
            "-Dcpu.core.override=",
            "-Dfont.size.override=",
            "-Dpython.console.encoding=UTF-8",
            "-Xshare:off",
            "-Dsun.java2d.d3d=false",
            "-Dlog4j.skipJansi=true",
            "-XX:+ShowCodeDetailsInExceptionMessages",
        )
    }
}