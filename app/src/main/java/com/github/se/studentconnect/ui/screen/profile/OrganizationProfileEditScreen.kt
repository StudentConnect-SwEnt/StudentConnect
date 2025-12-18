package com.github.se.studentconnect.ui.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.ui.profile.OrganizationMemberEdit
import com.github.se.studentconnect.ui.profile.OrganizationProfileEditUiState
import com.github.se.studentconnect.ui.profile.OrganizationProfileEditViewModel
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Organization Profile Edit Screen.
 *
 * Allows organization owners to edit all organization details and manage members.
 *
 * @param organizationId The ID of the organization to edit
 * @param onBack Callback when the back button is clicked
 * @param viewModel ViewModel for managing the edit screen state
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationProfileEditScreen(
    organizationId: String,
    onBack: () -> Unit,
    viewModel: OrganizationProfileEditViewModel = run {
      val context = LocalContext.current
      viewModel {
        OrganizationProfileEditViewModel(
            organizationId = organizationId,
            context = context,
            organizationRepository = OrganizationRepositoryProvider.repository,
            userRepository = UserRepositoryProvider.repository,
            mediaRepository = MediaRepositoryProvider.repository)
      }
    },
    modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  HandleEffects(uiState, snackbarHostState, viewModel)

  Scaffold(
      modifier = modifier.fillMaxSize(),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        OrganizationEditTopBar(
            onBack = onBack,
            onSave = { viewModel.saveOrganization() },
            isSaving = uiState.isSaving,
            isLoading = uiState.isLoading)
      }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
          when {
            uiState.isLoading -> {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
              }
            }
            uiState.organization != null -> {
              OrganizationEditContent(uiState = uiState, viewModel = viewModel, modifier = Modifier)
            }
          }
        }

        OrganizationEditDialogs(uiState, viewModel)
      }
}

@Composable
private fun HandleEffects(
    uiState: OrganizationProfileEditUiState,
    snackbarHostState: SnackbarHostState,
    viewModel: OrganizationProfileEditViewModel
) {
  val scope = rememberCoroutineScope()

  // Show snackbar for success/error messages
  LaunchedEffect(uiState.successMessage) {
    uiState.successMessage?.let { message ->
      scope.launch {
        snackbarHostState.showSnackbar(message)
        viewModel.clearSuccessMessage()
      }
    }
  }

  LaunchedEffect(uiState.error) {
    uiState.error?.let { error ->
      scope.launch {
        snackbarHostState.showSnackbar(error)
        viewModel.clearError()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrganizationEditTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    isLoading: Boolean
) {
  TopAppBar(
      title = {
        Text(
            stringResource(R.string.title_edit_organization),
            style = MaterialTheme.typography.titleLarge)
      },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.content_description_back))
        }
      },
      actions = {
        // Save button with enhanced visibility
        FilledTonalButton(
            onClick = onSave,
            enabled = !isSaving && !isLoading,
            modifier =
                Modifier.padding(
                    end = dimensionResource(R.dimen.org_edit_top_bar_save_button_end_padding)),
            colors =
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
              if (isSaving) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(dimensionResource(R.dimen.org_edit_top_bar_progress_size)),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = dimensionResource(R.dimen.org_edit_top_bar_progress_stroke_width))
                Spacer(
                    modifier =
                        Modifier.width(
                            dimensionResource(R.dimen.org_edit_top_bar_progress_spacing)))
              }
              Text(
                  text =
                      if (isSaving) stringResource(R.string.button_save) + "..."
                      else stringResource(R.string.button_save),
                  fontWeight = FontWeight.SemiBold)
            }
      },
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface,
              titleContentColor = MaterialTheme.colorScheme.onSurface))
}

@Composable
private fun OrganizationEditDialogs(
    uiState: OrganizationProfileEditUiState,
    viewModel: OrganizationProfileEditViewModel
) {
  if (uiState.showRemoveMemberDialog && uiState.memberToRemove != null) {
    RemoveMemberDialog(
        member = uiState.memberToRemove,
        onConfirm = { viewModel.confirmRemoveMember() },
        onDismiss = { viewModel.dismissRemoveMemberDialog() })
  }

  if (uiState.showChangeRoleDialog && uiState.memberToChangeRole != null) {
    ChangeRoleDialog(
        member = uiState.memberToChangeRole,
        availableRoles = uiState.roles.map { it.name } + listOf("Member"),
        onConfirm = { newRole -> viewModel.confirmChangeRole(newRole) },
        onDismiss = { viewModel.dismissChangeRoleDialog() })
  }
}

