package com.github.se.studentconnect.model.authentication

import com.github.se.studentconnect.model.BaseRepositoryProvider

object AuthRepositoryProvider : BaseRepositoryProvider<AuthRepository>() {
  override fun getCurrentRepository(): AuthRepository = AuthRepositoryFirebase()
}
