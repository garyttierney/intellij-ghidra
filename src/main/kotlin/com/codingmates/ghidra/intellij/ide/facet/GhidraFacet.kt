package com.codingmates.ghidra.intellij.ide.facet


import com.codingmates.ghidra.intellij.ide.facet.model.GhidraProperties
import com.intellij.facet.*
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.*
import com.intellij.util.messages.MessageBusConnection
import java.nio.file.Paths


class GhidraFacet(
    facetType: FacetType<out Facet<*>, *>,
    module: Module,
    name: String,
    configuration: GhidraFacetConfiguration,
    underlyingFacet: Facet<*>?
) : Facet<GhidraFacetConfiguration>(facetType, module, name, configuration, underlyingFacet) {
    private val connection: MessageBusConnection = module.project.messageBus.connect()

    var installationProperties: GhidraProperties? = null
    val installationPath
        get() = configuration.ghidraState.installationPath

    init {
        connection.subscribe(FacetManager.FACETS_TOPIC, object : FacetManagerListener {
            override fun beforeFacetRemoved(facet: Facet<*>) {
                removeLibrary()
            }

            override fun facetConfigurationChanged(facet: Facet<*>) {
                updateLibrary()
            }
        })

        connection.subscribe(ModuleRootListener.TOPIC, object : ModuleRootListener {
            override fun rootsChanged(event: ModuleRootEvent) {
                invokeLater { updateLibrary() }
            }
        })
    }

    override fun initFacet() {
        invokeLater {
            try {
                installationProperties = GhidraProperties.discoverFromRuntime(Paths.get(installationPath))
            } catch (e: Exception) {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("IDE-errors")
                    .createNotification(e.message.toString(), NotificationType.ERROR)
                    .notify(module.project)
            }
        }
    }

    fun removeLibrary() = runWriteAction {
        val rootManager = ModuleRootManager.getInstance(module)
        val model = rootManager.modifiableModel
        var modelChanged = false

        try {
            val libraries = ModifiableModelsProvider.getInstance().libraryTableModifiableModel
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
            val libraries = ModifiableModelsProvider.getInstance().libraryTableModifiableModel

            var library = libraries.getLibraryByName(GHIDRA_LIBRARY_NAME)
            if (library == null) {
                library = libraries.createLibrary(GHIDRA_LIBRARY_NAME)

                val libraryModel = library.modifiableModel

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
        } catch (err: Exception) {
            // TODO: log this
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
            .modules.firstNotNullOf { FacetManager.getInstance(it).getFacetByType(FACET_TYPE_ID) }
    }
}