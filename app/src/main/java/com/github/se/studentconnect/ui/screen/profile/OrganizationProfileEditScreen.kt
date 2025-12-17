package com.github.se.studentconnect.ui.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.ui.profile.OrganizationMemberEdit
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

  Scaffold(
      modifier = modifier.fillMaxSize(),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
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
              // Save button
              Button(
                  onClick = { viewModel.saveOrganization() },
                  enabled = !uiState.isSaving && !uiState.isLoading,
                  modifier = Modifier.padding(end = 8.dp)) {
                    if (uiState.isSaving) {
                      CircularProgressIndicator(
                          modifier = Modifier.size(20.dp),
                          color = MaterialTheme.colorScheme.onPrimary,
                          strokeWidth = 2.dp)
                    } else {
                      Text(stringResource(R.string.button_save))
                    }
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
          uiState.organization != null -> {
            OrganizationEditContent(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues))
          }
        }

        // Dialogs
        if (uiState.showRemoveMemberDialog && uiState.memberToRemove != null) {
          RemoveMemberDialog(
              member = uiState.memberToRemove!!,
              onConfirm = { viewModel.confirmRemoveMember() },
              onDismiss = { viewModel.dismissRemoveMemberDialog() })
        }

        if (uiState.showChangeRoleDialog && uiState.memberToChangeRole != null) {
          ChangeRoleDialog(
              member = uiState.memberToChangeRole!!,
              availableRoles = uiState.roles.map { it.name } + listOf("Member"),
              onConfirm = { newRole -> viewModel.confirmChangeRole(newRole) },
              onDismiss = { viewModel.dismissChangeRoleDialog() })
        }
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
      modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Logo section
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
              isError = uiState.nameError != null,
              supportingText =
                  uiState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true)
        }

        item {
          OrganizationTypeSelector(
              selectedType = uiState.type, onTypeSelected = { viewModel.updateType(it) })
        }

        item {
          OutlinedTextField(
              value = uiState.description,
              onValueChange = { viewModel.updateDescription(it) },
              label = { Text(stringResource(R.string.label_organization_description)) },
              placeholder = { Text(stringResource(R.string.placeholder_organization_description)) },
              isError = uiState.descriptionError != null,
              supportingText =
                  uiState.descriptionError?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                  },
              modifier = Modifier.fillMaxWidth().height(120.dp),
              maxLines = 5)
        }

        item {
          OutlinedTextField(
              value = uiState.location ?: "",
              onValueChange = { viewModel.updateLocation(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_organization_location)) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true)
        }

        // Social Links section
        item { SectionHeader(title = stringResource(R.string.section_social_links)) }
        item {
          OutlinedTextField(
              value = uiState.socialWebsite ?: "",
              onValueChange = { viewModel.updateSocialWebsite(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_website)) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true)
        }
        item {
          OutlinedTextField(
              value = uiState.socialInstagram ?: "",
              onValueChange = { viewModel.updateSocialInstagram(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_insta)) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true)
        }
        item {
          OutlinedTextField(
              value = uiState.socialX ?: "",
              onValueChange = { viewModel.updateSocialX(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_x)) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true)
        }
        item {
          OutlinedTextField(
              value = uiState.socialLinkedIn ?: "",
              onValueChange = { viewModel.updateSocialLinkedIn(it.ifBlank { null }) },
              label = { Text(stringResource(R.string.label_linkedin)) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true)
        }

        // Members section
        item { SectionHeader(title = stringResource(R.string.section_members)) }
        items(uiState.members) { member -> MemberEditCard(member = member, viewModel = viewModel) }

        item { Spacer(modifier = Modifier.height(16.dp)) }
      }
}

/** Section header. */
@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp))
    Divider(color = MaterialTheme.colorScheme.outlineVariant)
  }
}

