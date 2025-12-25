package com.mathieucuvelier.customrunplugin

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NonNls

class CustomConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return CustomRunConfigurationBase(project, this, "Custom Run Configuration")
    }

    override fun getId(): @NonNls String {
        return CustomRunConfigurationType.ID
    }

    override fun getOptionsClass(): Class<out BaseState?> {
        return CustomRunConfigurationOptions::class.java
    }
}