package com.codingmates.ghidra.intellij.ide.facet


import com.intellij.ProjectTopics
import com.intellij.facet.Facet
import com.intellij.facet.FacetManager
import com.intellij.facet.FacetManagerAdapter
import com.intellij.facet.FacetType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.libraries.Library
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

    init {
        connection.subscribe(FacetManager.FACETS_TOPIC, object : FacetManagerAdapter() {
            override fun beforeFacetRemoved(facet: Facet<*>) {
                removeLibrary()
            }

            override fun facetConfigurationChanged(facet: Facet<*>) {
                updateLibrary()
            }
        })

        connection.subscribe(ProjectTopics.PROJECT_ROOTS, object : ModuleRootListener {
            override fun rootsChanged(event: ModuleRootEvent) {
                invokeLater { updateLibrary() }
            }
        })
    }

    override fun initFacet() {
        updateLibrary()
    }

    fun removeLibrary() = runWriteAction {
        val rootManager = ModuleRootManager.getInstance(module)
        val model = rootManager.modifiableModel
        var modelChanged = false

        try {
            val libraries = ModifiableModelsProvider.SERVICE.getInstance().libraryTableModifiableModel
            val library = libraries.getLibraryByName(GHIDRA_LIBRARY_NAME)

            if (library != null) {
                libraries.removeLibrary(library)
                modelChanged = true
            }
        } finally {
            if (modelChanged) {
                model.commit()
            } else {
                model.dispose()
            }
        }
    }

    fun updateLibrary() = runWriteAction {
        val rootManager = ModuleRootManager.getInstance(module)
        val model = rootManager.modifiableModel

        try {
            val installation = configuration.loadGhidraInstallation()
            val libraries = ModifiableModelsProvider.SERVICE.getInstance().libraryTableModifiableModel

            var library = libraries.getLibraryByName(GHIDRA_LIBRARY_NAME)
            if (library == null) {
                library = libraries.createLibrary(GHIDRA_LIBRARY_NAME)

                val libraryModel = library.modifiableModel
                installation.binaries.forEach { libraryModel.addRoot(it, OrderRootType.CLASSES) }
                installation.sources.forEach { libraryModel.addRoot(it, OrderRootType.SOURCES) }

                libraryModel.commit()
                libraries.commit()
            }

            if (!libraries.isChanged) {
                libraries.dispose()
            }

            var hasLibrary = false
            for (entry in model.orderEntries) {
                if (entry is LibraryOrderEntry && entry.libraryName == GHIDRA_LIBRARY_NAME) {
                    hasLibrary = true
                    break
                }
            }

            if (!hasLibrary) {
                model.addLibraryEntry(library)
            }
        } finally {
            if (model.isChanged) {
                model.commit()
            } else {
                model.dispose()
            }
        }
    }

    companion object {
        const val GHIDRA_LIBRARY_NAME = "Ghidra"

        fun findAnyInProject(project: Project) = ModuleManager.getInstance(project)
            .modules
            .mapNotNull { FacetManager.getInstance(it).getFacetByType(GhidraFacetType.FACET_TYPE_ID) }
            .first()
    }
}