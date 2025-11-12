package com.github.se.studentconnect.model

/**
 * Represents a User in the StudentConnect application.
 *
 * @property userId The unique identifier for the user (from Firebase Auth).
 * @property email The user's email address.
 * @property username The unique username for the user (alphanumeric, underscore, hyphen, period).
 * @property firstName The user's first name.
 * @property lastName The user's last name.
 * @property university The university the user is attending in Switzerland.
 * @property hobbies A list of the user's interests and hobbies.
 * @property profilePictureUrl URL to the user's profile picture (optional).
 * @property bio A short biography or description about the user (optional).
 * @property country The user's country (optional).
 * @property birthday The user's birthday in DD/MM/YYYY format (optional).
 * @property createdAt Timestamp when the user profile was created (in milliseconds).
 * @property updatedAt Timestamp when the user profile was last updated (in milliseconds).
 */
data class User(
    val userId: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val university: String,
    val hobbies: List<String> = emptyList(),
    val profilePictureUrl: String? = null, // optional
    val bio: String? = null, // optional
    val country: String? = null, // optional
    val birthday: String? = null, // optional - format: "31/12/1980"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
  init {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    require(email.isNotBlank()) { "Email cannot be blank" }
    require(isValidEmail(email)) { "Email must be valid" }
    require(username.isNotBlank()) { "Username cannot be blank" }
    require(username.length in 3..20) { "Username must be between 3 and 20 characters" }
    require(isValidUsername(username)) {
      "Username can only contain alphanumeric characters, underscores, hyphens, and periods"
    }
    require(firstName.isNotBlank()) { "First name cannot be blank" }
    require(firstName.length <= 100) { "First name cannot exceed 100 characters" }
    require(lastName.isNotBlank()) { "Last name cannot be blank" }
    require(lastName.length <= 100) { "Last name cannot exceed 100 characters" }
    require(university.isNotBlank()) { "University cannot be blank" }
    require(university.length <= 200) { "University name cannot exceed 200 characters" }
    require(createdAt > 0) { "Created timestamp must be positive" }
    require(updatedAt > 0) { "Updated timestamp must be positive" }
    require(updatedAt >= createdAt) { "Updated timestamp cannot be before created timestamp" }
    bio?.let { require(it.length <= 500) { "Bio cannot exceed 500 characters" } }
  }

  /** Returns the user's full name. */
  fun getFullName(): String = "$firstName $lastName"

  /** Returns true if the user has a profile picture. */
  fun hasProfilePicture(): Boolean = !profilePictureUrl.isNullOrBlank()

  /** Returns true if the user has a bio. */
  fun hasBio(): Boolean = !bio.isNullOrBlank()

  /** Wrapper class for nullable updates to distinguish between "no change" and "set to null" */
  sealed class UpdateValue<T> {
    class NoChange<T> : UpdateValue<T>()

    data class SetValue<T>(val value: T?) : UpdateValue<T>()
  }

  /**
   * Returns a new User instance with updated fields. Uses UpdateValue wrapper to properly handle
   * nullable field updates.
   *
   * @param email The new email (use UpdateValue.SetValue to change, UpdateValue.NoChange to keep
   *   current).
   * @param firstName The new first name (use UpdateValue.SetValue to change, UpdateValue.NoChange
   *   to keep current).
   * @param lastName The new last name (use UpdateValue.SetValue to change, UpdateValue.NoChange to
   *   keep current).
   * @param university The new university (use UpdateValue.SetValue to change, UpdateValue.NoChange
   *   to keep current).
   * @param hobbies The new list of hobbies (use UpdateValue.SetValue to change,
   *   UpdateValue.NoChange to keep current).
   * @param profilePictureUrl The new profile picture URL (use UpdateValue.SetValue to change,
   *   UpdateValue.NoChange to keep current).
   * @param bio The new bio (use UpdateValue.SetValue to change, UpdateValue.NoChange to keep
   *   current).
   * @param country The new country (use UpdateValue.SetValue to change, UpdateValue.NoChange to
   *   keep current).
   * @param birthday The new birthday (use UpdateValue.SetValue to change, UpdateValue.NoChange to
   *   keep current).
   * @return A new User instance with the updated fields.
   */
  fun update(
      email: UpdateValue<String> = UpdateValue.NoChange(),
      username: UpdateValue<String> = UpdateValue.NoChange(),
      firstName: UpdateValue<String> = UpdateValue.NoChange(),
      lastName: UpdateValue<String> = UpdateValue.NoChange(),
      university: UpdateValue<String> = UpdateValue.NoChange(),
      hobbies: UpdateValue<List<String>> = UpdateValue.NoChange(),
      profilePictureUrl: UpdateValue<String?> = UpdateValue.NoChange(),
      country: UpdateValue<String?> = UpdateValue.NoChange(),
      birthday: UpdateValue<String?> = UpdateValue.NoChange(),
      bio: UpdateValue<String?> = UpdateValue.NoChange()
  ): User {
    return copy(
        email =
            when (email) {
              is UpdateValue.SetValue -> email.value ?: this.email
              else -> this.email
            },
        username =
            when (username) {
              is UpdateValue.SetValue -> username.value ?: this.username
              else -> this.username
            },
        firstName =
            when (firstName) {
              is UpdateValue.SetValue -> firstName.value ?: this.firstName
              else -> this.firstName
            },
        lastName =
            when (lastName) {
              is UpdateValue.SetValue -> lastName.value ?: this.lastName
              else -> this.lastName
            },
        university =
            when (university) {
              is UpdateValue.SetValue -> university.value ?: this.university
              else -> this.university
            },
        hobbies =
            when (hobbies) {
              is UpdateValue.SetValue -> hobbies.value ?: this.hobbies
              else -> this.hobbies
            },
        profilePictureUrl =
            when (profilePictureUrl) {
              is UpdateValue.SetValue -> profilePictureUrl.value
              else -> this.profilePictureUrl
            },
        country =
            when (country) {
              is UpdateValue.SetValue -> country.value
              else -> this.country
            },
        birthday =
            when (birthday) {
              is UpdateValue.SetValue -> birthday.value
              else -> this.birthday
            },
        bio =
            when (bio) {
              is UpdateValue.SetValue -> bio.value
              else -> this.bio
            },
        updatedAt = System.currentTimeMillis())
  }

  /**
   * Converts the User to a Map for Firestore storage.
   *
   * @return A map representation of the User.
   */
  fun toMap(): Map<String, Any?> {
    return mapOf(
        "userId" to userId,
        "email" to email,
        "username" to username, // Stored in lowercase
        "firstName" to firstName,
        "lastName" to lastName,
        "university" to university,
        "hobbies" to hobbies,
        "profilePictureUrl" to profilePictureUrl,
        "country" to country,
        "birthday" to birthday,
        "bio" to bio,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt)
  }

  companion object {
    private fun isValidEmail(email: String): Boolean {
      return email.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))
    }

    private fun isValidUsername(username: String): Boolean {
      return username.matches(Regex("^[a-zA-Z0-9_.-]+$"))
    }

    /**
     * Creates a User instance from a Map (typically from Firestore).
     *
     * @param map The map containing user data.
     * @return A User instance, or null if the map is invalid.
     */
    fun fromMap(map: Map<String, Any?>): User? {
      return try {
        val createdAtValue = (map["createdAt"] as? Number)?.toLong()
        val updatedAtValue = (map["updatedAt"] as? Number)?.toLong()

        // Only allow createdAt fallback for new documents, fail for missing updatedAt
        val createdAt = createdAtValue ?: System.currentTimeMillis()
        val updatedAt = updatedAtValue ?: return null // Fail if updatedAt is missing

        User(
            userId = map["userId"] as? String ?: return null,
            email = map["email"] as? String ?: return null,
            username = map["username"] as? String ?: return null,
            firstName = map["firstName"] as? String ?: return null,
            lastName = map["lastName"] as? String ?: return null,
            university = map["university"] as? String ?: return null,
            hobbies = (map["hobbies"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            profilePictureUrl = map["profilePictureUrl"] as? String,
            country = map["country"] as? String,
            birthday = map["birthday"] as? String,
            bio = map["bio"] as? String,
            createdAt = createdAt,
            updatedAt = updatedAt)
      } catch (e: Exception) {
        null
      }
    }
  }
}
