package com.mathieucuvelier.customrunplugin

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import java.io.File
import java.util.Locale

class CustomRunConfigurationBase(project: Project, factory: ConfigurationFactory?, name: String?) :
    RunConfigurationBase<CustomRunConfigurationOptions?>(project, factory, name) {
    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
        return CustomSettingsEditor()
    }

    override fun getOptions(): CustomRunConfigurationOptions {
        return super.getOptions() as CustomRunConfigurationOptions
    }

    var executionType: ExecutionType
        get() = options.executionType
        set(type) {
            options.executionType = type
        }

    var customCommand: String?
        get() = options.customCommand
        set(command) {
            options.customCommand = command
        }

    var arguments: String?
        get() = options.arguments
        set(args) {
            options.arguments = args
        }

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(executionEnvironment) {
            @Throws(ExecutionException::class)
            override fun startProcess(): ProcessHandler {
                val type: ExecutionType = executionType

                val commandLine = if (type == ExecutionType.OTHER) commandLineForOtherCase
                else getRustcAndCargoCommandLine(type == ExecutionType.RUSTC)

                return OSProcessHandler(commandLine)
            }
        }
    }

    @get:Throws(ExecutionException::class)
    val commandLineForOtherCase: GeneralCommandLine
        get() {
            val commandLine: GeneralCommandLine
            val customCmd = this.customCommand
            if (customCmd == null || customCmd.trim { it <= ' ' }.isEmpty()) {
                throw ExecutionException("No executable specified. Please select an executable in the run configuration settings.")
            }

            val file = File(customCmd)
            if (file.exists()) {
                commandLine = getCommandLineWithCustomExecutable(file)
            } else {
                val fileWithPath = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS(customCmd)
                if (fileWithPath == null || !fileWithPath.exists()) {
                    throw ExecutionException("Executable not found: '$customCmd'. Please verify the path or check that it exists in your PATH environment variable.")
                }
                commandLine = getCommandLineWithCustomExecutable(fileWithPath)
            }
            return commandLine
        }

    private fun capitalize(original: String): String {
        return original.take(1).uppercase(Locale.getDefault()) + original.substring(1)
            .lowercase(Locale.getDefault())
    }

    @Throws(ExecutionException::class)
    fun getRustcAndCargoCommandLine(isRustc: Boolean): GeneralCommandLine {
        val commandLine: GeneralCommandLine
        val command = if (isRustc) "rustc" else "cargo"
        val executable = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS(command)
        if (executable == null || !executable.exists()) throw ExecutionException(capitalize(command) + " is not found in your PATH environment variable. Please verify your Rust installation and ensure " + command + " is accessible from the command line.")
        commandLine = GeneralCommandLine(executable.absolutePath)
        if (!this.arguments!!.isBlank()) commandLine.addParameters(*ParametersList.parse(this.arguments!!))
        commandLine.setWorkDirectory(project.basePath)
        return commandLine
    }


    @Throws(ExecutionException::class)
    private fun getCommandLineWithCustomExecutable(file: File): GeneralCommandLine {
        if (file.isDirectory()) {
            throw ExecutionException("The specified path is a directory, not an executable file: '" + file.absolutePath + "'. Please select a valid executable file.")
        }

        if (!file.canExecute()) {
            throw ExecutionException("The file '" + file.absolutePath + "' is not executable. Please check file permissions.")
        }

        val commandLine = GeneralCommandLine(file.absolutePath)

        val args = this.arguments
        if (args != null && args.trim { it <= ' ' }.isNotBlank()) {
            commandLine.addParameters(*ParametersList.parse(args))
        }

        commandLine.setWorkDirectory(project.basePath)

        return commandLine
    }

    override fun getOptionsClass(): Class<out RunConfigurationOptions> {
        return CustomRunConfigurationOptions::class.java
    }
}