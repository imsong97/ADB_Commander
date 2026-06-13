package com.ch0pp4.adbcommander.preference

import com.ch0pp4.adbcommander.presentation.model.MainTab
import java.util.prefs.Preferences

class AppPreferences {
    private val prefs = Preferences.userNodeForPackage(AppPreferences::class.java)

    private val TAB_BROADCAST_VISIBLE = "tab_broadcast_visible"
    private val TAB_COMMAND_VISIBLE = "tab_command_visible"
    private val TAB_BROADCAST_EXPANDED = "tab_broadcast_expanded"
    private val TAB_COMMAND_EXPANDED = "tab_command_expanded"
    private val TAB_WIDTH = "left_panel_width"

    fun isTabVisible(tab: MainTab): Boolean = when (tab) {
        MainTab.SEND_BROADCAST -> prefs.getBoolean(TAB_BROADCAST_VISIBLE, true)
        MainTab.COMMAND_LIST -> prefs.getBoolean(TAB_COMMAND_VISIBLE, true)
    }

    fun setTabVisible(tab: MainTab, visible: Boolean) {
        val key = when (tab) {
            MainTab.SEND_BROADCAST -> TAB_BROADCAST_VISIBLE
            MainTab.COMMAND_LIST -> TAB_COMMAND_VISIBLE
        }
        prefs.putBoolean(key, visible)
    }

    fun getVisibleTabs(): Set<MainTab> =
        MainTab.entries.filter { isTabVisible(it) }.toSet()

    fun getExpandedTabs(): Set<MainTab> = MainTab.entries.filter { tab ->
        when (tab) {
            MainTab.SEND_BROADCAST -> prefs.getBoolean(TAB_BROADCAST_EXPANDED, true)
            MainTab.COMMAND_LIST -> prefs.getBoolean(TAB_COMMAND_EXPANDED, false)
        }
    }.toSet()

    fun setExpandedTabs(expandedTabs: Set<MainTab>) {
        prefs.putBoolean(TAB_BROADCAST_EXPANDED, MainTab.SEND_BROADCAST in expandedTabs)
        prefs.putBoolean(TAB_COMMAND_EXPANDED, MainTab.COMMAND_LIST in expandedTabs)
    }

    fun getLeftPanelWidth(): Float = prefs.getFloat(TAB_WIDTH, 220f)

    fun setLeftPanelWidth(width: Float) = prefs.putFloat(TAB_WIDTH, width)
}