package com.github.se.studentconnect.ui.profile

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationMemberInvitation
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRole
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.organization.SocialLinks
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OrganizationProfileEditViewModelTest {

  private lateinit var testDispatcher: TestDispatcher
  private lateinit var testScope: TestScope
  private lateinit var context: Context
  private lateinit var organizationRepository: TestOrganizationRepository
  private lateinit var userRepository: TestUserRepository
  private lateinit var mediaRepository: TestMediaRepository
  private lateinit var viewModel: OrganizationProfileEditViewModel

  private val testOrgId = "test_org_123"
  private val testUserId = "test_user_123"
  private val otherUserId = "other_user_456"

  private val testUser =
      User(
          userId = testUserId,
          firstName = "Test",
          lastName = "User",
          email = "test@example.com",
          username = "testuser",
          university = "EPFL")

  private val otherUser =
      User(
          userId = otherUserId,
          firstName = "Other",
          lastName = "User",
          email = "other@example.com",
          username = "otheruser",
          university = "EPFL")

  private val testRole = OrganizationRole(name = "President", description = "Leader of the org")

  private val testOrganization =
      Organization(
          id = testOrgId,
          name = "Test Organization",
          type = OrganizationType.StudentClub,
          description = "Test description",
          logoUrl = "logo123",
          location = "EPFL",
          mainDomains = listOf("Technology", "Science"),
          ageRanges = listOf("18-25", "26-35"),
          typicalEventSize = "50-100",
          roles = listOf(testRole),
          socialLinks =
              SocialLinks(
                  website = "https://test.com",
                  instagram = "@testorg",
                  x = "@testorg",
                  linkedin = "testorg"),
          memberUids = listOf(testUserId, otherUserId),
          memberRoles = mapOf(otherUserId to "President"),
          createdBy = testUserId,
          createdAt = Timestamp.now())

  @Before
  fun setUp() {
    testDispatcher = StandardTestDispatcher()
    testScope = TestScope(testDispatcher)
    Dispatchers.setMain(testDispatcher)

    context = ApplicationProvider.getApplicationContext()
    organizationRepository = TestOrganizationRepository(listOf(testOrganization))
    userRepository = TestUserRepository(listOf(testUser, otherUser))
    mediaRepository = TestMediaRepository()

    // Mock authentication - needs to be mutable
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns testUserId
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `viewModel initializes and loads organization data`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(testOrganization, state.organization)
        assertEquals("Test Organization", state.name)
        assertEquals(OrganizationType.StudentClub, state.type)
        assertEquals("Test description", state.description)
        assertEquals("EPFL", state.location)
        assertEquals("logo123", state.logoUrl)
        assertEquals(2, state.members.size)
      }

  @Test
  fun `viewModel loads organization not found`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = "non_existent",
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Organization not found", state.error)
        assertNull(state.organization)
      }

  @Test
  fun `viewModel rejects non-owner access`() =
      testScope.runTest {
        every { AuthenticationProvider.currentUser } returns otherUserId

        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Only the owner can edit this organization", state.error)
      }

  @Test
  fun `updateName updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateName("New Organization Name")

        val state = viewModel.uiState.value
        assertEquals("New Organization Name", state.name)
        assertNull(state.nameError)
      }

  @Test
  fun `updateType updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateType(OrganizationType.Company)

        val state = viewModel.uiState.value
        assertEquals(OrganizationType.Company, state.type)
      }

  @Test
  fun `updateDescription updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateDescription("New description")

        val state = viewModel.uiState.value
        assertEquals("New description", state.description)
        assertNull(state.descriptionError)
      }

  @Test
  fun `updateLocation updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateLocation("New Location")

        val state = viewModel.uiState.value
        assertEquals("New Location", state.location)
      }

  @Test
  fun `updateSocialLinks updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateSocialWebsite("https://newsite.com")
        viewModel.updateSocialInstagram("@newinstagram")
        viewModel.updateSocialX("@newx")
        viewModel.updateSocialLinkedIn("newlinkedin")

        val state = viewModel.uiState.value
        assertEquals("https://newsite.com", state.socialWebsite)
        assertEquals("@newinstagram", state.socialInstagram)
        assertEquals("@newx", state.socialX)
        assertEquals("newlinkedin", state.socialLinkedIn)
      }

  @Test
  fun `updateLogoUri updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val mockUri = Mockito.mock(Uri::class.java)
        viewModel.updateLogoUri(mockUri)

        val state = viewModel.uiState.value
        assertEquals(mockUri, state.logoUri)
      }

  @Test
  fun `removeLogo clears logo state`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.removeLogo()

        val state = viewModel.uiState.value
        assertNull(state.logoUri)
        assertNull(state.logoUrl)
      }

  @Test
  fun `saveOrganization validates empty name`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateName("")
        viewModel.saveOrganization()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Organization name cannot be empty", state.nameError)
        assertFalse(state.isSaving)
      }

  @Test
  fun `saveOrganization validates name length`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateName("a".repeat(201))
        viewModel.saveOrganization()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.nameError?.contains("exceeds") == true)
        assertFalse(state.isSaving)
      }

  @Test
  fun `saveOrganization validates description length`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateDescription("a".repeat(1001))
        viewModel.saveOrganization()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.descriptionError?.contains("exceeds") == true)
        assertFalse(state.isSaving)
      }

  @Test
  fun `saveOrganization succeeds with valid data`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateName("Updated Name")
        viewModel.updateDescription("Updated description")
        viewModel.saveOrganization()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertEquals("Organization updated successfully", state.successMessage)
        assertNull(state.error)
      }

  @Test
  fun `saveOrganization uploads new logo if uri is set`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val mockUri = Mockito.mock(Uri::class.java)
        viewModel.updateLogoUri(mockUri)
        viewModel.saveOrganization()

        advanceUntilIdle()

        assertTrue(mediaRepository.uploadCalled)
        val state = viewModel.uiState.value
        assertEquals("new_logo_id", state.logoUrl)
        assertNull(state.logoUri)
      }

  @Test
  fun `showRemoveMemberDialog prevents removing owner`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val ownerMember =
            OrganizationMemberEdit(
                userId = testUserId,
                name = "Test User",
                role = "Owner",
                avatarUrl = null,
                isOwner = true)

        viewModel.showRemoveMemberDialog(ownerMember)

        val state = viewModel.uiState.value
        assertFalse(state.showRemoveMemberDialog)
        assertEquals("Cannot remove the organization owner", state.error)
      }

  @Test
  fun `showRemoveMemberDialog shows dialog for non-owner`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val member =
            OrganizationMemberEdit(
                userId = otherUserId,
                name = "Other User",
                role = "Member",
                avatarUrl = null,
                isOwner = false)

        viewModel.showRemoveMemberDialog(member)

        val state = viewModel.uiState.value
        assertTrue(state.showRemoveMemberDialog)
        assertEquals(member, state.memberToRemove)
      }

  @Test
  fun `dismissRemoveMemberDialog clears dialog state`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val member =
            OrganizationMemberEdit(
                userId = otherUserId,
                name = "Other User",
                role = "Member",
                avatarUrl = null,
                isOwner = false)

        viewModel.showRemoveMemberDialog(member)
        viewModel.dismissRemoveMemberDialog()

        val state = viewModel.uiState.value
        assertFalse(state.showRemoveMemberDialog)
        assertNull(state.memberToRemove)
      }

  @Test
  fun `confirmRemoveMember removes member successfully`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val member =
            OrganizationMemberEdit(
                userId = otherUserId,
                name = "Other User",
                role = "Member",
                avatarUrl = null,
                isOwner = false)

        viewModel.showRemoveMemberDialog(member)
        viewModel.confirmRemoveMember()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showRemoveMemberDialog)
        assertNull(state.memberToRemove)
        assertEquals("Member removed successfully", state.successMessage)
      }

  @Test
  fun `showChangeRoleDialog prevents changing owner role`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val ownerMember =
            OrganizationMemberEdit(
                userId = testUserId,
                name = "Test User",
                role = "Owner",
                avatarUrl = null,
                isOwner = true)

        viewModel.showChangeRoleDialog(ownerMember)

        val state = viewModel.uiState.value
        assertFalse(state.showChangeRoleDialog)
        assertEquals("Cannot change the owner's role", state.error)
      }

  @Test
  fun `showChangeRoleDialog shows dialog for non-owner`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val member =
            OrganizationMemberEdit(
                userId = otherUserId,
                name = "Other User",
                role = "Member",
                avatarUrl = null,
                isOwner = false)

        viewModel.showChangeRoleDialog(member)

        val state = viewModel.uiState.value
        assertTrue(state.showChangeRoleDialog)
        assertEquals(member, state.memberToChangeRole)
      }

  @Test
  fun `dismissChangeRoleDialog clears dialog state`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val member =
            OrganizationMemberEdit(
                userId = otherUserId,
                name = "Other User",
                role = "Member",
                avatarUrl = null,
                isOwner = false)

        viewModel.showChangeRoleDialog(member)
        viewModel.dismissChangeRoleDialog()

        val state = viewModel.uiState.value
        assertFalse(state.showChangeRoleDialog)
        assertNull(state.memberToChangeRole)
      }

  @Test
  fun `confirmChangeRole updates member role successfully`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val member =
            OrganizationMemberEdit(
                userId = otherUserId,
                name = "Other User",
                role = "President",
                avatarUrl = null,
                isOwner = false)

        viewModel.showChangeRoleDialog(member)
        viewModel.confirmChangeRole("Vice President")

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showChangeRoleDialog)
        assertNull(state.memberToChangeRole)
        assertEquals("Role updated successfully", state.successMessage)
      }

  @Test
  fun `clearSuccessMessage clears success state`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()
        viewModel.updateName("Updated Name")
        viewModel.saveOrganization()
        advanceUntilIdle()

        viewModel.clearSuccessMessage()

        val state = viewModel.uiState.value
        assertNull(state.successMessage)
      }

  @Test
  fun `clearError clears error state`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()
        viewModel.updateName("")
        viewModel.saveOrganization()
        advanceUntilIdle()

        viewModel.clearError()

        val state = viewModel.uiState.value
        assertNull(state.error)
      }

  @Test
  fun `updateMainDomains updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateMainDomains(listOf("Domain1", "Domain2"))

        val state = viewModel.uiState.value
        assertEquals(listOf("Domain1", "Domain2"), state.mainDomains)
      }

  @Test
  fun `updateAgeRanges updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateAgeRanges(listOf("18-25", "26-35"))

        val state = viewModel.uiState.value
        assertEquals(listOf("18-25", "26-35"), state.ageRanges)
      }

  @Test
  fun `updateTypicalEventSize updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateTypicalEventSize("50-100")

        val state = viewModel.uiState.value
        assertEquals("50-100", state.typicalEventSize)
      }

  @Test
  fun `updateRoles updates state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val newRoles = listOf(OrganizationRole("VP", "Vice President"))
        viewModel.updateRoles(newRoles)

        val state = viewModel.uiState.value
        assertEquals(newRoles, state.roles)
      }

  @Test
  fun `saveOrganization handles upload error`() =
      testScope.runTest {
        val failingMediaRepo = TestMediaRepository(shouldFail = true)

        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = failingMediaRepo)

        advanceUntilIdle()

        val mockUri = Mockito.mock(Uri::class.java)
        viewModel.updateLogoUri(mockUri)
        viewModel.saveOrganization()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.error?.contains("Failed to upload logo") == true)
      }

  @Test
  fun `saveOrganization handles repository error`() =
      testScope.runTest {
        val failingOrgRepo =
            TestOrganizationRepository(listOf(testOrganization), shouldFailOnSave = true)

        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = failingOrgRepo,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateName("Valid Name")
        viewModel.saveOrganization()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.error?.contains("Failed to update organization") == true)
      }

  @Test
  fun `loadOrganizationData handles error`() =
      testScope.runTest {
        val failingRepo = TestOrganizationRepository(emptyList(), shouldFail = true)

        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = failingRepo,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.error?.contains("Failed to load organization") == true)
      }

  @Test
  fun `members are loaded with correct roles`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.members.size)

        val owner = state.members.find { it.userId == testUserId }
        assertNotNull(owner)
        assertTrue(owner!!.isOwner)
        assertEquals("Owner", owner.role)

        val member = state.members.find { it.userId == otherUserId }
        assertNotNull(member)
        assertFalse(member!!.isOwner)
        assertEquals("President", member.role)
      }

  @Test
  fun `confirmRemoveMember handles error`() =
      testScope.runTest {
        val failingRepo =
            TestOrganizationRepository(listOf(testOrganization), shouldFailOnSave = true)

        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = failingRepo,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val member =
            OrganizationMemberEdit(
                userId = otherUserId,
                name = "Other User",
                role = "Member",
                avatarUrl = null,
                isOwner = false)

        viewModel.showRemoveMemberDialog(member)
        viewModel.confirmRemoveMember()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("Failed to remove member") == true)
      }

  @Test
  fun `confirmChangeRole handles error`() =
      testScope.runTest {
        val failingRepo =
            TestOrganizationRepository(listOf(testOrganization), shouldFailOnSave = true)

        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = failingRepo,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val member =
            OrganizationMemberEdit(
                userId = otherUserId,
                name = "Other User",
                role = "President",
                avatarUrl = null,
                isOwner = false)

        viewModel.showChangeRoleDialog(member)
        viewModel.confirmChangeRole("Vice President")

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("Failed to change role") == true)
      }

  @Test
  fun `loadMembers handles user not found`() =
      testScope.runTest {
        val emptyUserRepo = TestUserRepository(emptyList())

        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = emptyUserRepo,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Should still load but with empty members list
        assertNotNull(state.organization)
        assertEquals(0, state.members.size)
      }

  @Test
  fun `saveOrganization preserves empty description`() =
      testScope.runTest {
        viewModel =
            OrganizationProfileEditViewModel(
                organizationId = testOrgId,
                context = context,
                organizationRepository = organizationRepository,
                userRepository = userRepository,
                mediaRepository = mediaRepository)

        advanceUntilIdle()

        viewModel.updateName("Valid Name")
        viewModel.updateDescription("")
        viewModel.saveOrganization()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Organization updated successfully", state.successMessage)
        assertNotNull(organizationRepository.savedOrganization)
        assertNull(organizationRepository.savedOrganization?.description)
      }

  // Test helper repositories
  private class TestOrganizationRepository(
      var organizations: List<Organization>,
      var shouldFail: Boolean = false,
      var shouldFailOnSave: Boolean = false
  ) : OrganizationRepository {

    var savedOrganization: Organization? = null

    override suspend fun saveOrganization(organization: Organization) {
      if (shouldFailOnSave) {
        throw Exception("Test save error")
      }
      savedOrganization = organization
      organizations = organizations.map { if (it.id == organization.id) organization else it }
    }

    override suspend fun getOrganizationById(organizationId: String): Organization? {
      if (shouldFail) {
        throw Exception("Test load error")
      }
      return organizations.find { it.id == organizationId }
    }

    override suspend fun getAllOrganizations(): List<Organization> {
      if (shouldFail) {
        throw Exception("Test error")
      }
      return organizations
    }

    override suspend fun getNewOrganizationId(): String = "new_org_id"

    override suspend fun sendMemberInvitation(
        organizationId: String,
        userId: String,
        role: String,
        invitedBy: String
    ) {}

    override suspend fun acceptMemberInvitation(organizationId: String, userId: String) {}

    override suspend fun rejectMemberInvitation(organizationId: String, userId: String) {}

    override suspend fun getPendingInvitations(
        organizationId: String
    ): List<OrganizationMemberInvitation> = emptyList()

    override suspend fun getUserPendingInvitations(
        userId: String
    ): List<OrganizationMemberInvitation> = emptyList()

    override suspend fun addMemberToOrganization(organizationId: String, userId: String) {}
  }

  private class TestUserRepository(var users: List<User>) : UserRepository {

    override suspend fun getUserById(userId: String): User? = users.find { it.userId == userId }

    override suspend fun getUserByEmail(email: String): User? = null

    override suspend fun getAllUsers(): List<User> = users

    override suspend fun getUsersPaginated(
        limit: Int,
        lastUserId: String?
    ): Pair<List<User>, Boolean> = Pair(users, false)

    override suspend fun saveUser(user: User) {}

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

    override suspend fun deleteUser(userId: String) {}

    override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

    override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

    override suspend fun getNewUid(): String = "new_uid"

    override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

    override suspend fun addEventToUser(eventId: String, userId: String) {}

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {}

    override suspend fun getInvitations(
        userId: String
    ): List<com.github.se.studentconnect.model.activities.Invitation> = emptyList()

    override suspend fun acceptInvitation(eventId: String, userId: String) {}

    override suspend fun declineInvitation(eventId: String, userId: String) {}

    override suspend fun removeInvitation(eventId: String, userId: String) {}

    override suspend fun joinEvent(eventId: String, userId: String) {}

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {}

    override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

    override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

    override suspend fun addPinnedEvent(userId: String, eventId: String) {}

    override suspend fun removePinnedEvent(userId: String, eventId: String) {}

    override suspend fun getPinnedEvents(userId: String): List<String> = emptyList()

    override suspend fun checkUsernameAvailability(username: String): Boolean = true

    override suspend fun pinOrganization(userId: String, organizationId: String) {}

    override suspend fun unpinOrganization(userId: String) {}

    override suspend fun getPinnedOrganization(userId: String): String? = null

    override suspend fun leaveEvent(eventId: String, userId: String) {}
  }

  private class TestMediaRepository(var shouldFail: Boolean = false) : MediaRepository {
    var uploadCalled = false
    var uploadedUri: Uri? = null

    override suspend fun upload(uri: Uri, path: String?): String {
      if (shouldFail) {
        throw Exception("Upload failed")
      }
      uploadCalled = true
      uploadedUri = uri
      return "new_logo_id"
    }

    override suspend fun download(id: String): Uri {
      return Mockito.mock(Uri::class.java)
    }

    override suspend fun delete(id: String) {}
  }
}
