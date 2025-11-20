package com.github.se.studentconnect.ui.screen.signup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.theme.AppTheme
import java.util.UUID

data class TeamRole(val id: String, val name: String, val description: String?)

data class TeamRolesState(
    val roleName: String,
    val roleDescription: String,
    val roles: List<TeamRole>
)

data class TeamRolesCallbacks(
    val onRoleNameChange: (String) -> Unit,
    val onRoleDescriptionChange: (String) -> Unit,
    val onAddRole: () -> Unit,
    val onRemoveRole: (TeamRole) -> Unit,
    val onBackClick: () -> Unit,
    val onSkipClick: () -> Unit,
    val onContinueClick: () -> Unit
)

@Composable
fun TeamRolesScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onContinueClick: (List<TeamRole>) -> Unit = {}
) {
  var roleName by remember { mutableStateOf("") }
  var roleDescription by remember { mutableStateOf("") }
  var roles by remember { mutableStateOf<List<TeamRole>>(emptyList()) }

  val suggestions =
      listOf(
          stringResource(R.string.team_roles_suggestion_president),
          stringResource(R.string.team_roles_suggestion_vice_president),
          stringResource(R.string.team_roles_suggestion_treasurer),
          stringResource(R.string.team_roles_suggestion_secretary),
          stringResource(R.string.team_roles_suggestion_event_manager),
          stringResource(R.string.team_roles_suggestion_community_manager),
          stringResource(R.string.team_roles_suggestion_marketing_lead),
          stringResource(R.string.team_roles_suggestion_operations_lead))

  TeamRolesContent(
      state =
          TeamRolesState(roleName = roleName, roleDescription = roleDescription, roles = roles),
      suggestions = suggestions,
      callbacks =
          TeamRolesCallbacks(
              onRoleNameChange = { roleName = it },
              onRoleDescriptionChange = { roleDescription = it },
              onAddRole = {
                val trimmedName = roleName.trim()
                if (trimmedName.isNotEmpty()) {
                  val newRole =
                      TeamRole(
                          id = UUID.randomUUID().toString(),
                          name = trimmedName,
                          description = roleDescription.trim().ifBlank { null })
                  roles = listOf(newRole) + roles
                  roleName = ""
                  roleDescription = ""
                }
              },
              onRemoveRole = { role -> roles = roles.filterNot { it.id == role.id } },
              onBackClick = onBackClick,
              onSkipClick = onSkipClick,
              onContinueClick = { onContinueClick(roles) }),
      modifier = modifier)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun TeamRolesContent(
    state: TeamRolesState,
    suggestions: List<String>,
    callbacks: TeamRolesCallbacks,
    modifier: Modifier = Modifier
) {
  val rolesListState = rememberLazyListState()

  LaunchedEffect(state.roles.size) {
    if (state.roles.isNotEmpty()) {
      rolesListState.animateScrollToItem(0)
    }
  }

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(
                    horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                    vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING),
        horizontalAlignment = Alignment.Start) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween) {
                SignUpBackButton(onClick = callbacks.onBackClick)

                SignUpSkipButton(onClick = callbacks.onSkipClick)
              }

          SignUpMediumSpacer()

          SignUpTitle(text = stringResource(R.string.team_roles_title))
          SignUpSmallSpacer()
          SignUpSubtitle(text = stringResource(R.string.team_roles_subtitle))

          SignUpLargeSpacer()

          RolesFormCard(
              roleName = state.roleName,
              roleDescription = state.roleDescription,
              suggestions = suggestions,
              onRoleNameChange = callbacks.onRoleNameChange,
              onRoleDescriptionChange = callbacks.onRoleDescriptionChange,
              onAddRole = callbacks.onAddRole)

          Spacer(modifier = Modifier.height(SignUpScreenConstants.ROLES_FORM_TO_LIST_SPACING))

          Text(
              text = stringResource(R.string.team_roles_current_section_title),
              style =
                  MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface))

          Spacer(modifier = Modifier.height(SignUpScreenConstants.SECTION_TITLE_TO_CONTENT_SPACING))

          Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(
                targetState = state.roles.isEmpty(),
                label = "roles_list_state",
            ) { isEmpty ->
              if (isEmpty) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  EmptyRolesState(modifier = Modifier.fillMaxWidth())
                }
              } else {
                LazyColumn(
                    state = rolesListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement =
                        Arrangement.spacedBy(SignUpScreenConstants.ROLE_ITEM_SPACING),
                    contentPadding =
                        PaddingValues(bottom = SignUpScreenConstants.ROLES_LIST_BOTTOM_PADDING)) {
                      items(items = state.roles, key = { it.id }) { role ->
                        RoleCard(role = role, onRemoveRole = callbacks.onRemoveRole)
                      }
                    }
              }
            }
          }

          Spacer(modifier = Modifier.height(SignUpScreenConstants.SUBTITLE_TO_CONTENT_SPACING))

          SignUpPrimaryButton(
              text = stringResource(R.string.button_start_now),
              iconRes = R.drawable.ic_arrow_forward,
              onClick = callbacks.onContinueClick,
              enabled = state.roles.isNotEmpty(),
              modifier = Modifier.align(Alignment.CenterHorizontally))
        }
  }
}

