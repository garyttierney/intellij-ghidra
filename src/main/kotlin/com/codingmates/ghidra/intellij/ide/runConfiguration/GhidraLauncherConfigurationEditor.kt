package com.codingmates.ghidra.intellij.ide.runConfiguration

import com.intellij.execution.ui.DefaultJreSelector
import com.intellij.execution.ui.JrePathEditor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.PanelWithAnchor
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent


class GhidraLauncherConfigurationEditor(project: Project) : SettingsEditor<GhidraLauncherConfiguration>(),
    PanelWithAnchor {

    private var anchorComponent: JComponent? = null
    private val jreEditor = JrePathEditor(DefaultJreSelector.projectSdk(project))

    private val configPanel = panel(LCFlags.fillX) {
        row {
            jreEditor(CCFlags.growX)
        }
    }

    override fun applyEditorTo(configuration: GhidraLauncherConfiguration) {
        configuration.alternativeJrePath = jreEditor.jrePathOrName
        configuration.isAlternativeJrePathEnabled = jreEditor.isAlternativeJreSelected
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
    }

    override fun setAnchor(anchor: @Nullable JComponent?) {
        anchorComponent = anchor
        jreEditor.anchor = anchor
    }

    override fun disposeEditor() {
    }
}