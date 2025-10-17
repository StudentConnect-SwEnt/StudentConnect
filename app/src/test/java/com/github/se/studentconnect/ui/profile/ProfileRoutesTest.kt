package com.github.se.studentconnect.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileRoutesTest {

  private val userId = "testUserId123"

  @Test
  fun `EDIT_NAME route is correct`() {
    assertEquals("edit_name/{userId}", ProfileRoutes.EDIT_NAME)
  }

  @Test
  fun `editName generates correct route`() {
    assertEquals("edit_name/$userId", ProfileRoutes.editName(userId))
  }

  @Test
  fun `EDIT_BIO route is correct`() {
    assertEquals("edit_bio/{userId}", ProfileRoutes.EDIT_BIO)
  }

  @Test
  fun `editBio generates correct route`() {
    assertEquals("edit_bio/$userId", ProfileRoutes.editBio(userId))
  }

  @Test
  fun `EDIT_ACTIVITIES route is correct`() {
    assertEquals("edit_activities/{userId}", ProfileRoutes.EDIT_ACTIVITIES)
  }

  @Test
  fun `editActivities generates correct route`() {
    assertEquals("edit_activities/$userId", ProfileRoutes.editActivities(userId))
  }

  @Test
  fun `EDIT_BIRTHDAY route is correct`() {
    assertEquals("edit_birthday/{userId}", ProfileRoutes.EDIT_BIRTHDAY)
  }

  @Test
  fun `editBirthday generates correct route`() {
    assertEquals("edit_birthday/$userId", ProfileRoutes.editBirthday(userId))
  }

  @Test
  fun `EDIT_NATIONALITY route is correct`() {
    assertEquals("edit_nationality/{userId}", ProfileRoutes.EDIT_NATIONALITY)
  }

  @Test
  fun `editNationality generates correct route`() {
    assertEquals("edit_nationality/$userId", ProfileRoutes.editNationality(userId))
  }

  @Test
  fun `EDIT_PICTURE route is correct`() {
    assertEquals("edit_profile_picture/{userId}", ProfileRoutes.EDIT_PICTURE)
  }

  @Test
  fun `editPicture generates correct route`() {
    assertEquals("edit_profile_picture/$userId", ProfileRoutes.editPicture(userId))
  }

  @Test
  fun `routes with special characters in userId`() {
    val specialUserId = "user-123_test@domain.com"
    assertEquals("edit_name/$specialUserId", ProfileRoutes.editName(specialUserId))
    assertEquals("edit_bio/$specialUserId", ProfileRoutes.editBio(specialUserId))
  }

  @Test
  fun `routes with empty userId`() {
    val emptyUserId = ""
    assertEquals("edit_name/", ProfileRoutes.editName(emptyUserId))
    assertEquals("edit_bio/", ProfileRoutes.editBio(emptyUserId))
  }

  @Test
  fun `routes with numeric userId`() {
    val numericUserId = "12345"
    assertEquals("edit_name/$numericUserId", ProfileRoutes.editName(numericUserId))
    assertEquals("edit_profile_picture/$numericUserId", ProfileRoutes.editPicture(numericUserId))
  }
}
