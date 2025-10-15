package com.github.se.studentconnect.ui.screen.signup

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.semantics.AccessibilityAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.AnnotatedString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AddPictureScreenTest {

  companion object {
    private const val CONTINUE_LABEL = "Continue"
    private const val UPLOAD_PROMPT = "Upload/Take your profile photo"
    private const val PICKER_SUCCESS_URI = "content://photo/42"
    private const val BACK_DESCRIPTION = "Back"
    private const val PLACEHOLDER = "ic_user"
  }

  private lateinit var controller: ActivityController<ComponentActivity>
  private lateinit var viewModel: SignUpViewModel

  @Before
  fun setUp() {
    controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
    viewModel = SignUpViewModel()
  }

  @After
  fun tearDown() {
    controller.pause().stop().destroy()
    runOnIdle()
  }

  @Test
  fun `initial render disables continue`() {
    composeScreen()

    assertTextExists(UPLOAD_PROMPT)
    assertButtonDisabled(CONTINUE_LABEL)
  }

  @Test
  fun `skip selects placeholder and enables continue`() {
    var skipInvoked = false
    composeScreen(onSkip = { skipInvoked = true })

    findButtonByText("Skip").invokeClick()

    assertTrue(skipInvoked)
    assertEquals(PLACEHOLDER, viewModel.state.value.profilePictureUri)
    assertButtonEnabled(CONTINUE_LABEL)
  }

  @Test
  fun `image picker success updates state and triggers continue`() {
    var continueCalled = false
    var recordedCallback: ((String?) -> Unit)? = null

    composeScreen(
        onPickImage = { callback -> recordedCallback = callback },
        onContinue = { continueCalled = true })

    findUploadCard().invokeClick()

    val callback = requireNotNull(recordedCallback) { "Upload card did not invoke picker." }
    callback(PICKER_SUCCESS_URI)
    runOnIdle()

    assertEquals(PICKER_SUCCESS_URI, viewModel.state.value.profilePictureUri)
    assertTextExists("Photo selected")
    assertButtonEnabled(CONTINUE_LABEL)

    findButtonByText(CONTINUE_LABEL).invokeClick()
    assertTrue(continueCalled)
  }

  @Test
  fun `image picker dismissal keeps continue disabled`() {
    var recordedCallback: ((String?) -> Unit)? = null
    composeScreen(onPickImage = { callback -> recordedCallback = callback })

    findUploadCard().invokeClick()
    val callback = requireNotNull(recordedCallback)

    callback(null)
    runOnIdle()

    assertNull(viewModel.state.value.profilePictureUri)
    assertButtonDisabled(CONTINUE_LABEL)
  }

  @Test
  fun `back button delegates correctly`() {
    var backInvoked = 0
    composeScreen(onBack = { backInvoked += 1 })

    findNodeByContentDescription(BACK_DESCRIPTION).invokeClick()

    assertEquals(1, backInvoked)
  }

  @Test
  fun `view model updates recompute continue state`() {
    composeScreen()

    viewModel.setProfilePictureUri("content://external")
    runOnIdle()
    assertButtonEnabled(CONTINUE_LABEL)

    viewModel.setProfilePictureUri(null)
    runOnIdle()
    assertButtonDisabled(CONTINUE_LABEL)
  }

  private fun composeScreen(
      onPickImage: (onResult: (String?) -> Unit) -> Unit = {},
      onSkip: () -> Unit = {},
      onContinue: () -> Unit = {},
      onBack: () -> Unit = {}
  ) {
    controller.get().setContent {
      AddPictureScreen(
          viewModel = viewModel,
          onPickImage = onPickImage,
          onSkip = onSkip,
          onContinue = onContinue,
          onBack = onBack)
    }
    runOnIdle()
  }

  private fun assertTextExists(text: String) {
    requireNotNull(findNode { semanticsText(it).any { entry -> entry == text } }) {
      "Expected text '$text' not found."
    }
  }

  private fun assertButtonEnabled(label: String) {
    val node = findButtonByText(label)
    assertTrue("Button '$label' expected to be enabled.", !node.isDisabled())
  }

  private fun assertButtonDisabled(label: String) {
    val node = findButtonByText(label)
    assertTrue("Button '$label' expected to be disabled.", node.isDisabled())
  }

  private fun findButtonByText(text: String): SemanticsNode {
    val textNode =
        findNode { semanticsText(it).any { entry -> entry == text } }
            ?: error("Text '$text' not found in semantics tree.")
    return textNode.clickableAncestor()
        ?: error("No clickable ancestor found for node containing '$text'.")
  }

  private fun findUploadCard(): SemanticsNode {
    val labelNode =
        findNode { semanticsText(it).any { entry -> entry == UPLOAD_PROMPT } }
            ?: error("Upload card label not found.")
    return labelNode.clickableAncestor() ?: labelNode
  }

  private fun findNodeByContentDescription(description: String): SemanticsNode {
    val node =
        findNode {
          val descriptions = it.config.getOrNull(SemanticsProperties.ContentDescription) ?: return@findNode false
          description in descriptions
        }
            ?: error("Content description '$description' not found.")
    return node.clickableAncestor() ?: node
  }

  private fun findNode(predicate: (SemanticsNode) -> Boolean): SemanticsNode? {
    fun search(node: SemanticsNode): SemanticsNode? {
      if (predicate(node)) return node
      for (child in node.children) {
        val result = search(child)
        if (result != null) return result
      }
      return null
    }
    return search(semanticsRoot())
  }

  private fun SemanticsNode.clickableAncestor(): SemanticsNode? {
    var current: SemanticsNode? = this
    while (current != null) {
      if (current.config.getOrNull(SemanticsActions.OnClick) != null) return current
      current = current.parent
    }
    return null
  }

  private fun SemanticsNode.invokeClick() {
    @Suppress("UNCHECKED_CAST")
    val accessibilityAction =
        config.getOrNull(SemanticsActions.OnClick) as? AccessibilityAction<() -> Boolean>
            ?: error("Node is not clickable.")
    val handler = accessibilityAction.action ?: error("No handler attached to clickable node.")
    handler.invoke()
    runOnIdle()
  }

  private fun SemanticsNode.isDisabled(): Boolean =
      config.getOrNull(SemanticsProperties.Disabled) != null

  private fun semanticsText(node: SemanticsNode): List<String> =
      node.config.getOrNull(SemanticsProperties.Text)?.map(AnnotatedString::text) ?: emptyList()

  private fun semanticsRoot(): SemanticsNode {
    val content =
        controller.get().window.decorView.findViewById<ViewGroup>(android.R.id.content)
    val rootView = content.getChildAt(0) ?: error("Compose root view missing.")
    val owner = extractSemanticsOwner(rootView)
    return owner.unmergedRootSemanticsNode
  }

  private fun extractSemanticsOwner(rootView: Any): SemanticsOwner {
    var current: Class<*>? = rootView::class.java
    while (current != null) {
      try {
        val field = current.getDeclaredField("semanticsOwner")
        field.isAccessible = true
        return field.get(rootView) as SemanticsOwner
      } catch (_: NoSuchFieldException) {
        current = current.superclass
      }
    }
    error("Unable to locate semanticsOwner field on Compose root view.")
  }

  private fun runOnIdle() {
    Robolectric.flushForegroundThreadScheduler()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }
}
