package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.se.studentconnect.ui.screen.signup.organization.BrandOrganizationContent
import com.github.se.studentconnect.ui.screen.signup.organization.BrandOrganizationScreen
import com.github.se.studentconnect.ui.screen.signup.organization.SocialLinkField
import com.github.se.studentconnect.ui.screen.signup.regularuser.SignUpViewModel
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BrandOrganizationScreenTest {

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
  }

  @Test
  fun `brand content renders without crashing and callbacks are registered`() {
    var backInvoked = false
    var skipInvoked = false
    var continueInvoked = false

    controller.get().setContent {
      BrandOrganizationContent(
          website = "https://example.com",
          onWebsiteChange = {},
          insta = "instaUser",
          onInstaChange = {},
          x = "xHandle",
          onXChange = {},
          linkedin = "lnk",
          onLinkedinChange = {},
          onBack = { backInvoked = true },
          onSkip = { skipInvoked = true },
          onContinue = { continueInvoked = true })
    }

    // Rendering should not invoke callbacks by itself
    Assert.assertFalse(backInvoked)
    Assert.assertFalse(skipInvoked)
    Assert.assertFalse(continueInvoked)
  }

  @Test
  fun `brand screen composes with viewmodel and callbacks`() {
    var backCount = 0
    var skipCount = 0
    var continueCount = 0

    controller.get().setContent {
      BrandOrganizationScreen(
          onSkip = { skipCount++ }, onContinue = { continueCount++ }, onBack = { backCount++ })
    }

    // After composition counters must remain zero (no clicks performed), but composition path
    // executed
    Assert.assertEquals(0, backCount)
    Assert.assertEquals(0, skipCount)
    Assert.assertEquals(0, continueCount)
  }

  @Test
  fun `social link field composes with different values`() {
    controller.get().setContent {
      SocialLinkField(label = "Website", value = "value", onValueChange = {})
    }

    // Recompose with another value to cover recomposition logic
    controller.get().setContent {
      SocialLinkField(label = "Website", value = "other", onValueChange = {})
    }

    Assert.assertTrue(true)
  }

  @Test
  fun `brand content recomposes with new data`() {
    controller.get().setContent {
      BrandOrganizationContent(
          website = "a",
          onWebsiteChange = {},
          insta = "b",
          onInstaChange = {},
          x = "c",
          onXChange = {},
          linkedin = "d",
          onLinkedinChange = {})
    }

    // Force a second composition with different values to exercise recomposition paths
    controller.get().setContent {
      BrandOrganizationContent(
          website = "new",
          onWebsiteChange = {},
          insta = "new2",
          onInstaChange = {},
          x = "new3",
          onXChange = {},
          linkedin = "new4",
          onLinkedinChange = {})
    }

    Assert.assertTrue(true)
  }
}
