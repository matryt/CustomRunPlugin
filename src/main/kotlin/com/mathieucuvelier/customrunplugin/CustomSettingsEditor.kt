package com.mathieucuvelier.customrunplugin

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

class CustomSettingsEditor : SettingsEditor<CustomRunConfigurationBase>() {
    private lateinit var executionTypeComboBox: ComboBox<ExecutionType>
    private lateinit var customCommandField: TextFieldWithBrowseButton
    private lateinit var argumentsField: RawCommandLineEditor
    private var customCommandRow: Row? = null

    private var tempExecutionType: ExecutionType = ExecutionType.RUSTC

    override fun resetEditorFrom(configuration: CustomRunConfigurationBase) {
        tempExecutionType = configuration.executionType
        executionTypeComboBox.selectedItem = configuration.executionType
        customCommandField.text = configuration.customCommand.toString()
        argumentsField.text = configuration.arguments
    }

    override fun applyEditorTo(configuration: CustomRunConfigurationBase) {
        val selectedType = executionTypeComboBox.selectedItem as? ExecutionType ?: ExecutionType.RUSTC

        if (selectedType == ExecutionType.OTHER && customCommandField.text.trim().isEmpty()) {
            throw ConfigurationException("Please specify an executable path when using 'Other' execution type")
        }

        configuration.executionType = selectedType
        configuration.customCommand = customCommandField.text
        configuration.arguments = argumentsField.text
    }

    override fun createEditor(): JComponent {
        configureCustomCommandField()
        argumentsField = RawCommandLineEditor()

        val panel = panel {
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
        return panel
    }

    private fun configureCustomCommandField() {
        customCommandField = TextFieldWithBrowseButton()
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withTitle("Select Executable")
            .withDescription("Choose the executable to run")

        customCommandField.addActionListener {
            com.intellij.openapi.fileChooser.FileChooser.chooseFile(descriptor, null, null) { file ->
                customCommandField.text = file.path
                fireEditorStateChanged()
            }
        }
    }

    private fun configureVisibilityLogic() {
        executionTypeComboBox.addActionListener {
            val selected = executionTypeComboBox.selectedItem as? ExecutionType
            if (selected != null) {
                tempExecutionType = selected
                customCommandRow?.visible(selected == ExecutionType.OTHER)
                fireEditorStateChanged()
            }
        }

        customCommandRow?.visible(tempExecutionType == ExecutionType.OTHER)

        val documentListener = object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = fireEditorStateChanged()
            override fun removeUpdate(e: DocumentEvent?) = fireEditorStateChanged()
            override fun changedUpdate(e: DocumentEvent?) = fireEditorStateChanged()
        }

        customCommandField.textField.document.addDocumentListener(documentListener)
        argumentsField.textField.document.addDocumentListener(documentListener)
    }
}
