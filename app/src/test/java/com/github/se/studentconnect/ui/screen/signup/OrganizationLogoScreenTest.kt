package com.github.se.studentconnect.ui.screen.signup

import android.net.Uri
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpStep
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpViewModel
import org.junit.Assert.*
import org.junit.Test

class OrganizationSignUpViewModelUnitTest {

  @Test
  fun setLogoUri_updatesState() {
    val vm = OrganizationSignUpViewModel()
    val uri = Uri.parse("content://test/image.png")

    vm.setLogoUri(uri)

    assertEquals(uri, vm.state.value.logoUri)
  }

  @Test
  fun setOrganizationName_trimsWhitespace() {
    val vm = OrganizationSignUpViewModel()
    vm.setOrganizationName("  My Org  ")
    assertEquals("My Org", vm.state.value.organizationName)
  }

  @Test
  fun toggleOrganizationType_setsAndUnsets() {
    val vm = OrganizationSignUpViewModel()
    val type = OrganizationType.Association

    vm.toggleOrganizationType(type)
    assertEquals(type, vm.state.value.organizationType)

    // toggling again should unset
    vm.toggleOrganizationType(type)
    assertNull(vm.state.value.organizationType)
  }

  @Test
  fun nextAndPrevStep_moveBetweenSteps() {
    val vm = OrganizationSignUpViewModel()
    val initial = vm.state.value.currentStep
    vm.nextStep()
    assertEquals(initial, OrganizationSignUpStep.Info)
    assertEquals(OrganizationSignUpStep.Logo, vm.state.value.currentStep)

    vm.prevStep()
    assertEquals(OrganizationSignUpStep.Info, vm.state.value.currentStep)
  }

  @Test
  fun isInfoValid_dependsOnNameAndType() {
    val vm = OrganizationSignUpViewModel()
    assertFalse(vm.isInfoValid)
    vm.setOrganizationName("Org")
    assertFalse(vm.isInfoValid)
    vm.toggleOrganizationType(
        OrganizationType.Association)
    assertTrue(vm.isInfoValid)
  }
}
