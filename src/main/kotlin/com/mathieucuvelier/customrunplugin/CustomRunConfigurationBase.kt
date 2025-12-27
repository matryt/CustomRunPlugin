package com.mathieucuvelier.customrunplugin

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import java.io.File
import java.util.Locale

class CustomRunConfigurationBase(project: Project, factory: ConfigurationFactory?, name: String?,
                                 private val executableResolver: ExecutableResolver = DefaultExecutableResolver()) :
    RunConfigurationBase<CustomRunConfigurationOptions?>(project, factory, name) {

    companion object {
        private val LOG: Logger = Logger.getInstance(CustomRunConfigurationBase::class.java)
        const val NO_EXECUTABLE_SPECIFIED_MSG: String = "No executable specified. Please select an executable in the run configuration settings."
        const val EXECUTABLE_NOT_FOUND_PREFIX: String = "Executable not found: '"
        const val EXECUTABLE_NOT_FOUND_SUFFIX: String = "'. Please verify the path or check that it exists in your PATH environment variable."
        const val DIRECTORY_INSTEAD_OF_EXECUTABLE_PREFIX: String = "The specified path is a directory, not an executable file: '"
        const val DIRECTORY_INSTEAD_OF_EXECUTABLE_SUFFIX: String = "'. Please select a valid executable file."
        const val FILE_NOT_EXECUTABLE_PREFIX: String = "The file '"
        const val FILE_NOT_EXECUTABLE_SUFFIX: String = "' is not executable. Please check file permissions."
    }

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
            val customCmd = this.customCommand
            if (customCmd == null || customCmd.trim { it <= ' ' }.isEmpty()) {
                LOG.warn("No executable specified for Other execution type")
                throw ExecutionException(NO_EXECUTABLE_SPECIFIED_MSG)
            }

            val file = File(customCmd)
            val resolvedFile: File = if (file.exists()) {
                file
            } else {
                val fileWithPath = executableResolver.findExecutable(customCmd)
                if (fileWithPath == null || !fileWithPath.exists()) {
                    LOG.warn("Executable not found: $customCmd")
                    throw ExecutionException(EXECUTABLE_NOT_FOUND_PREFIX + customCmd + EXECUTABLE_NOT_FOUND_SUFFIX)
                }
                fileWithPath
            }

            return getCommandLineWithCustomExecutable(resolvedFile)
        }

    private fun capitalize(original: String): String {
        return original.take(1).uppercase(Locale.getDefault()) + original.substring(1)
            .lowercase(Locale.getDefault())
    }

    @Throws(ExecutionException::class)
    fun getRustcAndCargoCommandLine(isRustc: Boolean): GeneralCommandLine {
        val command = if (isRustc) "rustc" else "cargo"
        val executable = executableResolver.findExecutable(command)
        if (executable == null || !executable.exists()) {
            LOG.warn("$command not found in PATH")
            throw ExecutionException(capitalize(command) + " is not found in your PATH environment variable. Please verify your Rust installation and ensure " + command + " is accessible from the command line.")
        }
        return buildCommandLine(executable.absolutePath, this.arguments)
    }


    @Throws(ExecutionException::class)
    private fun getCommandLineWithCustomExecutable(file: File): GeneralCommandLine {
        if (file.isDirectory()) {
            throw ExecutionException(DIRECTORY_INSTEAD_OF_EXECUTABLE_PREFIX + file.absolutePath + DIRECTORY_INSTEAD_OF_EXECUTABLE_SUFFIX)
        }

        if (!isExecutableFile(file)) {
            throw ExecutionException(FILE_NOT_EXECUTABLE_PREFIX + file.absolutePath + FILE_NOT_EXECUTABLE_SUFFIX)
        }

        return buildCommandLine(file.absolutePath, this.arguments)
    }

    private fun isExecutableFile(file: File): Boolean {
        val isWindows = System.getProperty("os.name").startsWith("Windows")
        return if (isWindows) {
            // On Windows accept common executable extensions when canExecute() may be false
            val lower = file.name.lowercase()
            val windowsExt = listOf(".exe", ".bat", ".cmd", ".com")
            file.canExecute() || windowsExt.any { lower.endsWith(it) }
        } else {
            file.canExecute()
        }
    }

    private fun buildCommandLine(exePath: String, args: String?): GeneralCommandLine {
        val commandLine = GeneralCommandLine(exePath)
        if (!args.isNullOrBlank()) {
            commandLine.addParameters(*ParametersList.parse(args))
        }
        val workDir = project.basePath ?: System.getProperty("user.dir")
        commandLine.setWorkDirectory(workDir)

        return commandLine
    }

    override fun getOptionsClass(): Class<out RunConfigurationOptions> {
        return CustomRunConfigurationOptions::class.java
    }
}