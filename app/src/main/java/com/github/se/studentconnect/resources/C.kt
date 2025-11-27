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

    // About screen test tags (new screen inspired from DescriptionScreen)
    const val about_screen_container = "about_screen_container"
    const val about_app_bar = "about_app_bar"
    const val about_back = "about_back"
    const val about_skip = "about_skip"
    const val about_title = "about_title"
    const val about_subtitle = "about_subtitle"
    const val about_prompt_container = "about_prompt_container"
    const val about_input = "about_input"
    const val about_continue = "about_continue"

    const val experiences_screen_container = "experiences_screen_container"
    const val experiences_top_bar = "experiences_top_bar"
    const val experiences_title = "experiences_title"
    const val experiences_subtitle = "experiences_subtitle"
    const val experiences_topic_grid = "experiences_topic_grid"
    const val experiences_cta = "experiences_cta"
    const val activities_screen = "activities_screen"
    const val map_screen = "map_screen"
    const val map_top_app_bar = "map_top_app_bar"
    const val map_container = "map_container"
    const val map_locate_user_fab = "map_locate_user_fab"
    const val map_toggle_view_fab = "map_toggle_view_fab"
    const val map_search_field = "map_search_field"
    const val map_event_info_card = "map_event_info_card"

    // Navigation test tags
    const val bottom_navigation_menu = "bottom_navigation_menu"
    const val home_tab = "home_tab"
    const val map_tab = "map_tab"
    const val activities_tab = "activities_tab"
    const val profile_tab = "profile_tab"

    // Search test tags
    const val search_screen = "search_screen"
    const val search_input_field = "search_input_field"
    const val back_button = "back_button"
    const val user_search_result = "user_search_result"
    const val user_search_result_title = "user_search_result_title"
    const val event_search_result = "event_search_result"
    const val event_search_result_title = "event_search_result_title"

    fun getTabTestTag(tab: Tab): String =
        when (tab) {
          is Tab.Home -> home_tab
          is Tab.Map -> map_tab
          is Tab.Activities -> activities_tab
          is Tab.Profile -> profile_tab
        }

    const val visitor_profile_screen = "visitor_profile_screen"
    const val visitor_profile_top_bar = "visitor_profile_top_bar"
    const val visitor_profile_back = "visitor_profile_back"
    const val visitor_profile_user_card = "visitor_profile_user_card"
    const val visitor_profile_user_id = "visitor_profile_user_id"
    const val visitor_profile_user_name = "visitor_profile_user_name"
    const val visitor_profile_bio = "visitor_profile_bio"
    const val visitor_profile_pinned_section = "visitor_profile_pinned_section"
    const val visitor_profile_empty_state = "visitor_profile_empty_state"
    const val visitor_profile_avatar = "visitor_profile_avatar"
    const val visitor_profile_add_friend = "visitor_profile_add_friend"
    const val visitor_profile_cancel_friend = "visitor_profile_cancel_friend"
    const val visitor_profile_remove_friend = "visitor_profile_remove_friend"
    const val visitor_profile_dialog_confirm = "visitor_profile_dialog_confirm"
    const val visitor_profile_dialog_dismiss = "visitor_profile_dialog_dismiss"
    const val visitor_profile_loading = "visitor_profile_loading"
    const val visitor_profile_error = "visitor_profile_error"

    const val qr_scanner_screen = "qr_scanner_screen"
    const val qr_scanner_back = "qr_scanner_back"
    const val qr_scanner_instructions = "qr_scanner_instructions"
    const val qr_scanner_focus = "qr_scanner_focus"
    const val qr_scanner_error = "qr_scanner_error"
    const val qr_scanner_permission = "qr_scanner_permission"
    const val qr_scanner_placeholder = "qr_scanner_placeholder"

    // Organization profile test tags
    const val org_profile_screen = "org_profile_screen"
    const val org_profile_header = "org_profile_header"
    const val org_profile_avatar_banner = "org_profile_avatar_banner"
    const val org_profile_avatar = "org_profile_avatar"
    const val org_profile_title = "org_profile_title"
    const val org_profile_description = "org_profile_description"
    const val org_profile_follow_button = "org_profile_follow_button"
    const val org_profile_about_section = "org_profile_about_section"
    const val org_profile_tab_events = "org_profile_tab_events"
    const val org_profile_tab_members = "org_profile_tab_members"
    const val org_profile_events_list = "org_profile_events_list"
    const val org_profile_event_row_prefix = "org_profile_event_row"
    const val org_profile_event_card_prefix = "org_profile_event_card"
    const val org_profile_members_grid = "org_profile_members_grid"
    const val org_profile_member_card_prefix = "org_profile_member_card"
    const val org_profile_events_empty = "org_profile_events_empty"
    const val org_profile_members_empty = "org_profile_members_empty"
  }
}
