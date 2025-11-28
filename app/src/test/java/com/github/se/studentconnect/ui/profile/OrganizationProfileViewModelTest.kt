package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.repository.OrganizationRepositoryLocal
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizationProfileViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModel: OrganizationProfileViewModel
  private lateinit var organizationRepository: OrganizationRepositoryLocal
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal

  @Before
  fun setUp() {
    organizationRepository = OrganizationRepositoryLocal()
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)
  }

  @Test
  fun `selectTab updates selected tab to EVENTS`() {
    viewModel.selectTab(OrganizationTab.MEMBERS)
    viewModel.selectTab(OrganizationTab.EVENTS)

    assertEquals(OrganizationTab.EVENTS, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `selectTab updates selected tab to MEMBERS`() {
    viewModel.selectTab(OrganizationTab.MEMBERS)

    assertEquals(OrganizationTab.MEMBERS, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `multiple tab selections work correctly`() {
    assertEquals(OrganizationTab.EVENTS, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(OrganizationTab.MEMBERS)
    assertEquals(OrganizationTab.MEMBERS, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(OrganizationTab.EVENTS)
    assertEquals(OrganizationTab.EVENTS, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(OrganizationTab.MEMBERS)
    assertEquals(OrganizationTab.MEMBERS, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `selecting same tab multiple times is idempotent`() {
    viewModel.selectTab(OrganizationTab.EVENTS)
    viewModel.selectTab(OrganizationTab.EVENTS)
    viewModel.selectTab(OrganizationTab.EVENTS)

    assertEquals(OrganizationTab.EVENTS, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `OrganizationTab enum has correct values`() {
    val values = OrganizationTab.values()

    assertEquals(2, values.size)
    assertTrue(values.contains(OrganizationTab.EVENTS))
    assertTrue(values.contains(OrganizationTab.MEMBERS))
  }

  @Test
  fun `OrganizationProfileUiState default values are correct`() {
    val defaultState = OrganizationProfileUiState()

    assertNull(defaultState.organization)
    assertEquals(OrganizationTab.EVENTS, defaultState.selectedTab)
    assertFalse(defaultState.isLoading)
    assertNull(defaultState.error)
  }

  @Test
  fun `companion object constants have expected values`() {
    assertEquals(120, OrganizationProfileViewModel.AVATAR_BANNER_HEIGHT)
    assertEquals(80, OrganizationProfileViewModel.AVATAR_SIZE)
    assertEquals(3, OrganizationProfileViewModel.AVATAR_BORDER_WIDTH)
    assertEquals(40, OrganizationProfileViewModel.AVATAR_ICON_SIZE)
    assertEquals(140, OrganizationProfileViewModel.EVENT_CARD_WIDTH)
    assertEquals(100, OrganizationProfileViewModel.EVENT_CARD_HEIGHT)
    assertEquals(72, OrganizationProfileViewModel.MEMBER_AVATAR_SIZE)
    assertEquals(36, OrganizationProfileViewModel.MEMBER_ICON_SIZE)
    assertEquals(2, OrganizationProfileViewModel.GRID_COLUMNS)
    assertEquals(400, OrganizationProfileViewModel.MEMBERS_GRID_HEIGHT)
  }
}
