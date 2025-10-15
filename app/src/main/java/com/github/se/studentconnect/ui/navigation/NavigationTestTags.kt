package com.github.se.studentconnect.ui.navigation

object NavigationTestTags {
    const val BOTTOM_NAVIGATION_MENU = "bottom_navigation_menu"
    const val HOME_TAB = "home_tab"
    const val MAP_TAB = "map_tab"
    const val ACTIVITIES_TAB = "activities_tab"
    const val PROFILE_TAB = "profile_tab"

    fun getTabTestTag(tab: Tab): String =
        when (tab) {
            is Tab.Home -> HOME_TAB
            is Tab.Map -> MAP_TAB
            is Tab.Activities -> ACTIVITIES_TAB
            is Tab.Profile -> PROFILE_TAB
        }
}