package com.github.se.studentconnect.ui.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.activities.CountDownDisplay
import com.github.se.studentconnect.ui.screen.activities.CountDownViewModel
import com.github.se.studentconnect.ui.screen.activities.days
import com.github.se.studentconnect.ui.screen.camera.QrScannerScreen
import com.github.se.studentconnect.ui.utils.DialogNotImplemented
import com.github.se.studentconnect.viewmodel.EventViewModel
import com.github.se.studentconnect.viewmodel.TicketValidationResult

private val screenPadding = 25.dp

/** Test tags for the EventView screen and its components. */
object EventViewTestTags {
  const val EVENT_VIEW_SCREEN = "event_view_screen"
  const val TOP_APP_BAR = "event_view_top_app_bar"
  const val BACK_BUTTON = "event_view_back_button"
  const val EVENT_IMAGE = "event_view_image"
  const val INFO_SECTION = "event_view_info_section"
  const val COUNTDOWN_TIMER = "event_view_countdown_timer"
  const val COUNTDOWN_DAYS = "event_view_countdown_days"
  const val DESCRIPTION_TEXT = "event_view_description_text"
  const val CHAT_BUTTON = "event_view_chat_button"
  const val ACTION_BUTTONS_SECTION = "event_view_action_buttons_section"
  const val EDIT_EVENT_BUTTON = "event_view_edit_event_button"
  const val VISIT_WEBSITE_BUTTON = "event_view_visit_website_button"
  const val LOCATION_BUTTON = "event_view_location_button"
  const val SHARE_EVENT_BUTTON = "event_view_share_event_button"
  const val LEAVE_EVENT_BUTTON = "event_view_leave_event_button"
  const val LOADING_INDICATOR = "event_view_loading_indicator"
  const val SCAN_QR_BUTTON = "event_view_scan_qr_button"
  const val QR_SCANNER_DIALOG = "event_view_qr_scanner_dialog"
  const val VALIDATION_RESULT_VALID = "event_view_validation_result_valid"
  const val VALIDATION_RESULT_INVALID = "event_view_validation_result_invalid"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventView(
    eventUid: String,
    navController: NavHostController = NavHostController(LocalContext.current),
    eventViewModel: EventViewModel = viewModel(),
    hasJoined: Boolean,
) {
  val uiState by eventViewModel.uiState.collectAsState()
  val event = uiState.event
  val isLoading = uiState.isLoading
  val isJoined = uiState.isJoined
  val showQrScanner = uiState.showQrScanner
  val validationResult = uiState.ticketValidationResult

  val countDownViewModel: CountDownViewModel = viewModel()
  val timeLeft by countDownViewModel.timeLeft.collectAsState()

  LaunchedEffect(key1 = eventUid) { eventViewModel.fetchEvent(eventUid, hasJoined) }

  LaunchedEffect(event) { event?.let { countDownViewModel.startCountdown(it.start) } }

  // QR Scanner Dialog
  if (showQrScanner && event != null) {
    QrScannerDialog(
        eventUid = event.uid,
        eventViewModel = eventViewModel,
        onDismiss = { eventViewModel.hideQrScanner() },
        validationResult = validationResult)
  }

  Scaffold(
      modifier = Modifier.testTag(EventViewTestTags.EVENT_VIEW_SCREEN),
      topBar = {
        TopAppBar(
            title = {
              event?.let { Text(it.title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            },
            navigationIcon = {
              IconButton(
                  onClick = { navController.popBackStack() },
                  modifier = Modifier.testTag(EventViewTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
                  }
            },
            modifier = Modifier.testTag(EventViewTestTags.TOP_APP_BAR))
      }) { paddingValues ->
        if (isLoading) {
          Box(
              modifier = Modifier.fillMaxSize().testTag(EventViewTestTags.LOADING_INDICATOR),
              contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
              }
        } else if (event != null) {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Event Image",
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(250.dp)
                            .padding(horizontal = screenPadding)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .testTag(EventViewTestTags.EVENT_IMAGE),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer)

                EventActionButtons(
                    joined = isJoined,
                    currentEvent = event,
                    eventViewModel = eventViewModel,
                    modifier = Modifier.testTag(EventViewTestTags.ACTION_BUTTONS_SECTION),
                    navController = navController)

                InfoEvent(
                    timeLeft = timeLeft,
                    event = event,
                    modifier = Modifier.testTag(EventViewTestTags.INFO_SECTION))

                ChatButton()
              }
        }
      }
}

private const val DAY_IN_SECONDS = 86400

@Composable
private fun InfoEvent(timeLeft: Long, event: Event, modifier: Modifier = Modifier) {
  Column(
      verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
      modifier =
          modifier
              .fillMaxWidth()
              .padding(start = screenPadding, top = 6.dp, end = screenPadding, bottom = 6.dp)) {
        if (timeLeft > DAY_IN_SECONDS) {
          Text(
              modifier =
                  Modifier.align(Alignment.CenterHorizontally)
                      .fillMaxHeight()
                      .testTag(EventViewTestTags.COUNTDOWN_DAYS),
              color = MaterialTheme.colorScheme.primary,
              text = days(timeLeft) + " days left",
              style = MaterialTheme.typography.displaySmall)
        } else {
          Box(
              modifier =
                  Modifier.testTag(EventViewTestTags.COUNTDOWN_TIMER)
                      .align(Alignment.CenterHorizontally)) {
                CountDownDisplay(timeLeft)
              }
        }
        Text(text = "Description", style = titleTextStyle())
        Text(
            text = event.description,
            modifier = Modifier.testTag(EventViewTestTags.DESCRIPTION_TEXT))
      }
}

@Composable
private fun ChatButton(context: Context = LocalContext.current) {
  Button(
      onClick = { DialogNotImplemented(context) },
      modifier =
          Modifier.fillMaxWidth()
              .padding(start = screenPadding, top = 6.dp, end = screenPadding, bottom = 6.dp)
              .testTag(EventViewTestTags.CHAT_BUTTON),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
      Column(horizontalAlignment = Alignment.Start, modifier = Modifier.wrapContentHeight()) {
        Text(
            text = "Event chat",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(
            text = "Get The Latest News About The Event",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer)
      }
      Icon(
          painter = painterResource(id = R.drawable.ic_chat_bubble),
          contentDescription = "Home",
          modifier = Modifier.size(24.dp),
          tint = MaterialTheme.colorScheme.onSecondaryContainer)
    }
  }
}

@Composable
fun EventActionButtons(
    joined: Boolean,
    currentEvent: Event,
    eventViewModel: EventViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
  val context = LocalContext.current
  val currentUserId = AuthenticationProvider.currentUser
  val isOwner = !currentUserId.isNullOrBlank() && currentUserId == currentEvent.ownerId
  val editRoute =
      when (currentEvent) {
        is Event.Public -> Route.editPublicEvent(currentEvent.uid)
        is Event.Private -> Route.editPrivateEvent(currentEvent.uid)
      }

  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(start = screenPadding, end = screenPadding, bottom = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    if (isOwner) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            Button(
                onClick = { eventViewModel.showQrScanner() },
                modifier = Modifier.testTag(EventViewTestTags.SCAN_QR_BUTTON)) {
                  Icon(
                      imageVector = Icons.Default.QrCodeScanner,
                      contentDescription = null,
                      modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Scan Ticket")
                }
            Button(
                onClick = { navController.navigate(editRoute) },
                modifier = Modifier.testTag(EventViewTestTags.EDIT_EVENT_BUTTON)) {
                  Text("Edit event")
                }
          }
      Spacer(modifier = Modifier.height(12.dp))
    }
    JoinedEventActions(
        currentEvent = currentEvent,
        eventViewModel = eventViewModel,
        context = context,
        navController = navController,
        joined = joined)
  }
}

@Composable
private fun ButtonIcon(onClick: () -> Unit, id: Int, modifier: Modifier = Modifier) {
  IconButton(
      onClick = onClick,
      modifier =
          modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Icon(
            painter = painterResource(id = id),
            contentDescription = "Action button", // Generic description
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer)
      }
}

/** Show the actions available when the user has joined the event. */
@Composable
private fun JoinedEventActions(
    currentEvent: Event,
    eventViewModel: EventViewModel,
    context: Context,
    navController: NavHostController,
    joined: Boolean
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
  ) {
    Button(
        onClick = {
          if (joined) {
            eventViewModel.leaveEvent(eventUid = currentEvent.uid)
          } else {
            eventViewModel.joinEvent(eventUid = currentEvent.uid)
          }
        },
        modifier =
            Modifier.wrapContentSize()
                .padding(2.dp)
                .padding(start = 2.dp, top = 2.dp, end = 2.dp, bottom = 2.dp)) {
          Icon(
              painter =
                  if (joined) painterResource(id = R.drawable.ic_arrow_right)
                  else painterResource(id = R.drawable.ic_add),
              contentDescription = "action icon",
              modifier =
                  Modifier.size(20.dp).padding(start = 2.dp, top = 2.dp, end = 2.dp, bottom = 2.dp))
          Spacer(modifier = Modifier.width(2.dp))
          if (joined) Text("Leave") else Text("Join")
        }
    ButtonIcon(
        id = R.drawable.ic_location_pin,
        onClick = {
          currentEvent.location?.let { location ->
            val route = Route.mapWithLocation(location.latitude, location.longitude)
            navController.navigate(route)
          }
        },
        modifier = Modifier.testTag(EventViewTestTags.LOCATION_BUTTON))
    ButtonIcon(
        id = R.drawable.ic_web,
        onClick = {
          (currentEvent as? Event.Public)?.website?.let { website ->
            if (website.isNotEmpty()) {
              val fixedUrl =
                  if (!website.startsWith("http://") && !website.startsWith("https://")) {
                    "https://$website"
                  } else website
              try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl))
                context.startActivity(intent)
              } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                        context,
                        "No application can handle this request. Please install a web browser.",
                        Toast.LENGTH_LONG)
                    .show()
              }
            }
          }
        },
        modifier = Modifier.testTag(EventViewTestTags.VISIT_WEBSITE_BUTTON))
    ButtonIcon(
        id = R.drawable.ic_share,
        onClick = { DialogNotImplemented(context) },
        modifier = Modifier.testTag(EventViewTestTags.SHARE_EVENT_BUTTON))
  }
}

