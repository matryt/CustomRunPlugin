package com.mathieucuvelier.customrunplugin;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.NotNullLazyValue;

public class CustomRunConfigurationType extends ConfigurationTypeBase {
    static final String ID = "CUSTOM_RUN_CONFIGURATION";

    public CustomRunConfigurationType() {
        super(ID, "Custom Run Configuration", "Run custom executables with configurable arguments", NotNullLazyValue.createValue(() -> PluginIcons.CUSTOM_RUN));
        addFactory(new CustomConfigurationFactory(this));
    }
}
