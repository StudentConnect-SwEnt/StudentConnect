package com.github.se.studentconnect.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.utils.loadBitmapFromOrganization

private object OrganizationSuggestionsConstants {
  // Section
  const val SECTION_PADDING_BOTTOM_DP = 12

  // Title
  const val TITLE_FONT_SIZE_SP = 22
  const val TITLE_LINE_HEIGHT_SP = 28
  const val TITLE_FONT_WEIGHT = 400

  // LazyRow
  const val ROW_PADDING_BOTTOM_DP = 16
  const val ROW_SPACING_DP = 8

  // Card
  const val CARD_WIDTH_DP = 132
  const val CARD_PADDING_DP = 8
  const val CARD_CORNER_RADIUS_DP = 16
  const val CARD_ELEVATION_DP = 0

  // Image
  const val IMAGE_ASPECT_RATIO = 116f / 119f
  const val IMAGE_CORNER_RADIUS_DP = 8
  val PLACEHOLDER_DRAWABLE = R.drawable.avatar_23

  // Organization Name
  const val NAME_FONT_SIZE_SP = 14
  const val NAME_LINE_HEIGHT_SP = 20
  const val NAME_FONT_WEIGHT = 500
  const val NAME_LETTER_SPACING_SP = 0.1f

  // Organization Handle
  const val HANDLE_FONT_SIZE_SP = 12
  const val HANDLE_LINE_HEIGHT_SP = 16
  const val HANDLE_FONT_WEIGHT = 400
  const val HANDLE_LETTER_SPACING_SP = 0.4f

  // Text spacing
  const val TEXT_SPACING_DP = 2
}

data class OrganizationData(val id: String, val name: String, val handle: String)

@Composable
fun OrganizationSuggestions(
    organizations: List<OrganizationData>,
    onOrganizationClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth().testTag(C.Tag.org_suggestions_section)) {
    Text(
        text = stringResource(R.string.org_suggestions_title),
        style =
            TextStyle(
                fontSize = OrganizationSuggestionsConstants.TITLE_FONT_SIZE_SP.sp,
                lineHeight = OrganizationSuggestionsConstants.TITLE_LINE_HEIGHT_SP.sp,
                fontWeight = FontWeight(OrganizationSuggestionsConstants.TITLE_FONT_WEIGHT),
                color = MaterialTheme.colorScheme.onSurface),
        modifier =
            Modifier.padding(bottom = OrganizationSuggestionsConstants.SECTION_PADDING_BOTTOM_DP.dp)
                .testTag(C.Tag.org_suggestions_title))

    LazyRow(
        modifier =
            Modifier.fillMaxWidth()
                .padding(bottom = OrganizationSuggestionsConstants.ROW_PADDING_BOTTOM_DP.dp)
                .testTag(C.Tag.org_suggestions_row),
        horizontalArrangement =
            Arrangement.spacedBy(
                OrganizationSuggestionsConstants.ROW_SPACING_DP.dp, Alignment.Start),
        verticalAlignment = Alignment.Top) {
          items(organizations) { organization ->
            OrganizationCard(
                organization = organization, onClick = { onOrganizationClick(organization.id) })
          }
        }
  }
}

@Composable
private fun OrganizationCard(organization: OrganizationData, onClick: () -> Unit) {
  Card(
      modifier =
          Modifier.width(OrganizationSuggestionsConstants.CARD_WIDTH_DP.dp)
              .clickable(onClick = onClick)
              .semantics(mergeDescendants = false) {}
              .testTag("${C.Tag.org_suggestions_card}_${organization.id}"),
      shape = RoundedCornerShape(OrganizationSuggestionsConstants.CARD_CORNER_RADIUS_DP.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      elevation =
          CardDefaults.cardElevation(
              defaultElevation = OrganizationSuggestionsConstants.CARD_ELEVATION_DP.dp)) {
        Column(
            modifier = Modifier.padding(OrganizationSuggestionsConstants.CARD_PADDING_DP.dp),
            verticalArrangement =
                Arrangement.spacedBy(OrganizationSuggestionsConstants.TEXT_SPACING_DP.dp),
            horizontalAlignment = Alignment.Start) {
              OrganizationImage(organization.id)
              OrganizationInfo(organization)
            }
      }
}

@Composable
private fun OrganizationImage(organizationId: String) {
  val context = LocalContext.current
  var organization by remember { mutableStateOf<Organization?>(null) }
  LaunchedEffect(organization) {
    organization = OrganizationRepositoryProvider.repository.getOrganizationById(organizationId)
  }
  val imageBitmap = organization?.let { loadBitmapFromOrganization(context, it) }
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .aspectRatio(OrganizationSuggestionsConstants.IMAGE_ASPECT_RATIO)
              .background(
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  shape =
                      RoundedCornerShape(
                          OrganizationSuggestionsConstants.IMAGE_CORNER_RADIUS_DP.dp))
              .testTag("${C.Tag.org_suggestions_card_image}_$organizationId")) {
        if (imageBitmap != null) {
          Image(
              bitmap = imageBitmap,
              contentDescription = stringResource(R.string.content_description_organization_image),
              contentScale = ContentScale.Crop,
              modifier = Modifier.matchParentSize().clip(CircleShape))
        } else {
          Image(
              painter = painterResource(id = OrganizationSuggestionsConstants.PLACEHOLDER_DRAWABLE),
              contentDescription =
                  stringResource(R.string.content_description_organization_image_placeholder),
              contentScale = ContentScale.Fit,
              modifier = Modifier.matchParentSize())
        }
      }
}

@Composable
private fun OrganizationInfo(organization: OrganizationData) {
  Column(
      verticalArrangement =
          Arrangement.spacedBy(OrganizationSuggestionsConstants.TEXT_SPACING_DP.dp)) {
        Text(
            text = organization.name,
            style =
                TextStyle(
                    fontSize = OrganizationSuggestionsConstants.NAME_FONT_SIZE_SP.sp,
                    lineHeight = OrganizationSuggestionsConstants.NAME_LINE_HEIGHT_SP.sp,
                    fontWeight = FontWeight(OrganizationSuggestionsConstants.NAME_FONT_WEIGHT),
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = OrganizationSuggestionsConstants.NAME_LETTER_SPACING_SP.sp),
            modifier = Modifier.testTag("${C.Tag.org_suggestions_card_title}_${organization.id}"))

        Text(
            text = organization.handle,
            style =
                TextStyle(
                    fontSize = OrganizationSuggestionsConstants.HANDLE_FONT_SIZE_SP.sp,
                    lineHeight = OrganizationSuggestionsConstants.HANDLE_LINE_HEIGHT_SP.sp,
                    fontWeight = FontWeight(OrganizationSuggestionsConstants.HANDLE_FONT_WEIGHT),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = OrganizationSuggestionsConstants.HANDLE_LETTER_SPACING_SP.sp),
            modifier =
                Modifier.testTag("${C.Tag.org_suggestions_card_subtitle}_${organization.id}"))
      }
}
