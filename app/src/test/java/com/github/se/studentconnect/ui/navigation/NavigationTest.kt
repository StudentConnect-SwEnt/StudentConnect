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
}

