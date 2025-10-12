package com.github.se.studentconnect.ui.screen.signup

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthRepository
import com.github.se.studentconnect.ui.theme.AppTheme

object GetStartedScreenTestTags {
  const val CAROUSEL = "getting_started_carousel"
  const val GOOGLE_BUTTON = "google_sign_in_button"
}

private data class CarouselItem(
    val id: Int,
    @DrawableRes val imageResId: Int,
    val contentDescription: String
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetStartedScreen(
    onSignedIn: () -> Unit,
    onSignInError: (String) -> Unit = {},
    viewModel: GetStartedViewModel = viewModel()
) {
  val context = LocalContext.current
  val credentialManager = CredentialManager.create(context)
  val uiState by viewModel.uiState.collectAsState()
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(uiState.user) {
    if (uiState.user != null) {
      errorMessage = null
      onSignedIn()
    }
  }

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      errorMessage = it
      onSignInError(it)
      viewModel.clearErrorMsg()
    }
  }

  Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Reserved logo space
        Spacer(Modifier.height(64.dp))

        // Big carousel
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
          val items = remember {
            listOf(
                CarouselItem(0, R.drawable.fond, "Connect"),
                CarouselItem(1, R.drawable.fond, "Discover"),
                CarouselItem(2, R.drawable.fond, "Belong"),
            )
          }

          HorizontalMultiBrowseCarousel(
              state = rememberCarouselState { items.size },
              modifier =
                  Modifier.fillMaxSize()
                      .fillMaxHeight()
                      .padding(vertical = 16.dp)
                      .testTag(GetStartedScreenTestTags.CAROUSEL),
              preferredItemWidth = 275.dp,
              itemSpacing = 8.dp,
              contentPadding = PaddingValues(horizontal = 16.dp)) { index ->
                val item = items[index]
                Image(
                    modifier = Modifier.fillMaxHeight().maskClip(MaterialTheme.shapes.extraLarge),
                    painter = painterResource(id = item.imageResId),
                    contentDescription = item.contentDescription,
                    contentScale = ContentScale.Crop)
              }
        }

        // Tagline
        Text(
            text = "Never miss out again.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp))

        // Filled Google sign-in button with Google logo
        SignInButton(
            modifier =
                Modifier.fillMaxWidth()
                    .height(56.dp)
                    .testTag(GetStartedScreenTestTags.GOOGLE_BUTTON),
            isLoading = uiState.isLoading,
            onSignInClick = {
              errorMessage = null
              viewModel.signIn(context, credentialManager)
            })

        errorMessage?.let { error ->
          Spacer(Modifier.height(8.dp))
          Text(
              text = error,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(8.dp))
      }
}

@Composable
private fun SignInButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onSignInClick: () -> Unit
) {
  Button(
      onClick = onSignInClick,
      modifier = modifier,
      enabled = !isLoading,
      shape = RoundedCornerShape(28.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google logo",
                  modifier = Modifier.size(24.dp))
              Spacer(Modifier.width(12.dp))
              Text("Get Started With Google", style = MaterialTheme.typography.titleMedium)
            }
      }
}

@Preview(
    name = "Getting Started – Preview",
    showBackground = true,
    backgroundColor = 0xFFF4F1F6,
    widthDp = 412,
    heightDp = 915)
@Composable
private fun GettingStarted_Preview() {
  AppTheme {
    val previewViewModel = remember { GetStartedViewModel(PreviewAuthRepository()) }
    GetStartedScreen(onSignedIn = {}, onSignInError = {}, viewModel = previewViewModel)
  }
}

private class PreviewAuthRepository : AuthRepository {
  override suspend fun signInWithGoogle(
      credential: androidx.credentials.Credential
  ): Result<com.google.firebase.auth.FirebaseUser> =
      Result.failure(UnsupportedOperationException("Preview only"))

  override fun signOut(): Result<Unit> = Result.success(Unit)
}
