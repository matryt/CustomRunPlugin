package com.mathieucuvelier.customrunplugin;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CustomRunConfigurationBase extends RunConfigurationBase<CustomRunConfigurationOptions> {

    protected CustomRunConfigurationBase(@NotNull Project project, @Nullable ConfigurationFactory factory, @Nullable String name) {
        super(project, factory, name);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new CustomSettingsEditor();
    }

    @Override
    protected @NotNull CustomRunConfigurationOptions getOptions() {
        return (CustomRunConfigurationOptions) super.getOptions();
    }

    public ExecutionType getExecutionType() {
        return getOptions().getExecutionType();
    }

    public void setExecutionType(ExecutionType type) {
        getOptions().setExecutionType(type);
    }

    public String getCustomCommand() {
        return getOptions().getCustomCommand();
    }

    public void setCustomCommand(String command) {
        getOptions().setCustomCommand(command);
    }

    public String getArguments() {
        return getOptions().getArguments();
    }

    public void setArguments(String args) {
        getOptions().setArguments(args);
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        return new CommandLineState(executionEnvironment) {
            @Override
            protected @NotNull ProcessHandler startProcess() throws ExecutionException {
                ExecutionType type = getExecutionType();
                GeneralCommandLine commandLine;

                if (type == ExecutionType.OTHER) {
                    String customCmd = getCustomCommand();
                    if (customCmd == null || customCmd.trim().isEmpty()) {
                        throw new ExecutionException("No executable specified!");
                    }

                    File file = new File(customCmd);
                    if (file.exists()) {
                        commandLine = getCommandLineWithCustomExecutable(file);
                    } else {
                        File fileWithPath = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS(customCmd);
                        if (fileWithPath == null || !fileWithPath.exists()) {
                            throw new ExecutionException("Executable not found: " + customCmd);
                        }
                        commandLine = getCommandLineWithCustomExecutable(fileWithPath);
                    }
                } else {
                                        throw new ExecutionException("Execution type " + type + " is not supported yet.");
                }

                return new OSProcessHandler(commandLine);
            }
        };
    }

    @NotNull
    private GeneralCommandLine getCommandLineWithCustomExecutable(File file) throws ExecutionException {
        if (file.isDirectory()) {
            throw new ExecutionException("The given path isn't an executable file!");
        }

        GeneralCommandLine commandLine = new GeneralCommandLine(file.getAbsolutePath());

        String args = getArguments();
        if (args != null && !args.trim().isEmpty()) {
            commandLine.addParameters(ParametersList.parse(args));
        }

        commandLine.setWorkDirectory(getProject().getBasePath());

        return commandLine;
    }
}
