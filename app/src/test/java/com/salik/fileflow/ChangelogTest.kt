package com.salik.fileflow

import com.salik.fileflow.ui.screens.settings.AppChangelog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogTest {

    @Test
    fun testChangelogHasVersions() {
        assertFalse("Changelog should not be empty", AppChangelog.isEmpty())
        val latest = AppChangelog.first()
        assertNotNull(latest.version)
        assertNotNull(latest.releaseDate)
        assertTrue("Latest version must have added items", latest.added.isNotEmpty())
        assertTrue("Latest version must have security notes", latest.security.isNotEmpty())
    }
}
