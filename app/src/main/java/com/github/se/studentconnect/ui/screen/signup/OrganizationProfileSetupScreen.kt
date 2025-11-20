package com.github.se.studentconnect.ui.screen.signup

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.Activities
import com.github.se.studentconnect.ui.theme.AppTheme

private data class SimpleOption(val key: String, @StringRes val labelRes: Int)

private val ageRangeOptions =
    listOf(
        SimpleOption("under_18", R.string.age_range_under_18),
        SimpleOption("18_22", R.string.age_range_18_22),
        SimpleOption("23_25", R.string.age_range_23_25),
        SimpleOption("26_30", R.string.age_range_26_30),
        SimpleOption("30_plus", R.string.age_range_30_plus))

private val eventSizeOptions =
    listOf(
        SimpleOption("small", R.string.event_size_small),
        SimpleOption("medium", R.string.event_size_medium),
        SimpleOption("large", R.string.event_size_large),
        SimpleOption("xlarge", R.string.event_size_extra_large))

private val locationOptions =
    listOf(
        R.string.org_location_epfl,
        R.string.org_location_ethz,
        R.string.org_location_unige,
        R.string.org_location_unil,
        R.string.org_location_unibe,
        R.string.org_location_uzh,
        R.string.org_location_unibasel,
        R.string.org_location_hsg,
        R.string.org_location_lausanne,
        R.string.org_location_geneva,
        R.string.org_location_zurich,
        R.string.org_location_bern,
        R.string.org_location_basel,
        R.string.org_location_lugano,
        R.string.org_location_lucerne,
        R.string.org_location_neuchatel,
        R.string.org_location_fribourg,
        R.string.org_location_winterthur,
        R.string.org_location_other)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizationProfileSetupScreen(
    onBack: () -> Unit,
    onStartNow: () -> Unit,
    modifier: Modifier = Modifier
) {
  var selectedLocation by rememberSaveable { mutableStateOf("") }
  var isDropdownExpanded by remember { mutableStateOf(false) }
  var selectedDomains by rememberSaveable { mutableStateOf(listOf<String>()) }
  var selectedAgeRanges by rememberSaveable { mutableStateOf(listOf<String>()) }
  var selectedEventSize by rememberSaveable { mutableStateOf<String?>(null) }

  val locationLabels = locationOptions.map { stringResource(id = it) }

  val isFormValid =
      selectedLocation.isNotBlank() && selectedDomains.isNotEmpty() && selectedEventSize != null

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surface)
              .padding(
                  horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                  vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING)) {
        SignUpBackButton(onClick = onBack)

        SignUpLargeSpacer()

        SignUpTitle(text = stringResource(R.string.org_setup_title))

        SignUpSmallSpacer()

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement =
                Arrangement.spacedBy(SignUpScreenConstants.HEADER_TO_TITLE_SPACING)) {
              FormSectionLabel(text = stringResource(R.string.org_setup_main_location_label))
              LocationDropdownField(
                  selectedValue = selectedLocation,
                  placeholder = stringResource(R.string.org_setup_main_location_placeholder),
                  options = locationLabels,
                  expanded = isDropdownExpanded,
                  onExpandedChange = { isDropdownExpanded = it },
                  onOptionSelected = {
                    selectedLocation = it
                    isDropdownExpanded = false
                  })

              FormSectionLabel(text = stringResource(R.string.org_setup_main_domains_label))
              Text(
                  text = stringResource(R.string.org_setup_main_domains_hint),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
              FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(SignUpScreenConstants.ICON_SPACING),
                  verticalArrangement = Arrangement.spacedBy(SignUpScreenConstants.ICON_SPACING)) {
                    Activities.domainOptions.forEach { category ->
                      val isSelected = category.key in selectedDomains
                      val canSelectMore = isSelected || selectedDomains.size < MAX_DOMAIN_SELECTION
                      DomainChip(
                          text = category.labelRes?.let { stringResource(it) } ?: category.label,
                          icon = category.icon,
                          selected = isSelected,
                          enabled = canSelectMore,
                          onClick = {
                            selectedDomains =
                                if (isSelected) selectedDomains - category.key
                                else selectedDomains + category.key
                          })
                    }
                  }

              FormSectionLabel(text = stringResource(R.string.org_setup_age_range_label))
              FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(SignUpScreenConstants.ICON_SPACING),
                  verticalArrangement = Arrangement.spacedBy(SignUpScreenConstants.ICON_SPACING)) {
                    ageRangeOptions.forEach { option ->
                      val isSelected = option.key in selectedAgeRanges
                      SimpleSelectableChip(
                          text = stringResource(option.labelRes),
                          selected = isSelected,
                          onClick = {
                            selectedAgeRanges =
                                if (isSelected) selectedAgeRanges - option.key
                                else selectedAgeRanges + option.key
                          })
                    }
                  }

              FormSectionLabel(text = stringResource(R.string.org_setup_event_size_label))
              FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(SignUpScreenConstants.ICON_SPACING),
                  verticalArrangement = Arrangement.spacedBy(SignUpScreenConstants.ICON_SPACING)) {
                    eventSizeOptions.forEach { option ->
                      val isSelected = option.key == selectedEventSize
                      SimpleSelectableChip(
                          text = stringResource(option.labelRes),
                          selected = isSelected,
                          onClick = { selectedEventSize = option.key })
                    }
                  }

              Text(
                  text = stringResource(R.string.org_setup_event_size_hint),
                  style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

        SignUpLargeSpacer()

        SignUpPrimaryButton(
            text = stringResource(R.string.button_continue),
            iconRes = R.drawable.ic_arrow_forward,
            enabled = isFormValid,
            onClick = onStartNow,
            modifier = Modifier.align(Alignment.CenterHorizontally))
      }
}

