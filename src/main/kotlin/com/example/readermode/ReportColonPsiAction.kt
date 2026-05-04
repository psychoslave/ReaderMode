package com.example.readermode

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.io.File

/**
 * Debug action to report PSI node types for all colons in the current file.
 * Outputs to /tmp/colon_psi_report.txt for analysis.
 * Run via: Tools → Report Colon PSI (all colons)
 */
class ReportColonPsiAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        val report = StringBuilder()
        report.append("=== PSI Node Report for Colons in: ${psiFile.name} ===\n\n")

        // Walk all leaves and find colons
        var leaf: PsiElement? = psiFile.firstChild
        while (leaf != null) {
            walkAndReport(leaf, report)
            leaf = leaf.nextSibling
        }

        val outputFile = File("/tmp/colon_psi_report.txt")
        outputFile.writeText(report.toString())
        println("Report written to: ${outputFile.absolutePath}")
        println(report.toString())
    }

    private fun walkAndReport(element: PsiElement, report: StringBuilder) {
        if (element is LeafPsiElement && element.text == ":") {
            val path = buildParentPath(element)
            report.append("Colon at offset ${element.textRange.startOffset}:\n")
            report.append("  Text: '${element.text}'\n")
            report.append("  Parent path: $path\n")
            report.append("\n")
        }

        // Recurse
        var child = element.firstChild
        while (child != null) {
            walkAndReport(child, report)
            child = child.nextSibling
        }
    }

    private fun buildParentPath(element: PsiElement): String {
        val path = mutableListOf<String>()
        var current: PsiElement? = element
        var depth = 0
        while (current != null && depth < 10) {
            val name = current.javaClass.simpleName
            path.add(0, name)
            current = current.parent
            depth++
        }
        return path.joinToString(" ← ")
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}


