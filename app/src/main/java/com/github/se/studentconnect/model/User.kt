package com.github.se.studentconnect.model

/**
 * Represents a User in the StudentConnect application.
 *
 * @property userId The unique identifier for the user (from Firebase Auth).
 * @property email The user's email address.
 * @property firstName The user's first name.
 * @property lastName The user's last name.
 * @property university The university the user is attending in Switzerland.
 * @property hobbies A list of the user's interests and hobbies.
 * @property profilePictureUrl URL to the user's profile picture (optional).
 * @property bio A short biography or description about the user (optional).
 * @property createdAt Timestamp when the user profile was created (in milliseconds).
 * @property updatedAt Timestamp when the user profile was last updated (in milliseconds).
 */
data class User(
    val userId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val university: String,
    val hobbies: List<String> = emptyList(),
    val profilePictureUrl: String? = null, // optionnel
    val bio: String? = null, // optionnel
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
  init {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    require(email.isNotBlank()) { "Email cannot be blank" }
    require(email.contains("@")) { "Email must be valid" }
    require(firstName.isNotBlank()) { "First name cannot be blank" }
    require(lastName.isNotBlank()) { "Last name cannot be blank" }
    require(university.isNotBlank()) { "University cannot be blank" }
    require(createdAt > 0) { "Created timestamp must be positive" }
    require(updatedAt > 0) { "Updated timestamp must be positive" }
    require(updatedAt >= createdAt) { "Updated timestamp cannot be before created timestamp" }
  }

  /** Returns the user's full name. */
  fun getFullName(): String = "$firstName $lastName"

  /** Returns true if the user has a profile picture. */
  fun hasProfilePicture(): Boolean = !profilePictureUrl.isNullOrBlank()

  /** Returns true if the user has a bio. */
  fun hasBio(): Boolean = !bio.isNullOrBlank()

  /**
   * Returns a new User instance with updated fields.
   *
   * @param email The new email (optional).
   * @param firstName The new first name (optional).
   * @param lastName The new last name (optional).
   * @param university The new university (optional).
   * @param hobbies The new list of hobbies (optional).
   * @param profilePictureUrl The new profile picture URL (optional).
   * @param bio The new bio (optional).
   * @return A new User instance with the updated fields.
   */
  fun update(
      email: String? = null,
      firstName: String? = null,
      lastName: String? = null,
      university: String? = null,
      hobbies: List<String>? = null,
      profilePictureUrl: String? = null,
      bio: String? = null
  ): User {
    return copy(
        email = email ?: this.email,
        firstName = firstName ?: this.firstName,
        lastName = lastName ?: this.lastName,
        university = university ?: this.university,
        hobbies = hobbies ?: this.hobbies,
        profilePictureUrl = profilePictureUrl ?: this.profilePictureUrl,
        bio = bio ?: this.bio,
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
        "firstName" to firstName,
        "lastName" to lastName,
        "university" to university,
        "hobbies" to hobbies,
        "profilePictureUrl" to profilePictureUrl,
        "bio" to bio,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt)
  }

  companion object {
    /**
     * Creates a User instance from a Map (typically from Firestore).
     *
     * @param map The map containing user data.
     * @return A User instance, or null if the map is invalid.
     */
    fun fromMap(map: Map<String, Any?>): User? {
      return try {
        User(
            userId = map["userId"] as? String ?: return null,
            email = map["email"] as? String ?: return null,
            firstName = map["firstName"] as? String ?: return null,
            lastName = map["lastName"] as? String ?: return null,
            university = map["university"] as? String ?: return null,
            hobbies = (map["hobbies"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            profilePictureUrl = map["profilePictureUrl"] as? String,
            bio = map["bio"] as? String,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis())
      } catch (e: Exception) {
        null
      }
    }
  }
}
