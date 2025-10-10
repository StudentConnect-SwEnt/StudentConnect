package com.github.se.studentconnect.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.atomic.AtomicInteger
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileDialogsInstrumentedTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun editNameDialog_saveTrimsValues() {
    var savedFirstName: String? = null
    var savedLastName: String? = null
    var dismissed = false

    composeRule.setContent {
      MaterialTheme {
        EditNameDialog(
            currentFirstName = " Alex ",
            currentLastName = " Doe ",
            onDismiss = { dismissed = true },
            onSave = { first, last ->
              savedFirstName = first
              savedLastName = last
            },
            autoFocus = false)
      }
    }

    composeRule.onNodeWithText("Save").performClick()

    assertEquals("Alex", savedFirstName)
    assertEquals("Doe", savedLastName)
    assertTrue(dismissed)
  }

  @Test
  fun editNameDialog_blankFirstName_preventsSave() {
    val saveInvocations = AtomicInteger(0)
    var dismissed = false

    composeRule.setContent {
      MaterialTheme {
        EditNameDialog(
            currentFirstName = "",
            currentLastName = "Doe",
            onDismiss = { dismissed = true },
            onSave = { _, _ -> saveInvocations.incrementAndGet() },
            autoFocus = false)
      }
    }

    composeRule.onNodeWithText("Save").performClick()

    assertEquals(0, saveInvocations.get())
    composeRule.onNodeWithText("Edit Name").assertIsDisplayed()
    assertFalse(dismissed)
  }

  @Test
  fun birthdayPickerDialog_returnsFormattedDate() {
    var savedBirthday: String? = null

    composeRule.setContent {
      MaterialTheme {
        BirthdayPickerDialog(
            currentBirthday = "24/12/1994", onDismiss = {}, onSave = { savedBirthday = it })
      }
    }

    composeRule.onNodeWithText("Save").performClick()

    val result = requireNotNull(savedBirthday)
    assertTrue(result.matches(Regex("\\d{2}/\\d{2}/\\d{4}")))
  }

  @Test
  fun editActivitiesDialog_allowsEditingAndSaves() {
    var savedActivities: String? = null

    composeRule.setContent {
      MaterialTheme {
        EditActivitiesDialog(
            currentActivities = listOf("Chess"),
            onDismiss = {},
            onSave = { savedActivities = it },
            autoFocus = false)
      }
    }

    val activitiesField = composeRule.onNode(hasText("Chess", substring = true, ignoreCase = false))
    activitiesField.performClick()
    activitiesField.performTextInput(", Hiking")

    composeRule.onNodeWithText("Save").performClick()

    assertEquals("Chess, Hiking", savedActivities)
  }
}
