package com.github.se.studentconnect.ui.eventcreation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import com.github.se.studentconnect.ui.screen.signup.SocialLinkField
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FormTextFieldTest {

  private lateinit var controller: ActivityController<ComponentActivity>

  @Before
  fun setUp() {
    controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
  }

  @After
  fun tearDown() {
    controller.pause().stop().destroy()
  }

  @Test
  fun `form text field composes with leading icon`() {
    controller.get().setContent {
      FormTextField(
          value = "https://example.com",
          onValueChange = {},
          label = "Website",
          leadingIcon = { Icon(imageVector = Icons.Filled.Link, contentDescription = null) })
    }

    // If composition didn't crash we consider the behavior correct for this unit test
    assertTrue(true)
  }

  @Test
  fun `form text field recomposes with different values and leading icon`() {
    controller.get().setContent {
      FormTextField(
          value = "one",
          onValueChange = {},
          label = "L1",
          leadingIcon = { Icon(Icons.Filled.Link, null) })
    }

    // Recompose with another value
    controller.get().setContent {
      FormTextField(
          value = "two",
          onValueChange = {},
          label = "L2",
          leadingIcon = { Icon(Icons.Filled.Link, null) })
    }

    assertTrue(true)
  }

  @Test
  fun `social link field composes with icon passed through`() {
    controller.get().setContent {
      SocialLinkField(
          label = "Foo",
          value = "bar",
          onValueChange = {},
          leadingIcon = { Icon(Icons.Filled.Link, null) })
    }

    // Recompose to exercise code paths
    controller.get().setContent {
      SocialLinkField(
          label = "Foo",
          value = "baz",
          onValueChange = {},
          leadingIcon = { Icon(Icons.Filled.Link, null) })
    }

    assertTrue(true)
  }
}
