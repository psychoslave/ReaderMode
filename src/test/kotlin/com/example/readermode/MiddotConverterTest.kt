package com.example.readermode

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class MiddotConverterTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(
        // camelCase
        "quickBrownFox,         quick·brown·fox",
        "getHTMLParser,         get·html·parser",
        "parseHTML,             parse·html",
        // PascalCase
        "QuickBrownFox,         quick·brown·fox",
        "HTMLParser,            html·parser",
        // snake_case
        "quick_brown_fox,       quick·brown·fox",
        // SCREAMING_SNAKE
        "QUICK_BROWN_FOX,       quick·brown·fox",
        // kebab-case
        "quick-brown-fox,       quick·brown·fox",
        // mixed digits – split only at letter boundaries
        "getV8Engine,           get·v8·engine",
    )
    fun `converts compound identifiers to middot form`(input: String, expected: String) {
        assertEquals(expected.trim(), MiddotConverter.convert(input.trim()))
    }

    @ParameterizedTest(name = "null for single-word or invalid: {0}")
    @CsvSource(
        "foo",          // single word
        "x",            // too short
        "__init__",     // Python dunder – reduces to single word
        "_",            // underscore only
        "123abc",       // starts with digit
        "'hello world'",// contains space / non-identifier char (quoted for CSV)
    )
    fun `returns null for non-compound or invalid input`(input: String) {
        assertNull(MiddotConverter.convert(input.trim('\'')))
    }
}

