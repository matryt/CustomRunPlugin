package com.mathieucuvelier.customrunplugin

import com.intellij.execution.ExecutionException
import com.intellij.openapi.project.Project
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

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
        val invalid = "/path/to/invalid/executable"
        config.customCommand = invalid
        val exception = assertThrows(ExecutionException::class.java) {
            config.commandLineForOtherCase
        }
        assertEquals(CustomRunConfigurationBase.EXECUTABLE_NOT_FOUND_PREFIX + invalid + CustomRunConfigurationBase.EXECUTABLE_NOT_FOUND_SUFFIX, exception.message)
    }

    @Test
    fun `test getCommandLineForOtherCase with null command`() {
        val config = CustomRunConfigurationBase(mockProject, mockFactory, "TestConfig")
        config.customCommand = null
        val exception = assertThrows(ExecutionException::class.java) {
            config.commandLineForOtherCase
        }
        assertEquals(CustomRunConfigurationBase.NO_EXECUTABLE_SPECIFIED_MSG, exception.message)
    }

    @Test
    fun `test getRustcAndCargoCommandLine with rustc`() {
        val isWindows = System.getProperty("os.name").startsWith("Windows")
        val exeName = if (isWindows) "rustc.exe" else "rustc"
        val tempDir: Path = Files.createTempDirectory("fakeRust")
        val rustExePath = tempDir.resolve(exeName)
        Files.createFile(rustExePath)
        rustExePath.toFile().setExecutable(true)

        val resolver = ExecutableResolver { cmd ->
            if (cmd == "rustc") rustExePath.toFile() else null
        }

        val config = CustomRunConfigurationBase(mockProject, mockFactory, "TestConfig", resolver)
        val commandLine = config.getRustcAndCargoCommandLine(true)
        val expectedExecutable = exeName
        assertEquals(expectedExecutable, File(commandLine.exePath).name)
    }

    @Test
    fun `test getRustcAndCargoCommandLine with cargo`() {
        val isWindows = System.getProperty("os.name").startsWith("Windows")
        val exeName = if (isWindows) "cargo.exe" else "cargo"
        val tempDir: Path = Files.createTempDirectory("fakeCargo")
        val cargoExePath = tempDir.resolve(exeName)
        Files.createFile(cargoExePath)
        cargoExePath.toFile().setExecutable(true)

        val resolver = ExecutableResolver { cmd ->
            if (cmd == "cargo") cargoExePath.toFile() else null
        }

        val config = CustomRunConfigurationBase(mockProject, mockFactory, "TestConfig", resolver)
        val commandLine = config.getRustcAndCargoCommandLine(false)
        val expectedExecutable = exeName
        assertEquals(expectedExecutable, File(commandLine.exePath).name)
    }
}
