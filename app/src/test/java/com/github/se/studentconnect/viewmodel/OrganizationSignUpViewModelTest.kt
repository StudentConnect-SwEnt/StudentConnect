package com.github.se.studentconnect.viewmodel

import android.net.Uri
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRole
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpStep
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpViewModel
import com.google.firebase.Timestamp
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OrganizationSignUpViewModelTest {

  private lateinit var vm: OrganizationSignUpViewModel

  @Before
  fun setup() {
    vm = OrganizationSignUpViewModel()
  }

  @Test
  fun initialState_defaults() {
    val s = vm.state.value
    Assert.assertEquals("", s.organizationName)
    Assert.assertNull(s.organizationType)
    Assert.assertNull(s.logoUri)
    Assert.assertEquals("", s.description)
    Assert.assertEquals(emptyList<OrganizationRole>(), s.teamRoles)
    Assert.assertEquals(OrganizationSignUpStep.Info, s.currentStep)
    Assert.assertFalse(vm.isInfoValid)
  }

  @Test
  fun setName_and_type_and_infoValid() {
    vm.setOrganizationName("  Test Org  ")
    Assert.assertEquals("Test Org", vm.state.value.organizationName)

    vm.setOrganizationType(OrganizationType.Association)
    Assert.assertEquals(OrganizationType.Association, vm.state.value.organizationType)
    Assert.assertTrue(vm.isInfoValid)

    // toggle the same type should clear it
    vm.toggleOrganizationType(OrganizationType.Association)
    Assert.assertNull(vm.state.value.organizationType)
    Assert.assertFalse(vm.isInfoValid)
  }

  @Test
  fun logoUri_blank_becomesNull_and_nonBlank_set() {
    vm.setLogoUri(Uri.parse(""))
    Assert.assertNull(vm.state.value.logoUri)

    val uri = Uri.parse("file://logo.png")
    vm.setLogoUri(uri)
    Assert.assertEquals(uri, vm.state.value.logoUri)

    vm.setLogoUri(null)
    Assert.assertNull(vm.state.value.logoUri)
  }

  @Test
  fun roles_add_remove_and_set() {
    val r1 = OrganizationRole("Dev", "Writes code")
    val r2 = OrganizationRole("Designer", "Makes things pretty")

    vm.addRole(r1)
    Assert.assertEquals(listOf(r1), vm.state.value.teamRoles)

    vm.addRole(r2)
    // addRole prepends
    Assert.assertEquals(listOf(r2, r1), vm.state.value.teamRoles)

    vm.removeRole(r1)
    Assert.assertEquals(listOf(r2), vm.state.value.teamRoles)

    vm.setRoles(listOf(r1, r2))
    Assert.assertEquals(listOf(r1, r2), vm.state.value.teamRoles)
  }

  @Test
  fun toggleDomain_and_ageRange_and_profileValidation() {
    vm.setLocation("EPFL")
    vm.toggleDomain("Sports")
    vm.toggleAgeRange("18-25")
    vm.setEventSize("Medium")

    Assert.assertEquals("EPFL", vm.state.value.location)
    Assert.assertTrue(vm.state.value.domains.contains("Sports"))
    Assert.assertTrue(vm.state.value.targetAgeRanges.contains("18-25"))
    Assert.assertEquals("Medium", vm.state.value.eventSize)

    Assert.assertTrue(vm.isProfileSetupValid)

    // toggling off should remove
    vm.toggleDomain("Sports")
    vm.toggleAgeRange("18-25")
    Assert.assertFalse(vm.state.value.domains.contains("Sports"))
    Assert.assertFalse(vm.state.value.targetAgeRanges.contains("18-25"))
  }

  @Test
  fun navigation_next_and_prev_steps() {
    Assert.assertEquals(OrganizationSignUpStep.Info, vm.state.value.currentStep)
    vm.nextStep()
    Assert.assertEquals(OrganizationSignUpStep.Logo, vm.state.value.currentStep)
    vm.nextStep()
    vm.nextStep()
    vm.nextStep()
    vm.nextStep()
    // Should be at ProfileSetup and not advance further
    Assert.assertEquals(OrganizationSignUpStep.ProfileSetup, vm.state.value.currentStep)

    vm.prevStep()
    Assert.assertEquals(OrganizationSignUpStep.Team, vm.state.value.currentStep)
    vm.goTo(OrganizationSignUpStep.Info)
    Assert.assertEquals(OrganizationSignUpStep.Info, vm.state.value.currentStep)
  }

  @Test
  fun createOrganizationModel_populates_fields_correctly() {
    vm.setOrganizationName("My Club")
    vm.setOrganizationType(OrganizationType.StudentClub)
    vm.setDescription("") // blank should become null in model

    vm.setWebsite("https://example.com")
    vm.setInstagram("insta")
    vm.setX("xhandle")
    vm.setLinkedin("https://linkedin")

    vm.setLocation("Lausanne")
    vm.toggleDomain("Music")
    vm.toggleAgeRange("16-20")
    vm.setEventSize("Large")

    val role = OrganizationRole("President", "Leads")
    vm.addRole(role)

    val orgId = "org123"
    val currentUserId = "user456"
    val uploadedLogoUrl = "https://storage/logo.png"

    val model: Organization = vm.createOrganizationModel(orgId, currentUserId, uploadedLogoUrl)

    Assert.assertEquals(orgId, model.id)
    Assert.assertEquals("My Club", model.name)
    Assert.assertEquals(OrganizationType.StudentClub, model.type)
    // blank description becomes null
    Assert.assertNull(model.description)
    Assert.assertEquals(uploadedLogoUrl, model.logoUrl)
    Assert.assertEquals("Lausanne", model.location)
    Assert.assertTrue(model.mainDomains.contains("Music"))
    Assert.assertTrue(model.ageRanges.contains("16-20"))
    Assert.assertEquals("Large", model.typicalEventSize)
    Assert.assertEquals(listOf(role), model.roles)
    Assert.assertEquals("https://example.com", model.socialLinks.website)
    Assert.assertEquals("insta", model.socialLinks.instagram)
    Assert.assertEquals("xhandle", model.socialLinks.x)
    Assert.assertEquals("https://linkedin", model.socialLinks.linkedin)
    Assert.assertEquals(currentUserId, model.createdBy)
    Assert.assertNotNull(model.createdAt)
    Assert.assertTrue(model.createdAt is Timestamp)
  }

  @Test
  fun reset_clears_state() {
    vm.setOrganizationName("Something")
    vm.setOrganizationType(OrganizationType.NGO)
    vm.reset()
    val s = vm.state.value
    Assert.assertEquals("", s.organizationName)
    Assert.assertNull(s.organizationType)
    Assert.assertEquals(OrganizationSignUpStep.Info, s.currentStep)
  }
}
