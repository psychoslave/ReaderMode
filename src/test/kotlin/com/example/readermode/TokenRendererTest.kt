package com.example.readermode

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TokenRendererTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(
        "{, tap",
        "}, hop",
        "(, do",
        "), go",
        "[, at",
        "], ate",
        "',', eft",
        ";, ay",
        "!, non-",
    )
    fun `maps single brackets to words`(input: String, expected: String) {
        assertEquals(expected, TokenRenderer.wordFor(input[0]))
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
        assertEquals(expected.trim(), TokenRenderer.toWords(input.trim()))
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(
        "->, whose",
        "::, whence",
        "fn, from",
        "=>, to",
        "=, here",
        "==, par",
        "===, fit",
    )
    fun `maps operator tokens to words`(input: String, expected: String) {
        assertEquals(expected, TokenRenderer.wordForOperator(input))
    }

    @ParameterizedTest(name = "sigil rendering: {0} → {1}")
    @CsvSource(
        "\$manager,         lo-manager",
        "\$someVariable,    lo-some·variable",
        "\$HTMLParser,      lo-html·parser",
        "\$this,            lo-this",
        "\$\$variable,      lo-lo-variable",
    )
    fun `renders dollar-sigil variables`(rawToken: String, expected: String) {
        val sigils   = rawToken.takeWhile { it == '$' }.length
        val name     = rawToken.substring(sigils)
        val nameForm = MiddotConverter.convert(name) ?: name
        val prefix   = TokenRenderer.SIGIL_PREFIX.repeat(sigils)
        assertEquals(expected.trim(), prefix + nameForm)
    }

    @ParameterizedTest(name = "isReaderModePlaceholder({0}) = {1}")
    @CsvSource(
        "go,                true",
        "go go go,          true",
        "do hop,            true",
        "tap,               true",
        "at,                true",
        "ate,               true",
        "eft,               true",
        "ay,                true",
        "whose,             true",
        "whence,            true",
        "from,              true",
        "to,                true",
        "here,              true",
        "par,               true",
        "fit,               true",
        "non-,              true",
        "lo-manager,        true",
        "lo-some·variable,  true",
        "lo-lo-variable,        true",
        // ternary words
        "thereupon,             true",
        "otherwise,             true",
        "'should lo-manager',   true",
        "'should do',           true",
        "'should some·thing',   true",
        // non-reader-mode
        "quick·brown,           false",
        "hello,                 false",
        "'',                    false",
    )
    fun `identifies reader-mode structural placeholders`(input: String, expected: Boolean) {
        assertEquals(expected, TokenRenderer.isReaderModePlaceholder(input.trim('\'')))
    }

    @Test
    fun `isConnectingPrefix is true only for prefix-connector tokens`() {
        assertTrue(TokenRenderer.isConnectingPrefix('!'))
        assertFalse(TokenRenderer.isConnectingPrefix('('))
        assertFalse(TokenRenderer.isConnectingPrefix(')'))
        assertFalse(TokenRenderer.isConnectingPrefix(';'))
    }

    @Test
    fun `ternary triplet constants have expected values`() {
        assertEquals("should",    TokenRenderer.TERNARY_W0)
        assertEquals("thereupon", TokenRenderer.TERNARY_W1)
        assertEquals("otherwise", TokenRenderer.TERNARY_W2)
    }
}
