package com.codingmates.ghidra.intellij.ide.facet

import com.intellij.facet.ui.FacetEditorContext
import com.intellij.facet.ui.FacetEditorTab
import com.intellij.facet.ui.FacetValidatorsManager
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


class GhidraFacetConfigurationEditor(
    private val state: GhidraFacetState,
    private val _context: FacetEditorContext,
    private val _validator: FacetValidatorsManager
) : FacetEditorTab() {

    private val installationPathEditor = JTextField(state.installationPath)

    override fun createComponent(): JComponent {
        val top = JPanel(BorderLayout())
        top.add(JLabel("Path to Ghidra installation: "), BorderLayout.WEST)
        top.add(installationPathEditor)
        val facetPanel = JPanel(BorderLayout())
        facetPanel.add(top, BorderLayout.NORTH)
        return facetPanel
    }

    /**
     * @return the name of this facet for display in this editor tab.
     */
    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return GhidraFacetType.FACET_NAME
    }


    override fun isModified(): Boolean {
        return !StringUtil.equals(state.installationPath, installationPathEditor.text.trim())
    }

    override fun apply() {
        // Not much to go wrong here, but fulfill the contract
        try {
            val newTextContent: String = installationPathEditor.text
            state.installationPath = newTextContent
        } catch (e: Exception) {
            throw ConfigurationException(e.toString())
        }
    }

    override fun reset() {
        installationPathEditor.text = state.installationPath
    }
}