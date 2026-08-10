package com.codingmates.ghidra.intellij.ide.runConfiguration

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.codingmates.ghidra.intellij.ide.icons.GhidraIcons
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project


class GhidraLauncherConfigurationType : ConfigurationTypeBase(
    ID,
    GhidraBundle.message("ghidra.run-configuration.type.name"),
    GhidraBundle.message("ghidra.run-configuration.type.description"),
    GhidraIcons.Ghidra
), ConfigurationType {

    companion object {
        const val ID = "GhidraLauncherConfiguration"
    }

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