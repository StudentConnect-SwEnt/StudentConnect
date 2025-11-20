package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizationProfileViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModel: OrganizationProfileViewModel

  @Before
  fun setUp() {
    viewModel = OrganizationProfileViewModel()
  }

  @Test
  fun `initial state has organization loaded`() = runTest {
    val state = viewModel.uiState.value

    assertNotNull(state.organization)
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertEquals(OrganizationTab.EVENTS, state.selectedTab)
  }

  @Test
  fun `initial organization has correct default data`() {
    val org = viewModel.uiState.value.organization!!

    assertEquals("org_evolve", org.organizationId)
    assertEquals("Evolve", org.name)
    assertFalse(org.isFollowing)
    assertEquals(2, org.events.size)
    assertEquals(6, org.members.size)
  }

  @Test
  fun `viewModel with custom organizationId uses provided id`() {
    val customViewModel = OrganizationProfileViewModel("custom_org_id")
    val org = customViewModel.uiState.value.organization!!

    assertEquals("custom_org_id", org.organizationId)
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
  fun `toggleFollow changes isFollowing from false to true`() {
    assertFalse(viewModel.uiState.value.organization!!.isFollowing)

    viewModel.toggleFollow()

    assertTrue(viewModel.uiState.value.organization!!.isFollowing)
  }

  @Test
  fun `toggleFollow changes isFollowing from true to false`() {
    viewModel.toggleFollow()
    assertTrue(viewModel.uiState.value.organization!!.isFollowing)

    viewModel.toggleFollow()

    assertFalse(viewModel.uiState.value.organization!!.isFollowing)
  }

  @Test
  fun `toggleFollow preserves other organization data`() {
    val originalOrg = viewModel.uiState.value.organization!!
    val originalName = originalOrg.name
    val originalEvents = originalOrg.events
    val originalMembers = originalOrg.members

    viewModel.toggleFollow()

    val updatedOrg = viewModel.uiState.value.organization!!
    assertEquals(originalName, updatedOrg.name)
    assertEquals(originalEvents.size, updatedOrg.events.size)
    assertEquals(originalMembers.size, updatedOrg.members.size)
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
  fun `mock events have correct structure`() {
    val events = viewModel.uiState.value.organization!!.events

    events.forEachIndexed { index, event ->
      assertTrue(event.eventId.isNotBlank())
      assertTrue(event.cardTitle.isNotBlank())
      assertTrue(event.cardDate.isNotBlank())
      assertTrue(event.title.isNotBlank())
      assertTrue(event.subtitle.isNotBlank())
    }
  }

  @Test
  fun `mock members have correct structure`() {
    val members = viewModel.uiState.value.organization!!.members

    members.forEachIndexed { index, member ->
      assertTrue(member.memberId.isNotBlank())
      assertTrue(member.name.isNotBlank())
      assertTrue(member.role.isNotBlank())
      assertNotNull(member.avatarUrl)
    }
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

  @Test
  fun `state flow emits updates correctly`() = runTest {
    val states = mutableListOf<OrganizationProfileUiState>()
    states.add(viewModel.uiState.value)

    viewModel.selectTab(OrganizationTab.MEMBERS)
    states.add(viewModel.uiState.value)

    viewModel.toggleFollow()
    states.add(viewModel.uiState.value)

    assertEquals(OrganizationTab.EVENTS, states[0].selectedTab)
    assertEquals(OrganizationTab.MEMBERS, states[1].selectedTab)
    assertTrue(states[2].organization!!.isFollowing)
  }
}
