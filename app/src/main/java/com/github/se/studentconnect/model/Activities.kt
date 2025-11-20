package com.github.se.studentconnect.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.se.studentconnect.R

/**
 * Represents a unified category that can be used for both user activities and organization domains.
 *
 * @property key Unique identifier for the category (e.g., "Sports", "social")
 * @property label Display name for the category (used for activity filters)
 * @property labelRes String resource ID for the category label (used for organization domains)
 * @property icon Icon to display for the category
 * @property isActivityCategory Whether this category is used for user activity filtering
 */
data class Category(
    val key: String,
    val label: String,
    @StringRes val labelRes: Int? = null,
    val icon: ImageVector,
    val isActivityCategory: Boolean = true
)

/**
 * Central source of truth for all available activities/hobbies and organization domains in the app.
 *
 * Used in:
 * - Sign-up flow (ExperiencesScreen)
 * - Profile editing (EditActivitiesScreen)
 * - Organization profile setup (OrganizationProfileSetupScreen)
 * - Event filtering (FilterBar)
 */
object Activities {

  /**
   * All available categories, including both activity categories and organization-specific domains.
   * This is the unified source of truth for both user interests and organization focus areas.
   */
  private val allCategories: List<Category> =
      listOf(
          // Activity categories (used for user interests)
          // Note: Keys use plural form ("Sports") to match filterOptions, but labelRes uses singular ("Sport") for org domains
          Category("Sports", "Sports", R.string.domain_sport, Icons.Outlined.SportsScore, isActivityCategory = true),
          Category("Science", "Science", R.string.domain_science, Icons.Outlined.Science, isActivityCategory = true),
          Category("Music", "Music", R.string.domain_music, Icons.Outlined.LibraryMusic, isActivityCategory = true),
          Category("Language", "Language", null, Icons.Outlined.Language, isActivityCategory = true),
          Category("Art", "Art", null, Icons.Outlined.Brush, isActivityCategory = true),
          Category("Tech", "Tech", R.string.domain_tech, Icons.Outlined.Memory, isActivityCategory = true),
          // Organization-specific domains
          Category("social", "Social", R.string.domain_social, Icons.Outlined.PeopleAlt, isActivityCategory = false),
          Category("culture", "Culture", R.string.domain_culture, Icons.Outlined.StarBorder, isActivityCategory = false),
          Category("career", "Career", R.string.domain_career, Icons.Outlined.WorkOutline, isActivityCategory = false),
          Category("parties", "Parties", R.string.domain_parties, Icons.Outlined.Celebration, isActivityCategory = false),
          Category("other", "Other", R.string.domain_other, Icons.Outlined.MoreHoriz, isActivityCategory = false))

  /** Filter options for categorizing activities (backward compatible - returns list of strings) */
  val filterOptions: List<String> = allCategories.filter { it.isActivityCategory }.map { it.key }

  /** Map of activity categories to their respective activities */
  val experienceTopics =
      mapOf(
          "Sports" to
              listOf(
                  "Bowling",
                  "Football",
                  "Tennis",
                  "Squatch",
                  "Running",
                  "Cycling",
                  "Volleyball",
                  "Baseball",
                  "Climbing",
                  "Rowing",
                  "Rugby",
                  "Hockey",
                  "MMA"),
          "Science" to
              listOf(
                  "Astronomy",
                  "Biology",
                  "Chemistry",
                  "Physics",
                  "Robotics",
                  "Ecology",
                  "Genetics",
                  "Medicine",
                  "Research",
                  "Space",
                  "Ocean",
                  "Energy",
                  "Climate",
                  "Geology",
                  "Neuro-sci"),
          "Music" to
              listOf(
                  "Choir",
                  "Guitar",
                  "Piano",
                  "Jazz",
                  "Drums",
                  "Violin",
                  "DJing",
                  "Theory",
                  "Opera",
                  "Bands",
                  "Compose",
                  "Recording",
              ),
          "Language" to
              listOf(
                  "Spanish",
                  "French",
                  "German",
                  "Japanese",
                  "Mandarin",
                  "Italian",
                  "Arabic",
                  "Russian",
                  "Korean",
                  "Hindi",
                  "Greek",
                  "Dutch",
                  "Swedish",
                  "Finnish"),
          "Art" to
              listOf(
                  "Painting",
                  "Photo",
                  "Design",
                  "Theatre",
                  "Dance",
                  "Sculpture",
                  "Animation",
                  "Film",
                  "Crafts",
                  "Fashion",
                  "Architecture",
                  "Ceramics"),
          "Tech" to
              listOf(
                  "AI",
                  "Web",
                  "Mobile",
                  "Cybersecurity",
                  "AR/VR",
                  "Cloud",
                  "IoT",
                  "Data",
                  "Blockchain",
                  "Robotics",
                  "Edge",
                  "DevOps",
                  "GameDev",
                  "Hardware",
                  "ML"))

  /** All activities as a flat, sorted list */
  val allActivities: List<String> by lazy { experienceTopics.values.flatten().distinct().sorted() }

  /**
   * All available categories for organization domains.
   * Includes both activity categories and organization-specific domains.
   * This unified list allows organizations to select from the same categories that users use for activities.
   */
  val domainOptions: List<Category> = allCategories
}
