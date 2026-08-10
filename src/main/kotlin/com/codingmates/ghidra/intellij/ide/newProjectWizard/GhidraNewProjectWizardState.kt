package com.codingmates.ghidra.intellij.ide.newProjectWizard

import com.intellij.ide.util.PropertiesComponent

/**
 * Use to store persistent data across Intellij start-ups.
 */
object GhidraNewProjectWizardState {
    private const val KEY_PATH = "ghidra.wizard.path"  // Save Ghidra path since this probably won't change for each
                                                       // user
    private val props = PropertiesComponent.getInstance()

    var lastPath: String
        get() = props.getValue(KEY_PATH, "")
        set(value) { props.setValue(KEY_PATH, value) }
}