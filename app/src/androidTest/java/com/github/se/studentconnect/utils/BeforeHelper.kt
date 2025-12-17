package com.github.se.studentconnect.utils

import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.notification.NotificationRepositoryLocal
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.model.user.UserRepositoryProvider

fun beforeTestRepositoriesInitialization() {
  UserRepositoryProvider.overrideForTests(UserRepositoryLocal())
  EventRepositoryProvider.overrideForTests(EventRepositoryLocal())
  NotificationRepositoryProvider.overrideForTests(NotificationRepositoryLocal())
  MediaRepositoryProvider.overrideForTests(MockMediaRepository())
}

fun afterTestRepositoriesCleanup() {
  UserRepositoryProvider.cleanOverrideForTests()
  EventRepositoryProvider.cleanOverrideForTests()
  NotificationRepositoryProvider.cleanOverrideForTests()
  MediaRepositoryProvider.cleanOverrideForTests()
}
