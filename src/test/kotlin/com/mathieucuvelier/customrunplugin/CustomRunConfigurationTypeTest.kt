package com.mathieucuvelier.customrunplugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class CustomRunConfigurationTypeTest {

    @Test
    fun `test CustomRunConfigurationType ID`() {
        val type = CustomRunConfigurationType()
        assertEquals("CUSTOM_RUN_CONFIGURATION", type.id, "ID should match")
    }

    @Test
    fun `test CustomRunConfigurationType name`() {
        val type = CustomRunConfigurationType()
        assertEquals("Custom Run Configuration", type.displayName, "Display name should match")
    }

    @Test
    fun `test CustomRunConfigurationType icon`() {
        val type = CustomRunConfigurationType()
        assertNotNull(type.icon, "Icon should not be null")
    }
}
