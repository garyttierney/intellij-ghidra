package com.codingmates.ghidra.intellij.ide.facet.model

import com.intellij.openapi.vfs.VirtualFile

data class GhidraInstallation(
    val path: String,
    val version: GhidraVersion,
    val sources: List<VirtualFile>,
    val binaries: List<VirtualFile>
)