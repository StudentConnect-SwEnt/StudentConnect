package com.github.se.studentconnect.ui.userqr

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lightspark.composeqr.QrCodeView

@Composable
fun UserQRCode(userId: String) {
  QrCodeView(data = userId, modifier = Modifier.size(220.dp))
}
