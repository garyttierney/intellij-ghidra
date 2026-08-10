package com.codingmates.ghidra.intellij.ide.newProjectWizard

import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.util.Key


interface GhidraData {
    val type: GhidraProjectType
    val typeProperty: ObservableMutableProperty<GhidraProjectType>
    val ghidraPath: String
    val ghidraPathProperty: ObservableMutableProperty<String>
    var ghidraModules: Map<String, String>
    val ghidraModulesProperty: ObservableMutableProperty<Map<String, String>>

    companion object {
        val KEY: Key<GhidraData> = Key.create(GhidraData::class.java.name)
    }
}
