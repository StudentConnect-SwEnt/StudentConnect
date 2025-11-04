package com.github.se.studentconnect.model

/**
 * Central source of truth for all available activities/hobbies in the app.
 *
 * Used in:
 * - Sign-up flow (ExperiencesScreen)
 * - Profile editing (EditActivitiesScreen)
 */
object Activities {

  /** Filter options for categorizing activities */
  val filterOptions = listOf("Sports", "Science", "Music", "Language", "Art", "Tech")

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
}
