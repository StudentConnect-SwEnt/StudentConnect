package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.ui.profile.OrganizationManagementViewModel
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import kotlinx.coroutines.Dispatchers

/**
 * Organization management screen.
 *
 * Allows users to view organizations they belong to or create a new organization.
 *
 * @param currentUserId The ID of the current user
 * @param onBack Callback when back button is pressed
 * @param onCreateOrganization Callback to navigate to organization creation flow
 * @param onOrganizationClick Callback when an organization is clicked (passes organization ID)
 * @param onEditOrganization Callback when edit organization is clicked (passes organization ID)
 * @param viewModel ViewModel for organization management
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationManagementScreen(
    currentUserId: String,
    onBack: () -> Unit,
    onCreateOrganization: () -> Unit,
    onOrganizationClick: (String) -> Unit = {},
    onEditOrganization: (String) -> Unit = {},
    viewModel: OrganizationManagementViewModel = viewModel {
      OrganizationManagementViewModel(
          userId = currentUserId,
          organizationRepository = OrganizationRepositoryProvider.repository,
          userRepository = UserRepositoryProvider.repository)
    },
    modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val lifecycleOwner = LocalLifecycleOwner.current

  // Reload organizations when returning to this screen (e.g., after creating a new one)
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.loadUserOrganizations()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

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
                        organization = organization,
                        currentUserId = currentUserId,
                        isPinned = uiState.pinnedOrganizationId == organization.id,
                        onPinClick = { viewModel.togglePinOrganization(organization.id) },
                        onClick = { onOrganizationClick(organization.id) },
                        onEditClick = { onEditOrganization(organization.id) })
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
                  }
                }
          }
        }
      }
}

/**
 * Card component displaying organization information.
 *
 * @param organization The organization to display
 * @param currentUserId The ID of the current user
 * @param isPinned Whether this organization is pinned to the profile
 * @param onPinClick Callback when the pin button is clicked
 * @param onClick Callback when the card is clicked
 * @param onEditClick Callback when the edit button is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun OrganizationCard(
    organization: Organization,
    currentUserId: String,
    isPinned: Boolean,
    onPinClick: () -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository

  // Load organization logo if available
  val logoUrl = organization.logoUrl
  val logoBitmap by
      produceState<ImageBitmap?>(initialValue = null, logoUrl, repository) {
        value =
            logoUrl?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure {
                    android.util.Log.e(
                        "OrganizationCard", "Failed to download organization logo: $id", it)
                  }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

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
              // Organization Logo/Avatar
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
                    val bitmap = logoBitmap
                    if (bitmap != null) {
                      // Show organization logo
                      Image(
                          bitmap = bitmap,
                          contentDescription = organization.name,
                          modifier =
                              Modifier.size(
                                      dimensionResource(R.dimen.org_management_card_icon_size))
                                  .clip(CircleShape),
                          contentScale = ContentScale.Crop)
                    } else {
                      // Show Business icon as fallback
                      Icon(
                          imageVector = Icons.Default.Business,
                          contentDescription = null,
                          modifier =
                              Modifier.size(
                                  dimensionResource(R.dimen.org_management_icon_size_medium)),
                          tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                  }

              Spacer(modifier = Modifier.width(dimensionResource(R.dimen.org_management_padding)))

              // Organization Info
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = organization.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
                Text(
                    text = organization.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
                Text(
                    text =
                        pluralStringResource(
                            R.plurals.text_members_count,
                            organization.memberUids.size,
                            organization.memberUids.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              // Action buttons column
              Column(horizontalAlignment = Alignment.End) {
                // Edit button (only for owner)
                val isOwner = organization.createdBy == currentUserId
                if (isOwner) {
                  Button(
                      onClick = onEditClick,
                      modifier = Modifier.height(32.dp),
                      colors =
                          ButtonDefaults.buttonColors(
                              containerColor = MaterialTheme.colorScheme.primaryContainer,
                              contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                      contentPadding =
                          androidx.compose.foundation.layout.PaddingValues(
                              horizontal = 12.dp, vertical = 4.dp),
                      shape = RoundedCornerShape(16.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.button_edit),
                            modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.button_edit),
                            style = MaterialTheme.typography.labelMedium)
                      }
                  Spacer(modifier = Modifier.height(8.dp))
                }

                // Pin button
                IconButton(onClick = onPinClick) {
                  Icon(
                      imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                      contentDescription =
                          stringResource(
                              if (isPinned) R.string.content_description_unpin_organization
                              else R.string.content_description_pin_organization),
                      tint =
                          if (isPinned) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
      }
}
