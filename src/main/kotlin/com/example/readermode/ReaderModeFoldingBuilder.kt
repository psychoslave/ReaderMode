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
 * Ternary (? :)         → three-part rendering   (see below)
 *
 * Ternary rendering — W0 / W1 / W2 triplet:
 *   condition ? trueExpr : falseExpr
 *     →  should condition thereupon trueExpr otherwise falseExpr
 *
 *   Because the condition precedes "?" in document order, a single left-to-right
 *   pass cannot inject W0 ("should") before the condition's first token.
 *   The builder therefore runs in two phases:
 *
 *   Phase 1 — pre-scan:
 *     Visit all "?" leaves.  For each one that belongs to a ternary expression
 *     (detected by parent PSI class name), walk down to the first significant
 *     leaf of that ternary subtree (= first leaf of the condition) and record
 *     its text offset in `ternaryConditionStarts`.
 *
 *   Phase 2 — fold building (the existing single-pass visitor):
 *     For each leaf:
 *      - offset ∈ ternaryConditionStarts  → prepend W0 ("should ") to its placeholder
 *      - text == "?" in ternary context   → fold as W1 ("thereupon")
 *      - text == ":" in ternary context   → fold as W2 ("otherwise")
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
 *
 *   $x = $n > 0 ? $a : $b
 *     →  [lo-x][ here ][should lo-n][ par ][ 0 ][ thereupon ][ lo-a ][ otherwise ][ lo-b]
 *     =  lo-x here should lo-n par 0 thereupon lo-a otherwise lo-b
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

        // ── Phase 1: locate the first significant leaf of every ternary condition ──
        // The condition starts before "?" in document order, so we must identify
        // its first-leaf offset in a separate pass before building folds.
        val ternaryConditionStarts = mutableSetOf<Int>()
        root.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element !is LeafPsiElement || element.text != "?") return
                if (!isTernaryContext(element)) return
                firstSignificantLeaf(element.parent)
                    ?.let { ternaryConditionStarts += it.textRange.startOffset }
            }
        })

        // ── Phase 2: build fold descriptors ───────────────────────────────────────
        root.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element !is LeafPsiElement) return
                val text  = element.text
                val start = element.textRange.startOffset
                val end   = element.textRange.endOffset
                val isCondStart = start in ternaryConditionStarts

                // Shared leading-space logic for bracket and operator folds.
                fun leading() = if (start > 0
                    && !source[start - 1].isWhitespace()
                    && !TokenRenderer.isConnectingPrefix(source[start - 1])
                ) " " else ""

                // W0 prefix ("should ") with appropriate leading space.
                // When this leaf starts a ternary condition, W0 absorbs the
                // leading space so the bracket/operator branch must not add it again.
                fun w0() = if (isCondStart) leading() + TokenRenderer.TERNARY_W0 + " " else ""

                when {
                    // ── Ternary "?" → W1 "thereupon" ──────────────────────────────
                    text == "?" && isTernaryContext(element) -> {
                        val lead = if (start > 0 && !source[start - 1].isWhitespace()) " " else ""
                        val tail = if (end < source.length && !source[end].isWhitespace()) " " else ""
                        descriptors += FoldingDescriptor(
                            element.node, element.textRange, null,
                            lead + TokenRenderer.TERNARY_W1 + tail,
                        )
                    }

                    // ── Ternary ":" → W2 "otherwise" ──────────────────────────────
                    text == ":" && isTernaryContext(element) -> {
                        val lead = if (start > 0 && !source[start - 1].isWhitespace()) " " else ""
                        val tail = if (end < source.length && !source[end].isWhitespace()) " " else ""
                        descriptors += FoldingDescriptor(
                            element.node, element.textRange, null,
                            lead + TokenRenderer.TERNARY_W2 + tail,
                        )
                    }

                    // ── Colon usages (context-sensitive) ─────────────────────────────
                    text == ":" && !isTernaryContext(element) -> {
                        when {
                            isReturnTypeColon(element) -> {
                                val lead = if (start > 0 && !source[start - 1].isWhitespace()) " " else ""
                                val tail = if (end < source.length && !source[end].isWhitespace()) " " else ""
                                descriptors += FoldingDescriptor(
                                    element.node, element.textRange, null,
                                    lead + TokenRenderer.COLON_RETURN_TYPE + tail,
                                )
                            }
                            isNamedArgumentColon(element) -> {
                                val lead = if (start > 0 && !source[start - 1].isWhitespace()) " " else ""
                                val tail = if (end < source.length && !source[end].isWhitespace()) " " else ""
                                descriptors += FoldingDescriptor(
                                    element.node, element.textRange, null,
                                    lead + TokenRenderer.COLON_NAMED_ARG + tail,
                                )
                            }
                            isBlockStartColon(element) -> {
                                val prevChar = if (start > 0) source[start - 1] else null
                                val lead = if (start > 0 && !source[start - 1].isWhitespace() && prevChar != ')') " " else ""
                                val tail = if (end < source.length && !source[end].isWhitespace()) " " else ""
                                descriptors += FoldingDescriptor(
                                    element.node, element.textRange, null,
                                    lead + TokenRenderer.COLON_BLOCK_START + tail,
                                )
                            }
                            isLabelColon(element) -> {
                                // Suffix, no leading/trailing space
                                descriptors += FoldingDescriptor(
                                    element.node, element.textRange, null,
                                    TokenRenderer.COLON_LABEL_SUFFIX,
                                )
                            }
                        }
                    }

                    // ── Single-character bracket / prefix operator ─────────────────
                    text.length == 1 && TokenRenderer.isBracket(text[0]) -> {
                        val word = TokenRenderer.wordFor(text[0])!!
                        // W0 already carries the leading space; don't double-add.
                        val lead = if (isCondStart) "" else leading()
                        val tail = if (end < source.length
                            && !source[end].isWhitespace()
                            && !TokenRenderer.isBracket(source[end])
                            && !word.endsWith("-")
                        ) " " else ""
                        descriptors += FoldingDescriptor(
                            element.node, element.textRange, null,
                            w0() + lead + word + tail,
                        )
                    }

                    // ── Multi-character structural operator (e.g. ->) ─────────────
                    TokenRenderer.isOperator(text) -> {
                        val word = TokenRenderer.wordForOperator(text)!!
                        val lead = if (isCondStart) "" else leading()
                        val tail = if (end < source.length
                            && !source[end].isWhitespace()
                            && !TokenRenderer.isBracket(source[end])
                            && !word.endsWith("-")
                        ) " " else ""
                        descriptors += FoldingDescriptor(
                            element.node, element.textRange, null,
                            w0() + lead + word + tail,
                        )
                    }

                    // ── $ variable sigil (e.g. $manager, $$someVar) ───────────────
                    text.length > 1 && text[0] == '$' -> {
                        val sigils   = text.takeWhile { it == '$' }.length
                        val name     = text.substring(sigils)
                        val nameForm = MiddotConverter.convert(name) ?: name
                        val prefix   = TokenRenderer.SIGIL_PREFIX.repeat(sigils)
                        descriptors += FoldingDescriptor(
                            element.node, element.textRange, null,
                            w0() + prefix + nameForm,
                        )
                    }

                    // ── Compound identifier ────────────────────────────────────────
                    text.length >= 2 -> {
                        val middot = MiddotConverter.convert(text)
                        when {
                            middot != null ->
                                descriptors += FoldingDescriptor(
                                    element.node, element.textRange, null,
                                    w0() + middot,
                                )
                            isCondStart ->
                                // No middot conversion but W0 still needs a fold anchor.
                                descriptors += FoldingDescriptor(
                                    element.node, element.textRange, null,
                                    w0() + text,
                                )
                        }
                    }

                    // ── Bare single-char condition start (literal digit, quote, …) ─
                    isCondStart ->
                        descriptors += FoldingDescriptor(
                            element.node, element.textRange, null,
                            w0() + text,
                        )
                }
            }
        })

        return descriptors.toTypedArray()
    }

    /**
     * Returns true when [element] is a `?` or `:` that belongs to a ternary
     * (conditional) expression.  Detection uses the parent PSI class name to
     * avoid hard compile-time dependencies on language-plugin classes.
     * Covers PHP (`PhpTernaryExpression`) and Java (`PsiConditionalExpression`).
     */
    private fun isTernaryContext(element: LeafPsiElement): Boolean {
        val name = element.parent?.javaClass?.simpleName ?: return false
        return "Ternary" in name || "Conditional" in name
    }

    /**
     * Returns the first non-blank [LeafPsiElement] in the subtree rooted at
     * [element], using a depth-first left-to-right traversal.
     * Used to locate the offset at which W0 ("should") must be injected.
     */
    private fun firstSignificantLeaf(element: PsiElement): LeafPsiElement? {
        if (element is LeafPsiElement && element.text.isNotBlank()) return element
        var child = element.firstChild
        while (child != null) {
            val result = firstSignificantLeaf(child)
            if (result != null) return result
            child = child.nextSibling
        }
        return null
    }

    override fun getPlaceholderText(node: ASTNode): String {
        val text = node.text
        if (text.length == 1) TokenRenderer.wordFor(text[0])?.let { return it }
        TokenRenderer.wordForOperator(text)?.let { return it }
        if (text.length > 1 && text[0] == '$') {
            val sigils = text.takeWhile { it == '$' }.length
            val name   = text.substring(sigils)
            return TokenRenderer.SIGIL_PREFIX.repeat(sigils) + (MiddotConverter.convert(name) ?: name)
        }
        return MiddotConverter.convert(text) ?: text
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean =
        ReaderModeService.getInstance().isEnabled

    // Helper functions for colon context detection
    private fun isReturnTypeColon(element: LeafPsiElement): Boolean {
        val parent = element.parent ?: return false
        val name = parent.javaClass.simpleName
        return name.contains("ReturnType") || name.contains("Function")
    }
    private fun isNamedArgumentColon(element: LeafPsiElement): Boolean {
        val parent = element.parent ?: return false
        val name = parent.javaClass.simpleName
        return name.contains("NamedArgument")
    }
    private fun isBlockStartColon(element: LeafPsiElement): Boolean {
        val parent = element.parent ?: return false
        val name = parent.javaClass.simpleName
        return name == "IfImpl" || name == "WhileImpl" ||
               name == "ForImpl" || name == "ForeachImpl" ||
               name == "PhpCaseImpl"
    }
    private fun isLabelColon(element: LeafPsiElement): Boolean {
        val parent = element.parent ?: return false
        val name = parent.javaClass.simpleName
        return name.contains("Label")
    }
}
