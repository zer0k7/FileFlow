package com.fileflow.app

import com.fileflow.app.core.model.ChangelogVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogTest {

    @Test
    fun testChangelogVersionModel() {
        val entry = ChangelogVersion(
            version = "1.0.0",
            releaseDate = "2026-08-17",
            added = listOf("Initial release"),
            changed = emptyList(),
            fixed = emptyList(),
            security = listOf("All processing is offline")
        )
        assertEquals("1.0.0", entry.version)
        assertEquals("2026-08-17", entry.releaseDate)
        assertTrue(entry.added.isNotEmpty())
        assertTrue(entry.security.isNotEmpty())
        assertTrue(entry.changed.isEmpty())
        assertTrue(entry.fixed.isEmpty())
    }

    @Test
    fun testChangelogVersionSecurityDefaults() {
        val entry = ChangelogVersion(
            version = "0.9.0",
            releaseDate = "2026-01-01",
            added = listOf("Something"),
            changed = emptyList(),
            fixed = emptyList()
        )
        assertTrue(entry.security.isEmpty())
    }
}