/** Main edit content. */
@Composable
private fun OrganizationEditContent(
    uiState: com.github.se.studentconnect.ui.profile.OrganizationProfileEditUiState,
    viewModel: OrganizationProfileEditViewModel,
    modifier: Modifier = Modifier
) {
  LazyColumn(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = dimensionResource(R.dimen.org_edit_horizontal_padding))
              .testTag("profile_edit_list"),
      verticalArrangement =
          Arrangement.spacedBy(dimensionResource(R.dimen.org_edit_vertical_spacing))) {
        item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.org_edit_top_spacing))) }

        // Logo section with enhanced visuals
        item {
          LogoSection(logoUrl = uiState.logoUrl, logoUri = uiState.logoUri, viewModel = viewModel)
        }

        // Basic Information section
        item { SectionHeader(title = stringResource(R.string.section_basic_info)) }
        item {
          OutlinedTextField(
              value = uiState.name,
              onValueChange = { viewModel.updateName(it) },
              label = { Text(stringResource(R.string.label_organization_name)) },
              placeholder = { Text(stringResource(R.string.placeholder_organization_name)) },
              leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
              },
              isError = uiState.nameError != null,
              supportingText =
                  uiState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(dimensionResource(R.dimen.org_edit_field_corner_radius)))
        }

        item {
          OrganizationTypeSelector(
              selectedType = uiState.type, onTypeSelected = { viewModel.updateType(it) })
        }

        item {
          Column {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text(stringResource(R.string.label_organization_description)) },
                placeholder = {
                  Text(stringResource(R.string.placeholder_organization_description))
                },
                isError = uiState.descriptionError != null,
                supportingText =
                    uiState.descriptionError?.let {
                      { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                modifier =
                    Modifier.fillMaxWidth()
                        .height(dimensionResource(R.dimen.org_edit_description_height)),
                maxLines = 6,
                shape = RoundedCornerShape(dimensionResource(R.dimen.org_edit_field_corner_radius)))

            // Character count
            Text(
                text = "${uiState.description.length} characters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier.align(Alignment.End)
                        .padding(top = dimensionResource(R.dimen.org_edit_description_top_padding)))
          }
        }

        item {
          OutlinedTextField(
              value = uiState.location ?: "",
              onValueChange = { viewModel.updateLocation(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_organization_location)) },
              leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
              },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(12.dp))
        }

        // Social Links section
        item {
          Spacer(modifier = Modifier.height(dimensionResource(R.dimen.org_edit_section_spacing)))
          SectionHeader(title = stringResource(R.string.section_social_links))
        }
        item {
          OutlinedTextField(
              value = uiState.socialWebsite ?: "",
              onValueChange = { viewModel.updateSocialWebsite(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_website)) },
              leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
              },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(12.dp))
        }
        item {
          OutlinedTextField(
              value = uiState.socialInstagram ?: "",
              onValueChange = { viewModel.updateSocialInstagram(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_insta)) },
              leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_logo_instagram),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.org_edit_social_icon_size)))
              },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(12.dp))
        }
        item {
          OutlinedTextField(
              value = uiState.socialX ?: "",
              onValueChange = { viewModel.updateSocialX(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_x)) },
              leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_logo_x),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.org_edit_social_icon_size)))
              },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(12.dp))
        }
        item {
          OutlinedTextField(
              value = uiState.socialLinkedIn ?: "",
              onValueChange = { viewModel.updateSocialLinkedIn(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_linkedin)) },
              leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_logo_linkedin),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.org_edit_social_icon_size)))
              },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(12.dp))
        }

        // Members section
        item {
          Spacer(modifier = Modifier.height(dimensionResource(R.dimen.org_edit_section_spacing)))
          SectionHeader(title = stringResource(R.string.section_members))
        }
        items(uiState.members) { member -> MemberEditCard(member = member, viewModel = viewModel) }

        item {
          Spacer(modifier = Modifier.height(dimensionResource(R.dimen.org_edit_bottom_spacing)))
        }
      }
}

