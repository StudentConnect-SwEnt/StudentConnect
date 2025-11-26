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
  Other;

  companion object {
    /** Safely parses a string to an OrganizationType, defaulting to Other if unknown. */
    fun fromString(value: String?): OrganizationType {
      return entries.find { it.name.equals(value, ignoreCase = true) } ?: Other
    }
  }
}

/** Represents a role within an organization. Used in the TeamRolesScreen. */
data class OrganizationRole(val name: String, val description: String? = null)

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
  fun toMap(): Map<String, String?> =
      mapOf("website" to website, "instagram" to instagram, "x" to x, "linkedin" to linkedin)
}

/**
 * Represents an Organization in the StudentConnect application.
 *
 * @property id The unique identifier for the organization (usually the Firestore Document ID).
 * @property name The organization's name.
 * @property type The type of organization.
 * @property description Optional description (bio).
 * @property logoUrl URL to the logo in Storage.
 * @property location Text representation of location (e.g. "EPFL").
 * @property mainDomains List of domains/tags (e.g., "Sports", "Music").
 * @property ageRanges List of target age groups.
 * @property typicalEventSize Description of event size (e.g., "Small", "Large").
 * @property roles List of distinct roles within the organization team.
 * @property socialLinks Container for external profile links.
 * @property createdAt Timestamp of creation.
 * @property createdBy User ID of the creator (admin).
 */
data class Organization(
    val id: String,
    val name: String,
    val type: OrganizationType,
    val description: String? = null,
    val logoUrl: String? = null,
    val location: String? = null,
    val mainDomains: List<String> = emptyList(),
    val ageRanges: List<String> = emptyList(),
    val typicalEventSize: String? = null,
    val roles: List<OrganizationRole> = emptyList(),
    val socialLinks: SocialLinks = SocialLinks(),
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String
) {

  /** Converts the model to a Map for Firestore storage. */
  fun toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "type" to type.name,
        "description" to description,
        "logoUrl" to logoUrl,
        "location" to location,
        "mainDomains" to mainDomains,
        "ageRanges" to ageRanges,
        "typicalEventSize" to typicalEventSize,
        "roles" to roles.map { mapOf("name" to it.name, "description" to it.description) },
        "socialLinks" to socialLinks.toMap(),
        "createdAt" to createdAt,
        "createdBy" to createdBy)
  }

  companion object {
    /**
     * Creates an OrganizationModel from a Firestore Map. Returns null if critical fields (id, name,
     * creator) are missing.
     */
    fun fromMap(map: Map<String, Any?>): Organization? {
      return try {
        val id = map["id"] as? String ?: return null
        val name = map["name"] as? String ?: return null
        val createdBy = map["createdBy"] as? String ?: return null

        // Safe parsing of Enum
        val typeString = map["type"] as? String
        val type = OrganizationType.fromString(typeString)

        // Safe parsing of simple fields
        val description = map["description"] as? String
        val logoUrl = map["logoUrl"] as? String
        val location = map["location"] as? String
        val typicalEventSize = map["typicalEventSize"] as? String
        val createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now()

        // Safe parsing of Lists
        @Suppress("UNCHECKED_CAST")
        val mainDomains = (map["mainDomains"] as? List<String>) ?: emptyList()

        @Suppress("UNCHECKED_CAST")
        val ageRanges = (map["ageRanges"] as? List<String>) ?: emptyList()

        // Safe parsing of Roles (List of Maps)
        val rawRoles = map["roles"] as? List<Map<String, Any?>> ?: emptyList()
        val roles =
            rawRoles.mapNotNull { roleMap ->
              val rName = roleMap["name"] as? String
              val rDesc = roleMap["description"] as? String
              if (rName != null) OrganizationRole(rName, rDesc) else null
            }

        // Safe parsing of Social Links
        val socialMap = map["socialLinks"] as? Map<String, String?>
        val socialLinks =
            SocialLinks(
                website = socialMap?.get("website"),
                instagram = socialMap?.get("instagram"),
                x = socialMap?.get("x"),
                linkedin = socialMap?.get("linkedin"))

        Organization(
            id = id,
            name = name,
            type = type,
            description = description,
            logoUrl = logoUrl,
            location = location,
            mainDomains = mainDomains,
            ageRanges = ageRanges,
            typicalEventSize = typicalEventSize,
            roles = roles,
            socialLinks = socialLinks,
            createdAt = createdAt,
            createdBy = createdBy)
      } catch (e: Exception) {
        // Log exception here if you have a crashlytics service
        null
      }
    }
  }
}
