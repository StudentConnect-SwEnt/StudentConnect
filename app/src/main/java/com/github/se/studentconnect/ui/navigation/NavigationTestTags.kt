package com.github.se.studentconnect.ui.navigation

object NavigationTestTags {
  const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
  const val GO_BACK_BUTTON = "GoBackButton"
  const val TOP_BAR_TITLE = "TopBarTitle"
  const val HOME_TAB = "HomeTab"
  const val MAP_TAB = "MapTab"
  const val CREATE_EVENT_TAB = "CreateEventTab"
  const val EVENTS_TAB = "EventsTab"
  const val PROFILE_TAB = "ProfileTab"

  fun getTabTestTag(tab: Tab): String =
      when (tab) {
        is Tab.Home -> HOME_TAB
        is Tab.Map -> MAP_TAB
        is Tab.CreateEvent -> CREATE_EVENT_TAB
        is Tab.Events -> EVENTS_TAB
        is Tab.Profile -> PROFILE_TAB
      }
}
