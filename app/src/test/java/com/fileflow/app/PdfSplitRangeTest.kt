package com.fileflow.app

import com.fileflow.app.core.engine.pdf.PdfSplitEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class PdfSplitRangeTest {

    @Test
    fun testParseSinglePage() {
        val engine = PdfSplitEngine(context = null as Any? as android.content.Context, storageManager = null as Any? as com.fileflow.app.core.saf.StorageManager)
        val result = engine.parsePageRange("3", 10)
        assertEquals(listOf(3), result)
    }

    @Test
    fun testParseRange() {
        val engine = PdfSplitEngine(context = null as Any? as android.content.Context, storageManager = null as Any? as com.fileflow.app.core.saf.StorageManager)
        val result = engine.parsePageRange("1-3, 5, 8", 10)
        assertEquals(listOf(1, 2, 3, 5, 8), result)
    }

    @Test
    fun testParseAll() {
        val engine = PdfSplitEngine(context = null as Any? as android.content.Context, storageManager = null as Any? as com.fileflow.app.core.saf.StorageManager)
        val result = engine.parsePageRange("all", 5)
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }
}
