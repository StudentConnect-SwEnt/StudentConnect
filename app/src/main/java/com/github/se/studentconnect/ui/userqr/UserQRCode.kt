package com.github.se.studentconnect.ui.userqr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lightspark.composeqr.QrCodeView

@Composable
fun UserQRCode(
    userId: String
) {
    QrCodeView(
        data = userId,
        modifier = Modifier.size(220.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun UserQRCodePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "User QR Code Preview",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))
                
                UserQRCode(userId = "user123456789")
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                
                Text(
                    text = "User ID: user123456789",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Empty User ID")
@Composable
fun UserQRCodeEmptyPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Empty User ID QR Code",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))
                
                UserQRCode(userId = "")
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                
                Text(
                    text = "User ID: (empty)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Special Characters")
@Composable
fun UserQRCodeSpecialCharsPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Special Characters QR Code",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))
                
                UserQRCode(userId = "user@#$%^&*()_+-=[]{}|;':\",./<>?")
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                
                Text(
                    text = "User ID: user@#$%^&*()_+-=[]{}|;':\",./<>?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Unicode Characters")
@Composable
fun UserQRCodeUnicodePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Unicode Characters QR Code",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))
                
                UserQRCode(userId = "用户123测试")
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                
                Text(
                    text = "User ID: 用户123测试",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

