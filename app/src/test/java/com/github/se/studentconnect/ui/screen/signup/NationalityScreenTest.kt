package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.se.studentconnect.ui.components.Country
import com.github.se.studentconnect.ui.components.CountryRow
import com.github.se.studentconnect.ui.components.loadCountries
import com.github.se.studentconnect.ui.screen.signup.regularuser.NationalityScreen
import com.github.se.studentconnect.ui.screen.signup.regularuser.SignUpViewModel
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NationalityScreenTest {

  private lateinit var controller: ActivityController<ComponentActivity>
  private lateinit var viewModel: SignUpViewModel

  @Before
  fun setUp() {
    controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
    viewModel = SignUpViewModel()
  }

  @After
  fun tearDown() {
    controller.pause().stop().destroy()
    runOnIdle()
  }

  @Test
  fun `screen reflects nationality validity when selection changes`() {
    composeNationalityScreen()

    assertFalse(viewModel.isNationalityValid)

    viewModel.setNationality("us")
    runOnIdle()

    assertTrue(viewModel.isNationalityValid)

    viewModel.setNationality("")
    runOnIdle()

    assertFalse(viewModel.isNationalityValid)
  }

  @Test
  fun `preselected nationality stays uppercase`() {
    viewModel.setNationality("fr")

    composeNationalityScreen()

    assertEquals("FR", viewModel.state.value.nationality)
  }

  @Test
  fun `loadCountries returns sorted locales with flags`() {
    val countries = loadCountries()

    assertTrue(countries.isNotEmpty())
    assertEquals(countries.sortedBy { it.name }, countries)

    val france = countries.first { it.code == "FR" }
    assertEquals("France", france.name)
    assertEquals(2, france.flag.codePointCount(0, france.flag.length))
  }

  @Test
  fun `countryCodeToEmoji maps valid iso to flag`() {
    assertEquals("🇺🇸", invokeCountryCodeToEmoji("us"))
  }

  @Test
  fun `countryCodeToEmoji falls back for invalid input`() {
    assertEquals("🌐", invokeCountryCodeToEmoji("invalid"))
    assertEquals("🌐", invokeCountryCodeToEmoji("Z"))
  }

  @Test
  fun `country row renders both states and propagates clicks`() {
    var clickCount = 0
    val country = Country(code = "ZZ", name = "Zedland", flag = "\uD83C\uDDFF\uD83C\uDDFF")

    val onSelect = { clickCount += 1 }

    controller.get().setContent {
      AppTheme {
        CountryRow(country = country, isSelected = false, onSelect = onSelect)
        CountryRow(country = country, isSelected = true, onSelect = onSelect)
      }
    }

    runOnIdle()
    onSelect()
    assertEquals(1, clickCount)
  }

  private fun composeNationalityScreen() {
    controller.get().setContent {
      NationalityScreen(viewModel = viewModel, onContinue = {}, onBack = {})
    }
    runOnIdle()
  }

  private fun invokeCountryCodeToEmoji(code: String): String {
    val clazz = Class.forName("com.github.se.studentconnect.ui.components.CountryPickerKt")
    val method = clazz.getDeclaredMethod("countryCodeToEmoji", String::class.java)
    method.isAccessible = true
    return method.invoke(null, code) as String
  }

  private fun runOnIdle() {
    Robolectric.flushForegroundThreadScheduler()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }
}
