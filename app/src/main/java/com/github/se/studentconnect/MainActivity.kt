package com.github.se.studentconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              MainContent()
            }
      }
    }
  }
}

@Composable
fun MainContent() {
  Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Greeting("StudentConnect")

        Spacer(modifier = Modifier.height(32.dp))

        // Example of using the converted SVG icons
        Text(
            text = "Available Icons:",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
          Icon(
              painter = painterResource(id = R.drawable.ic_home),
              contentDescription = "Home",
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(16.dp))
          Icon(
              painter = painterResource(id = R.drawable.ic_user),
              contentDescription = "User",
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.secondary)
          Spacer(modifier = Modifier.width(16.dp))
          Icon(
              painter = painterResource(id = R.drawable.ic_search),
              contentDescription = "Search",
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.tertiary)
        }
      }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
      text = "Hello $name!",
      modifier = modifier.semantics { testTag = C.Tag.greeting },
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onBackground)
}
