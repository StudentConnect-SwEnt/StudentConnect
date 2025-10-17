package com.github.se.studentconnect.ui.utils

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.screen.signup.experienceTopics
import com.github.se.studentconnect.ui.screen.signup.filterOptions
import com.github.se.studentconnect.ui.utils.DialogNotImplemented
import kotlinx.coroutines.launch

data class FilterData(
    val categories: List<String>,
    val location: Location?,
    val radiusKm: Float,
    val priceRange: ClosedFloatingPointRange<Float>,
    val showOnlyFavorites: Boolean
)

// Constants for filter sliders to keep ranges consistent and maintainable
private const val MIN_RADIUS = 1f
private const val MAX_RADIUS = 100f
private const val RADIUS_STEPS = 99

private const val MIN_PRICE = 0f
private const val MAX_PRICE = 200f
private const val PRICE_STEPS = 199

private const val DEFAULT_RADIUS = 10f
private val DEFAULT_PRICE_RANGE: ClosedFloatingPointRange<Float> = 0f..50f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBar(
    context: Context,
    onCalendarClick: () -> Unit = { DialogNotImplemented(context) },
    onApplyFilters: (FilterData) -> Unit = {}
) {
  var showBottomSheet by remember { mutableStateOf(false) }
  var showLocationPicker by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  val availableCategories = remember { filterOptions }
  val selectedFilters = remember { mutableStateListOf<String>() }
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  var searchRadius by remember { mutableFloatStateOf(DEFAULT_RADIUS) }
  var priceRange by remember { mutableStateOf(DEFAULT_PRICE_RANGE) }

  var showOnlyFavorites by remember { mutableStateOf(false) }

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        FilterChip(
            text = "Paris",
            onClick = { DialogNotImplemented(context) },
            icon = R.drawable.ic_location)
        FilterChip(
            icon = R.drawable.ic_calendar,
            onClick = onCalendarClick,
            testTag = "calendar_button")
        FilterChip(
            text = "Filters",
            icon = R.drawable.ic_filter,
            onClick = { showBottomSheet = true })
        FilterChipWithHighlight(
            text = "Favorites",
            icon = R.drawable.ic_heart,
            isSelected = showOnlyFavorites,
            onClick = {
              showOnlyFavorites = !showOnlyFavorites
              val currentFilterData =
                  FilterData(
                      categories = selectedFilters.toList(),
                      location = selectedLocation,
                      radiusKm = searchRadius,
                      priceRange = priceRange,
                      showOnlyFavorites = showOnlyFavorites)
              onApplyFilters(currentFilterData)
            })
      }

  if (showBottomSheet) {
    ModalBottomSheet(
        onDismissRequest = { showBottomSheet = false },
        sheetState = sheetState,
    ) {
      val scrollState = rememberScrollState()
      Column(
          modifier =
              Modifier.fillMaxWidth()
                  .height(LocalConfiguration.current.screenHeightDp.dp * 0.75f)
                  .padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                  Text("Filter Events", style = MaterialTheme.typography.headlineSmall)
                  Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Close Filters",
                      modifier =
                          Modifier.size(24.dp).clickable {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion {
                                  if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                  }
                                }
                          })
                }
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .testTag("filter_bottom_sheet_scroll")) {
                  Text("Categories & Tags", style = MaterialTheme.typography.titleMedium)
                  Spacer(modifier = Modifier.height(8.dp))
                  FlowRow(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableCategories.forEach { category ->
                          Column {
                            val isCategorySelected = category in selectedFilters
                            SelectableChip(
                                text = category,
                                isSelected = isCategorySelected,
                                onClick = {
                                  if (isCategorySelected) {
                                    selectedFilters.remove(category)
                                    experienceTopics[category]?.forEach { topic ->
                                      selectedFilters.remove(topic)
                                    }
                                  } else {
                                    selectedFilters.add(category)
                                  }
                                })
                            AnimatedVisibility(
                                visible = isCategorySelected, enter = fadeIn(), exit = fadeOut()) {
                                  val topics = experienceTopics[category] ?: emptyList()
                                  FlowRow(
                                      modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                                      verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        topics.take(8).forEach { topic ->
                                          TopicMiniChip(
                                              text = topic,
                                              isSelected = topic in selectedFilters,
                                              onClick = {
                                                if (topic in selectedFilters) {
                                                  selectedFilters.remove(topic)
                                                } else {
                                                  selectedFilters.add(topic)
                                                }
                                              })
                                        }
                                      }
                                }
                          }
                        }
                      }
                  Spacer(modifier = Modifier.height(24.dp))

                  Text("Location", style = MaterialTheme.typography.titleMedium)
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { showLocationPicker = true }) {
                          Text(selectedLocation?.name ?: "Select Location")
                        }
                      }
                  Text(
                      "Radius: ${searchRadius.toInt()} km",
                      modifier = Modifier.padding(top = 8.dp),
                      color =
                          if (selectedLocation == null)
                              MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                          else MaterialTheme.colorScheme.onSurface)
                  Slider(
                      value = searchRadius,
                      onValueChange = { searchRadius = it },
                      valueRange = MIN_RADIUS..MAX_RADIUS,
                      steps = RADIUS_STEPS,
                      modifier = Modifier.fillMaxWidth(),
                      enabled = selectedLocation != null)
                  Spacer(modifier = Modifier.height(24.dp))

                  Text("Price (€)", style = MaterialTheme.typography.titleMedium)
                  Spacer(modifier = Modifier.height(8.dp))
                  RangeSlider(
                      value = priceRange,
                      onValueChange = { priceRange = it },
                      valueRange = MIN_PRICE..MAX_PRICE,
                      steps = PRICE_STEPS,
                      onValueChangeFinished = {},
                      modifier = Modifier.fillMaxWidth())
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Min: ${priceRange.start.toInt()}€")
                        Text("Max: ${priceRange.endInclusive.toInt()}€")
                      }
                  Spacer(modifier = Modifier.height(16.dp))
                }

            Button(
                onClick = {
                  val filters =
                      FilterData(
                          categories = selectedFilters.toList(),
                          location = selectedLocation,
                          radiusKm = searchRadius,
                          priceRange = priceRange,
                          showOnlyFavorites = showOnlyFavorites)
                  onApplyFilters(filters)
                  scope
                      .launch { sheetState.hide() }
                      .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                          showBottomSheet = false
                        }
                      }
                },
                modifier = Modifier.fillMaxWidth()) {
                  Text("Apply Filters")
                }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                  selectedFilters.clear()
                  selectedLocation = null
                  searchRadius = DEFAULT_RADIUS
                  priceRange = DEFAULT_PRICE_RANGE
                  onApplyFilters(
                      FilterData(
                          emptyList(),
                          null,
                          DEFAULT_RADIUS,
                          DEFAULT_PRICE_RANGE,
                          showOnlyFavorites))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors()) {
                  Text("Reset Filters")
                }
          }
      }
      if (showLocationPicker) {
        // LocationPickerDialog - commented out as it may not exist
        // LocationPickerDialog(
        //     initialLocation = selectedLocation,
        //     initialRadius = searchRadius,
        //     onDismiss = { showLocationPicker = false },
        //     onLocationSelected = { newLocation, newRadius ->
        //       selectedLocation = newLocation
        //       searchRadius = newRadius
        //       showLocationPicker = false
        //     })
        DialogNotImplemented(context)
        showLocationPicker = false
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipWithHighlight(
    onClick: () -> Unit,
    icon: Int,
    text: String? = null,
    isSelected: Boolean
) {
  val targetBackgroundColor by
      animateColorAsState(
          targetValue =
              if (isSelected) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          label = "chipBgColor")
  val targetContentColor by
      animateColorAsState(
          targetValue =
              if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
          label = "chipContentColor")

  Surface(
      onClick = onClick,
      shape = RoundedCornerShape(24.dp),
      color = targetBackgroundColor,
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Icon(
              painter = painterResource(icon),
              contentDescription = text,
              tint = targetContentColor,
              modifier = Modifier.size(18.dp),
          )
          if (text != null) {
            Text(
                text = text,
                color = targetContentColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
          }
        }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FilterChip(
    onClick: () -> Unit,
    icon: Int,
    text: String? = null,
    testTag: String? = null
) {
  Surface(
      onClick = onClick,
      modifier = testTag?.let { Modifier.testTag(it) } ?: Modifier,
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Icon(
                  painter = painterResource(icon),
                  contentDescription = text,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
              )
              if (text != null) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp)
              }
            }
      }
}

@Composable
fun SelectableChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
  Surface(
      onClick = onClick,
      shape = RoundedCornerShape(24.dp),
      color =
          if (isSelected) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.surfaceVariant,
      contentColor =
          if (isSelected) MaterialTheme.colorScheme.onPrimary
          else MaterialTheme.colorScheme.onSurfaceVariant,
      border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall)
      }
}

@Composable
fun TopicMiniChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
  Surface(
      onClick = onClick,
      shape = RoundedCornerShape(16.dp),
      color =
          if (isSelected) MaterialTheme.colorScheme.tertiaryContainer
          else MaterialTheme.colorScheme.surfaceVariant,
      contentColor =
          if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer
          else MaterialTheme.colorScheme.onSurfaceVariant,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall)
      }
}
