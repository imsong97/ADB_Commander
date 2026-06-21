package com.ch0pp4.adbcommander.preference

import java.util.prefs.Preferences

class AppPreferences {
    private val prefs = Preferences.userNodeForPackage(AppPreferences::class.java)

    fun getLeftPanelWidth(): Float = prefs.getFloat("left_panel_width", 220f)

    fun setLeftPanelWidth(width: Float) = prefs.putFloat("left_panel_width", width)

    fun getExpandedCollectionIds(): Set<Int> {
        val raw = prefs.get("collection_expanded_ids", "")
        return raw.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setExpandedCollectionIds(ids: Set<Int>) {
        prefs.put("collection_expanded_ids", ids.joinToString(","))
    }

    fun removeExpandedCollectionId(id: Int) {
        setExpandedCollectionIds(getExpandedCollectionIds() - id)
    }

    fun getHiddenCollectionIds(): Set<Int> {
        val raw = prefs.get("collection_hidden_ids", "")
        return raw.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setHiddenCollectionIds(ids: Set<Int>) {
        prefs.put("collection_hidden_ids", ids.joinToString(","))
    }

    fun removeHiddenCollectionId(id: Int) {
        setHiddenCollectionIds(getHiddenCollectionIds() - id)
    }
}
