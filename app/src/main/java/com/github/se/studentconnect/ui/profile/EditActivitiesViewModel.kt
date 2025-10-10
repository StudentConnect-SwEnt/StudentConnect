package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import kotlin.collections.filter
import kotlin.collections.sorted
import kotlin.collections.toList
import kotlin.collections.toMutableSet
import kotlin.collections.toSet
import kotlin.text.contains
import kotlin.text.isBlank
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for EditActivitiesScreen. Handles activity selection, search, and Firebase updates. */
class EditActivitiesViewModel(
    private val userRepository: UserRepository,
    private val userId: String
) : ViewModel() {

  // User data state
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  // Available activities
  private val _availableActivities = MutableStateFlow<List<String>>(emptyList())
  val availableActivities: StateFlow<List<String>> = _availableActivities.asStateFlow()

  // Selected activities
  private val _selectedActivities = MutableStateFlow<Set<String>>(emptySet())
  val selectedActivities: StateFlow<Set<String>> = _selectedActivities.asStateFlow()

  // Search query
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // Filtered activities based on search
  private val _filteredActivities = MutableStateFlow<List<String>>(emptyList())
  val filteredActivities: StateFlow<List<String>> = _filteredActivities.asStateFlow()

  // Loading state
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Error state
  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  // Success state
  private val _successMessage = MutableStateFlow<String?>(null)
  val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

  init {
    loadUserProfile()
    initializeAvailableActivities()
  }

  /** Loads the current user's profile from Firebase. */
  private fun loadUserProfile() {
    viewModelScope.launch {
      userRepository.getUserById(
          userId = userId,
          onSuccess = { user ->
            _user.value = user
            _selectedActivities.value = (user?.hobbies ?: emptyList()).toSet()
          },
          onFailure = { exception ->
            _errorMessage.value = exception.message ?: "Failed to load profile"
          })
    }
  }

  /** Initializes the list of available activities. */
  private fun initializeAvailableActivities() {
    val activities =
        listOf(
            // Sports
            "Football",
            "Basketball",
            "Tennis",
            "Swimming",
            "Hiking",
            "Running",
            "Cycling",
            "Volleyball",
            "Badminton",
            "Table Tennis",
            "Soccer",
            "Rugby",
            "Cricket",
            "Golf",
            "Skiing",
            "Snowboarding",
            "Ice Skating",
            "Rock Climbing",
            "Yoga",
            "Pilates",
            "Martial Arts",
            "Boxing",
            "Weightlifting",
            "CrossFit",

            // Arts
            "Painting",
            "Photography",
            "Music",
            "Theater",
            "Dance",
            "Drawing",
            "Sculpture",
            "Digital Art",
            "Graphic Design",
            "Film Making",
            "Writing",
            "Poetry",
            "Creative Writing",
            "Journalism",
            "Blogging",
            "Podcasting",

            // Technology
            "Coding",
            "Gaming",
            "Robotics",
            "AI/ML",
            "Web Development",
            "Mobile Development",
            "Data Science",
            "Cybersecurity",
            "Blockchain",
            "IoT",
            "AR/VR",
            "Game Development",
            "Software Engineering",
            "UI/UX Design",

            // Social
            "Volunteering",
            "Debate",
            "Networking",
            "Travel",
            "Language Exchange",
            "Cultural Exchange",
            "Community Service",
            "Mentoring",
            "Public Speaking",
            "Event Planning",
            "Social Media",
            "Content Creation",

            // Academic
            "Reading",
            "Research",
            "Languages",
            "Mathematics",
            "Physics",
            "Chemistry",
            "Biology",
            "History",
            "Philosophy",
            "Economics",
            "Psychology",
            "Sociology",
            "Literature",
            "Linguistics",
            "Anthropology",
            "Political Science",

            // Other
            "Cooking",
            "Gardening",
            "Fashion",
            "Interior Design",
            "DIY Projects",
            "Board Games",
            "Chess",
            "Puzzles",
            "Collecting",
            "Antiques",
            "Wine Tasting",
            "Coffee Brewing",
            "Baking",
            "Mixology")

    _availableActivities.value = activities.sorted()
    _filteredActivities.value = activities.sorted()
  }

  /**
   * Updates the search query and filters activities.
   *
   * @param query The search query
   */
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
    filterActivities(query)
  }

  /**
   * Filters activities based on search query.
   *
   * @param query The search query
   */
  private fun filterActivities(query: String) {
    val filtered =
        if (query.isBlank()) {
          _availableActivities.value
        } else {
          _availableActivities.value.filter { activity ->
            activity.contains(query, ignoreCase = true)
          }
        }
    _filteredActivities.value = filtered
  }

  /**
   * Toggles the selection of an activity.
   *
   * @param activity The activity to toggle
   */
  fun toggleActivitySelection(activity: String) {
    val currentSelection = _selectedActivities.value.toMutableSet()
    if (currentSelection.contains(activity)) {
      currentSelection.remove(activity)
    } else {
      currentSelection.add(activity)
    }
    _selectedActivities.value = currentSelection
  }

  /**
   * Checks if an activity is selected.
   *
   * @param activity The activity to check
   * @return True if the activity is selected
   */
  fun isActivitySelected(activity: String): Boolean {
    return _selectedActivities.value.contains(activity)
  }

  /** Saves the selected activities to Firebase. */
  fun saveActivities() {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        _errorMessage.value = null

        val currentUser = _user.value ?: throw kotlin.Exception("User not found")
        val activitiesList = _selectedActivities.value.toList()
        val updatedUser = currentUser.update(hobbies = User.UpdateValue.SetValue(activitiesList))

        // Save to Firestore
        userRepository.saveUser(
            user = updatedUser,
            onSuccess = {
              _user.value = updatedUser
              _isLoading.value = false
              _successMessage.value = "Activities updated successfully!"
            },
            onFailure = { exception ->
              _isLoading.value = false
              _errorMessage.value = exception.message ?: "Failed to update activities"
            })
      } catch (e: Exception) {
        _isLoading.value = false
        _errorMessage.value = e.message ?: "Failed to update activities"
      }
    }
  }

  /**
   * Gets the number of selected activities.
   *
   * @return The count of selected activities
   */
  fun getSelectedCount(): Int {
    return _selectedActivities.value.size
  }

  /** Clears error message. */
  fun clearErrorMessage() {
    _errorMessage.value = null
  }

  /** Clears success message. */
  fun clearSuccessMessage() {
    _successMessage.value = null
  }
}
