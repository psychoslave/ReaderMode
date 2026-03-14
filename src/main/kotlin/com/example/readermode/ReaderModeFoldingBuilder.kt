package com.example.readermode

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Folding builder — one fold per token, no merging.
 *
 * Identifiers         → middot placeholder     (quickBrownFox  →  quick·brown·fox)
 * Brackets / braces   → word placeholder       ((  →  do,  {  →  tap)
 * Member-access (->)  → word placeholder       (->  →  whose)
 * $ variable sigil    → "see" + name           ($someVar  →  see some·var)
 *
 * Bracket and operator placeholders are padded with spaces so adjacent raw text
 * or other folds are never visually glued:
 *  - leading  space when the preceding source character is not whitespace
 *  - trailing space when the following source character is neither whitespace
 *    nor another bracket (brackets add their own leading space, avoiding doubles)
 *
 * Examples
 *   $obj->makeSomething()
 *     →  [see obj][ whose ][make·something][ do][ go]
 *     =  see obj whose make·something do go
 *
 *   )))
 *     →  [ go][ go][ go]
 *     =  go go go
 */
class ReaderModeFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(
        root: PsiElement,
        document: Document,
        quick: Boolean,
    ): Array<FoldingDescriptor> {
        if (quick) return emptyArray()

        val source      = document.charsSequence
        val descriptors = mutableListOf<FoldingDescriptor>()

        root.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element !is LeafPsiElement) return
                val text = element.text

                when {
                    // ── Single-character bracket ───────────────────────────────
                    text.length == 1 && BracketRenderer.isBracket(text[0]) -> {
                        val word  = BracketRenderer.wordFor(text[0])!!
                        val start = element.textRange.startOffset
                        val end   = element.textRange.endOffset
                        val leading  = if (start > 0 && !source[start - 1].isWhitespace()) " " else ""
                        val trailing = if (end < source.length
                            && !source[end].isWhitespace()
                            && !BracketRenderer.isBracket(source[end])
                        ) " " else ""
                        descriptors.add(
                            FoldingDescriptor(element.node, element.textRange, null, leading + word + trailing)
                        )
                    }

                    // ── Multi-character structural operator (e.g. ->) ─────────
                    BracketRenderer.isOperator(text) -> {
                        val word  = BracketRenderer.wordForOperator(text)!!
                        val start = element.textRange.startOffset
                        val end   = element.textRange.endOffset
                        val leading  = if (start > 0 && !source[start - 1].isWhitespace()) " " else ""
                        val trailing = if (end < source.length
                            && !source[end].isWhitespace()
                            && !BracketRenderer.isBracket(source[end])
                        ) " " else ""
                        descriptors.add(
                            FoldingDescriptor(element.node, element.textRange, null, leading + word + trailing)
                        )
                    }

                    // ── $ variable sigil (e.g. $manager, $someVariable) ───────
                    text.length > 1 && text[0] == '$' -> {
                        val name     = text.substring(1)
                        val nameForm = MiddotConverter.convert(name) ?: name
                        descriptors.add(
                            FoldingDescriptor(
                                element.node, element.textRange, null,
                                "${BracketRenderer.SIGIL_WORD} $nameForm"
                            )
                        )
                    }

                    // ── Compound identifier ────────────────────────────────────
                    text.length >= 2 -> {
                        val middot = MiddotConverter.convert(text) ?: return
                        descriptors.add(
                            FoldingDescriptor(element.node, element.textRange, null, middot)
                        )
                    }
                }
            }
        })

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        val text = node.text
        if (text.length == 1) BracketRenderer.wordFor(text[0])?.let { return it }
        BracketRenderer.wordForOperator(text)?.let { return it }
        if (text.length > 1 && text[0] == '$') {
            val name = text.substring(1)
            return "${BracketRenderer.SIGIL_WORD} ${MiddotConverter.convert(name) ?: name}"
        }
        return MiddotConverter.convert(text) ?: text
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean =
        ReaderModeService.getInstance().isEnabled
}
