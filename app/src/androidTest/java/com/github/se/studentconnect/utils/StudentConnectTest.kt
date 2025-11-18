package com.github.se.studentconnect.utils

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.se.studentconnect.HttpClientProvider
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

const val UI_WAIT_TIMEOUT = 5_000L

/**
 * Custom annotation to disable anonymous sign in for a test.
 *
 * Example:
 * ```
 * @Test
 * @NoAnonymousSignIn
 * fun my_test() {
 *   // ...
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class NoAnonymousSignIn

/** Rule for the NoAnonymousSignIn annotation. */
private class MethodInfoRule : TestWatcher() {
  var method: Method? = null

  override fun starting(description: Description) {
    method = description.testClass.getMethod(description.methodName)
  }
}

/** Base class for all StudentConnect tests, providing common setup and utility functions. */
abstract class StudentConnectTest {

  // rule for NoAnonymousSignIn annotation
  @get:Rule private val methodInfo = MethodInfoRule()

  abstract fun createInitializedRepository(): EventRepository

  open fun initializeHTTPClient(): OkHttpClient = FakeHttpClient.getClient()

  val repository: EventRepository
    get() = EventRepositoryProvider.repository

  val httpClient
    get() = HttpClientProvider.client

  val currentUser: FirebaseUser
    get() = FirebaseEmulator.auth.currentUser!!

  init {
    assert(FirebaseEmulator.isRunning) { "FirebaseEmulator must be running" }
  }

  @Before
  open fun setUp() {
    EventRepositoryProvider.repository = createInitializedRepository()
    HttpClientProvider.client = initializeHTTPClient()

    // check if @NoAnonymousSignIn is present
    val annotated = methodInfo.method?.isAnnotationPresent(NoAnonymousSignIn::class.java) == true

    val shouldSignInAnonymously = FirebaseEmulator.isRunning && !annotated

    if (shouldSignInAnonymously) {
      runTest { FirebaseEmulator.auth.signInAnonymously().await() }
    }
  }

  @After
  open fun tearDown() {
    if (FirebaseEmulator.isRunning) {
      FirebaseEmulator.auth.signOut()
      FirebaseEmulator.clearAuthEmulator()
      FirebaseEmulator.clearFirestoreEmulator()
      FirebaseEmulator.clearStorageEmulator()
    }
  }

  fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>
      .checkActivityStateOnPressBack(shouldFinish: Boolean) {
    activityRule.scenario.onActivity { activity ->
      activity.onBackPressedDispatcher.onBackPressed()
    }
    waitUntil { activity.isFinishing == shouldFinish }
    assertEquals(shouldFinish, activity.isFinishing)
  }

  companion object {
    fun Timestamp.toDateString(): String {
      val date = this.toDate()
      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
      return dateFormat.format(date)
    }

    fun Timestamp.Companion.fromDate(year: Int, month: Int, day: Int): Timestamp {
      val calendar = Calendar.getInstance()
      calendar.set(year, month, day, 0, 0, 0)
      return Timestamp(calendar.time)
    }
  }
}
