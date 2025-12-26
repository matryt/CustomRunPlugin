package com.mathieucuvelier.customrunplugin.testutils

import com.mathieucuvelier.customrunplugin.ExecutableResolver
import java.io.File

/**
 * Utility to create small fake resolvers for tests.
 */
object FakeExecutableResolver {
    fun fromFile(file: File): ExecutableResolver = ExecutableResolver { _ -> file }
}

