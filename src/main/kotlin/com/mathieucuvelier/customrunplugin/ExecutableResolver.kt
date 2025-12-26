package com.mathieucuvelier.customrunplugin

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import java.io.File

/**
 * Abstraction to resolve an executable by name or path. Default implementation uses
 * PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS under the hood.
 */
fun interface ExecutableResolver {
    fun findExecutable(commandOrPath: String): File?
}

class DefaultExecutableResolver : ExecutableResolver {
    override fun findExecutable(commandOrPath: String): File? {
        return PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS(commandOrPath)
    }
}
