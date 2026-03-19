package com.example.readermode

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.io.File

class ColonPsiReporterAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor: Editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
        val psiFile: PsiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return

        val output = StringBuilder()
        psiFile.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: com.intellij.psi.PsiElement) {
                super.visitElement(element)
                if (element is LeafPsiElement && element.text == ":") {
                    val parent = element.parent
                    val grandparent = parent?.parent
                    val offset = element.textOffset
                    val line = editor.document.getLineNumber(offset) + 1
                    output.append("Colon at offset $offset (line $line): parent=${parent?.javaClass?.simpleName}, grandparent=${grandparent?.javaClass?.simpleName}\n")
                }
            }
        })
        // Write output to file
        val outFile = File("/tmp/colon_psi_report.txt")
        outFile.writeText(output.toString())
    }
}
