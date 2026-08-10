package com.codingmates.ghidra.intellij.ide.newProjectWizard

import com.codingmates.ghidra.intellij.ide.GhidraBundle
import com.codingmates.ghidra.intellij.ide.icons.GhidraIcons
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.*
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import javax.swing.Icon


class GhidraNewProjectWizard : GeneratorNewProjectWizard {
    override val id: String = GhidraBundle.message("ghidra.id")

    override val name: String = GhidraBundle.message("ghidra.name")

    override val icon: Icon = GhidraIcons.Ghidra

    override fun createStep(context: WizardContext): NewProjectWizardStep =
        RootNewProjectWizardStep(context)
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::GitNewProjectWizardStep)
            .nextStep(::GhidraStep)
            .nextStep(::AssetsStep)

    class Builder : GeneratorNewProjectWizardBuilderAdapter(GhidraNewProjectWizard()) {
        override fun getWeight(): Int = JVM_WEIGHT + 100
    }
}