@Composable
private fun RolesFormCard(
    roleName: String,
    roleDescription: String,
    suggestions: List<String>,
    onRoleNameChange: (String) -> Unit,
    onRoleDescriptionChange: (String) -> Unit,
    onAddRole: () -> Unit
) {
  Surface(
      modifier = Modifier.fillMaxWidth(),
      tonalElevation = SignUpScreenConstants.SURFACE_TONAL_ELEVATION,
      shadowElevation = SignUpScreenConstants.SURFACE_SHADOW_ELEVATION,
      shape = RoundedCornerShape(SignUpScreenConstants.FORM_CARD_CORNER_RADIUS),
      color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(SignUpScreenConstants.FORM_CARD_PADDING),
            verticalArrangement = Arrangement.spacedBy(SignUpScreenConstants.FORM_FIELD_SPACING)) {
              Text(
                  text = stringResource(R.string.team_roles_add_section_title),
                  style =
                      MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurfaceVariant)

              Column {
                RoleNameDropdownField(
                    value = roleName,
                    onValueChange = onRoleNameChange,
                    suggestions = suggestions,
                    modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(SignUpScreenConstants.TITLE_TO_SUBTITLE_SPACING))

                Text(
                    text = stringResource(R.string.team_roles_name_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier =
                        Modifier.padding(
                            horizontal = SignUpScreenConstants.HELPER_TEXT_HORIZONTAL_PADDING))
              }

              OutlinedTextField(
                  value = roleDescription,
                  onValueChange = onRoleDescriptionChange,
                  modifier = Modifier.fillMaxWidth(),
                  label = { Text(stringResource(R.string.team_roles_description_label)) },
                  minLines = 2,
                  maxLines = 3,
                  singleLine = false)

              Button(
                  onClick = onAddRole,
                  enabled = roleName.isNotBlank(),
                  modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.team_roles_add_button))
                  }
            }
      }
}

@Composable
private fun EmptyRolesState(modifier: Modifier = Modifier) {
  Column(
      modifier = modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Text(
            text = stringResource(R.string.team_roles_empty_state_title),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center)

        Spacer(
            modifier = Modifier.height(SignUpScreenConstants.EMPTY_STATE_TITLE_TO_SUBTITLE_SPACING))

        Text(
            text = stringResource(R.string.team_roles_empty_state_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier =
                Modifier.padding(horizontal = SignUpScreenConstants.EMPTY_STATE_HORIZONTAL_PADDING))
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleNameDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  var isFocused by remember { mutableStateOf(false) }

  val normalized = value.trim()
  val exactMatch = suggestions.any { it.equals(normalized, ignoreCase = true) }

  val filteredSuggestions =
      remember(normalized, suggestions) {
        if (normalized.isBlank()) {
          suggestions
        } else {
          suggestions.filter { it.contains(normalized, ignoreCase = true) }
        }
      }

  val shouldShowDropdown = filteredSuggestions.isNotEmpty() && !exactMatch

  LaunchedEffect(isFocused, value, exactMatch) {
    expanded = when {
      exactMatch -> false
      isFocused && shouldShowDropdown -> true
      !isFocused && value.isBlank() -> false
      else -> expanded
    }
  }

  ExposedDropdownMenuBox(
      expanded = expanded && shouldShowDropdown,
      onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
              onValueChange(newValue)
              val newNormalized = newValue.trim()
              val isExactMatch = suggestions.any { it.equals(newNormalized, ignoreCase = true) }
              expanded = newNormalized.isNotBlank() && !isExactMatch && filteredSuggestions.isNotEmpty()
            },
            modifier =
                modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).onFocusChanged {
                    focusState ->
                  isFocused = focusState.isFocused
                  if (focusState.isFocused && shouldShowDropdown) {
                    expanded = true
                  }
                },
            label = { Text(stringResource(R.string.team_roles_name_label)) },
            singleLine = true,
            trailingIcon = {
              if (filteredSuggestions.isNotEmpty()) {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
              }
            })

        ExposedDropdownMenu(
            expanded = expanded && shouldShowDropdown,
            onDismissRequest = { expanded = false }) {
              filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                      onValueChange(suggestion)
                      expanded = false
                    })
              }
            }
      }
}

@Composable
private fun RoleCard(
    role: TeamRole,
    onRemoveRole: (TeamRole) -> Unit,
    modifier: Modifier = Modifier
) {
  Surface(
      modifier = modifier.fillMaxWidth(),
      shape = RoundedCornerShape(SignUpScreenConstants.ROLE_CARD_CORNER_RADIUS),
      tonalElevation = SignUpScreenConstants.SURFACE_TONAL_ELEVATION,
      shadowElevation = SignUpScreenConstants.SURFACE_SHADOW_ELEVATION,
      color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(SignUpScreenConstants.ROLE_CARD_PADDING)) {
              Text(
                  text = role.name,
                  style =
                      MaterialTheme.typography.titleMedium.copy(
                          fontWeight = FontWeight.SemiBold,
                          color = MaterialTheme.colorScheme.onSurface))

              if (!role.description.isNullOrBlank()) {
                Spacer(
                    modifier =
                        Modifier.height(SignUpScreenConstants.ROLE_NAME_TO_DESCRIPTION_SPACING))
                Text(
                    text = role.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Spacer(
                  modifier =
                      Modifier.height(SignUpScreenConstants.ROLE_DESCRIPTION_TO_BUTTON_SPACING))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onRemoveRole(role) }) {
                  Text(
                      text = stringResource(R.string.team_roles_remove_button),
                      color = MaterialTheme.colorScheme.error,
                      style = MaterialTheme.typography.labelLarge)
                }
              }
            }
      }
}

@Preview(showBackground = true)
@Composable
private fun TeamRolesScreenPreview() {
  AppTheme { TeamRolesScreen() }
}
