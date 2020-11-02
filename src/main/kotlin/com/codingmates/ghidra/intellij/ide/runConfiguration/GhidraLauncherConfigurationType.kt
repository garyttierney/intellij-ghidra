package com.codingmates.ghidra.intellij.ide.runConfiguration

import com.codingmates.ghidra.intellij.ide.icons.GhidraIcons
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project


class GhidraLauncherConfigurationType : ConfigurationTypeBase(
    "GhidraLauncherConfiguration",
    "Ghidra Launcher",
    "Start the configured Ghidra installation with the project classpath.",
    GhidraIcons.Ghidra
), ConfigurationType {

    init {
        addFactory(
            object : ConfigurationFactory(this) {
                override fun createTemplateConfiguration(project: Project): RunConfiguration {
                    return GhidraLauncherConfiguration(project, this, "")
                }

                override fun getId() = name
            })
    }
}