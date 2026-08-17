package com.fileflow.app

import com.fileflow.app.core.engine.pdf.PdfSplitEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class PdfSplitRangeTest {

    @Test
    fun testParseSinglePage() {
        val result = PdfSplitEngine.parsePageRange("3", 10)
        assertEquals(listOf(3), result)
    }

    @Test
    fun testParseRange() {
        val result = PdfSplitEngine.parsePageRange("1-3, 5, 8", 10)
        assertEquals(listOf(1, 2, 3, 5, 8), result)
    }

    @Test
    fun testParseAll() {
        val result = PdfSplitEngine.parsePageRange("all", 5)
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }
}
