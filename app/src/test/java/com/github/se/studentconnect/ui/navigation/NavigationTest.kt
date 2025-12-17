package com.github.se.studentconnect.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTest {

  @Test
  fun `Route visitorProfile returns correct route`() {
    val route = Route.visitorProfile("user123")
    assertEquals("visitorProfile/user123", route)
  }

  @Test
  fun `Route organizationProfile returns correct route`() {
    val route = Route.organizationProfile("org456")
    assertEquals("organizationProfile/org456", route)
  }

  @Test
  fun `Route mapWithLocation returns correct route without eventUid`() {
    val route = Route.mapWithLocation(46.5, 6.6, 15.0)
    assertEquals("map/46.5/6.6/15.0", route)
  }

  @Test
  fun `Route mapWithLocation returns correct route with eventUid`() {
    val route = Route.mapWithLocation(46.5, 6.6, 15.0, "event123")
    assertEquals("map/46.5/6.6/15.0?eventUid=event123", route)
  }

  @Test
  fun `Route mapWithLocation uses default zoom`() {
    val route = Route.mapWithLocation(46.5, 6.6)
    assertEquals("map/46.5/6.6/15.0", route)
  }

  @Test
  fun `Route eventView returns correct route`() {
    val route = Route.eventView("event123", true)
    assertEquals("eventView/event123/true", route)
  }

  @Test
  fun `Route eventView with hasJoined false returns correct route`() {
    val route = Route.eventView("event123", false)
    assertEquals("eventView/event123/false", route)
  }

  @Test
  fun `Route editPublicEvent returns correct route`() {
    val route = Route.editPublicEvent("event123")
    assertEquals("edit_public_event/event123", route)
  }

  @Test
  fun `Route editPrivateEvent returns correct route`() {
    val route = Route.editPrivateEvent("event123")
    assertEquals("edit_private_event/event123", route)
  }

  @Test
  fun `Route createPublicEventFromTemplate returns correct route`() {
    val route = Route.createPublicEventFromTemplate("template123")
    assertEquals("create_public_event_from_template/template123", route)
  }

  @Test
  fun `Route createPrivateEventFromTemplate returns correct route`() {
    val route = Route.createPrivateEventFromTemplate("template123")
    assertEquals("create_private_event_from_template/template123", route)
  }

  @Test
  fun `Route pollsListScreen returns correct route`() {
    val route = Route.pollsListScreen("event123")
    assertEquals("pollsList/event123", route)
  }

  @Test
  fun `Route pollScreen returns correct route`() {
    val route = Route.pollScreen("event123", "poll456")
    assertEquals("poll/event123/poll456", route)
  }

  @Test
  fun `Route eventStatistics returns correct route`() {
    val route = Route.eventStatistics("event123")
    assertEquals("eventStatistics/event123", route)
  }

  @Test
  fun `Screen Home is top level destination`() {
    assertTrue(Screen.Home.isTopLevelDestination)
  }

  @Test
  fun `Screen Map is top level destination`() {
    assertTrue(Screen.Map.isTopLevelDestination)
  }

  @Test
  fun `Screen Activities is top level destination`() {
    assertTrue(Screen.Activities.isTopLevelDestination)
  }

  @Test
  fun `Screen Profile is top level destination`() {
    assertTrue(Screen.Profile.isTopLevelDestination)
  }

  @Test
  fun `Screen Auth is not top level destination`() {
    assertFalse(Screen.Auth.isTopLevelDestination)
  }

  @Test
  fun `Screen JoinedEvents is not top level destination`() {
    assertFalse(Screen.JoinedEvents.isTopLevelDestination)
  }

  @Test
  fun `Route constants are defined correctly`() {
    assertEquals("auth", Route.AUTH)
    assertEquals("get_started", Route.GET_STARTED)
    assertEquals("home", Route.HOME)
    assertEquals("map", Route.MAP)
    assertEquals("activities", Route.ACTIVITIES)
    assertEquals("profile", Route.PROFILE)
    assertEquals("search", Route.SEARCH)
    assertEquals("create_public_event", Route.CREATE_PUBLIC_EVENT)
    assertEquals("create_private_event", Route.CREATE_PRIVATE_EVENT)
    assertEquals("select_event_template", Route.SELECT_EVENT_TEMPLATE)
  }

  @Test
  fun `Route USER_ID_ARG is correct`() {
    assertEquals("userId", Route.USER_ID_ARG)
  }

  @Test
  fun `Route ORGANIZATION_ID_ARG is correct`() {
    assertEquals("organizationId", Route.ORGANIZATION_ID_ARG)
  }

  @Test
  fun `Route eventChat returns correct route`() {
    val route = Route.eventChat("event123")
    assertEquals("eventChat/event123", route)
  }

  @Test
  fun `Route joinedEvents returns correct route without userId`() {
    val route = Route.joinedEvents(null)
    assertEquals("joinedEvents", route)
  }

  @Test
  fun `Route joinedEvents returns correct route with userId`() {
    val route = Route.joinedEvents("user123")
    assertEquals("joinedEvents?userId=user123", route)
  }

  @Test
  fun `Route constants for onboarding are defined correctly`() {
    assertEquals("basic_info", Route.BASIC_INFO)
    assertEquals("nationality", Route.NATIONALITY)
    assertEquals("add_picture", Route.ADD_PICTURE)
    assertEquals("description", Route.DESCRIPTION)
    assertEquals("experiences", Route.EXPERIENCES)
  }

  @Test
  fun `Route constants for event templates are defined correctly`() {
    assertTrue(
        Route.CREATE_PUBLIC_EVENT_FROM_TEMPLATE.contains("create_public_event_from_template"))
    assertTrue(
        Route.CREATE_PRIVATE_EVENT_FROM_TEMPLATE.contains("create_private_event_from_template"))
  }

  @Test
  fun `Route constants for event editing are defined correctly`() {
    assertTrue(Route.EDIT_PUBLIC_EVENT.contains("edit_public_event"))
    assertTrue(Route.EDIT_PRIVATE_EVENT.contains("edit_private_event"))
  }

  @Test
  fun `Route EVENT_VIEW constant is defined correctly`() {
    assertEquals("eventView/{eventUid}", Route.EVENT_VIEW)
  }

  @Test
  fun `Route VISITOR_PROFILE constant contains userId parameter`() {
    assertTrue(Route.VISITOR_PROFILE.contains("{userId}"))
  }

  @Test
  fun `Route ORGANIZATION_PROFILE constant contains organizationId parameter`() {
    assertTrue(Route.ORGANIZATION_PROFILE.contains("{organizationId}"))
  }

  @Test
  fun `Route JOINED_EVENTS constant contains userId parameter`() {
    assertTrue(Route.JOINED_EVENTS.contains("{userId}"))
  }

  @Test
  fun `Route POLLS_LIST constant is defined correctly`() {
    assertEquals("pollsList/{eventUid}", Route.POLLS_LIST)
  }

  @Test
  fun `Route POLL_SCREEN constant is defined correctly`() {
    assertEquals("poll/{eventUid}/{pollUid}", Route.POLL_SCREEN)
  }

  @Test
  fun `Route EVENT_STATISTICS constant is defined correctly`() {
    assertEquals("eventStatistics/{eventUid}", Route.EVENT_STATISTICS)
  }

  @Test
  fun `Route EVENT_CHAT constant is defined correctly`() {
    assertEquals("eventChat/{eventUid}", Route.EVENT_CHAT)
  }

  @Test
  fun `Route MAP_WITH_LOCATION constant is defined correctly`() {
    assertEquals("map/{latitude}/{longitude}/{zoom}?eventUid={eventUid}", Route.MAP_WITH_LOCATION)
  }

  @Test
  fun `Screen CreatePublicEvent has correct properties`() {
    assertEquals(Route.CREATE_PUBLIC_EVENT, Screen.CreatePublicEvent.route)
    assertEquals("Create Public Event", Screen.CreatePublicEvent.name)
    assertFalse(Screen.CreatePublicEvent.isTopLevelDestination)
  }

  @Test
  fun `Screen CreatePrivateEvent has correct properties`() {
    assertEquals(Route.CREATE_PRIVATE_EVENT, Screen.CreatePrivateEvent.route)
    assertEquals("Create Private Event", Screen.CreatePrivateEvent.name)
    assertFalse(Screen.CreatePrivateEvent.isTopLevelDestination)
  }

  @Test
  fun `Screen EditPublicEvent has correct properties`() {
    assertEquals(Route.EDIT_PUBLIC_EVENT, Screen.EditPublicEvent.route)
    assertEquals("Edit Public Event", Screen.EditPublicEvent.name)
    assertFalse(Screen.EditPublicEvent.isTopLevelDestination)
  }

  @Test
  fun `Screen EditPrivateEvent has correct properties`() {
    assertEquals(Route.EDIT_PRIVATE_EVENT, Screen.EditPrivateEvent.route)
    assertEquals("Edit Private Event", Screen.EditPrivateEvent.name)
    assertFalse(Screen.EditPrivateEvent.isTopLevelDestination)
  }

  @Test
  fun `Screen EventView has correct properties`() {
    assertEquals(Route.EVENT_VIEW, Screen.EventView.route)
    assertEquals("Event Details", Screen.EventView.name)
    assertFalse(Screen.EventView.isTopLevelDestination)
  }

  @Test
  fun `Screen Search has correct properties`() {
    assertEquals(Route.SEARCH, Screen.Search.route)
    assertEquals("Search", Screen.Search.name)
    assertFalse(Screen.Search.isTopLevelDestination)
  }

  @Test
  fun `Screen EventStatistics has correct properties`() {
    assertEquals(Route.EVENT_STATISTICS, Screen.EventStatistics.route)
    assertEquals("Event Statistics", Screen.EventStatistics.name)
    assertFalse(Screen.EventStatistics.isTopLevelDestination)
  }

  @Test
  fun `Screen EventChat has correct properties`() {
    assertEquals(Route.EVENT_CHAT, Screen.EventChat.route)
    assertEquals("Event Chat", Screen.EventChat.name)
    assertFalse(Screen.EventChat.isTopLevelDestination)
  }

  @Test
  fun `Tab Home has correct properties`() {
    assertEquals("Home", Tab.Home.name)
    assertEquals(Screen.Home, Tab.Home.destination)
  }

  @Test
  fun `Tab Map has correct properties`() {
    assertEquals("Map", Tab.Map.name)
    assertEquals(Screen.Map, Tab.Map.destination)
  }

  @Test
  fun `Tab Activities has correct properties`() {
    assertEquals("Activities", Tab.Activities.name)
    assertEquals(Screen.Activities, Tab.Activities.destination)
  }

  @Test
  fun `Tab Profile has correct properties`() {
    assertEquals("Profile", Tab.Profile.name)
    assertEquals(Screen.Profile, Tab.Profile.destination)
  }

  @Test
  fun `bottomNavigationTabs contains all expected tabs`() {
    assertEquals(4, bottomNavigationTabs.size)
    assertTrue(bottomNavigationTabs.contains(Tab.Home))
    assertTrue(bottomNavigationTabs.contains(Tab.Map))
    assertTrue(bottomNavigationTabs.contains(Tab.Activities))
    assertTrue(bottomNavigationTabs.contains(Tab.Profile))
  }

  @Test
  fun `Screen Home has correct route and name`() {
    assertEquals("home", Screen.Home.route)
    assertEquals("Home", Screen.Home.name)
  }

  @Test
  fun `Screen Map has correct route and name`() {
    assertEquals("map", Screen.Map.route)
    assertEquals("Map", Screen.Map.name)
  }

  @Test
  fun `Screen Activities has correct route and name`() {
    assertEquals("activities", Screen.Activities.route)
    assertEquals("Activities", Screen.Activities.name)
  }

  @Test
  fun `Screen Profile has correct route and name`() {
    assertEquals("profile", Screen.Profile.route)
    assertEquals("Profile", Screen.Profile.name)
  }

  @Test
  fun `Screen Auth has correct route and name`() {
    assertEquals("auth", Screen.Auth.route)
    assertEquals("Authentication", Screen.Auth.name)
  }

  @Test
  fun `Route mapWithLocation handles negative coordinates`() {
    val route = Route.mapWithLocation(-46.5, -6.6, 10.0)
    assertEquals("map/-46.5/-6.6/10.0", route)
  }

  @Test
  fun `Route mapWithLocation handles large zoom values`() {
    val route = Route.mapWithLocation(46.5, 6.6, 20.0)
    assertEquals("map/46.5/6.6/20.0", route)
  }

  @Test
  fun `Route mapWithLocation handles small zoom values`() {
    val route = Route.mapWithLocation(46.5, 6.6, 1.0)
    assertEquals("map/46.5/6.6/1.0", route)
  }

  @Test
  fun `Route functions handle special characters in IDs`() {
    val route1 = Route.visitorProfile("user-123-abc")
    assertEquals("visitorProfile/user-123-abc", route1)

    val route2 = Route.organizationProfile("org_456_xyz")
    assertEquals("organizationProfile/org_456_xyz", route2)
  }

  @Test
  fun `Route functions handle empty strings gracefully`() {
    val route1 = Route.visitorProfile("")
    assertEquals("visitorProfile/", route1)

    val route2 = Route.eventChat("")
    assertEquals("eventChat/", route2)
  }
}
