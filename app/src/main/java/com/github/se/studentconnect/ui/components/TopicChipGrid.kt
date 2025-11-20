package com.github.se.studentconnect.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.resources.Variables

private val DefaultTopicChipWidth = 100.dp
private const val DefaultMaxColumns = 3
private val DefaultHorizontalSpacing = 16.dp
private val DefaultVerticalSpacing = 16.dp

object TopicChipGridTestTags {
  const val FILTER_ROW = "topicChipGrid_filterRow"
  const val FILTER_CHIP_PREFIX = "topicChipGrid_filterChip"
  const val TAG_CHIP_PREFIX = "topicChipGrid_tagChip"
}

/**
 * A selectable topic grid with optional category filters, mirroring the sign-up UI.
 *
 * @param tags Tags rendered as chips for the currently selected category
 * @param selectedTags Set of tags that should appear as selected
 * @param onTagToggle Callback invoked whenever a tag chip is tapped
 * @param modifier Modifier applied to the root column hosting the selector
 * @param filterOptions Optional list of category filters rendered as pill chips
 * @param selectedFilter Currently selected category filter
 * @param onFilterSelected Callback fired when a category filter is tapped
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicChipGrid(
    tags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    filterOptions: List<String> = emptyList(),
    selectedFilter: String? = null,
    onFilterSelected: ((String) -> Unit)? = null,
) {
  Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    val showFilters =
        filterOptions.isNotEmpty() && selectedFilter != null && onFilterSelected != null
    if (showFilters) {
      val rowModifier =
          Modifier.fillMaxWidth().semantics { testTag = TopicChipGridTestTags.FILTER_ROW }
      LazyRow(
          modifier = rowModifier,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 8.dp)) {
            items(filterOptions) { filter ->
              TopicFilterChip(
                  label = filter,
                  selected = filter == selectedFilter,
                  onClick = { onFilterSelected?.invoke(filter) },
                  modifier =
                      Modifier.semantics {
                        testTag = "${TopicChipGridTestTags.FILTER_CHIP_PREFIX}_$filter"
                      })
            }
          }
      Spacer(modifier = Modifier.height(70.dp))
    }

    if (tags.isEmpty()) {
      Text(
          text = stringResource(R.string.topic_selector_empty),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
          textAlign = TextAlign.Center)
    } else {
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        FlowRow(
            horizontalArrangement =
                Arrangement.spacedBy(
                    DefaultHorizontalSpacing, alignment = Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(DefaultVerticalSpacing),
            maxItemsInEachRow = DefaultMaxColumns) {
              tags.forEach { tag ->
                TopicChip(
                    label = tag,
                    width = DefaultTopicChipWidth,
                    selected = selectedTags.contains(tag),
                    onClick = { onTagToggle(tag) },
                    modifier =
                        Modifier.semantics {
                          testTag = "${TopicChipGridTestTags.TAG_CHIP_PREFIX}_$tag"
                        })
              }
            }
      }
    }
  }
}

private val RoundedChipShape = RoundedCornerShape(22.dp)

/**
 * Visual representation of a selectable topic chip used both in sign-up and event creation.
 *
 * @param label Text displayed inside the chip
 * @param width Width applied to the chip
 * @param selected Whether the chip should render in the selected state
 * @param onClick Callback invoked on chip press
 * @param modifier Modifier applied to the chip surface
 * @param shape Shape of the chip surface
 */
@Composable
fun TopicChip(
    label: String,
    width: Dp = DefaultTopicChipWidth,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedChipShape
) {
  val targetBackground =
      if (selected) MaterialTheme.colorScheme.primaryContainer else Variables.TopicChipBackground
  val targetContent =
      if (selected) MaterialTheme.colorScheme.onPrimaryContainer else Variables.TopicChipContent
  val targetBorder =
      if (selected) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

  val backgroundColor by animateColorAsState(targetValue = targetBackground, label = "topicChipBg")
  val contentColor by animateColorAsState(targetValue = targetContent, label = "topicChipFg")
  val borderColor by animateColorAsState(targetValue = targetBorder, label = "topicChipBorder")

  val borderStroke =
      if (selected) BorderStroke(2.dp, borderColor) else BorderStroke(1.dp, borderColor)

  Surface(
      onClick = onClick,
      color = backgroundColor,
      contentColor = contentColor,
      shape = shape,
      border = borderStroke,
      modifier = modifier.width(width)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            textAlign = TextAlign.Center,
            color = contentColor)
      }
}

/**
 * Rounded filter chip used to select activity categories.
 *
 * @param label Category name displayed in the chip
 * @param selected Whether the filter is currently active
 * @param onClick Callback invoked when the chip is tapped
 * @param icon Optional composable icon to display alongside the label.
 * @param modifier Modifier applied to the chip surface
 */
@Composable
fun TopicFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
  val contentColor = if (selected) Color.White else Variables.FilterChipContent
  val backgroundColor = if (selected) Variables.ColorOnClick else Variables.FilterChipBackground

  Surface(
      onClick = onClick,
      color = backgroundColor,
      contentColor = contentColor,
      shape = RoundedCornerShape(24.dp),
      modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically) {
              if (icon != null) {
                icon()
              } else {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor)
              }
              Text(text = label, style = MaterialTheme.typography.labelLarge, color = contentColor)
            }
      }
}
