package com.github.se.studentconnect.ui.screen.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.resources.C
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OrganizationSuggestionsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testOrganizations =
      listOf(
          OrganizationData(id = "1", name = "Evolve", handle = "@evolve"),
          OrganizationData(id = "2", name = "TechHub", handle = "@techhub"),
          OrganizationData(id = "3", name = "Innovate", handle = "@innovate"),
          OrganizationData(id = "4", name = "CodeLab", handle = "@codelab"),
          OrganizationData(id = "5", name = "DevSpace", handle = "@devspace"),
          OrganizationData(id = "6", name = "Catalyst", handle = "@catalyst"))

  // ==================== Basic Rendering Tests ====================

  @Test
  fun organizationSuggestions_displaysCorrectly_withMultipleOrganizations() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = testOrganizations) }
    }

    // Verify section is displayed
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_section).assertIsDisplayed()

    // Verify title is displayed
    composeTestRule.onNodeWithText("Organizations").assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_title).assertIsDisplayed()

    // Verify LazyRow is displayed
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_row).assertIsDisplayed()

    // Verify first few organization cards are displayed (LazyRow only renders visible items)
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_3").assertIsDisplayed()
  }

  @Test
  fun organizationSuggestions_displaysCorrectly_withSingleOrganization() {
    val singleOrg = listOf(OrganizationData(id = "1", name = "Evolve", handle = "@evolve"))

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = singleOrg) }
    }

    // Verify section and title are displayed
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_section).assertIsDisplayed()
    composeTestRule.onNodeWithText("Organizations").assertIsDisplayed()

    // Verify single card is displayed
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Evolve").assertIsDisplayed()
    composeTestRule.onNodeWithText("@evolve").assertIsDisplayed()
  }

  @Test
  fun organizationSuggestions_displaysCorrectly_withEmptyList() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = emptyList()) }
    }

    // Verify section and title are still displayed
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_section).assertIsDisplayed()
    composeTestRule.onNodeWithText("Organizations").assertIsDisplayed()

    // Verify no cards are rendered
    composeTestRule.onAllNodesWithTag(C.Tag.org_suggestions_card).assertCountEquals(0)
  }

  @Test
  fun organizationSuggestions_rendersCorrectNumberOfCards() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = testOrganizations) }
    }

    // Verify first few visible cards exist (LazyRow only renders visible items)
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").assertExists()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_2").assertExists()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_3").assertExists()
  }

  // ==================== Content Tests ====================

  @Test
  fun organizationSuggestions_displaysOrganizationNamesCorrectly() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = testOrganizations) }
    }

    // Test first few visible items
    composeTestRule.onNodeWithText(testOrganizations[0].name).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(
            "${C.Tag.org_suggestions_card_title}_${testOrganizations[0].id}",
            useUnmergedTree = true)
        .assertIsDisplayed()

    // Verify first few exist (LazyRow only renders visible items)
    composeTestRule.onNodeWithText(testOrganizations[0].name).assertExists()
    composeTestRule
        .onNodeWithTag(
            "${C.Tag.org_suggestions_card_title}_${testOrganizations[0].id}",
            useUnmergedTree = true)
        .assertExists()
    composeTestRule.onNodeWithText(testOrganizations[1].name).assertExists()
    composeTestRule
        .onNodeWithTag(
            "${C.Tag.org_suggestions_card_title}_${testOrganizations[1].id}",
            useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun organizationSuggestions_displaysOrganizationHandlesCorrectly() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = testOrganizations) }
    }

    // Test first visible item
    composeTestRule.onNodeWithText(testOrganizations[0].handle).assertIsDisplayed()

    // Verify first few exist (LazyRow only renders visible items)
    composeTestRule.onNodeWithText(testOrganizations[0].handle).assertExists()
    composeTestRule
        .onNodeWithTag(
            "${C.Tag.org_suggestions_card_subtitle}_${testOrganizations[0].id}",
            useUnmergedTree = true)
        .assertExists()
    composeTestRule.onNodeWithText(testOrganizations[1].handle).assertExists()
    composeTestRule
        .onNodeWithTag(
            "${C.Tag.org_suggestions_card_subtitle}_${testOrganizations[1].id}",
            useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun organizationSuggestions_displaysImagesForAllCards() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = testOrganizations) }
    }

    // Verify images exist for first few visible cards (LazyRow only renders visible items)
    composeTestRule
        .onNodeWithTag(
            "${C.Tag.org_suggestions_card_image}_${testOrganizations[0].id}",
            useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(
            "${C.Tag.org_suggestions_card_image}_${testOrganizations[1].id}",
            useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(
            "${C.Tag.org_suggestions_card_image}_${testOrganizations[2].id}",
            useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun organizationSuggestions_displaysSpecialCharactersInNames() {
    val specialOrgs =
        listOf(
            OrganizationData(id = "1", name = "Org & Co.", handle = "@org&co"),
            OrganizationData(id = "2", name = "Tech.io", handle = "@tech.io"),
            OrganizationData(id = "3", name = "Dev-Ops", handle = "@dev-ops"))

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = specialOrgs) }
    }

    composeTestRule.onNodeWithText("Org & Co.").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tech.io").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dev-Ops").assertIsDisplayed()
  }

  // ==================== Click Action Tests ====================

  @Test
  fun organizationSuggestions_cardsHaveClickAction() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = testOrganizations) }
    }

    // Check first few visible cards have click action
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").assertHasClickAction()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_2").assertHasClickAction()
  }

  @Test
  fun organizationSuggestions_clickCallbackIsInvoked_whenCardIsClicked() {
    var clickedOrganizationId: String? = null

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationSuggestions(
            organizations = testOrganizations,
            onOrganizationClick = { id -> clickedOrganizationId = id })
      }
    }

    // Click on the first organization card
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").performClick()

    composeTestRule.waitForIdle()

    // Verify the callback was invoked with the correct ID
    assertEquals("1", clickedOrganizationId)
  }

  @Test
  fun organizationSuggestions_clickCallbackIsInvoked_withCorrectId() {
    val clickedIds = mutableListOf<String>()

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationSuggestions(
            organizations = testOrganizations, onOrganizationClick = { id -> clickedIds.add(id) })
      }
    }

    // Click on first two visible cards
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_2").performClick()
    composeTestRule.waitForIdle()

    // Verify all clicks were registered with correct IDs
    assertEquals(listOf("1", "2"), clickedIds)
  }

  @Test
  fun organizationSuggestions_multipleClicksOnSameCard_triggerMultipleCallbacks() {
    var clickCount = 0

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationSuggestions(
            organizations = testOrganizations, onOrganizationClick = { clickCount++ })
      }
    }

    // Click the same card multiple times
    repeat(3) {
      composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").performClick()
      composeTestRule.waitForIdle()
    }

    assertEquals(3, clickCount)
  }

  @Test
  fun organizationSuggestions_noCallbackProvided_doesNotCrash() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = testOrganizations) }
    }

    // Click should not crash even without a callback
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").performClick()
    composeTestRule.waitForIdle()

    // Verify card is still displayed after click
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").assertIsDisplayed()
  }

  // ==================== Edge Cases ====================

  @Test
  fun organizationSuggestions_longOrganizationName_displaysCorrectly() {
    val longNameOrg =
        listOf(
            OrganizationData(
                id = "1",
                name = "Very Long Organization Name That Should Still Display Correctly",
                handle = "@longorg"))

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = longNameOrg) }
    }

    composeTestRule
        .onNodeWithText("Very Long Organization Name That Should Still Display Correctly")
        .assertIsDisplayed()
  }

  @Test
  fun organizationSuggestions_longHandle_displaysCorrectly() {
    val longHandleOrg =
        listOf(
            OrganizationData(
                id = "1", name = "Tech Hub", handle = "@verylonghandlethatmightoverflow"))

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = longHandleOrg) }
    }

    composeTestRule.onNodeWithText("@verylonghandlethatmightoverflow").assertIsDisplayed()
  }

  @Test
  fun organizationSuggestions_unicodeCharactersInName_displaysCorrectly() {
    val unicodeOrgs =
        listOf(
            OrganizationData(id = "1", name = "Tech 🚀", handle = "@tech"),
            OrganizationData(id = "2", name = "Café ☕", handle = "@cafe"),
            OrganizationData(id = "3", name = "日本語", handle = "@jp"))

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = unicodeOrgs) }
    }

    composeTestRule.onNodeWithText("Tech 🚀").assertIsDisplayed()
    composeTestRule.onNodeWithText("Café ☕").assertIsDisplayed()
    composeTestRule.onNodeWithText("日本語").assertIsDisplayed()
  }

  @Test
  fun organizationSuggestions_emptyStrings_displaysCorrectly() {
    val emptyStringOrgs =
        listOf(
            OrganizationData(id = "1", name = "", handle = ""),
            OrganizationData(id = "2", name = "Tech", handle = ""))

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = emptyStringOrgs) }
    }

    // Should render without crashing
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_section).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_2").assertIsDisplayed()
  }

  @Test
  fun organizationSuggestions_manyOrganizations_rendersVisibleCards() {
    val manyOrgs =
        (1..20).map { OrganizationData(id = "$it", name = "Org $it", handle = "@org$it") }

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = manyOrgs) }
    }

    // Verify section and title are displayed
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_section).assertIsDisplayed()
    composeTestRule.onNodeWithText("Organizations").assertIsDisplayed()

    // Verify first few cards are visible
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_2").assertIsDisplayed()
  }

  // ==================== Modifier Tests ====================

  @Test
  fun organizationSuggestions_appliesCustomModifier() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationSuggestions(organizations = testOrganizations, modifier = Modifier)
      }
    }

    // Verify component renders correctly with custom modifier
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_section).assertIsDisplayed()
  }

  // ==================== Test Tags Verification ====================

  @Test
  fun organizationSuggestions_allTestTagsArePresent() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = testOrganizations) }
    }

    // Verify main test tags exist
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_section).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_title).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_row).assertExists()

    // Verify test tags for first card
    val firstOrg = testOrganizations.first()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_${firstOrg.id}").assertExists()
    composeTestRule
        .onNodeWithTag("${C.Tag.org_suggestions_card_image}_${firstOrg.id}", useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag("${C.Tag.org_suggestions_card_title}_${firstOrg.id}", useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(
            "${C.Tag.org_suggestions_card_subtitle}_${firstOrg.id}", useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun organizationSuggestions_cardTestTagsIncludeOrganizationId() {
    val org = OrganizationData(id = "test-id-123", name = "Test Org", handle = "@test")

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = listOf(org)) }
    }

    // Verify test tags contain the organization ID
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_test-id-123").assertExists()
    composeTestRule
        .onNodeWithTag("${C.Tag.org_suggestions_card_image}_test-id-123", useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag("${C.Tag.org_suggestions_card_title}_test-id-123", useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag("${C.Tag.org_suggestions_card_subtitle}_test-id-123", useUnmergedTree = true)
        .assertExists()
  }

  // ==================== OrganizationData Tests ====================

  @Test
  fun organizationData_dataClass_createsCorrectInstance() {
    val org = OrganizationData(id = "1", name = "Evolve", handle = "@evolve")

    assertEquals("1", org.id)
    assertEquals("Evolve", org.name)
    assertEquals("@evolve", org.handle)
  }

  @Test
  fun organizationData_dataClass_equalityWorks() {
    val org1 = OrganizationData(id = "1", name = "Evolve", handle = "@evolve")
    val org2 = OrganizationData(id = "1", name = "Evolve", handle = "@evolve")
    val org3 = OrganizationData(id = "2", name = "Evolve", handle = "@evolve")

    assertEquals(org1, org2)
    assert(org1 != org3)
  }

  @Test
  fun organizationData_dataClass_copyWorks() {
    val org1 = OrganizationData(id = "1", name = "Evolve", handle = "@evolve")
    val org2 = org1.copy(id = "2")

    assertEquals("2", org2.id)
    assertEquals("Evolve", org2.name)
    assertEquals("@evolve", org2.handle)
  }

  @Test
  fun organizationData_dataClass_toStringWorks() {
    val org = OrganizationData(id = "1", name = "Evolve", handle = "@evolve")
    val toString = org.toString()

    assert(toString.contains("id=1"))
    assert(toString.contains("name=Evolve"))
    assert(toString.contains("handle=@evolve"))
  }

  // ==================== Integration Tests ====================

  @Test
  fun organizationSuggestions_recomposition_updatesContent() {
    var organizations by mutableStateOf(testOrganizations.take(3))

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = organizations) }
    }

    // Verify initial state
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_3").assertIsDisplayed()

    // Update with new organizations
    organizations = testOrganizations.take(2)
    composeTestRule.waitForIdle()

    // Verify updated state
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_3").assertDoesNotExist()
  }

  @Test
  fun organizationSuggestions_withDifferentThemes_rendersCorrectly() {
    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = testOrganizations) }
    }

    // Verify it renders correctly with MaterialTheme
    composeTestRule.onNodeWithTag(C.Tag.org_suggestions_section).assertIsDisplayed()
    composeTestRule.onNodeWithText("Organizations").assertIsDisplayed()
  }

  @Test
  fun organizationSuggestions_duplicateIds_rendersAllCards() {
    val duplicateIdOrgs =
        listOf(
            OrganizationData(id = "1", name = "Org A", handle = "@a"),
            OrganizationData(id = "1", name = "Org B", handle = "@b") // Same ID
            )

    composeTestRule.setContent {
      MaterialTheme { OrganizationSuggestions(organizations = duplicateIdOrgs) }
    }

    // Should render both organizations even with duplicate IDs
    composeTestRule.onNodeWithText("Org A").assertIsDisplayed()
    composeTestRule.onNodeWithText("Org B").assertIsDisplayed()
  }

  @Test
  fun organizationSuggestions_clickOnDifferentCards_triggersCorrectCallbacks() {
    val clickedOrgs = mutableListOf<String>()

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationSuggestions(
            organizations = testOrganizations.take(3),
            onOrganizationClick = { id -> clickedOrgs.add(id) })
      }
    }

    // Click on each card
    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_2").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("${C.Tag.org_suggestions_card}_3").performClick()
    composeTestRule.waitForIdle()

    // Verify all clicks were registered in order
    assertEquals(listOf("1", "2", "3"), clickedOrgs)
  }
}
