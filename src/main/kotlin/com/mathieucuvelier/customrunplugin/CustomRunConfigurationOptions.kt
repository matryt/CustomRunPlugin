package com.mathieucuvelier.customrunplugin

import com.intellij.execution.configurations.RunConfigurationOptions

class CustomRunConfigurationOptions : RunConfigurationOptions() {
    var executionTypeName by string(ExecutionType.RUSTC.name)
    var customCommand by string("")
    var arguments by string("")

    var executionType: ExecutionType
        get() = try {
            ExecutionType.valueOf(executionTypeName ?: ExecutionType.RUSTC.name)
        } catch (e: IllegalArgumentException) {
            ExecutionType.RUSTC
        }
        set(value) {
            executionTypeName = value.name
        }
}
