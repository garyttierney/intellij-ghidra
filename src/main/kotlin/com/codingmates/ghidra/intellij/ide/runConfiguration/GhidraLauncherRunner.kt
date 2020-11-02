package com.codingmates.ghidra.intellij.ide.runConfiguration

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.DefaultJavaProgramRunner


class GhidraLauncherRunner : DefaultJavaProgramRunner() {
    override fun getRunnerId() = "GhidraLauncherRunner"

    override fun canRun(executorId: String, runProfile: RunProfile): Boolean {
        return executorId == DefaultRunExecutor.EXECUTOR_ID && runProfile is GhidraLauncherConfiguration
    }
}