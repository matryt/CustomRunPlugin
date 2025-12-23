package com.mathieucuvelier.customrunplugin

import com.intellij.openapi.fileChooser.FileChooserDescriptor
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
import com.intellij.ui.dsl.builder.bindItem
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
        executionTypeComboBox.selectedItem = tempExecutionType
        customCommandField.text = configuration.customCommand
        argumentsField.text = configuration.arguments
    }

    override fun applyEditorTo(configuration: CustomRunConfigurationBase) {
        configuration.executionType = tempExecutionType
        configuration.customCommand = customCommandField.text
        configuration.arguments = argumentsField.text
    }

    override fun createEditor(): JComponent {
        configureCustomCommandField()
        argumentsField = RawCommandLineEditor()

        val panel = panel {
            row("Execution type:") {
                val comboBuilder = comboBox(ExecutionType.entries.toList())

                comboBuilder.bindItem(
                    { tempExecutionType },
                    {
                        if (it != null) {
                            tempExecutionType = it
                        }
                    }
                )

                executionTypeComboBox = comboBuilder.component
                executionTypeComboBox.renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) label.text = value.name.lowercase().replaceFirstChar { it.uppercase() }
                }
            }.layout(RowLayout.LABEL_ALIGNED)

            customCommandRow = row("  Custom executable path:") {
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
            customCommandRow?.visible(executionTypeComboBox.selectedItem == ExecutionType.OTHER)
            fireEditorStateChanged()
        }

        customCommandRow?.visible(executionTypeComboBox.selectedItem == ExecutionType.OTHER)

        val documentListener = object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = fireEditorStateChanged()
            override fun removeUpdate(e: DocumentEvent?) = fireEditorStateChanged()
            override fun changedUpdate(e: DocumentEvent?) = fireEditorStateChanged()
        }

        customCommandField.textField.document.addDocumentListener(documentListener)
        argumentsField.textField.document.addDocumentListener(documentListener)
    }
}
