package com.github.se.studentconnect.ui.eventcreation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R

/**
 * Shared Shell for Event Creation. Allows passing specific testTags to maintain compatibility with
 * existing tests.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventShell(
    modifier: Modifier = Modifier,
    navController: NavHostController?,
    title: String,
    canSave: Boolean,
    onSave: () -> Unit,
    scaffoldTestTag: String,
    topBarTestTag: String,
    backButtonTestTag: String,
    scrollColumnTestTag: String,
    saveButtonTestTag: String,
    content: @Composable ColumnScope.(onFocusChange: (Boolean) -> Unit) -> Unit
) {
  val scrollState = rememberScrollState()
  val isAtBottom by remember { derivedStateOf { scrollState.value >= scrollState.maxValue - 50 } }

  var isAnyFieldFocused by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current

  BackHandler(enabled = isAnyFieldFocused) {
    focusManager.clearFocus()
    keyboardController?.hide()
  }

  LaunchedEffect(isAnyFieldFocused) {
    if (!isAnyFieldFocused) {
      keyboardController?.hide()
    }
  }

  val buttonWidthFraction by
      animateFloatAsState(
          targetValue = if (isAtBottom) 0.9f else 0.35f,
          animationSpec = tween(durationMillis = 300),
          label = "buttonWidthFraction")

  Box(modifier = modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.testTag(scaffoldTestTag),
        topBar = {
          TopAppBar(
              modifier = Modifier.testTag(topBarTestTag),
              title = { Text(text = title) },
              navigationIcon = {
                IconButton(
                    modifier = Modifier.testTag(backButtonTestTag),
                    onClick = { navController?.popBackStack() }) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = stringResource(R.string.content_description_back))
                    }
              })
        }) { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .verticalScroll(scrollState)
                      .padding(paddingValues)
                      .padding(horizontal = 16.dp)
                      .testTag(scrollColumnTestTag),
              verticalArrangement = Arrangement.spacedBy(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            content { isFocused -> isAnyFieldFocused = isFocused }
            Spacer(modifier = Modifier.size(100.dp))
          }
        }

    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
      Box(
          modifier =
              Modifier.fillMaxWidth().height(90.dp).padding(horizontal = 16.dp, vertical = 16.dp)) {
            Surface(
                modifier =
                    Modifier.testTag(saveButtonTestTag)
                        .fillMaxWidth(buttonWidthFraction)
                        .align(if (isAtBottom) Alignment.Center else Alignment.CenterEnd)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp)),
                color =
                    if (canSave) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 6.dp,
                enabled = canSave,
                onClick = { if (canSave) onSave() }) {
                  Row(
                      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                      horizontalArrangement = Arrangement.Center,
                      verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.button_save),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary)
                      }
                }
          }
    }
  }
}