/** Section header with improved visual hierarchy. */
@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier.padding(
                vertical = dimensionResource(R.dimen.org_edit_section_header_vertical_padding))) {
          Box(
              modifier =
                  Modifier.width(dimensionResource(R.dimen.org_edit_section_header_indicator_width))
                      .height(dimensionResource(R.dimen.org_edit_section_header_indicator_height))
                      .background(
                          MaterialTheme.colorScheme.primary,
                          shape =
                              RoundedCornerShape(
                                  dimensionResource(
                                      R.dimen.org_edit_section_header_indicator_corner_radius))))
          Spacer(
              modifier = Modifier.width(dimensionResource(R.dimen.org_edit_section_header_spacing)))
          Text(
              text = title,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              letterSpacing =
                  dimensionResource(R.dimen.org_edit_section_header_letter_spacing).value.sp)
        }
    HorizontalDivider(
        thickness = dimensionResource(R.dimen.org_edit_section_divider_thickness),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  }
}

/** Enhanced logo section with better visual feedback. */
@Composable
private fun LogoSection(
    logoUrl: String?,
    logoUri: Uri?,
    viewModel: OrganizationProfileEditViewModel,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository

  val imageLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.updateLogoUri(it) }
      }

  // Load logo bitmap
  val logoBitmap by
      produceState<ImageBitmap?>(initialValue = null, logoUrl, logoUri, repository) {
        value =
            when {
              logoUri != null -> loadBitmapFromUri(context, logoUri, Dispatchers.IO)
              logoUrl != null -> {
                runCatching { repository.download(logoUrl) }
                    .getOrNull()
                    ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
              }
              else -> null
            }
      }

  ElevatedCard(
      modifier = modifier.fillMaxWidth(),
      colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation =
          CardDefaults.elevatedCardElevation(
              defaultElevation = dimensionResource(R.dimen.org_edit_card_elevation))) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(dimensionResource(R.dimen.org_edit_logo_card_padding)),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Logo display with enhanced styling
              Surface(
                  modifier = Modifier.size(dimensionResource(R.dimen.org_edit_logo_size)),
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shadowElevation = dimensionResource(R.dimen.org_edit_logo_shadow_elevation),
                  tonalElevation = dimensionResource(R.dimen.org_edit_logo_tonal_elevation)) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .border(
                                    dimensionResource(R.dimen.org_edit_logo_border_width),
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape)
                                .clickable { imageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center) {
                          if (logoBitmap != null) {
                            Image(
                                bitmap = logoBitmap!!,
                                contentDescription =
                                    stringResource(R.string.content_description_organization_logo),
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop)
                          } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                              Icon(
                                  imageVector = Icons.Default.Business,
                                  contentDescription = null,
                                  modifier =
                                      Modifier.size(
                                          dimensionResource(R.dimen.org_edit_logo_icon_size)),
                                  tint = MaterialTheme.colorScheme.primary)
                              Spacer(
                                  modifier =
                                      Modifier.height(
                                          dimensionResource(R.dimen.org_edit_logo_icon_spacing)))
                              Text(
                                  text = stringResource(R.string.text_tap_to_add),
                                  style = MaterialTheme.typography.labelSmall,
                                  color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                          }

                          // Edit icon overlay with better positioning
                          Surface(
                              modifier =
                                  Modifier.size(
                                          dimensionResource(R.dimen.org_edit_logo_edit_button_size))
                                      .align(Alignment.BottomEnd)
                                      .padding(
                                          dimensionResource(
                                              R.dimen.org_edit_logo_edit_button_padding)),
                              shape = CircleShape,
                              color = MaterialTheme.colorScheme.primary,
                              shadowElevation =
                                  dimensionResource(R.dimen.org_edit_logo_edit_button_shadow)) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription =
                                        stringResource(R.string.content_description_change_logo),
                                    modifier =
                                        Modifier.size(
                                                dimensionResource(
                                                    R.dimen.org_edit_logo_edit_button_icon_size))
                                            .padding(
                                                dimensionResource(
                                                    R.dimen
                                                        .org_edit_logo_edit_button_icon_padding)),
                                    tint = MaterialTheme.colorScheme.onPrimary)
                              }
                        }
                  }

              Spacer(
                  modifier =
                      Modifier.height(dimensionResource(R.dimen.org_edit_logo_buttons_top_spacing)))

              // Buttons with improved styling
              Row(
                  horizontalArrangement =
                      Arrangement.spacedBy(
                          dimensionResource(R.dimen.org_edit_logo_buttons_spacing))) {
                    FilledTonalButton(
                        onClick = { imageLauncher.launch("image/*") },
                        modifier =
                            Modifier.height(
                                dimensionResource(R.dimen.org_edit_logo_button_height))) {
                          Icon(
                              imageVector = Icons.Default.Edit,
                              contentDescription = null,
                              modifier =
                                  Modifier.size(
                                      dimensionResource(R.dimen.org_edit_logo_button_icon_size)))
                          Spacer(
                              modifier =
                                  Modifier.width(
                                      dimensionResource(R.dimen.org_edit_logo_button_icon_spacing)))
                          Text(
                              text = stringResource(R.string.button_change_logo),
                              fontWeight = FontWeight.Medium)
                        }

                    AnimatedVisibility(
                        visible = logoUrl != null || logoUri != null,
                        enter = fadeIn(),
                        exit = fadeOut()) {
                          TextButton(
                              onClick = { viewModel.removeLogo() },
                              modifier =
                                  Modifier.height(
                                      dimensionResource(R.dimen.org_edit_logo_button_height)),
                              colors =
                                  ButtonDefaults.textButtonColors(
                                      contentColor = MaterialTheme.colorScheme.error)) {
                                Text(
                                    text = stringResource(R.string.button_remove_logo),
                                    fontWeight = FontWeight.Medium)
                              }
                        }
                  }
            }
      }
}

