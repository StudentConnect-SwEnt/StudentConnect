package com.github.se.studentconnect.model.organization

import com.google.firebase.Timestamp

/**
 * Represents the type of organization in the StudentConnect application.
 *
 * Used in the organization signup flow to categorize organizations.
 */
enum class OrganizationType {
  Association,
  StudentClub,
  Company,
  NGO,
  Other
}

/**
 * Represents a role within an organization.
 *
 * @property name The name of the role (e.g., "President", "Treasurer", "Event Coordinator").
 * @property description Optional description of the role's responsibilities.
 */
data class OrganizationRole(
    val name: String,
    val description: String? = null
) {
  init {
    require(name.isNotBlank()) { "Role name cannot be blank" }
    require(name.length <= MAX_NAME_LENGTH) {
      "Role name cannot exceed $MAX_NAME_LENGTH characters"
    }
    description?.let {
      require(it.length <= MAX_DESCRIPTION_LENGTH) {
        "Role description cannot exceed $MAX_DESCRIPTION_LENGTH characters"
      }
    }
  }

  companion object {
    const val MAX_NAME_LENGTH = 100
    const val MAX_DESCRIPTION_LENGTH = 500
  }
}

/**
 * Represents social media and web links for an organization.
 *
 * @property website Optional website URL.
 * @property instagram Optional Instagram profile URL.
 * @property x Optional X (Twitter) profile URL.
 * @property linkedin Optional LinkedIn profile URL.
 */
data class SocialLinks(
    val website: String? = null,
    val instagram: String? = null,
    val x: String? = null,
    val linkedin: String? = null
) {
  init {
    website?.let {
      require(it.isNotBlank()) { "Website URL cannot be blank" }
      require(isValidUrl(it)) { "Website URL must be valid" }
      require(it.length <= MAX_URL_LENGTH) {
        "Website URL cannot exceed $MAX_URL_LENGTH characters"
      }
    }
    instagram?.let {
      require(it.isNotBlank()) { "Instagram URL cannot be blank" }
      require(isValidUrl(it)) { "Instagram URL must be valid" }
      require(it.length <= MAX_URL_LENGTH) {
        "Instagram URL cannot exceed $MAX_URL_LENGTH characters"
      }
    }
    x?.let {
      require(it.isNotBlank()) { "X URL cannot be blank" }
      require(isValidUrl(it)) { "X URL must be valid" }
      require(it.length <= MAX_URL_LENGTH) {
        "X URL cannot exceed $MAX_URL_LENGTH characters"
      }
    }
    linkedin?.let {
      require(it.isNotBlank()) { "LinkedIn URL cannot be blank" }
      require(isValidUrl(it)) { "LinkedIn URL must be valid" }
      require(it.length <= MAX_URL_LENGTH) {
        "LinkedIn URL cannot exceed $MAX_URL_LENGTH characters"
      }
    }
  }

  companion object {
    const val MAX_URL_LENGTH = 500

    private fun isValidUrl(url: String): Boolean {
      return try {
        val uri = java.net.URI(url)
        uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https")
      } catch (e: Exception) {
        false
      }
    }
  }
}

/**
 * Represents an Organization in the StudentConnect application.
 *
 * @property id The unique identifier for the organization.
 * @property name The organization's name.
 * @property type The type of organization (Association, StudentClub, Company, NGO, Other).
 * @property description Optional description of the organization.
 * @property logoUrl Optional URL to the organization's logo (in Firebase Storage).
 * @property bannerUrl Optional URL to the organization's banner image (in Firebase Storage).
 * @property location Optional location of the organization.
 * @property mainDomains List of main domains/interests (max 3).
 * @property ageRanges List of age ranges the organization targets.
 * @property typicalEventSize Optional typical event size description.
 * @property roles List of roles within the organization.
 * @property socialLinks Social media and web links for the organization.
 * @property createdAt Timestamp when the organization was created.
 * @property createdBy User ID of the creator.
 * @property members List of user IDs who are members of the organization.
 */
