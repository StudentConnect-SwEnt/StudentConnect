package com.github.se.studentconnect.ui.screen.signup.organization

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.organization.OrganizationRole
import com.github.se.studentconnect.ui.screen.signup.SignUpBackButton
import com.github.se.studentconnect.ui.screen.signup.SignUpLargeSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpMediumSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpPrimaryButton
import com.github.se.studentconnect.ui.screen.signup.SignUpScreenConstants
import com.github.se.studentconnect.ui.screen.signup.SignUpSkipButton
import com.github.se.studentconnect.ui.screen.signup.SignUpSmallSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpSubtitle
import com.github.se.studentconnect.ui.screen.signup.SignUpTitle

/**
 * State for the Team Roles screen.
 *
 * This state can be managed by a ViewModel (e.g., OrganizationSignupViewModel) to separate business
 * logic from UI.
 *
 * @property roleName Current input value for the role name field.
 * @property roleDescription Current input value for the role description field.
 * @property roles List of roles that have been added.
 */
data class TeamRolesState(
    val roleName: String = "",
    val roleDescription: String = "",
    val roles: List<OrganizationRole> = emptyList()
)

/**
 * Callbacks for the Team Roles screen.
 *
 * These callbacks should be provided by a ViewModel to handle business logic operations.
 *
 * @property onRoleNameChange Called when the role name input changes.
 * @property onRoleDescriptionChange Called when the role description input changes.
 * @property onAddRole Called when a new role should be added. The implementation should create an
 *   OrganizationRole from the current roleName and roleDescription values.
 * @property onRemoveRole Called when a role should be removed.
 * @property onBackClick Called when the back button is clicked.
 * @property onSkipClick Called when the skip button is clicked.
 * @property onContinueClick Called when the continue button is clicked.
 */
data class TeamRolesCallbacks(
    val onRoleNameChange: (String) -> Unit,
    val onRoleDescriptionChange: (String) -> Unit,
    val onAddRole: () -> Unit,
    val onRemoveRole: (OrganizationRole) -> Unit,
    val onBackClick: () -> Unit,
    val onSkipClick: () -> Unit,
    val onContinueClick: () -> Unit
)

/**
 * Team Roles screen for the organization signup flow.
 *
 * This screen allows users to add and manage roles for their organization. The screen is designed
 * to work with a ViewModel (e.g., OrganizationSignupViewModel) by accepting state and callbacks as
 * parameters.
 *
 * @param state The current state of the screen (should come from ViewModel).
 * @param callbacks Callbacks for user actions (should come from ViewModel).
 * @param modifier Optional modifier for the screen.
 */
@Composable
fun TeamRolesScreen(
    viewModel: OrganizationSignUpViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
  val state by viewModel.state

  var currentRoleName by remember { mutableStateOf("") }
  var currentRoleDescription by remember { mutableStateOf("") }

  TeamRolesScreen(
      state =
          TeamRolesState(
              roleName = currentRoleName,
              roleDescription = currentRoleDescription,
              roles = state.teamRoles),
      callbacks =
          TeamRolesCallbacks(
              onRoleNameChange = { currentRoleName = it },
              onRoleDescriptionChange = { currentRoleDescription = it },
              onAddRole = {
                if (currentRoleName.isNotBlank()) {
                  viewModel.addRole(
                      OrganizationRole(
                          name = currentRoleName.trim(),
                          description = currentRoleDescription.trim().ifBlank { null }))
                  // Reset form
                  currentRoleName = ""
                  currentRoleDescription = ""
                }
              },
              onRemoveRole = { role -> viewModel.removeRole(role) },
              onBackClick = {
                viewModel.prevStep()
                onBack()
              },
              onSkipClick = {
                viewModel.setRoles(emptyList()) // Clear roles on skip
                onContinue()
              },
              onContinueClick = { onContinue() }),
      modifier = modifier)
}

/** Stateless Team Roles screen. */
@Composable
fun TeamRolesScreen(
    state: TeamRolesState,
    callbacks: TeamRolesCallbacks,
    modifier: Modifier = Modifier
) {
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
      state = state, suggestions = suggestions, callbacks = callbacks, modifier = modifier)
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

          Spacer(
              modifier =
                  Modifier.Companion.height(SignUpScreenConstants.ROLES_FORM_TO_LIST_SPACING))

          Text(
              text = stringResource(R.string.team_roles_current_section_title),
              style =
                  MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface))

          Spacer(
              modifier =
                  Modifier.Companion.height(SignUpScreenConstants.SECTION_TITLE_TO_CONTENT_SPACING))

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
                      itemsIndexed(items = state.roles) { index, role ->
                        RoleCard(role = role, onRemoveRole = callbacks.onRemoveRole)
                      }
                    }
              }
            }
          }

          Spacer(
              modifier =
                  Modifier.Companion.height(SignUpScreenConstants.SUBTITLE_TO_CONTENT_SPACING))

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
internal fun RolesFormCard(
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

                Spacer(
                    modifier =
                        Modifier.Companion.height(SignUpScreenConstants.TITLE_TO_SUBTITLE_SPACING))

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
internal fun EmptyRolesState(modifier: Modifier = Modifier) {
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
            modifier =
                Modifier.Companion.height(
                    SignUpScreenConstants.EMPTY_STATE_TITLE_TO_SUBTITLE_SPACING))

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
internal fun RoleNameDropdownField(
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
    expanded =
        calculateExpandedState(exactMatch, isFocused, shouldShowDropdown, value.isBlank(), expanded)
  }

  ExposedDropdownMenuBox(
      expanded = expanded && shouldShowDropdown, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
              onValueChange(newValue)
              expanded = shouldExpandOnTextChange(newValue, suggestions, filteredSuggestions)
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
            expanded = expanded && shouldShowDropdown, onDismissRequest = { expanded = false }) {
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

internal fun calculateExpandedState(
    exactMatch: Boolean,
    isFocused: Boolean,
    shouldShowDropdown: Boolean,
    isValueBlank: Boolean,
    currentExpanded: Boolean
): Boolean {
  return when {
    exactMatch -> false
    isFocused && shouldShowDropdown -> true
    !isFocused && isValueBlank -> false
    else -> currentExpanded
  }
}

internal fun shouldExpandOnTextChange(
    newValue: String,
    suggestions: List<String>,
    filteredSuggestions: List<String>
): Boolean {
  val newNormalized = newValue.trim()
  val isExactMatch = suggestions.any { it.equals(newNormalized, ignoreCase = true) }
  return newNormalized.isNotBlank() && !isExactMatch && filteredSuggestions.isNotEmpty()
}

@Composable
internal fun RoleCard(
    role: OrganizationRole,
    onRemoveRole: (OrganizationRole) -> Unit,
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
                        Modifier.Companion.height(
                            SignUpScreenConstants.ROLE_NAME_TO_DESCRIPTION_SPACING))
                Text(
                    text = role.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Spacer(
                  modifier =
                      Modifier.Companion.height(
                          SignUpScreenConstants.ROLE_DESCRIPTION_TO_BUTTON_SPACING))

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
