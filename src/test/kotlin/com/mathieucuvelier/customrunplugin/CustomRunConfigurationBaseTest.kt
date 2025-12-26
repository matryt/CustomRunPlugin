package com.mathieucuvelier.customrunplugin

import com.intellij.execution.ExecutionException
import com.intellij.openapi.project.Project
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.File
import java.nio.file.Files

class CustomRunConfigurationBaseTest {

    private val mockProject: Project = Mockito.mock(Project::class.java)
    private val mockFactory: CustomConfigurationFactory = Mockito.mock(CustomConfigurationFactory::class.java)

    @Test
    fun `test getCommandLineForOtherCase with valid executable`() {
        val config = CustomRunConfigurationBase(mockProject, mockFactory, "TestConfig")
        val validExecutablePath = Files.createTempFile("validExecutable", null).toAbsolutePath().toString()
        config.customCommand = validExecutablePath
        config.executionType = ExecutionType.OTHER
        val commandLine = config.commandLineForOtherCase
        assertEquals(validExecutablePath, commandLine.commandLineString)
    }

    @Test
    fun `test getCommandLineForOtherCase with invalid executable`() {
        val config = CustomRunConfigurationBase(mockProject, mockFactory, "TestConfig")
        config.customCommand = "/path/to/invalid/executable"
        val exception = assertThrows(ExecutionException::class.java) {
            config.commandLineForOtherCase
        }
        assertEquals("Executable not found: '/path/to/invalid/executable'. Please verify the path or check that it exists in your PATH environment variable.", exception.message)
    }

    @Test
    fun `test getCommandLineForOtherCase with null command`() {
        val config = CustomRunConfigurationBase(mockProject, mockFactory, "TestConfig")
        config.customCommand = null
        val exception = assertThrows(ExecutionException::class.java) {
            config.commandLineForOtherCase
        }
        assertEquals("No executable specified. Please select an executable in the run configuration settings.", exception.message)
    }

    @Test
    fun `test getRustcAndCargoCommandLine with rustc`() {
        val config = CustomRunConfigurationBase(mockProject, mockFactory, "TestConfig")
        val commandLine = config.getRustcAndCargoCommandLine(true)
        val expectedExecutable = if (System.getProperty("os.name").startsWith("Windows")) "rustc.exe" else "rustc"
        assertEquals(expectedExecutable, File(commandLine.commandLineString).name)
    }

    @Test
    fun `test getRustcAndCargoCommandLine with cargo`() {
        val config = CustomRunConfigurationBase(mockProject, mockFactory, "TestConfig")
        val commandLine = config.getRustcAndCargoCommandLine(false)
        val expectedExecutable = if (System.getProperty("os.name").startsWith("Windows")) "cargo.exe" else "cargo"
        assertEquals(expectedExecutable, File(commandLine.commandLineString).name)
    }
}
