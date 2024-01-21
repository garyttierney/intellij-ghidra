package com.codingmates.ghidra.intellij.ide.runConfiguration

import com.intellij.execution.ui.DefaultJreSelector
import com.intellij.execution.ui.JrePathEditor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.PanelWithAnchor
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.annotations.Nullable
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JTextField


class GhidraLauncherConfigurationEditor(project: Project) : SettingsEditor<GhidraLauncherConfiguration>(),
    PanelWithAnchor {

    private var anchorComponent: JComponent? = null
    private val jreEditor = JrePathEditor(DefaultJreSelector.projectSdk(project))
    private val argEditor = ExpandableTextField()
    private val isHeadless = JCheckBox()

    private val configPanel = panel {
        row {
            cell(jreEditor).align(AlignX.FILL)
        }
        row("Arguments") {
            cell(argEditor).align(AlignX.FILL)
        }
        row("Use headless") {
            cell(isHeadless).align(AlignX.FILL)
        }
    }

    override fun applyEditorTo(configuration: GhidraLauncherConfiguration) {
        configuration.alternativeJrePath = jreEditor.jrePathOrName
        configuration.isAlternativeJrePathEnabled = jreEditor.isAlternativeJreSelected
        configuration.setArgs(argEditor.getText())
        configuration.setHeadless(isHeadless.isSelected())
        configuration.checkConfiguration()
    }

    override fun createEditor(): JComponent {
        return configPanel
    }

    override fun getAnchor(): JComponent? {
        return anchorComponent
    }

    override fun resetEditorFrom(s: GhidraLauncherConfiguration) {
        jreEditor.setPathOrName(s.alternativeJrePath, s.isAlternativeJrePathEnabled)
        argEditor.text = s.getArgs()
        isHeadless.isSelected = s.getHeadless()
    }

    override fun setAnchor(anchor: @Nullable JComponent?) {
        anchorComponent = anchor
        jreEditor.anchor = anchor
    }

    override fun disposeEditor() {
    }
}
