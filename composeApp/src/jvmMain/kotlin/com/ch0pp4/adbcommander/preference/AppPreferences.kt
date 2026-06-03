package com.ch0pp4.adbcommander.preference

import com.ch0pp4.adbcommander.presentation.model.MainTab
import java.util.prefs.Preferences

class AppPreferences {
    private val prefs = Preferences.userNodeForPackage(AppPreferences::class.java)

    private val TAB_BROADCAST_VISIBLE = "tab_broadcast_visible"
    private val TAB_COMMAND_VISIBLE = "tab_command_visible"

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
}