@Composable
fun titleTextStyle(): TextStyle =
    MaterialTheme.typography.titleLarge.copy(
        color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)

@Composable
private fun QrScannerDialog(
    eventUid: String,
    eventViewModel: EventViewModel,
    onDismiss: () -> Unit,
    validationResult: TicketValidationResult?
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .fillMaxHeight(0.7f)
                .testTag(EventViewTestTags.QR_SCANNER_DIALOG),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)) {
          Box(modifier = Modifier.fillMaxSize()) {
            QrScannerScreen(
                onBackClick = onDismiss,
                onProfileDetected = { userId ->
                  eventViewModel.validateParticipant(eventUid, userId)
                },
                modifier = Modifier.fillMaxSize(),
                isActive = validationResult == null)

            validationResult?.let { result ->
              ValidationResultOverlay(
                  result = result,
                  onScanNext = { eventViewModel.clearValidationResult() },
                  onClose = onDismiss)
            }
          }
        }
  }
}

@Composable
private fun ValidationResultOverlay(
    result: TicketValidationResult,
    onScanNext: () -> Unit,
    onClose: () -> Unit
) {
  val isValid = result is TicketValidationResult.Valid

  Box(
      modifier =
          Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
      contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors =
                CardDefaults.cardColors(containerColor = getValidationContainerColor(isValid))) {
              Column(
                  modifier = Modifier.padding(24.dp).testTag(getValidationTestTag(result)),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ValidationIcon(isValid = isValid)
                    ValidationTitle(isValid = isValid)
                    ValidationMessage(result = result, isValid = isValid)

                    Button(onClick = onScanNext, modifier = Modifier.fillMaxWidth()) {
                      Text("Scan Next")
                    }

                    OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                      Text("Close Scanner")
                    }
                  }
            }
      }
}