/** Enhanced organization type selector with card-based UI. */
@Composable
private fun OrganizationTypeSelector(
    selectedType: OrganizationType,
    onTypeSelected: (OrganizationType) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
        text = stringResource(R.string.label_organization_type),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            Modifier.padding(
                bottom = dimensionResource(R.dimen.org_edit_type_selector_bottom_spacing)))

    Column(
        verticalArrangement =
            Arrangement.spacedBy(dimensionResource(R.dimen.org_edit_type_selector_item_spacing))) {
          OrganizationType.entries.forEach { type ->
            OrganizationTypeItem(
                type = type, isSelected = selectedType == type, onTypeSelected = onTypeSelected)
          }
        }
  }
}

@Composable
private fun OrganizationTypeItem(
    type: OrganizationType,
    isSelected: Boolean,
    onTypeSelected: (OrganizationType) -> Unit
) {
  ElevatedCard(
      onClick = { onTypeSelected(type) },
      modifier = Modifier.fillMaxWidth(),
      colors =
          CardDefaults.elevatedCardColors(
              containerColor =
                  if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surface),
      elevation =
          CardDefaults.elevatedCardElevation(
              defaultElevation =
                  if (isSelected) dimensionResource(R.dimen.org_edit_type_card_elevation_selected)
                  else dimensionResource(R.dimen.org_edit_type_card_elevation_default))) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal =
                            dimensionResource(R.dimen.org_edit_type_card_horizontal_padding),
                        vertical = dimensionResource(R.dimen.org_edit_type_card_vertical_padding)),
            verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                  selected = isSelected,
                  onClick = { onTypeSelected(type) },
                  colors =
                      androidx.compose.material3.RadioButtonDefaults.colors(
                          selectedColor = MaterialTheme.colorScheme.primary))
              Spacer(
                  modifier = Modifier.width(dimensionResource(R.dimen.org_edit_type_card_spacing)))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getOrganizationTypeLabel(type),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color =
                        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface)
                if (isSelected) {
                  Text(
                      text = stringResource(R.string.text_selected),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.primary)
                }
              }
            }
      }
}

/** Get localized label for organization type. */
@Composable
private fun getOrganizationTypeLabel(type: OrganizationType): String {
  return when (type) {
    OrganizationType.Association -> stringResource(R.string.organization_type_association)
    OrganizationType.StudentClub -> stringResource(R.string.organization_type_student_club)
    OrganizationType.Company -> stringResource(R.string.organization_type_company)
    OrganizationType.NGO -> stringResource(R.string.organization_type_ngo)
    OrganizationType.Other -> stringResource(R.string.organization_type_other)
  }
}

