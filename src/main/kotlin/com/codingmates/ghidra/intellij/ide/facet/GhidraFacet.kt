package com.codingmates.ghidra.intellij.ide.facet


import com.intellij.facet.Facet
import com.intellij.facet.FacetManager
import com.intellij.facet.FacetManagerListener
import com.intellij.facet.FacetType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.*
import com.intellij.util.messages.MessageBusConnection


class GhidraFacet(
    facetType: FacetType<out Facet<*>, *>,
    module: Module,
    name: String,
    configuration: GhidraFacetConfiguration,
    underlyingFacet: Facet<*>?
) : Facet<GhidraFacetConfiguration>(facetType, module, name, configuration, underlyingFacet) {
    private val connection: MessageBusConnection = module.project.messageBus.connect()

    val installationPath
        get() = configuration.ghidraState.installationPath

    companion object {
        fun findAnyInProject(project: Project) = ModuleManager.getInstance(project)
            .modules.firstNotNullOf { FacetManager.getInstance(it).getFacetByType(FACET_TYPE_ID) }
    }
}