@Composable
private fun ValidationIcon(isValid: Boolean) {
  Icon(
      painter = painterResource(id = if (isValid) R.drawable.ic_add else R.drawable.ic_arrow_right),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = getValidationContentColor(isValid))
}

@Composable
private fun ValidationTitle(isValid: Boolean) {
  Text(
      text = if (isValid) "Valid Ticket" else "Invalid Ticket",
      style = MaterialTheme.typography.headlineMedium,
      color = getValidationContentColor(isValid))
}

@Composable
private fun ValidationMessage(result: TicketValidationResult, isValid: Boolean) {
  val message =
      when (result) {
        is TicketValidationResult.Valid -> "Participant ID: ${result.participantId}"
        is TicketValidationResult.Invalid ->
            "User ${result.userId} is not registered for this event"
      }

  Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = getValidationContentColor(isValid))
}

@Composable
private fun getValidationContainerColor(isValid: Boolean) =
    if (isValid) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.errorContainer

@Composable
private fun getValidationContentColor(isValid: Boolean) =
    if (isValid) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onErrorContainer

private fun getValidationTestTag(result: TicketValidationResult) =
    when (result) {
      is TicketValidationResult.Valid -> EventViewTestTags.VALIDATION_RESULT_VALID
      is TicketValidationResult.Invalid -> EventViewTestTags.VALIDATION_RESULT_INVALID
    }

@Preview(showBackground = true)
@Composable
fun EventViewPreview() {
  MaterialTheme { EventView(eventUid = "event-killer-concert-01", hasJoined = false) }
}
