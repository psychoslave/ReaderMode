package com.example.readermode

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.DumbAware

/**
 * View-menu toggle that enables / disables Reader Mode.
 *
 * On every toggle it immediately collapses (ON) or expands (OFF) all fold regions
 * that belong to the reader-mode plugin (identified by middot in identifiers, or
 * bracket-word placeholders like "go", "do go", "tap hop", etc.).
 */
class ToggleReaderModeAction : ToggleAction(), DumbAware {

    override fun isSelected(e: AnActionEvent): Boolean =
        ReaderModeService.getInstance().isEnabled

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        ReaderModeService.getInstance().isEnabled = state

        val project = e.project ?: return

        // Apply the collapse / expand change to every open text editor immediately.
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(project).allEditors
                .filterIsInstance<TextEditor>()
                .map { it.editor }
                .forEach { editor ->
                    editor.foldingModel.runBatchFoldingOperation {
                        editor.foldingModel.allFoldRegions
                            .filter { isReaderModeFold(it.placeholderText) }
                            .forEach { fold ->
                                // collapsed = reader-mode ON  →  expanded when turning OFF
                                fold.isExpanded = !state
                            }
                    }
                }
        }
    }

    private fun isReaderModeFold(placeholder: String): Boolean =
        MiddotConverter.MIDDOT in placeholder || BracketRenderer.isReaderModePlaceholder(placeholder)
}
