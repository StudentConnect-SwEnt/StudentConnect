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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
            title = { Text("My Organizations", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadUserOrganizations() }) { Text("Retry") }
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
                      modifier = Modifier.padding(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No Organizations Yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text =
                                "You're not a member of any organizations. Create one or join an existing one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(32.dp))

                        // Create Organization Button
                        Button(
                            onClick = onCreateOrganization,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(28.dp)) {
                              Icon(
                                  imageVector = Icons.Default.Add,
                                  contentDescription = null,
                                  modifier = Modifier.size(24.dp))
                              Spacer(modifier = Modifier.width(8.dp))
                              Text(
                                  text = "Create Organization",
                                  fontSize = 18.sp,
                                  fontWeight = FontWeight.SemiBold)
                            }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Join Organization Button
                        Button(
                            onClick = onJoinOrganization,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            shape = RoundedCornerShape(28.dp)) {
                              Icon(
                                  imageVector = Icons.Default.PersonAdd,
                                  contentDescription = null,
                                  modifier = Modifier.size(24.dp))
                              Spacer(modifier = Modifier.width(8.dp))
                              Text(
                                  text = "Join Organization",
                                  fontSize = 18.sp,
                                  fontWeight = FontWeight.SemiBold)
                            }
                      }
                }
          }
          else -> {
            // Show list of organizations
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  item {
                    Text(
                        text = "Your Organizations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp))
                  }

                  items(uiState.userOrganizations) { organization ->
                    OrganizationCard(
                        organizationName = organization.name,
                        organizationType = organization.type.name,
                        memberCount = organization.memberUids.size,
                        onClick = { onOrganizationClick(organization.id) })
                  }

                  item {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Create New Organization Button
                    Button(
                        onClick = onCreateOrganization,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        shape = RoundedCornerShape(28.dp)) {
                          Icon(
                              imageVector = Icons.Default.Add,
                              contentDescription = null,
                              modifier = Modifier.size(24.dp))
                          Spacer(modifier = Modifier.width(8.dp))
                          Text(
                              text = "Create New Organization",
                              fontSize = 16.sp,
                              fontWeight = FontWeight.Medium)
                        }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Join Organization Button
                    Button(
                        onClick = onJoinOrganization,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                        shape = RoundedCornerShape(28.dp)) {
                          Icon(
                              imageVector = Icons.Default.PersonAdd,
                              contentDescription = null,
                              modifier = Modifier.size(24.dp))
                          Spacer(modifier = Modifier.width(8.dp))
                          Text(
                              text = "Join Organization",
                              fontSize = 16.sp,
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
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
              // Organization Icon/Avatar
              Box(
                  modifier =
                      Modifier.size(56.dp)
                          .clip(CircleShape)
                          .background(MaterialTheme.colorScheme.primaryContainer)
                          .border(
                              width = 2.dp,
                              color = MaterialTheme.colorScheme.primary,
                              shape = CircleShape),
                  contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                  }

              Spacer(modifier = Modifier.width(16.dp))

              // Organization Info
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = organizationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = organizationType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$memberCount members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
      }
}