data class OrganizationModel(
    val id: String,
    val name: String,
    val type: OrganizationType,
    val description: String? = null,
    val logoUrl: String? = null,
    val bannerUrl: String? = null,
    val location: String? = null,
    val mainDomains: List<String> = emptyList(),
    val ageRanges: List<String> = emptyList(),
    val typicalEventSize: String? = null,
    val roles: List<OrganizationRole> = emptyList(),
    val socialLinks: SocialLinks = SocialLinks(),
    val createdAt: Timestamp,
    val createdBy: String,
    val members: List<String> = emptyList()
) {
  init {
    require(id.isNotBlank()) { "Organization ID cannot be blank" }
    require(name.isNotBlank()) { "Organization name cannot be blank" }
    require(name.length <= MAX_NAME_LENGTH) {
      "Organization name cannot exceed $MAX_NAME_LENGTH characters"
    }
    description?.let {
      require(it.length <= MAX_DESCRIPTION_LENGTH) {
        "Organization description cannot exceed $MAX_DESCRIPTION_LENGTH characters"
      }
    }
    require(mainDomains.size <= MAX_MAIN_DOMAINS) {
      "Main domains cannot exceed $MAX_MAIN_DOMAINS items"
    }
    mainDomains.forEach { domain ->
      require(domain.isNotBlank()) { "Domain cannot be blank" }
      require(domain.length <= MAX_DOMAIN_LENGTH) {
        "Domain cannot exceed $MAX_DOMAIN_LENGTH characters"
      }
    }
    ageRanges.forEach { range ->
      require(range.isNotBlank()) { "Age range cannot be blank" }
      require(range.length <= MAX_AGE_RANGE_LENGTH) {
        "Age range cannot exceed $MAX_AGE_RANGE_LENGTH characters"
      }
    }
    typicalEventSize?.let {
      require(it.length <= MAX_EVENT_SIZE_LENGTH) {
        "Typical event size cannot exceed $MAX_EVENT_SIZE_LENGTH characters"
      }
    }
    location?.let {
      require(it.length <= MAX_LOCATION_LENGTH) {
        "Location cannot exceed $MAX_LOCATION_LENGTH characters"
      }
    }
    require(createdBy.isNotBlank()) { "Created by user ID cannot be blank" }
  }

  /**
   * Converts the OrganizationModel to a Map for Firestore storage.
   *
   * @return A map representation of the OrganizationModel.
   */
  fun toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "type" to type.name,
        "description" to description,
        "logoUrl" to logoUrl,
        "bannerUrl" to bannerUrl,
        "location" to location,
        "mainDomains" to mainDomains,
        "ageRanges" to ageRanges,
        "typicalEventSize" to typicalEventSize,
        "roles" to roles.map { role ->
          mapOf(
              "name" to role.name,
              "description" to role.description)
        },
        "socialLinks" to
            mapOf(
                "website" to socialLinks.website,
                "instagram" to socialLinks.instagram,
                "x" to socialLinks.x,
                "linkedin" to socialLinks.linkedin),
        "createdAt" to createdAt,
        "createdBy" to createdBy,
        "members" to members)
  }

  companion object {
    const val MAX_NAME_LENGTH = 200
    const val MAX_DESCRIPTION_LENGTH = 2000
    const val MAX_MAIN_DOMAINS = 3
    const val MAX_DOMAIN_LENGTH = 100
    const val MAX_AGE_RANGE_LENGTH = 50
    const val MAX_EVENT_SIZE_LENGTH = 100
    const val MAX_LOCATION_LENGTH = 200

    /**
     * Creates an OrganizationModel instance from a Map (typically from Firestore).
     *
     * @param map The map containing organization data.
     * @return An OrganizationModel instance, or null if the map is invalid.
     */
    fun fromMap(map: Map<String, Any?>): OrganizationModel? {
      return try {
        val id = map["id"] as? String ?: return null
        val name = map["name"] as? String ?: return null
        val typeString = map["type"] as? String ?: return null
        val type =
            try {
              OrganizationType.valueOf(typeString)
            } catch (e: IllegalArgumentException) {
              return null
            }
        val description = map["description"] as? String
        val logoUrl = map["logoUrl"] as? String
        val bannerUrl = map["bannerUrl"] as? String
        val location = map["location"] as? String
        val mainDomains =
            (map["mainDomains"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val ageRanges =
            (map["ageRanges"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val typicalEventSize = map["typicalEventSize"] as? String
        val rolesList = (map["roles"] as? List<*>) ?: emptyList()
        val roles =
            rolesList.mapNotNull { roleMap ->
              val roleData = roleMap as? Map<*, *> ?: return@mapNotNull null
              val roleName = roleData["name"] as? String ?: return@mapNotNull null
              val roleDescription = roleData["description"] as? String
              OrganizationRole(name = roleName, description = roleDescription)
            }
        val socialLinksMap = map["socialLinks"] as? Map<*, *>
        val socialLinks =
            SocialLinks(
                website = socialLinksMap?.get("website") as? String,
                instagram = socialLinksMap?.get("instagram") as? String,
                x = socialLinksMap?.get("x") as? String,
                linkedin = socialLinksMap?.get("linkedin") as? String)
        val createdAt = map["createdAt"] as? Timestamp ?: return null
        val createdBy = map["createdBy"] as? String ?: return null
        val members =
            (map["members"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        OrganizationModel(
            id = id,
            name = name,
            type = type,
            description = description,
            logoUrl = logoUrl,
            bannerUrl = bannerUrl,
            location = location,
            mainDomains = mainDomains,
            ageRanges = ageRanges,
            typicalEventSize = typicalEventSize,
            roles = roles,
            socialLinks = socialLinks,
            createdAt = createdAt,
            createdBy = createdBy,
            members = members)
      } catch (e: Exception) {
        null
      }
    }
  }
}

