package com.example.readermode

/**
 * Maps bracket/brace characters to their reader-mode words and provides
 * helpers used by the folding builder and the toggle action.
 *
 *  {  →  through
 *  }  →  hop
 *  (  →  do
 *  )  →  go
 *
 * Adjacent brackets with no whitespace between them (e.g. `)))`) are folded
 * into a single region whose placeholder is space-joined words ("go go go").
 */
object BracketRenderer {

    private val BRACKET_WORDS = mapOf(
        '{' to "through",
        '}' to "hop",
        '(' to "do",
        ')' to "go",
    )

    private val WORDS_SET: Set<String> = BRACKET_WORDS.values.toSet()

    fun isBracket(c: Char): Boolean = c in BRACKET_WORDS

    fun wordFor(c: Char): String? = BRACKET_WORDS[c]

    fun toWords(text: String): String =
        text.map { wordFor(it) ?: it.toString() }.joinToString(" ")

    /**
     * Returns true when [placeholder] is a reader-mode bracket placeholder,
     * i.e. every space-separated token is one of the four bracket words.
     * Used by [ToggleReaderModeAction] to identify which folds belong to us.
     */
    fun isReaderModePlaceholder(placeholder: String): Boolean =
        placeholder.isNotBlank()
            && placeholder.trim().split(' ').filter { it.isNotEmpty() }.all { it in WORDS_SET }
}

