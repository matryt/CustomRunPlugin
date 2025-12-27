package com.mathieucuvelier.customrunplugin

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.TopGap
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class CustomSettingsEditor : SettingsEditor<CustomRunConfigurationBase>() {
    private var panelComponent: JComponent? = null
    private lateinit var executionTypeComboBox: ComboBox<ExecutionType>
    private lateinit var customCommandField: TextFieldWithBrowseButton
    private lateinit var argumentsField: RawCommandLineEditor
    private var customCommandRow: Row? = null
    private var listenersInitialized: Boolean = false

    public override fun resetEditorFrom(configuration: CustomRunConfigurationBase) {
        if (panelComponent == null) createEditor()

        executionTypeComboBox.selectedItem = configuration.executionType
        customCommandField.text = configuration.customCommand ?: ""
        argumentsField.text = configuration.arguments ?: ""
    }

    public override fun applyEditorTo(configuration: CustomRunConfigurationBase) {
        if (panelComponent == null) createEditor()

        val selectedType = executionTypeComboBox.selectedItem as? ExecutionType ?: ExecutionType.RUSTC

        if (selectedType == ExecutionType.OTHER && customCommandField.text.trim().isEmpty()) {
            throw ConfigurationException(CustomRunConfigurationBase.NO_EXECUTABLE_SPECIFIED_MSG)
        }

        configuration.executionType = selectedType
        configuration.customCommand = customCommandField.text.takeIf { it.isNotBlank() }
        configuration.arguments = argumentsField.text
    }

    public override fun createEditor(): JComponent {
        // Create once and reuse to avoid duplicated listeners/lateinit issues in tests
        if (panelComponent != null) return panelComponent!!

        configureCustomCommandField()
        argumentsField = RawCommandLineEditor()

        val builtPanel = panel {
            row("Execution type:") {
                executionTypeComboBox = comboBox(ExecutionType.entries.toList()).component
                executionTypeComboBox.renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) label.text = value.name.lowercase().replaceFirstChar { it.uppercase() }
                }
            }.layout(RowLayout.LABEL_ALIGNED)

            customCommandRow = row("Custom executable path:") {
                cell(customCommandField).align(Align.FILL)
            }.topGap(TopGap.SMALL).layout(RowLayout.LABEL_ALIGNED)

            row("Arguments:") {
                cell(argumentsField).resizableColumn()
            }.topGap(TopGap.SMALL).layout(RowLayout.LABEL_ALIGNED)
        }

        configureVisibilityLogic()
        panelComponent = builtPanel
        return panelComponent!!
    }

    private fun configureCustomCommandField() {
        customCommandField = TextFieldWithBrowseButton()
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withTitle("Select Executable")
            .withDescription("Choose the executable to run")

        customCommandField.addActionListener {
            FileChooser.chooseFile(descriptor, null, null) { file ->
                customCommandField.text = file.path
                fireEditorStateChanged()
            }
        }
    }

    private fun configureVisibilityLogic() {
        if (!this::executionTypeComboBox.isInitialized) return
        if (listenersInitialized) return

        val updateVisibility = {
            val selected = executionTypeComboBox.selectedItem as? ExecutionType
            customCommandRow?.visible(selected == ExecutionType.OTHER)
            fireEditorStateChanged()
        }

        executionTypeComboBox.addActionListener { updateVisibility() }

        updateVisibility()

        val documentListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = fireEditorStateChanged()
            override fun removeUpdate(e: DocumentEvent?) = fireEditorStateChanged()
            override fun changedUpdate(e: DocumentEvent?) = fireEditorStateChanged()
        }

        if (this::customCommandField.isInitialized) customCommandField.textField.document.addDocumentListener(documentListener)
        if (this::argumentsField.isInitialized) argumentsField.textField.document.addDocumentListener(documentListener)

        listenersInitialized = true
    }
}