@Composable
private fun LocationDropdownField(
    selectedValue: String,
    placeholder: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  val displayText = selectedValue.takeIf { it.isNotBlank() }
  val primary = MaterialTheme.colorScheme.primary
  val surface = MaterialTheme.colorScheme.surface
  val horizontalPadding = SignUpScreenConstants.BUTTON_HORIZONTAL_PADDING / 2

  Surface(
      modifier = modifier.fillMaxWidth().clickable { onExpandedChange(true) },
      shape = MaterialTheme.shapes.large,
      border = BorderStroke(OutlineWidth, primary.copy(alpha = 0.6f)),
      color = surface) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = horizontalPadding,
                        vertical = SignUpScreenConstants.BUTTON_VERTICAL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = stringResource(R.string.content_description_search),
                    tint = primary)
                Spacer(modifier = Modifier.width(SignUpScreenConstants.ICON_SPACING))
                Text(
                    text = displayText ?: placeholder,
                    color =
                        if (displayText != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge)
              }
              Icon(
                  imageVector = Icons.Outlined.ArrowDropDown,
                  contentDescription = stringResource(R.string.content_description_open_dropdown),
                  tint = primary)
            }
      }

  if (expanded) {
    Dialog(onDismissRequest = {}) {
      Surface(
          modifier =
              Modifier.fillMaxWidth()
                  .padding(horizontal = horizontalPadding)
                  .heightIn(max = DropdownMaxHeight),
          shape = MaterialTheme.shapes.extraLarge,
          tonalElevation = DropdownSurfaceElevation,
          color = MaterialTheme.colorScheme.surface) {
            LazyColumn {
              items(options) { option ->
                val isSelected = option == selectedValue
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable {
                              onOptionSelected(option)
                              onExpandedChange(false)
                            }
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = SignUpScreenConstants.BUTTON_VERTICAL_PADDING),
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = option,
                          style =
                              MaterialTheme.typography.bodyLarge.copy(
                                  fontWeight =
                                      if (isSelected) FontWeight.Bold else FontWeight.Normal),
                          color = if (isSelected) primary else MaterialTheme.colorScheme.onSurface)
                    }
              }
            }
          }
    }
  }
}

