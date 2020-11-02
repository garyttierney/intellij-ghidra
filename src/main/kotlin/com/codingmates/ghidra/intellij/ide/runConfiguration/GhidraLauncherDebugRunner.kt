package com.codingmates.ghidra.intellij.ide.runConfiguration

import com.intellij.debugger.impl.GenericDebuggerRunner
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultDebugExecutor


class GhidraLauncherDebugRunner : GenericDebuggerRunner() {
    override fun getRunnerId() = "GhidraLauncherDebugger"

    override fun canRun(executorId: String, runProfile: RunProfile): Boolean {
        return DefaultDebugExecutor.EXECUTOR_ID == executorId && runProfile is GhidraLauncherConfiguration
    }
}