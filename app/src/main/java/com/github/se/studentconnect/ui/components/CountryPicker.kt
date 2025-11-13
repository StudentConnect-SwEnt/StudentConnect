package com.github.se.studentconnect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

// Constants
private val RowHorizontalPadding = 16.dp
private val RowVerticalPadding = 12.dp
private val CountryRowHorizontalPadding = 12.dp
private val CountryFlagSize = 40.dp
private val SelectedBackgroundAlpha = 0.12f
private val SelectedBorderAlpha = 0.4f
private val FlagCircleAlpha = 0.15f

/** Data class representing a country. */
data class Country(val code: String, val name: String, val flag: String)

/**
 * Surface containing a scrollable list of countries.
 *
 * @param filteredCountries List of countries to display
 * @param selectedCode Currently selected country code
 * @param onCountrySelect Callback when a country is selected
 * @param modifier Modifier for the surface
 */
@Composable
fun CountryListSurface(
    filteredCountries: List<Country>,
    selectedCode: String?,
    onCountrySelect: (Country) -> Unit,
    modifier: Modifier = Modifier
) {
  Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
      border =
          BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
      modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              items(filteredCountries) { country ->
                CountryRow(
                    country = country,
                    isSelected = selectedCode == country.code,
                    onSelect = { onCountrySelect(country) })
              }
            }
      }
}

/**
 * Single row representing a country in the list.
 *
 * @param country Country to display
 * @param isSelected Whether this country is currently selected
 * @param onSelect Callback when this country is selected
 */
@Composable
fun CountryRow(country: Country, isSelected: Boolean, onSelect: () -> Unit) {
  val (background, border) = getCountryRowColors(isSelected)
  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = CountryRowHorizontalPadding)
              .clickable(onClick = onSelect),
      color = background,
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(width = if (isSelected) 1.dp else 0.dp, color = border)) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = RowHorizontalPadding, vertical = RowVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RowHorizontalPadding)) {
              Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = FlagCircleAlpha),
                  modifier = Modifier.size(CountryFlagSize)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      Text(
                          text = country.flag,
                          style = MaterialTheme.typography.headlineSmall,
                          maxLines = 1)
                    }
                  }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style =
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
              }
            }
      }
}

@Composable
private fun getCountryRowColors(isSelected: Boolean): Pair<Color, Color> {
  val background =
      if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = SelectedBackgroundAlpha)
      else Color.Transparent
  val border =
      if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = SelectedBorderAlpha)
      else Color.Transparent
  return background to border
}

/**
 * Filters countries based on a search query.
 *
 * @param query Search query
 * @param countries List of countries to filter
 * @return Filtered list of countries
 */
fun filterCountries(query: String, countries: List<Country>): List<Country> {
  val trimmed = query.trim()
  return if (trimmed.isBlank()) countries
  else countries.filter { it.name.startsWith(trimmed, ignoreCase = true) }
}

/**
 * Loads all available countries from the system locale.
 *
 * @return List of countries sorted by name
 */
fun loadCountries(): List<Country> {
  return Locale.getISOCountries()
      .map { iso ->
        val locale = Locale("", iso)
        val name = locale.getDisplayCountry(Locale.US)
        Country(code = iso, name = name, flag = countryCodeToEmoji(iso))
      }
      .sortedBy { it.name }
}

/**
 * Converts a country code to its emoji flag representation.
 *
 * @param countryCode ISO country code
 * @return Emoji flag or globe icon if invalid
 */
private fun countryCodeToEmoji(countryCode: String): String {
  val normalized = countryCode.uppercase(Locale.US)
  if (normalized.length != 2 || normalized.any { it !in 'A'..'Z' }) return "🌐"
  val first = normalized[0].code - 'A'.code + 0x1F1E6
  val second = normalized[1].code - 'A'.code + 0x1F1E6
  return String(Character.toChars(first)) + String(Character.toChars(second))
}