@Composable
private fun FormSectionLabel(text: String) {
  Text(
      text = text,
      style =
          MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant))
}

/**
 * Base composable for selectable chips with common styling and behavior.
 *
 * @param text The text to display in the chip
 * @param selected Whether the chip is currently selected
 * @param enabled Whether the chip can be clicked (defaults to true)
 * @param onClick Callback when the chip is clicked
 * @param selectedBackgroundColor Background color when selected
 * @param unselectedBackgroundColor Background color when not selected
 * @param selectedContentColor Content color when selected
 * @param unselectedContentColor Content color when not selected
 * @param content The content to display inside the chip
 */
@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    selectedBackgroundColor: androidx.compose.ui.graphics.Color,
    unselectedBackgroundColor: androidx.compose.ui.graphics.Color,
    selectedContentColor: androidx.compose.ui.graphics.Color,
    unselectedContentColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
  val primary = MaterialTheme.colorScheme.primary
  val backgroundColor by
      animateColorAsState(
          if (selected) selectedBackgroundColor else unselectedBackgroundColor,
          label = "chipBackground")
  val contentColor = if (selected) selectedContentColor else unselectedContentColor
  val chipModifier =
      Modifier.semantics(mergeDescendants = true) {
        this.selected = selected
        if (!enabled) disabled()
      }
  Surface(
      modifier = chipModifier.clickable(enabled = enabled, onClick = onClick),
      shape = MaterialTheme.shapes.large,
      color = backgroundColor,
      contentColor = contentColor,
      border =
          BorderStroke(
              width = OutlineWidth,
              color = primary.copy(alpha = if (selected) 0.7f else 0.4f))) {
        content()
      }
}

@Composable
private fun DomainChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
  SelectableChip(
      text = text,
      selected = selected,
      enabled = enabled,
      onClick = onClick,
      selectedBackgroundColor = MaterialTheme.colorScheme.primary,
      unselectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
      selectedContentColor = MaterialTheme.colorScheme.onPrimary,
      unselectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = SignUpScreenConstants.BUTTON_HORIZONTAL_PADDING / 2,
                    vertical = SignUpScreenConstants.BUTTON_VERTICAL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChipContentSpacing)) {
              Icon(
                  imageVector = icon,
                  contentDescription = null,
                  modifier = Modifier.size(SignUpScreenConstants.ICON_SIZE))
              Text(
                  text = text,
                  style =
                      MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            }
      }
}

@Composable
private fun SimpleSelectableChip(text: String, selected: Boolean, onClick: () -> Unit) {
  val contentColor =
      if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
  SelectableChip(
      text = text,
      selected = selected,
      enabled = true,
      onClick = onClick,
      selectedBackgroundColor = MaterialTheme.colorScheme.primary,
      unselectedBackgroundColor = MaterialTheme.colorScheme.surface,
      selectedContentColor = MaterialTheme.colorScheme.onPrimary,
      unselectedContentColor = MaterialTheme.colorScheme.primary) {
        Text(
            text = text,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium, color = contentColor),
            modifier =
                Modifier.padding(
                    horizontal = SignUpScreenConstants.BUTTON_HORIZONTAL_PADDING / 2,
                    vertical = SignUpScreenConstants.BUTTON_VERTICAL_PADDING))
      }
}

private const val MAX_DOMAIN_SELECTION = 3
private val DropdownMaxHeight = SignUpScreenConstants.BUTTON_HEIGHT * 6
private val DropdownSurfaceElevation = SignUpScreenConstants.BUTTON_VERTICAL_PADDING / 2
private val ChipContentSpacing = SignUpScreenConstants.ICON_SPACING * (2f / 3f)
private val OutlineWidth = 1.dp

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OrganizationProfileSetupScreenPreview() {
  AppTheme {
    OrganizationProfileSetupScreen(
        onBack = {},
        onStartNow = {}
    )
  }
}
