package com.github.se.studentconnect.model.authentication

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * A Firebase implementation of [AuthRepository].
 *
 * Retrieves a Google ID token via Credential Manager and authenticates the user with Firebase. Also
 * handles sign-out and credential state clearing.
 *
 * @param auth The [FirebaseAuth] instance for Firebase authentication.
 * @param helper A [GoogleSignInHelper] to extract Google ID token credentials and convert them to
 *   Firebase credentials.
 */
class AuthRepositoryFirebase(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val helper: GoogleSignInHelper = DefaultGoogleSignInHelper()
) : AuthRepository {

  /** Builds the Google sign-in option to be used with Credential Manager. */
  fun getGoogleSignInOption(serverClientId: String): GetSignInWithGoogleOption =
      GetSignInWithGoogleOption.Builder(serverClientId).build()

  override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> {
    return try {
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val idToken = helper.extractIdTokenCredential(credential.data).idToken
        val firebaseCred = helper.toFirebaseCredential(idToken)

        val user = auth.signInWithCredential(firebaseCred).await().user
        if (user == null) {
          Result.failure(IllegalStateException("Login failed: missing Firebase user"))
        } else {
          Result.success(user)
        }
      } else {
        Result.failure(IllegalStateException("Login failed: not a Google ID credential"))
      }
    } catch (ce: CancellationException) {
      throw ce
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Login failed: ${e.localizedMessage ?: "Unexpected error."}", e))
    }
  }

  override fun signOut(): Result<Unit> =
      try {
        auth.signOut()
        Result.success(Unit)
      } catch (e: Exception) {
        Result.failure(
            IllegalStateException("Logout failed: ${e.localizedMessage ?: "Unexpected error."}", e))
      }
}
