package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.ui.profile.OrganizationManagementViewModel

/**
 * Organization management screen.
 *
 * Allows users to view organizations they belong to or create a new organization.
 *
 * @param currentUserId The ID of the current user
 * @param onBack Callback when back button is pressed
 * @param onCreateOrganization Callback to navigate to organization creation flow
 * @param onJoinOrganization Callback to navigate to join organization flow
 * @param onOrganizationClick Callback when an organization is clicked (passes organization ID)
 * @param viewModel ViewModel for organization management
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationManagementScreen(
    currentUserId: String,
    onBack: () -> Unit,
    onCreateOrganization: () -> Unit,
    onJoinOrganization: () -> Unit = {},
    onOrganizationClick: (String) -> Unit = {},
    viewModel: OrganizationManagementViewModel = viewModel {
      OrganizationManagementViewModel(
          userId = currentUserId,
          organizationRepository = OrganizationRepositoryProvider.repository)
    },
    modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  // Check if user should be redirected to organization creation
  LaunchedEffect(uiState.shouldRedirectToCreation) {
    if (uiState.shouldRedirectToCreation) {
      onCreateOrganization()
    }
  }

  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = {
              Text(
                  stringResource(R.string.title_my_organizations),
                  style = MaterialTheme.typography.titleLarge)
            },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_back))
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface))
      }) { paddingValues ->
        when {
          uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator()
                }
          }
          uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(
                        modifier =
                            Modifier.height(dimensionResource(R.dimen.org_management_padding)))
                    Button(onClick = { viewModel.loadUserOrganizations() }) {
                      Text(stringResource(R.string.button_retry))
                    }
                  }
                }
          }
          uiState.userOrganizations.isEmpty() -> {
            // Empty state - show create organization prompt
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier =
                          Modifier.padding(
                              dimensionResource(R.dimen.org_management_empty_state_padding))) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            modifier =
                                Modifier.size(
                                    dimensionResource(R.dimen.org_management_icon_size_large)),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(
                            modifier =
                                Modifier.height(
                                    dimensionResource(R.dimen.org_management_empty_state_spacing)))
                        Text(
                            text = stringResource(R.string.text_no_organizations_yet),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(
                            modifier =
                                Modifier.height(dimensionResource(R.dimen.org_management_spacing)))
                        Text(
                            text = stringResource(R.string.text_no_organizations_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier =
                                Modifier.padding(
                                    horizontal = dimensionResource(R.dimen.org_management_padding)))
                        Spacer(
                            modifier =
                                Modifier.height(
                                    dimensionResource(R.dimen.org_management_empty_state_padding)))

                        // Create Organization Button
                        Button(
                            onClick = onCreateOrganization,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(
                                        dimensionResource(R.dimen.org_management_button_height)),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary),
                            shape =
                                RoundedCornerShape(
                                    dimensionResource(
                                        R.dimen.org_management_button_corner_radius))) {
                              Icon(
                                  imageVector = Icons.Default.Add,
                                  contentDescription = null,
                                  modifier =
                                      Modifier.size(
                                          dimensionResource(
                                              R.dimen.org_management_icon_size_small)))
                              Spacer(
                                  modifier =
                                      Modifier.width(
                                          dimensionResource(R.dimen.org_management_spacing)))
                              Text(
                                  text = stringResource(R.string.button_create_organization),
                                  fontSize =
                                      dimensionResource(R.dimen.org_management_button_text_size)
                                          .value
                                          .sp,
                                  fontWeight = FontWeight.SemiBold)
                            }

                        Spacer(
                            modifier =
                                Modifier.height(dimensionResource(R.dimen.org_management_spacing)))

                        // Join Organization Button
                        Button(
                            onClick = onJoinOrganization,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(
                                        dimensionResource(R.dimen.org_management_button_height)),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            shape =
                                RoundedCornerShape(
                                    dimensionResource(
                                        R.dimen.org_management_button_corner_radius))) {
                              Icon(
                                  imageVector = Icons.Default.PersonAdd,
                                  contentDescription = null,
                                  modifier =
                                      Modifier.size(
                                          dimensionResource(
                                              R.dimen.org_management_icon_size_small)))
                              Spacer(
                                  modifier =
                                      Modifier.width(
                                          dimensionResource(R.dimen.org_management_spacing)))
                              Text(
                                  text = stringResource(R.string.button_join_organization),
                                  fontSize =
                                      dimensionResource(R.dimen.org_management_button_text_size)
                                          .value
                                          .sp,
                                  fontWeight = FontWeight.SemiBold)
                            }
                      }
                }
          }
          else -> {
            // Show list of organizations
            LazyColumn(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(paddingValues)
                        .padding(dimensionResource(R.dimen.org_management_padding)),
                verticalArrangement =
                    Arrangement.spacedBy(dimensionResource(R.dimen.org_management_spacing))) {
                  item {
                    Text(
                        text = stringResource(R.string.text_your_organizations),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            Modifier.padding(
                                bottom = dimensionResource(R.dimen.org_management_spacing)))
                  }

                  items(uiState.userOrganizations) { organization ->
                    OrganizationCard(
                        organizationName = organization.name,
                        organizationType = organization.type.name,
                        memberCount = organization.memberUids.size,
                        onClick = { onOrganizationClick(organization.id) })
                  }

                  item {
                    Spacer(
                        modifier =
                            Modifier.height(dimensionResource(R.dimen.org_management_spacing)))

                    // Create New Organization Button
                    Button(
                        onClick = onCreateOrganization,
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(dimensionResource(R.dimen.org_management_button_height)),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        shape =
                            RoundedCornerShape(
                                dimensionResource(R.dimen.org_management_button_corner_radius))) {
                          Icon(
                              imageVector = Icons.Default.Add,
                              contentDescription = null,
                              modifier =
                                  Modifier.size(
                                      dimensionResource(R.dimen.org_management_icon_size_small)))
                          Spacer(
                              modifier =
                                  Modifier.width(dimensionResource(R.dimen.org_management_spacing)))
                          Text(
                              text = stringResource(R.string.button_create_new_organization),
                              fontSize =
                                  dimensionResource(R.dimen.org_management_button_text_size_small)
                                      .value
                                      .sp,
                              fontWeight = FontWeight.Medium)
                        }

                    Spacer(
                        modifier =
                            Modifier.height(dimensionResource(R.dimen.org_management_spacing)))

                    // Join Organization Button
                    Button(
                        onClick = onJoinOrganization,
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(dimensionResource(R.dimen.org_management_button_height)),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                        shape =
                            RoundedCornerShape(
                                dimensionResource(R.dimen.org_management_button_corner_radius))) {
                          Icon(
                              imageVector = Icons.Default.PersonAdd,
                              contentDescription = null,
                              modifier =
                                  Modifier.size(
                                      dimensionResource(R.dimen.org_management_icon_size_small)))
                          Spacer(
                              modifier =
                                  Modifier.width(dimensionResource(R.dimen.org_management_spacing)))
                          Text(
                              text = stringResource(R.string.button_join_organization),
                              fontSize =
                                  dimensionResource(R.dimen.org_management_button_text_size_small)
                                      .value
                                      .sp,
                              fontWeight = FontWeight.Medium)
                        }
                  }
                }
          }
        }
      }
}

/**
 * Card component displaying organization information.
 *
 * @param organizationName Name of the organization
 * @param organizationType Type of the organization
 * @param memberCount Number of members in the organization
 * @param onClick Callback when the card is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun OrganizationCard(
    organizationName: String,
    organizationType: String,
    memberCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      elevation =
          CardDefaults.cardElevation(
              defaultElevation = dimensionResource(R.dimen.org_management_card_elevation)),
      shape = RoundedCornerShape(dimensionResource(R.dimen.org_management_card_corner_radius))) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(dimensionResource(R.dimen.org_management_card_padding)),
            verticalAlignment = Alignment.CenterVertically) {
              // Organization Icon/Avatar
              Box(
                  modifier =
                      Modifier.size(dimensionResource(R.dimen.org_management_card_icon_size))
                          .clip(CircleShape)
                          .background(MaterialTheme.colorScheme.primaryContainer)
                          .border(
                              width = dimensionResource(R.dimen.org_management_card_border_width),
                              color = MaterialTheme.colorScheme.primary,
                              shape = CircleShape),
                  contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier =
                            Modifier.size(
                                dimensionResource(R.dimen.org_management_icon_size_medium)),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                  }

              Spacer(modifier = Modifier.width(dimensionResource(R.dimen.org_management_padding)))

              // Organization Info
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = organizationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
                Text(
                    text = organizationType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
                Text(
                    text = stringResource(R.string.text_members_count, memberCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
      }
}
