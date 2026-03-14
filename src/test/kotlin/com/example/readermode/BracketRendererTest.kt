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

    @ParameterizedTest(name = "isReaderModePlaceholder({0}) = {1}")
    @CsvSource(
        "go,           true",
        "go go go,     true",
        "do hop,       true",
        "tap,          true",
        "quick·brown,  false",   // middot placeholder, not a bracket one
        "hello,        false",   // arbitrary word
        "'',           false",   // empty
    )
    fun `identifies reader-mode bracket placeholders`(input: String, expected: Boolean) {
        assertEquals(expected, BracketRenderer.isReaderModePlaceholder(input.trim('\'')))
    }
}

