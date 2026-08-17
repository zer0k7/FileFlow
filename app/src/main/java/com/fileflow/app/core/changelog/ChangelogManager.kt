package com.fileflow.app.core.changelog

import android.content.Context
import com.fileflow.app.core.model.ChangelogVersion
import org.json.JSONArray
import java.io.InputStreamReader

object ChangelogManager {

    fun loadChangelog(context: Context): List<ChangelogVersion> {
        val list = mutableListOf<ChangelogVersion>()
        try {
            val jsonString = context.assets.open("changelog.json").use { stream ->
                InputStreamReader(stream, Charsets.UTF_8).readText()
            }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val addedList = parseStringList(obj.optJSONArray("added"))
                val changedList = parseStringList(obj.optJSONArray("changed"))
                val fixedList = parseStringList(obj.optJSONArray("fixed"))
                val securityList = parseStringList(obj.optJSONArray("security"))

                list.add(
                    ChangelogVersion(
                        version = obj.getString("version"),
                        releaseDate = obj.getString("releaseDate"),
                        added = addedList,
                        changed = changedList,
                        fixed = fixedList,
                        security = securityList
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    private fun parseStringList(jsonArray: JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()
        val result = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            result.add(jsonArray.getString(i))
        }
        return result
    }
}