/** Logo section with upload/change/remove functionality. */
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

  Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    // Logo display
    Box(
        modifier =
            Modifier.size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { imageLauncher.launch("image/*") },
        contentAlignment = Alignment.Center) {
          if (logoBitmap != null) {
            Image(
                bitmap = logoBitmap!!,
                contentDescription = stringResource(R.string.content_description_organization_logo),
                modifier = Modifier.size(120.dp).clip(CircleShape),
                contentScale = ContentScale.Crop)
          } else {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer)
          }

          // Edit icon overlay
          Box(
              modifier =
                  Modifier.size(36.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primary)
                      .align(Alignment.BottomEnd)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.content_description_change_logo),
                    modifier = Modifier.size(20.dp).align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onPrimary)
              }
        }

    Spacer(modifier = Modifier.height(12.dp))

    // Buttons
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(
          onClick = { imageLauncher.launch("image/*") },
          colors =
              ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
            Text(stringResource(R.string.button_change_logo))
          }

      if (logoUrl != null || logoUri != null) {
        TextButton(onClick = { viewModel.removeLogo() }) {
          Text(stringResource(R.string.button_remove_logo))
        }
      }
    }
  }
}

/** Organization type selector. */
@Composable
private fun OrganizationTypeSelector(
    selectedType: OrganizationType,
    onTypeSelected: (OrganizationType) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
        text = stringResource(R.string.label_organization_type),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(bottom = 8.dp))

    OrganizationType.entries.forEach { type ->
      Row(
          modifier =
              Modifier.fillMaxWidth().clickable { onTypeSelected(type) }.padding(vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selectedType == type, onClick = { onTypeSelected(type) })
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = getOrganizationTypeLabel(type), style = MaterialTheme.typography.bodyLarge)
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

/** Member edit card. */
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

  Card(
      modifier = modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
              // Avatar
              Box(
                  modifier =
                      Modifier.size(48.dp)
                          .clip(CircleShape)
                          .background(
                              Brush.verticalGradient(
                                  listOf(
                                      MaterialTheme.colorScheme.primaryContainer,
                                      MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)))),
                  contentAlignment = Alignment.Center) {
                    if (avatarBitmap != null) {
                      Image(
                          bitmap = avatarBitmap!!,
                          contentDescription = null,
                          modifier = Modifier.size(48.dp).clip(CircleShape),
                          contentScale = ContentScale.Crop)
                    } else {
                      Icon(
                          imageVector = Icons.Outlined.Person,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(24.dp))
                    }
                  }

              Spacer(modifier = Modifier.width(12.dp))

              // Name and role
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(
                    text = member.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              // Action buttons (only for non-owners)
              if (!member.isOwner) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  // Change role button
                  IconButton(
                      onClick = { viewModel.showChangeRoleDialog(member) },
                      modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription =
                                stringResource(R.string.content_description_change_member_role),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp))
                      }

                  // Remove button
                  IconButton(
                      onClick = { viewModel.showRemoveMemberDialog(member) },
                      modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription =
                                stringResource(R.string.content_description_remove_member),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                      }
                }
              } else {
                // Show badge for owner
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                      Text(
                          text = stringResource(R.string.text_role_owner),
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onPrimaryContainer,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
              }
            }
      }
}

/** Remove member confirmation dialog. */
@Composable
private fun RemoveMemberDialog(
    member: OrganizationMemberEdit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.dialog_remove_member_title)) },
      text = { Text(stringResource(R.string.dialog_remove_member_message, member.name)) },
      confirmButton = {
        TextButton(
            onClick = onConfirm,
            colors =
                ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
              Text(stringResource(R.string.button_remove_member))
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
      })
}

/** Change role dialog. */
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
      title = { Text(stringResource(R.string.dialog_change_role_title)) },
      text = {
        Column {
          Text(stringResource(R.string.dialog_select_role, member.name))
          Spacer(modifier = Modifier.height(16.dp))

          availableRoles.forEach { role ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable { selectedRole = role }
                        .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                  RadioButton(selected = selectedRole == role, onClick = { selectedRole = role })
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = role, style = MaterialTheme.typography.bodyLarge)
                }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { onConfirm(selectedRole) }) {
          Text(stringResource(R.string.button_save))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
      })
}