/** Enhanced member edit card with better visual hierarchy. */
@Composable
private fun MemberEditCard(
    member: OrganizationMemberEdit,
    viewModel: OrganizationProfileEditViewModel,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository

  val avatarBitmap by
      produceState<ImageBitmap?>(initialValue = null, member.avatarUrl, repository) {
        value =
            member.avatarUrl?.let { url ->
              runCatching { repository.download(url) }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

  ElevatedCard(
      modifier = modifier.fillMaxWidth(),
      colors =
          CardDefaults.elevatedCardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer),
      elevation =
          CardDefaults.elevatedCardElevation(
              defaultElevation = dimensionResource(R.dimen.org_edit_member_card_elevation)),
      shape = RoundedCornerShape(dimensionResource(R.dimen.org_edit_member_card_corner_radius))) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(dimensionResource(R.dimen.org_edit_member_card_padding)),
            verticalAlignment = Alignment.CenterVertically) {
              // Enhanced avatar with border
              Surface(
                  modifier = Modifier.size(dimensionResource(R.dimen.org_edit_member_avatar_size)),
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shadowElevation = dimensionResource(R.dimen.org_edit_card_elevation)) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .border(
                                    dimensionResource(R.dimen.org_edit_member_avatar_border_width),
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape),
                        contentAlignment = Alignment.Center) {
                          if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap!!,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop)
                          } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier =
                                    Modifier.size(
                                        dimensionResource(
                                            R.dimen.org_edit_member_avatar_icon_size)))
                          }
                        }
                  }

              Spacer(
                  modifier =
                      Modifier.width(dimensionResource(R.dimen.org_edit_member_avatar_spacing)))

              // Name and role with improved typography
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(
                    modifier =
                        Modifier.height(dimensionResource(R.dimen.org_edit_member_name_spacing)))
                Text(
                    text = member.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              // Action buttons (only for non-owners)
              if (!member.isOwner) {
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            dimensionResource(R.dimen.org_edit_member_action_button_spacing))) {
                      // Change role button
                      Surface(
                          modifier =
                              Modifier.size(
                                  dimensionResource(R.dimen.org_edit_member_action_button_size)),
                          shape = CircleShape,
                          color = MaterialTheme.colorScheme.primaryContainer,
                          onClick = { viewModel.showChangeRoleDialog(member) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription =
                                    stringResource(R.string.content_description_change_member_role),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier =
                                    Modifier.padding(
                                        dimensionResource(
                                            R.dimen.org_edit_member_action_button_padding)))
                          }

                      // Remove button
                      Surface(
                          modifier =
                              Modifier.size(
                                  dimensionResource(R.dimen.org_edit_member_action_button_size)),
                          shape = CircleShape,
                          color = MaterialTheme.colorScheme.errorContainer,
                          onClick = { viewModel.showRemoveMemberDialog(member) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription =
                                    stringResource(R.string.content_description_remove_member),
                                tint = MaterialTheme.colorScheme.error,
                                modifier =
                                    Modifier.padding(
                                        dimensionResource(
                                            R.dimen.org_edit_member_action_button_padding)))
                          }
                    }
              } else {
                // Enhanced owner badge
                Surface(
                    shape =
                        RoundedCornerShape(
                            dimensionResource(R.dimen.org_edit_member_owner_badge_corner_radius)),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation =
                        dimensionResource(R.dimen.org_edit_member_owner_badge_elevation)) {
                      Row(
                          modifier =
                              Modifier.padding(
                                  horizontal =
                                      dimensionResource(
                                          R.dimen.org_edit_member_owner_badge_horizontal_padding),
                                  vertical =
                                      dimensionResource(
                                          R.dimen.org_edit_member_owner_badge_vertical_padding)),
                          verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_crown),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier =
                                    Modifier.size(
                                        dimensionResource(
                                            R.dimen.org_edit_member_owner_badge_icon_size)))
                            Spacer(
                                modifier =
                                    Modifier.width(
                                        dimensionResource(
                                            R.dimen.org_edit_member_owner_badge_spacing)))
                            Text(
                                text = stringResource(R.string.text_role_owner),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                          }
                    }
              }
            }
      }
}

