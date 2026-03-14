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
 * Identifiers   → middot placeholder  (e.g. quickBrownFox → quick·brown·fox)
 * Brackets      → word placeholder    (e.g. (  →  do,  {  →  tap)
 *
 * Bracket placeholders are padded with spaces so adjacent raw text or other folds
 * are never visually glued:
 *  - leading  space when the preceding source character is not whitespace
 *  - trailing space when the following source character is neither whitespace
 *    nor another bracket (brackets add their own leading space, avoiding doubles)
 *
 * Examples
 *   __construct(CartManager $manager)
 *     →  __construct[ do ][cart·manager] $manager[ go]
 *     =  __construct do cart·manager $manager go
 *
 *   makeSomething()
 *     →  [make·something][ do][ go]
 *     =  make·something do go
 *
 *   )))
 *     →  [ go][ go][ go]   (each ) preceded by non-whitespace)
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
                    // ── Bracket token ─────────────────────────────────────────────
                    text.length == 1 && BracketRenderer.isBracket(text[0]) -> {
                        val word  = BracketRenderer.wordFor(text[0])!!
                        val start = element.textRange.startOffset
                        val end   = element.textRange.endOffset

                        // Leading space: preceding char exists and is not whitespace.
                        val leading = if (start > 0 && !source[start - 1].isWhitespace()) " " else ""

                        // Trailing space: following char exists, is not whitespace, and is
                        // not a bracket (brackets add their own leading space — no doubling).
                        val trailing = if (end < source.length
                            && !source[end].isWhitespace()
                            && !BracketRenderer.isBracket(source[end])
                        ) " " else ""

                        descriptors.add(
                            FoldingDescriptor(element.node, element.textRange, null, leading + word + trailing)
                        )
                    }

                    // ── Compound identifier ───────────────────────────────────────
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
        return MiddotConverter.convert(text) ?: text
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean =
        ReaderModeService.getInstance().isEnabled
}
