package com.github.se.studentconnect.model.location

/** A data class representing a geographical location with latitude, longitude, and a name. */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String?, // optional name
)
