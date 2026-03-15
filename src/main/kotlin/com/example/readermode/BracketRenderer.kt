package com.example.readermode

/**
 * Maps structural tokens to their reader-mode word replacements and provides
 * helpers used by the folding builder and the toggle action.
 *
 * Brackets / braces (single-character):
 *  {  →  tap      }  →  hop
 *  (  →  do       )  →  go
 *
 * Subscript brackets (single-character):
 *  [  →  at      ]  →  ate
 *
 * Argument / list separator (single-character):
 *  ,  →  eft
 *
 * Expression terminator (single-character):
 *  ;  →  ay
 *
 * Prefix-negation operator (single-character, prefix connector):
 *  !  →  non-   (no trailing space; chains as non-non-expr for !!)
 *
 * Member-access operators (multi-character):
 *  ->  →  whose
 *
 * Scope-resolution operator (multi-character):
 *  ::  →  whence
 *
 * Variable sigil:
 *  $name     →  see name
 *  $someVar  →  see some·var
 */
object BracketRenderer {

    // All single-character structural tokens that get word replacements.
    // Semicolon is included alongside brackets so the spacing logic
    // (no trailing space before a token that will add its own leading space)
    // applies uniformly.
    private val BRACKET_WORDS = mapOf(
        '{' to "tap",
        '}' to "hop",
        '(' to "do",
        ')' to "go",
        '[' to "at",
        ']' to "ate",
        ',' to "eft",
        ';' to "ay",
        '!' to "non-",
    )

    /** Multi-character structural operators and their reader-mode words. */
    private val OPERATOR_WORDS = mapOf(
        "->" to "whose",
        "::" to "whence",
    )

    /**
     * Word used to render the `$` variable sigil.
     *
     * "see" is a complete English word (no apocope) with several converging
     * meanings that all fit the role of a variable sigil:
     *  - **Deictic interjection** — "See!" points at / indicates something,
     *    mirroring the sigil's role of marking a name as a reference to a value.
     *  - **Ecclesiastical noun** — a "see" is the seat where autonomous power
     *    is exercised; by metaphor, the locus at which a value resides and acts
     *    (cf. a memory location).
     *  - Starts with **s**, echoing the struck-S shape of `$` itself.
     */
    const val SIGIL_WORD = "see"

    private val WORDS_SET: Set<String> = (BRACKET_WORDS.values + OPERATOR_WORDS.values).toSet()

    fun isBracket(c: Char): Boolean = c in BRACKET_WORDS
    fun wordFor(c: Char): String? = BRACKET_WORDS[c]

    fun toWords(text: String): String =
        text.map { wordFor(it) ?: it.toString() }.joinToString(" ")

    fun isOperator(text: String): Boolean = text in OPERATOR_WORDS
    fun wordForOperator(text: String): String? = OPERATOR_WORDS[text]

    /**
     * Returns true when [c] maps to a prefix-connector word (ends with '-').
     * The following token must not add a leading space — the hyphen already
     * connects them — and [c] itself suppresses its own trailing space.
     * This makes `!!expr` chain as `non-non-expr` instead of `non- non- expr`.
     */
    fun isConnectingPrefix(c: Char): Boolean = wordFor(c)?.endsWith("-") == true

    /**
     * Returns true when [placeholder] is a reader-mode structural placeholder:
     *  - bracket/operator words ("do", "go", "whose", etc.)
     *  - sigil fold starting with [SIGIL_WORD]
     *
     * Used by [ToggleReaderModeAction] to identify which folds belong to us.
     */
    fun isReaderModePlaceholder(placeholder: String): Boolean {
        val trimmed = placeholder.trim()
        return when {
            trimmed == SIGIL_WORD || trimmed.startsWith("$SIGIL_WORD ") -> true
            else -> trimmed.isNotEmpty()
                && trimmed.split(' ').filter { it.isNotEmpty() }.all { it in WORDS_SET }
        }
    }
}
