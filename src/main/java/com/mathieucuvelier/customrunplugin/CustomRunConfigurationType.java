package com.mathieucuvelier.customrunplugin;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NotNullLazyValue;

public class CustomRunConfigurationType extends ConfigurationTypeBase {
    static final String ID = "CUSTOM_RUN_CONFIGURATION";

    public CustomRunConfigurationType() {
        super(ID, "Custom Run Configuration", "A custom run configuration example", NotNullLazyValue.createValue(() -> AllIcons.Nodes.Console));
        addFactory(new CustomConfigurationFactory(this));
    }
}
