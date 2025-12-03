package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.notification.NotificationRepositoryLocal
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.utils.StudentConnectTest
import com.github.se.studentconnect.viewmodel.NotificationViewModel
import org.junit.Before
import org.junit.Rule

class HomeScreenEditEventTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  override fun createInitializedRepository() = EventRepositoryLocal()

  private lateinit var ownerId: String
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var notificationRepository: NotificationRepositoryLocal
  private lateinit var homeViewModel: HomePageViewModel
  private lateinit var notificationViewModel: NotificationViewModel

  @Before
  fun captureOwner() {
    ownerId = currentUser.uid
    userRepository = UserRepositoryLocal()
    notificationRepository = NotificationRepositoryLocal()
    homeViewModel = HomePageViewModel(repository, userRepository)
    notificationViewModel = NotificationViewModel(notificationRepository)
  }
}
