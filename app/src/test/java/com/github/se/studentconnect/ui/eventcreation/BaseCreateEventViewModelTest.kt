package com.github.se.studentconnect.ui.eventcreation

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.Timestamp
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BaseCreateEventViewModelTest {

  private class FakeViewModel(
      initialState: CreateEventUiState.Public,
      eventRepository: EventRepository,
      mediaRepository: MediaRepository,
      userRepository: UserRepository,
      organizationRepository: OrganizationRepository,
      friendsRepository: FriendsRepository,
      notificationRepository: NotificationRepository
  ) :
      BaseCreateEventViewModel<CreateEventUiState.Public>(
          initialState,
          eventRepository,
          mediaRepository,
          userRepository,
          organizationRepository,
          friendsRepository,
          notificationRepository) {
    override fun buildEvent(uid: String, ownerId: String, bannerPath: String?): Event =
        Event.Public(
            uid = uid,
            ownerId = ownerId,
            title = "t",
            description = "d",
            imageUrl = bannerPath,
            location = null,
            start = Timestamp.now(),
            end = null,
            maxCapacity = null,
            participationFee = null,
            isFlash = false,
            subtitle = "",
            tags = emptyList(),
            website = null)
  }

  @Test
  fun resolveBannerForSave_offlineStagesFile() = runTest {
    val appContext = ApplicationProvider.getApplicationContext<Context>()

    val tmpDir = File(appContext.filesDir, "banner_test").apply { mkdirs() }
    val source = File(tmpDir, "banner.png").apply { writeText("banner-bytes") }
    val uri = Uri.fromFile(source)

    val vm =
        FakeViewModel(
            initialState = CreateEventUiState.Public(bannerImageUri = uri),
            eventRepository = mockk(relaxed = true),
            mediaRepository = mockk(relaxed = true),
            userRepository = mockk(relaxed = true),
            organizationRepository = mockk(relaxed = true),
            friendsRepository = mockk(relaxed = true),
            notificationRepository = mockk(relaxed = true))

    val result = vm.resolveBannerForSave(appContext, "event-1")

    val resClass = result::class.java
    val pathField = resClass.getDeclaredField("bannerPathForEvent").apply { isAccessible = true }
    val pendingField = resClass.getDeclaredField("pendingUpload").apply { isAccessible = true }

    val stagedPath = pathField.get(result) as String?
    val pendingUpload = pendingField.get(result)

    assertNotNull(stagedPath)
    assertNotNull(pendingUpload)
  }

  @Test
  fun bannerUploadJob_canBeConstructedViaReflection() {
    val clazz =
        Class.forName(
            "com.github.se.studentconnect.ui.eventcreation.BaseCreateEventViewModel\$BannerUploadJob")
    val ctor =
        clazz.getDeclaredConstructor(
            String::class.java, String::class.java, String::class.java, String::class.java)
    ctor.isAccessible = true

    val instance = ctor.newInstance("/tmp/file.png", "events/x/banner", "event-1", null)

    val filePath = clazz.getDeclaredField("filePath").apply { isAccessible = true }.get(instance)
    assertEquals("/tmp/file.png", filePath)
    assertNotNull(instance)
  }
}
