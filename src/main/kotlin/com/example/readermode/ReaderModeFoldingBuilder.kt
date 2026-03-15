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
 * Identifiers           → middot placeholder     (quickBrownFox  →  quick·brown·fox)
 * Brackets / braces     → word placeholder       ((  →  do,  {  →  tap)
 * Prefix-negation (!)   → prefix placeholder     (!  →  non-)
 * Member-access (->)    → word placeholder       (->  →  whose)
 * Scope-resolution (::) → word placeholder       (::  →  whence)
 * $ variable sigil      → "lo-" prefix           ($someVar  →  lo-some·var, $$v  →  lo-lo-v)
 *
 * Bracket and operator placeholders are padded with spaces so adjacent raw text
 * or other folds are never visually glued:
 *  - leading  space when the preceding source character is not whitespace
 *    AND is not a connecting-prefix token (e.g. !) — so !! chains as non-non-
 *  - trailing space when the following source character is neither whitespace
 *    nor another structural token, AND the word itself does not end with '-'
 *
 * Examples
 *   $obj->makeSomething()
 *     →  [lo-obj][ whose ][make·something][ do][ go]
 *     =  lo-obj whose make·something do go
 *
 *   !!$ready
 *     →  [non-][non-][lo-ready]
 *     =  non-non-lo-ready
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
                    // ── Single-character bracket / prefix operator ─────────────
                    text.length == 1 && BracketRenderer.isBracket(text[0]) -> {
                        val word  = BracketRenderer.wordFor(text[0])!!
                        val start = element.textRange.startOffset
                        val end   = element.textRange.endOffset
                        val leading  = if (start > 0
                            && !source[start - 1].isWhitespace()
                            && !BracketRenderer.isConnectingPrefix(source[start - 1])
                        ) " " else ""
                        val trailing = if (end < source.length
                            && !source[end].isWhitespace()
                            && !BracketRenderer.isBracket(source[end])
                            && !word.endsWith("-")
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
                        val leading  = if (start > 0
                            && !source[start - 1].isWhitespace()
                            && !BracketRenderer.isConnectingPrefix(source[start - 1])
                        ) " " else ""
                        val trailing = if (end < source.length
                            && !source[end].isWhitespace()
                            && !BracketRenderer.isBracket(source[end])
                            && !word.endsWith("-")
                        ) " " else ""
                        descriptors.add(
                            FoldingDescriptor(element.node, element.textRange, null, leading + word + trailing)
                        )
                    }

                    // ── $ variable sigil (e.g. $manager, $$someVar) ───────────
                    text.length > 1 && text[0] == '$' -> {
                        val sigils   = text.takeWhile { it == '$' }.length
                        val name     = text.substring(sigils)
                        val nameForm = MiddotConverter.convert(name) ?: name
                        val prefix   = BracketRenderer.SIGIL_PREFIX.repeat(sigils)
                        descriptors.add(
                            FoldingDescriptor(element.node, element.textRange, null, prefix + nameForm)
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
            val sigils = text.takeWhile { it == '$' }.length
            val name   = text.substring(sigils)
            return BracketRenderer.SIGIL_PREFIX.repeat(sigils) + (MiddotConverter.convert(name) ?: name)
        }
        return MiddotConverter.convert(text) ?: text
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean =
        ReaderModeService.getInstance().isEnabled
}
