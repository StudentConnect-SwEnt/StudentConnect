package com.github.se.studentconnect.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileRoutesTest {

  @Test
  fun `ProfileRoutes contains all required route constants`() {
    assertEquals("friends_list/{userId}", ProfileRoutes.FRIENDS_LIST)
    assertEquals("edit_profile_picture/{userId}", ProfileRoutes.EDIT_PICTURE)
    assertEquals("edit_bio/{userId}", ProfileRoutes.EDIT_BIO)
    assertEquals("edit_activities/{userId}", ProfileRoutes.EDIT_ACTIVITIES)
    assertEquals("edit_name/{userId}", ProfileRoutes.EDIT_NAME)
    assertEquals("edit_birthday/{userId}", ProfileRoutes.EDIT_BIRTHDAY)
    assertEquals("edit_nationality/{userId}", ProfileRoutes.EDIT_NATIONALITY)
  }

  @Test
  fun `ProfileRoutes route constants are not empty`() {
    assertFalse(ProfileRoutes.FRIENDS_LIST.isBlank())
    assertFalse(ProfileRoutes.EDIT_PICTURE.isBlank())
    assertFalse(ProfileRoutes.EDIT_NAME.isBlank())
    assertFalse(ProfileRoutes.EDIT_BIO.isBlank())
    assertFalse(ProfileRoutes.EDIT_ACTIVITIES.isBlank())
    assertFalse(ProfileRoutes.EDIT_BIRTHDAY.isBlank())
    assertFalse(ProfileRoutes.EDIT_NATIONALITY.isBlank())
  }

  @Test
  fun `ProfileRoutes route constants are lowercase with underscores`() {
    assertTrue(ProfileRoutes.EDIT_PICTURE.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_NAME.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_BIO.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_ACTIVITIES.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_BIRTHDAY.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_NATIONALITY.contains("edit"))
  }

  @Test
  fun `ProfileRoutes route constants are descriptive`() {
    assertTrue(ProfileRoutes.EDIT_PICTURE.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_PICTURE.contains("picture"))

    assertTrue(ProfileRoutes.EDIT_NAME.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_NAME.contains("name"))

    assertTrue(ProfileRoutes.EDIT_BIO.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_BIO.contains("bio"))

    assertTrue(ProfileRoutes.EDIT_ACTIVITIES.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_ACTIVITIES.contains("activities"))

    assertTrue(ProfileRoutes.EDIT_BIRTHDAY.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_BIRTHDAY.contains("birthday"))

    assertTrue(ProfileRoutes.EDIT_NATIONALITY.contains("edit"))
    assertTrue(ProfileRoutes.EDIT_NATIONALITY.contains("nationality"))
  }

  @Test
  fun `ProfileRoutes route constants are unique`() {
    val routes =
        listOf(
            ProfileRoutes.FRIENDS_LIST,
            ProfileRoutes.EDIT_PICTURE,
            ProfileRoutes.EDIT_NAME,
            ProfileRoutes.EDIT_BIO,
            ProfileRoutes.EDIT_ACTIVITIES,
            ProfileRoutes.EDIT_BIRTHDAY,
            ProfileRoutes.EDIT_NATIONALITY)

    val uniqueRoutes = routes.distinct()
    assertEquals(routes.size, uniqueRoutes.size)
  }

  @Test
  fun `ProfileRoutes route constants follow naming convention`() {
    // All routes should start with "edit_"
    val routes =
        listOf(
            ProfileRoutes.EDIT_PICTURE,
            ProfileRoutes.EDIT_NAME,
            ProfileRoutes.EDIT_BIO,
            ProfileRoutes.EDIT_ACTIVITIES,
            ProfileRoutes.EDIT_BIRTHDAY,
            ProfileRoutes.EDIT_NATIONALITY)

    routes.forEach { route ->
      assertTrue("Route $route should start with 'edit_'", route.startsWith("edit_"))
    }
  }

  @Test
  fun `ProfileRoutes route constants are appropriate length`() {
    val routes =
        listOf(
            ProfileRoutes.EDIT_PICTURE,
            ProfileRoutes.EDIT_NAME,
            ProfileRoutes.EDIT_BIO,
            ProfileRoutes.EDIT_ACTIVITIES,
            ProfileRoutes.EDIT_BIRTHDAY,
            ProfileRoutes.EDIT_NATIONALITY)

    routes.forEach { route ->
      assertTrue("Route too short: $route", route.length >= 10)
      assertTrue("Route too long: $route", route.length <= 40)
    }
  }

  @Test
  fun `ProfileRoutes route constants are valid navigation routes`() {
    val routes =
        listOf(
            ProfileRoutes.EDIT_PICTURE,
            ProfileRoutes.EDIT_NAME,
            ProfileRoutes.EDIT_BIO,
            ProfileRoutes.EDIT_ACTIVITIES,
            ProfileRoutes.EDIT_BIRTHDAY,
            ProfileRoutes.EDIT_NATIONALITY)

    routes.forEach { route ->
      assertTrue("Route $route should not be empty", route.isNotEmpty())
      assertTrue("Route $route should contain userId parameter", route.contains("{userId}"))
    }
  }

  @Test
  fun `ProfileRoutes route constants are consistent in structure`() {
    // All edit routes should follow the pattern "edit_[field]/{userId}"
    val editRoutes =
        listOf(
            ProfileRoutes.EDIT_PICTURE,
            ProfileRoutes.EDIT_NAME,
            ProfileRoutes.EDIT_BIO,
            ProfileRoutes.EDIT_ACTIVITIES,
            ProfileRoutes.EDIT_BIRTHDAY,
            ProfileRoutes.EDIT_NATIONALITY)

    editRoutes.forEach { route ->
      assertTrue("Route $route should start with 'edit_'", route.startsWith("edit_"))
      assertTrue("Route $route should contain '{userId}'", route.contains("{userId}"))
    }
  }

  @Test
  fun `ProfileRoutes route constants match their purpose`() {
    // Test that route names match their intended functionality
    assertEquals("edit_profile_picture/{userId}", ProfileRoutes.EDIT_PICTURE)
    assertEquals("edit_name/{userId}", ProfileRoutes.EDIT_NAME)
    assertEquals("edit_bio/{userId}", ProfileRoutes.EDIT_BIO)
    assertEquals("edit_activities/{userId}", ProfileRoutes.EDIT_ACTIVITIES)
    assertEquals("edit_birthday/{userId}", ProfileRoutes.EDIT_BIRTHDAY)
    assertEquals("edit_nationality/{userId}", ProfileRoutes.EDIT_NATIONALITY)
  }

  @Test
  fun `ProfileRoutes route constants are navigation-friendly`() {
    val routes =
        listOf(
            ProfileRoutes.EDIT_PICTURE,
            ProfileRoutes.EDIT_NAME,
            ProfileRoutes.EDIT_BIO,
            ProfileRoutes.EDIT_ACTIVITIES,
            ProfileRoutes.EDIT_BIRTHDAY,
            ProfileRoutes.EDIT_NATIONALITY)

    routes.forEach { route ->
      // Routes should not contain spaces, special characters, or be too long for navigation
      assertFalse("Route $route should not contain spaces", route.contains(" "))
      assertFalse("Route $route should not contain hyphens", route.contains("-"))
      assertFalse("Route $route should not contain dots", route.contains("."))
      assertTrue("Route $route should be reasonable length for navigation", route.length <= 35)
    }
  }

  @Test
  fun `ProfileRoutes functions work correctly`() {
    val testUserId = "test_user_123"

    assertEquals("friends_list/$testUserId", ProfileRoutes.friendsList(testUserId))
    assertEquals("edit_profile_picture/$testUserId", ProfileRoutes.editPicture(testUserId))
    assertEquals("edit_name/$testUserId", ProfileRoutes.editName(testUserId))
    assertEquals("edit_bio/$testUserId", ProfileRoutes.editBio(testUserId))
    assertEquals("edit_activities/$testUserId", ProfileRoutes.editActivities(testUserId))
    assertEquals("edit_birthday/$testUserId", ProfileRoutes.editBirthday(testUserId))
    assertEquals("edit_nationality/$testUserId", ProfileRoutes.editNationality(testUserId))
  }

  @Test
  fun `ProfileRoutes functions handle special characters in userId`() {
    val specialUserId = "user-123_test"

    assertEquals("friends_list/$specialUserId", ProfileRoutes.friendsList(specialUserId))
    assertEquals("edit_profile_picture/$specialUserId", ProfileRoutes.editPicture(specialUserId))
    assertEquals("edit_name/$specialUserId", ProfileRoutes.editName(specialUserId))
    assertEquals("edit_bio/$specialUserId", ProfileRoutes.editBio(specialUserId))
    assertEquals("edit_activities/$specialUserId", ProfileRoutes.editActivities(specialUserId))
    assertEquals("edit_birthday/$specialUserId", ProfileRoutes.editBirthday(specialUserId))
    assertEquals("edit_nationality/$specialUserId", ProfileRoutes.editNationality(specialUserId))
  }

  @Test
  fun `ProfileRoutes functions handle empty userId`() {
    val emptyUserId = ""

    assertEquals("friends_list/$emptyUserId", ProfileRoutes.friendsList(emptyUserId))
    assertEquals("edit_profile_picture/$emptyUserId", ProfileRoutes.editPicture(emptyUserId))
    assertEquals("edit_name/$emptyUserId", ProfileRoutes.editName(emptyUserId))
    assertEquals("edit_bio/$emptyUserId", ProfileRoutes.editBio(emptyUserId))
    assertEquals("edit_activities/$emptyUserId", ProfileRoutes.editActivities(emptyUserId))
    assertEquals("edit_birthday/$emptyUserId", ProfileRoutes.editBirthday(emptyUserId))
    assertEquals("edit_nationality/$emptyUserId", ProfileRoutes.editNationality(emptyUserId))
  }

  @Test
  fun `friendsList function creates correct route`() {
    assertEquals("friends_list/user123", ProfileRoutes.friendsList("user123"))
    assertEquals("friends_list/abc-xyz", ProfileRoutes.friendsList("abc-xyz"))
  }

  @Test
  fun `FRIENDS_LIST constant has correct format`() {
    assertTrue(ProfileRoutes.FRIENDS_LIST.contains("friends_list"))
    assertTrue(ProfileRoutes.FRIENDS_LIST.contains("{userId}"))
  }
}
