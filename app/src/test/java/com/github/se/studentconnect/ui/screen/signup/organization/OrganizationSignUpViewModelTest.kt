package com.github.se.studentconnect.ui.screen.signup.organization

import android.net.Uri
import com.github.se.studentconnect.model.organization.OrganizationRole
import com.github.se.studentconnect.model.organization.OrganizationType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OrganizationSignUpViewModelTest {
  private lateinit var viewModel: OrganizationSignUpViewModel

  @Before
  fun setUp() {
    viewModel = OrganizationSignUpViewModel()
  }

  @Test
  fun `setOrganizationName updates state`() {
    viewModel.setOrganizationName("Test Org")
    assertEquals("Test Org", viewModel.state.value.organizationName)
  }

  @Test
  fun `toggleOrganizationType sets and unsets type`() {
    viewModel.toggleOrganizationType(OrganizationType.Association)
    assertEquals(OrganizationType.Association, viewModel.state.value.organizationType)
    viewModel.toggleOrganizationType(OrganizationType.Association)
    assertNull(viewModel.state.value.organizationType)
  }

  @Test
  fun `setLogoUri updates state`() {
    val uri = Uri.parse("http://logo.com/logo.png")
    viewModel.setLogoUri(uri)
    assertEquals(uri, viewModel.state.value.logoUri)
  }

  @Test
  fun `setDescription updates state`() {
    viewModel.setDescription("Description")
    assertEquals("Description", viewModel.state.value.description)
  }

  @Test
  fun `setWebsite trims and updates state`() {
    viewModel.setWebsite("  https://site.com  ")
    assertEquals("https://site.com", viewModel.state.value.websiteUrl)
  }

  @Test
  fun `setInstagram trims and updates state`() {
    viewModel.setInstagram("  insta ")
    assertEquals("insta", viewModel.state.value.instagramHandle)
  }

  @Test
  fun `setX trims and updates state`() {
    viewModel.setX("  xhandle ")
    assertEquals("xhandle", viewModel.state.value.xHandle)
  }

  @Test
  fun `setLinkedin trims and updates state`() {
    viewModel.setLinkedin("  linkedin.com ")
    assertEquals("linkedin.com", viewModel.state.value.linkedinUrl)
  }

  @Test
  fun `setLocation updates state`() {
    viewModel.setLocation("Paris")
    assertEquals("Paris", viewModel.state.value.location)
  }

  @Test
  fun `toggleDomain adds and removes domain`() {
    viewModel.toggleDomain("Tech")
    assertTrue(viewModel.state.value.domains.contains("Tech"))
    viewModel.toggleDomain("Tech")
    assertFalse(viewModel.state.value.domains.contains("Tech"))
  }

  @Test
  fun `toggleAgeRange adds and removes age range`() {
    viewModel.toggleAgeRange("18-25")
    assertTrue(viewModel.state.value.targetAgeRanges.contains("18-25"))
    viewModel.toggleAgeRange("18-25")
    assertFalse(viewModel.state.value.targetAgeRanges.contains("18-25"))
  }

  @Test
  fun `setEventSize updates state`() {
    viewModel.setEventSize("Large")
    assertEquals("Large", viewModel.state.value.eventSize)
  }

  @Test
  fun `addRole adds role to teamRoles`() {
    val role = OrganizationRole("President")
    viewModel.addRole(role)
    assertTrue(viewModel.state.value.teamRoles.contains(role))
  }

  @Test
  fun `removeRole removes role from teamRoles`() {
    val role = OrganizationRole("President")
    viewModel.addRole(role)
    viewModel.removeRole(role)
    assertFalse(viewModel.state.value.teamRoles.contains(role))
  }

  @Test
  fun `setRoles sets teamRoles`() {
    val roles = listOf(OrganizationRole("A"), OrganizationRole("B"))
    viewModel.setRoles(roles)
    assertEquals(roles, viewModel.state.value.teamRoles)
  }

  @Test
  fun `nextStep advances step`() {
    assertEquals(OrganizationSignUpStep.Info, viewModel.state.value.currentStep)
    viewModel.nextStep()
    assertEquals(OrganizationSignUpStep.Logo, viewModel.state.value.currentStep)
  }

  @Test
  fun `prevStep goes back step`() {
    viewModel.nextStep() // Info -> Logo
    viewModel.prevStep()
    assertEquals(OrganizationSignUpStep.Info, viewModel.state.value.currentStep)
  }

  @Test
  fun `isInfoValid returns true only if name and type are set`() {
    assertFalse(viewModel.isInfoValid)
    viewModel.setOrganizationName("Org")
    assertFalse(viewModel.isInfoValid)
    viewModel.toggleOrganizationType(OrganizationType.Association)
    assertTrue(viewModel.isInfoValid)
  }

  @Test
  fun `isProfileSetupValid returns true only if location, domains, and eventSize are set`() {
    assertFalse(viewModel.isProfileSetupValid)
    viewModel.setLocation("Paris")
    viewModel.toggleDomain("Tech")
    viewModel.setEventSize("Large")
    assertTrue(viewModel.isProfileSetupValid)
  }

  @Test
  fun `createOrganizationModel builds correct OrganizationModel`() {
    viewModel.setOrganizationName("Org")
    viewModel.toggleOrganizationType(OrganizationType.Association)
    viewModel.setDescription("Desc")
    viewModel.setLogoUri(null)
    viewModel.setLocation("Paris")
    viewModel.toggleDomain("Tech")
    viewModel.toggleAgeRange("18-25")
    viewModel.setEventSize("Large")
    viewModel.setWebsite("site.com")
    viewModel.setInstagram("insta")
    viewModel.setX("x")
    viewModel.setLinkedin("linkedin")
    val role = OrganizationRole("President")
    viewModel.addRole(role)
    val model = viewModel.createOrganizationModel("id1", "user1", "logoUrl")
    assertEquals("id1", model.id)
    assertEquals("Org", model.name)
    assertEquals(OrganizationType.Association, model.type)
    assertEquals("Desc", model.description)
    assertEquals("logoUrl", model.logoUrl)
    assertEquals("Paris", model.location)
    assertEquals(listOf("Tech"), model.mainDomains)
    assertEquals(listOf("18-25"), model.ageRanges)
    assertEquals("Large", model.typicalEventSize)
    assertEquals(listOf(role), model.roles)
    assertEquals("site.com", model.socialLinks.website)
    assertEquals("insta", model.socialLinks.instagram)
    assertEquals("x", model.socialLinks.x)
    assertEquals("linkedin", model.socialLinks.linkedin)
    assertEquals("user1", model.createdBy)
    assertNotNull(model.createdAt)
  }
}
