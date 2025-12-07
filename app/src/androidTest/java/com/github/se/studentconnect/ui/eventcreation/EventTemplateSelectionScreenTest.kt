package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.navigation.Route
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.Date

class EventTemplateSelectionScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavHostController
    private lateinit var mockRepository: EventRepository

    @Before
    fun setUp() {
        mockNavController = mock(NavHostController::class.java)
        // 1. Mock the Interface (avoids 'final class' exceptions)
        mockRepository = mock(EventRepository::class.java)

        // 2. Inject the mock into the Provider
        EventRepositoryProvider.overrideForTests(mockRepository)
    }

    // KEY FIX: A helper function to allow matching nulls/any type in Kotlin.
    // This solves the issue where ViewModel queries with a user ID we don't know (or null).
    private fun <T> any(): T = Mockito.any()

    @Test
    fun emptyState_isDisplayed_whenNoEvents() {
        // Stub to return empty list for any user
        runBlocking {
            Mockito.doReturn(emptyList<Event>()).`when`(mockRepository).getEventsByOwner(any())
        }

        composeTestRule.setContent {
            EventTemplateSelectionScreen(navController = mockNavController)
        }

        // Wait for Empty State
        composeTestRule.waitUntil {
            composeTestRule.onAllNodes(hasTestTag(EventTemplateSelectionScreenTestTags.EMPTY_STATE))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(EventTemplateSelectionScreenTestTags.EMPTY_STATE)
            .assertIsDisplayed()
    }

    @Test
    fun backButton_navigatesBack() {
        // Stubbing to ensure no crashes during init
        runBlocking {
            Mockito.doReturn(emptyList<Event>()).`when`(mockRepository).getEventsByOwner(any())
        }

        composeTestRule.setContent {
            EventTemplateSelectionScreen(navController = mockNavController)
        }

        composeTestRule
            .onNodeWithTag(EventTemplateSelectionScreenTestTags.BACK_BUTTON)
            .assertIsDisplayed()
            .performClick()

        verify(mockNavController).popBackStack()
    }
}