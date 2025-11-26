package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.model.organization.OrganizationModel
import com.github.se.studentconnect.ui.screen.signup.SignUpPrimaryButton
import com.github.se.studentconnect.ui.screen.signup.SignUpScreenConstants

/**
 * Screen shown to indicate that the organization signup flow is to be continued later.
 *
 * Displays basic information about the created organization and exposes a logout action.
 *
 * @param organization the organization that was created (may be null if creation failed)
 * @param onLogout callback invoked when the user taps the Logout button
 * @param onBack optional callback invoked when the user navigates back
 */
@Composable
fun OrganizationToBeContinuedScreen(
    organization: OrganizationModel?,
    onLogout: () -> Unit = {},
    onBack: () -> Unit = {}
) {
  Column(
      modifier = Modifier.fillMaxSize().padding(SignUpScreenConstants.SCREEN_VERTICAL_PADDING),
      horizontalAlignment = Alignment.CenterHorizontally) {
        if (organization == null) {
          Text(text = "Organization information unavailable")
        } else {
          Text(text = organization.name)
          Spacer(modifier = Modifier.size(8.dp))
          Text(text = organization.type.name)
          Spacer(modifier = Modifier.size(8.dp))
          Text(text = organization.description ?: "")
          Spacer(modifier = Modifier.size(8.dp))
          Text(text = "Location: ${organization.location ?: "N/A"}")
          Spacer(modifier = Modifier.size(8.dp))
          Text(text = "Domains: ${organization.mainDomains.joinToString(", ")}")
        }

        Spacer(modifier = Modifier.size(16.dp))

        SignUpPrimaryButton(
            text = "Logout",
            onClick = { onLogout() },
            modifier = Modifier.size(width = 160.dp, height = 48.dp),
            iconRes = null)
      }
}
