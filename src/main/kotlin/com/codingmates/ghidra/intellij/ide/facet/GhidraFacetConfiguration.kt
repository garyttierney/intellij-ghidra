package com.codingmates.ghidra.intellij.ide.facet

import com.intellij.facet.FacetConfiguration
import com.intellij.facet.ui.FacetEditorContext
import com.intellij.facet.ui.FacetEditorTab
import com.intellij.facet.ui.FacetValidatorsManager
import com.intellij.openapi.components.PersistentStateComponent

class GhidraFacetConfiguration : FacetConfiguration, PersistentStateComponent<GhidraFacetSettings> {

    var ghidraState = GhidraFacetSettings()

    override fun createEditorTabs(
        editorContext: FacetEditorContext,
        validatorsManager: FacetValidatorsManager
    ): Array<FacetEditorTab> {
        return arrayOf(GhidraFacetConfigurationEditor(ghidraState, editorContext, validatorsManager))
    }

    override fun getState(): GhidraFacetSettings = ghidraState

    override fun loadState(state: GhidraFacetSettings) {
        ghidraState = state
    }
}