/**
 * Displays a confirmation dialog for removing a member from the organization.
 *
 * Shows a warning message with the member's name and requires explicit confirmation before
 * proceeding with the removal. The action cannot be undone once confirmed.
 *
 * @param member The member to be removed from the organization
 * @param onConfirm Callback invoked when the user confirms the removal
 * @param onDismiss Callback invoked when the user cancels or dismisses the dialog
 */
@Composable
private fun RemoveMemberDialog(
    member: OrganizationMemberEdit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Text(
            text = stringResource(R.string.dialog_remove_member_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
      },
      text = {
        Column {
          Text(
              text = stringResource(R.string.dialog_remove_member_message, member.name),
              style = MaterialTheme.typography.bodyLarge)
          Spacer(
              modifier =
                  Modifier.height(dimensionResource(R.dimen.org_edit_dialog_message_spacing)))
          Text(
              text = stringResource(R.string.dialog_action_cannot_be_undone),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Medium)
        }
      },
      confirmButton = {
        Button(
            onClick = onConfirm,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError)) {
              Text(
                  text = stringResource(R.string.button_remove_member),
                  fontWeight = FontWeight.SemiBold)
            }
      },
      dismissButton = {
        FilledTonalButton(onClick = onDismiss) {
          Text(text = stringResource(R.string.button_cancel), fontWeight = FontWeight.Medium)
        }
      },
      shape = RoundedCornerShape(dimensionResource(R.dimen.org_edit_dialog_corner_radius)))
}

/** Enhanced change role dialog with card-based selection. */
@Composable
private fun ChangeRoleDialog(
    member: OrganizationMemberEdit,
    availableRoles: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
  var selectedRole by remember { mutableStateOf(member.role) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Text(
            text = stringResource(R.string.dialog_change_role_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
      },
      text = {
        Column {
          Text(
              text = stringResource(R.string.dialog_select_role, member.name),
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface)
          Spacer(
              modifier =
                  Modifier.height(
                      dimensionResource(R.dimen.org_edit_dialog_role_selection_top_spacing)))

          Column(
              verticalArrangement =
                  Arrangement.spacedBy(
                      dimensionResource(R.dimen.org_edit_dialog_role_selection_item_spacing))) {
                availableRoles.forEach { role ->
                  RoleSelectionItem(
                      role = role,
                      isSelected = selectedRole == role,
                      onRoleSelected = { selectedRole = it })
                }
              }
        }
      },
      confirmButton = {
        Button(onClick = { onConfirm(selectedRole) }) {
          Text(text = stringResource(R.string.button_save), fontWeight = FontWeight.SemiBold)
        }
      },
      dismissButton = {
        FilledTonalButton(onClick = onDismiss) {
          Text(text = stringResource(R.string.button_cancel), fontWeight = FontWeight.Medium)
        }
      },
      shape = RoundedCornerShape(dimensionResource(R.dimen.org_edit_dialog_corner_radius)))
}

@Composable
private fun RoleSelectionItem(role: String, isSelected: Boolean, onRoleSelected: (String) -> Unit) {
  ElevatedCard(
      onClick = { onRoleSelected(role) },
      colors =
          CardDefaults.elevatedCardColors(
              containerColor =
                  if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant),
      elevation =
          CardDefaults.elevatedCardElevation(
              defaultElevation =
                  if (isSelected)
                      dimensionResource(R.dimen.org_edit_dialog_role_card_elevation_selected)
                  else dimensionResource(R.dimen.org_edit_dialog_role_card_elevation_default))) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal =
                            dimensionResource(R.dimen.org_edit_dialog_role_card_horizontal_padding),
                        vertical =
                            dimensionResource(R.dimen.org_edit_dialog_role_card_vertical_padding)),
            verticalAlignment = Alignment.CenterVertically) {
              RadioButton(selected = isSelected, onClick = { onRoleSelected(role) })
              Spacer(
                  modifier =
                      Modifier.width(dimensionResource(R.dimen.org_edit_dialog_role_card_spacing)))
              Text(
                  text = role,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                  color =
                      if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant)
            }
      }
}
