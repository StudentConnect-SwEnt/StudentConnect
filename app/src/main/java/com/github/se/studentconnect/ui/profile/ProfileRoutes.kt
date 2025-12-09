package com.github.se.studentconnect.ui.profile

/** Navigation routes for Profile-related screens. */
object ProfileRoutes {
  const val SETTINGS = "profile_settings"
  const val USER_CARD = "user_card"
  const val FRIENDS_LIST = "friends_list/{userId}"
  const val EDIT_PICTURE = "edit_profile_picture/{userId}"
  const val EDIT_BIO = "edit_bio/{userId}"
  const val EDIT_ACTIVITIES = "edit_activities/{userId}"
  const val EDIT_NAME = "edit_name/{userId}"
  const val EDIT_BIRTHDAY = "edit_birthday/{userId}"
  const val EDIT_NATIONALITY = "edit_nationality/{userId}"

  fun friendsList(userId: String) = "friends_list/$userId"

  /** Creates the route for editing profile picture. */
  fun editPicture(userId: String) = "edit_profile_picture/$userId"

  /** Creates the route for editing bio. */
  fun editBio(userId: String) = "edit_bio/$userId"

  /** Creates the route for editing activities. */
  fun editActivities(userId: String) = "edit_activities/$userId"

  /** Creates the route for editing name. */
  fun editName(userId: String) = "edit_name/$userId"

  /** Creates the route for editing birthday. */
  fun editBirthday(userId: String) = "edit_birthday/$userId"

  /** Creates the route for editing nationality. */
  fun editNationality(userId: String) = "edit_nationality/$userId"
}
