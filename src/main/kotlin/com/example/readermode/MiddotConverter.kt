package com.example.readermode

/**
 * Converts compound identifiers to middot-separated lowercase words.
 *
 * Supported naming conventions:
 *  - camelCase       → camel·case
 *  - PascalCase      → pascal·case
 *  - snake_case      → snake·case
 *  - SCREAMING_SNAKE → screaming·snake
 *  - kebab-case      → kebab·case
 *  - HTMLParser      → html·parser
 *  - parseHTML       → parse·html
 */
object MiddotConverter {

    const val MIDDOT = "·"

    /**
     * Regex that finds word boundaries in camelCase / PascalCase identifiers:
     *  1. Between a lowercase/digit and an uppercase:   "quickBrown" → ["quick","Brown"]
     *  2. Inside an all-caps run before cap+lower pair: "HTMLParser" → ["HTML","Parser"]
     */
    private val CAMEL_SPLIT = Regex("(?<=[a-z\\d])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])")

    /**
     * Returns the middot form of [identifier], or `null` if the input is not
     * a recognisable compound identifier (single word, non-identifier chars, etc.).
     */
    fun convert(identifier: String): String? {
        if (identifier.length < 2) return null
        // Only process tokens made of letters, digits, underscores, or hyphens.
        // This filters out string literals (contain quotes), operators, numbers, etc.
        if (!identifier.all { it.isLetterOrDigit() || it == '_' || it == '-' }) return null
        // Must start with a letter (rules out `_foo`, numeric literals, etc.)
        if (!identifier.first().isLetter()) return null

        val words: List<String> = when {
            '_' in identifier -> identifier.split('_').filter { it.isNotEmpty() }
            '-' in identifier -> identifier.split('-').filter { it.isNotEmpty() }
            else              -> CAMEL_SPLIT.split(identifier).filter { it.isNotEmpty() }
        }

        if (words.size <= 1) return null

        return words.joinToString(MIDDOT) { it.lowercase() }
    }
}

