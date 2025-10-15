package com.github.se.studentconnect.resources

import com.github.se.studentconnect.ui.navigation.Tab

// Like R, but C
object C {
  object Tag {
    const val greeting = "main_screen_greeting"
    const val main_screen_container = "main_screen_container"
    const val description_screen_container = "description_screen_container"
    const val description_app_bar = "description_app_bar"
    const val description_back = "description_back"
    const val description_skip = "description_skip"
    const val description_header = "description_header"
    const val description_title = "description_title"
    const val description_subtitle = "description_subtitle"
    const val description_prompt_container = "description_prompt_container"
    const val description_input = "description_input"
    const val description_continue = "description_continue"
    const val description_navigation = "description_navigation"
    const val experiences_screen_container = "experiences_screen_container"
    const val experiences_top_bar = "experiences_top_bar"
    const val experiences_title = "experiences_title"
    const val experiences_subtitle = "experiences_subtitle"
    const val experiences_filter_list = "experiences_filter_list"
    const val experiences_topic_grid = "experiences_topic_grid"
    const val experiences_filter_chip_prefix = "experiences_filter_chip"
    const val experiences_topic_chip_prefix = "experiences_topic_chip"
    const val experiences_cta = "experiences_cta"
    const val activities_screen = "activities_screen"
    const val map_screen = "map_screen"
    const val map_top_app_bar = "map_top_app_bar"
    const val map_container = "map_container"
    const val map_locate_user_fab = "map_locate_user_fab"
    const val map_toggle_view_fab = "map_toggle_view_fab"
    const val map_search_field = "map_search_field"

    // Navigation test tags
    const val bottom_navigation_menu = "bottom_navigation_menu"
    const val home_tab = "home_tab"
    const val map_tab = "map_tab"
    const val activities_tab = "activities_tab"
    const val profile_tab = "profile_tab"

    fun getTabTestTag(tab: Tab): String =
        when (tab) {
          is Tab.Home -> home_tab
          is Tab.Map -> map_tab
          is Tab.Activities -> activities_tab
          is Tab.Profile -> profile_tab
        }
  }
}
