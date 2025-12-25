package com.mathieucuvelier.customrunplugin

import com.intellij.openapi.util.IconLoader.getIcon
import javax.swing.Icon

interface PluginIcons {
    companion object {
        val CUSTOM_RUN: Icon = getIcon("/icons/customRun.svg", PluginIcons::class.java)
    }
}

