package com.example.readermode

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class BracketRendererTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(
        "{, tap",
        "}, hop",
        "(, do",
        "), go",
        "[, at",
        "], ate",
        ";, ay",
    )
    fun `maps single brackets to words`(input: String, expected: String) {
        assertEquals(expected, BracketRenderer.wordFor(input[0]))
    }

    @ParameterizedTest(name = "toWords({0}) = {1}")
    @CsvSource(
        "),           go",
        ")),          go go",
        "))),         go go go",
        "(},          do hop",
        "{(,          tap do",
        "))(,         go go do",
    )
    fun `converts bracket sequences to space-joined words`(input: String, expected: String) {
        assertEquals(expected.trim(), BracketRenderer.toWords(input.trim()))
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(
        "->, whose",
    )
    fun `maps operator tokens to words`(input: String, expected: String) {
        assertEquals(expected, BracketRenderer.wordForOperator(input))
    }

    @ParameterizedTest(name = "sigil rendering: {0} → {1}")
    @CsvSource(
        "\$manager,        see manager",
        "\$someVariable,   see some·variable",
        "\$HTMLParser,     see html·parser",
        "\$this,           see this",
    )
    fun `renders dollar-sigil variables`(rawToken: String, expected: String) {
        val name     = rawToken.removePrefix("$")
        val nameForm = MiddotConverter.convert(name) ?: name
        assertEquals(expected, "${BracketRenderer.SIGIL_WORD} $nameForm")
    }

    @ParameterizedTest(name = "isReaderModePlaceholder({0}) = {1}")
    @CsvSource(
        "go,                true",
        "go go go,          true",
        "do hop,            true",
        "tap,               true",
        "at,                true",
        "ate,               true",
        "ay,                true",
        "whose,             true",
        "see manager,       true",
        "see some·variable, true",
        "see,               true",
        "quick·brown,       false",
        "hello,             false",
        "'',                false",
    )
    fun `identifies reader-mode structural placeholders`(input: String, expected: Boolean) {
        assertEquals(expected, BracketRenderer.isReaderModePlaceholder(input.trim('\'')))
    }
}
