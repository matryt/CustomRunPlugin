package com.mathieucuvelier.customrunplugin

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.NotNullFactory
import com.intellij.openapi.util.NotNullLazyValue
import javax.swing.Icon

class CustomRunConfigurationType : ConfigurationTypeBase(
    ID,
    "Custom Run Configuration",
    "Run custom executables with configurable arguments",
    NotNullLazyValue.createValue<Icon?>(
        NotNullFactory { PluginIcons.CUSTOM_RUN })
) {
    init {
        addFactory(CustomConfigurationFactory(this))
    }

    companion object {
        const val ID: String = "CUSTOM_RUN_CONFIGURATION"
    }
}