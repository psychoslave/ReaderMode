package com.example.readermode

/**
 * Maps structural tokens to their reader-mode word replacements and provides
 * helpers used by the folding builder and the toggle action.
 *
 * Brackets / braces (single-character):
 *  {  →  tap
 *  }  →  hop
 *  (  →  do
 *  )  →  go
 *
 * Member-access operators (multi-character):
 *  ->  →  whose
 *
 * Comments are folded to [COMMENT_PLACEHOLDER] ("…").
 */
object BracketRenderer {

    private val BRACKET_WORDS = mapOf(
        '{' to "tap",
        '}' to "hop",
        '(' to "do",
        ')' to "go",
    )

    /** Multi-character structural operators and their reader-mode words. */
    private val OPERATOR_WORDS = mapOf(
        "->" to "whose",
    )

    /** Placeholder used for any comment fold. */
    const val COMMENT_PLACEHOLDER = "…"

    private val WORDS_SET: Set<String> = (BRACKET_WORDS.values + OPERATOR_WORDS.values).toSet()

    fun isBracket(c: Char): Boolean = c in BRACKET_WORDS
    fun wordFor(c: Char): String? = BRACKET_WORDS[c]

    fun toWords(text: String): String =
        text.map { wordFor(it) ?: it.toString() }.joinToString(" ")

    fun isOperator(text: String): Boolean = text in OPERATOR_WORDS
    fun wordForOperator(text: String): String? = OPERATOR_WORDS[text]

    /**
     * Returns true when [placeholder] is a reader-mode structural placeholder,
     * i.e. every space-separated token is one of the bracket/operator words.
     * Used by [ToggleReaderModeAction] to identify which folds belong to us.
     */
    fun isReaderModePlaceholder(placeholder: String): Boolean =
        placeholder.isNotBlank()
            && placeholder.trim().split(' ').filter { it.isNotEmpty() }.all { it in WORDS_SET }
}
