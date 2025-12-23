package com.mathieucuvelier.customrunplugin;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomConfigurationFactory extends ConfigurationFactory {
    public CustomConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new CustomRunConfigurationBase(project, this, "Custom Run Configuration");
    }

    @Override
    public @NotNull @NonNls String getId() {
        return CustomRunConfigurationType.ID;
    }

    @Override
    public @Nullable Class<? extends BaseState> getOptionsClass() {
        return CustomRunConfigurationOptions.class;
    }
}
