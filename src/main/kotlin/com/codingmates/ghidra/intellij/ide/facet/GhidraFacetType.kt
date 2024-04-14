package com.codingmates.ghidra.intellij.ide.facet

import com.codingmates.ghidra.intellij.ide.icons.GhidraIcons
import com.intellij.facet.Facet
import com.intellij.facet.FacetType
import com.intellij.facet.FacetTypeId
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType

class GhidraFacetType : FacetType<GhidraFacet, GhidraFacetConfiguration>(FACET_TYPE_ID, FACET_ID, FACET_NAME) {
    companion object {
        const val FACET_ID = "GHIDRA_EXT"
        const val FACET_NAME = "Ghidra"
    }

    override fun createDefaultConfiguration() = GhidraFacetConfiguration()

    override fun createFacet(
        module: Module,
        name: String,
        configuration: GhidraFacetConfiguration,
        underlyingFacet: Facet<*>?
    ) = GhidraFacet(this, module, name, configuration, underlyingFacet)

    override fun getIcon() = GhidraIcons.Ghidra

    override fun isSuitableModuleType(moduleType: ModuleType<*>?): Boolean {
        return true
    }
}

val FACET_TYPE_ID = FacetTypeId<GhidraFacet>(GhidraFacetType.FACET_ID)