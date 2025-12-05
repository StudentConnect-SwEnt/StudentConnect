package com.github.se.studentconnect.ui.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.ui.event.CountDownDisplay
import com.github.se.studentconnect.ui.event.CountDownViewModel
import com.github.se.studentconnect.ui.event.EventUiState
import com.github.se.studentconnect.ui.event.EventViewModel
import com.github.se.studentconnect.ui.event.InviteFriendsDialog
import com.github.se.studentconnect.ui.event.TicketValidationResult
import com.github.se.studentconnect.ui.event.days
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.poll.CreatePollDialog
import com.github.se.studentconnect.ui.screen.camera.QrScannerScreen
import com.github.se.studentconnect.ui.utils.DialogNotImplemented
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val screenPadding = 25.dp

// New centralized size values to avoid hardcoded dp usages
private val iconSize = 24.dp
private val smallSpacing = 8.dp

// URL protocol constants to avoid duplication
private const val HTTP_PROTOCOL = "http://"
private const val HTTPS_PROTOCOL = "https://"

/** Test tags for the EventView screen and its components. */
object EventViewTestTags {
  const val EVENT_VIEW_SCREEN = "event_view_screen"
  const val TOP_APP_BAR = "event_view_top_app_bar"
  const val BACK_BUTTON = "event_view_back_button"
  const val EVENT_IMAGE = "event_view_image"
  const val INFO_SECTION = "event_view_info_section"
  const val COUNTDOWN_TIMER = "event_view_countdown_timer"
  const val COUNTDOWN_DAYS = "event_view_countdown_days"
  const val TAGS_SECTION = "event_view_tags"
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
  const val VALIDATION_RESULT_ERROR = "event_view_validation_result_error"
  const val RETURN_TO_EVENT_BUTTON = "event_view_return_to_event_button"
  const val ATTENDEE_LIST_ITEM = "event_view_attendee_list_item"
  const val ATTENDEE_LIST_OWNER = "event_view_attendee_list_owner"
  const val ATTENDEE_LIST_CURRENT_USER = "event_view_attendee_list_current_user"
  const val ATTENDEE_LIST = "event_view_attendee_list"
  const val JOIN_BUTTON = "event_view_join_button"
  const val PARTICIPANTS_INFO = "event_view_participants_info"
  const val BASE_SCREEN = "event_view_base_screen"
  const val CREATE_POLL_BUTTON = "event_view_create_poll_button"
  const val VIEW_POLLS_BUTTON = "event_view_view_polls_button"
  const val VIEW_STATISTICS_BUTTON = "event_view_view_statistics_button"
  const val POLL_NOTIFICATION_CARD = "event_view_poll_notification_card"
  const val LEAVE_CONFIRMATION_DIALOG = "event_view_leave_confirmation_dialog"
  const val LEAVE_CONFIRMATION_CONFIRM = "event_view_leave_confirmation_confirm"
  const val LEAVE_CONFIRMATION_CANCEL = "event_view_leave_confirmation_cancel"
}

/** Displays the event detail screen and wires QR validation, countdown, and action buttons. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventView(
    eventUid: String,
    navController: NavHostController,
    eventViewModel: EventViewModel = viewModel(),
) {
  val context = LocalContext.current
  val uiState by eventViewModel.uiState.collectAsState()
  val event = uiState.event
  val isLoading = uiState.isLoading
  val isJoined = uiState.isJoined
  val showQrScanner = uiState.showQrScanner
  val validationResult = uiState.ticketValidationResult

  val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })

  LaunchedEffect(key1 = eventUid) { eventViewModel.fetchEvent(eventUid) }

  // QR Scanner Dialog
  if (showQrScanner && event != null) {
    QrScannerDialog(
        eventUid = event.uid,
        eventViewModel = eventViewModel,
        onDismiss = { eventViewModel.hideQrScanner() },
        validationResult = validationResult)
  }

  // Create Poll Dialog
  if (uiState.showCreatePollDialog && event != null) {
    CreatePollDialog(
        eventUid = event.uid,
        onDismiss = { eventViewModel.hideCreatePollDialog() },
        onPollCreated = { event.let { eventViewModel.fetchActivePolls(it.uid) } })
  }

  if (uiState.showInviteFriendsDialog && event is Event.Private) {
    InviteFriendsDialog(
        state = uiState,
        onToggleFriend = { eventViewModel.toggleFriendInvitation(it) },
        onSendInvites = { eventViewModel.updateInvitationsForEvent() },
        onDismiss = { eventViewModel.hideInviteFriendsDialog() })
  }

  // Leave Event Confirmation Dialog
  if (uiState.showLeaveConfirmDialog && event != null) {
    AlertDialog(
        onDismissRequest = { eventViewModel.hideLeaveConfirmDialog() },
        modifier = Modifier.testTag(EventViewTestTags.LEAVE_CONFIRMATION_DIALOG),
        title = { Text(text = stringResource(R.string.leave_event_confirmation_title)) },
        text = { Text(text = stringResource(R.string.leave_event_confirmation_message)) },
        confirmButton = {
          TextButton(
              onClick = {
                eventViewModel.hideLeaveConfirmDialog()
                eventViewModel.leaveEvent(eventUid = event.uid)
              },
              modifier = Modifier.testTag(EventViewTestTags.LEAVE_CONFIRMATION_CONFIRM)) {
                Text(text = stringResource(R.string.leave_event_confirm))
              }
        },
        dismissButton = {
          TextButton(
              onClick = { eventViewModel.hideLeaveConfirmDialog() },
              modifier = Modifier.testTag(EventViewTestTags.LEAVE_CONFIRMATION_CANCEL)) {
                Text(text = stringResource(R.string.leave_event_cancel))
              }
        })
  }

  Scaffold(
      modifier = Modifier.testTag(EventViewTestTags.EVENT_VIEW_SCREEN),
      topBar = {
        TopAppBar(
            title = {
              event?.let { Text(it.title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            },
            navigationIcon = {
              if (pagerState.currentPage == 1) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.testTag(EventViewTestTags.BACK_BUTTON)) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = stringResource(R.string.content_description_back))
                    }
              }
            },
            actions = {
              // View Polls button - only show if user is joined or is owner
              if (isJoined ||
                  (event != null && AuthenticationProvider.currentUser == event.ownerId)) {
                IconButton(
                    onClick = {
                      event?.let { navController.navigate(Route.pollsListScreen(it.uid)) }
                    },
                    modifier = Modifier.testTag(EventViewTestTags.VIEW_POLLS_BUTTON)) {
                      Icon(
                          painter = painterResource(id = R.drawable.ic_poll),
                          contentDescription = stringResource(R.string.button_view_polls))
                    }
              }
              // View Statistics button - only show if user is owner
              if (event != null && AuthenticationProvider.currentUser == event.ownerId) {
                IconButton(
                    onClick = {
                      event.let { navController.navigate(Route.eventStatistics(it.uid)) }
                    },
                    modifier = Modifier.testTag(EventViewTestTags.VIEW_STATISTICS_BUTTON)) {
                      Icon(
                          imageVector = Icons.Default.BarChart,
                          contentDescription = stringResource(R.string.content_description_view_statistics))
                    }
              }
            },
            modifier = Modifier.testTag(EventViewTestTags.TOP_APP_BAR),
        )
      },
  ) { paddingValues ->
    if (isLoading) {
      Box(
          modifier = Modifier.fillMaxSize().testTag(EventViewTestTags.LOADING_INDICATOR),
          contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
    } else if (event != null) {
      HorizontalPager(
          state = pagerState,
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          userScrollEnabled = false,
      ) { page ->
        when (page) {
          0 ->
              AttendeesList(
                  paddingValues = paddingValues,
                  pagerState = pagerState,
                  uiState = uiState,
                  navController = navController)
          1 ->
              BaseEventView(
                  paddingValues = paddingValues,
                  eventViewModel = eventViewModel,
                  event = event,
                  navController = navController,
                  pagerState = pagerState)
        }
      }
    }
  }
}

private const val DAY_IN_SECONDS = 86400

@Composable
private fun BaseEventView(
    paddingValues: PaddingValues,
    eventViewModel: EventViewModel,
    event: Event,
    navController: NavHostController,
    pagerState: PagerState
) {
  val coroutineScope = rememberCoroutineScope()
  val uiState by eventViewModel.uiState.collectAsState()
  val isJoined = uiState.isJoined
  val isFull = uiState.isFull
  val participantCount = uiState.participantCount

  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val profileId = event.imageUrl
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, profileId, repository) {
        value =
            profileId?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure {
                    android.util.Log.e("eventViewImage", "Failed to download event image: $id", it)
                  }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

  val countDownViewModel: CountDownViewModel = viewModel()
  val timeLeft by countDownViewModel.timeLeft.collectAsState()

  LaunchedEffect(event) { event.let { countDownViewModel.startCountdown(it.start) } }

  Box(
      modifier =
          Modifier.fillMaxSize().padding(paddingValues).testTag(EventViewTestTags.BASE_SCREEN)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              // Hero Image Section with Gradient Overlay
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(320.dp)
                          .testTag(EventViewTestTags.EVENT_IMAGE)) {
                    if (imageBitmap != null) {
                      Image(
                          bitmap = imageBitmap!!,
                          contentDescription =
                              stringResource(R.string.content_description_event_image),
                          modifier = Modifier.fillMaxSize(),
                          contentScale = ContentScale.Crop)
                      // Gradient overlay for better text readability
                      Box(
                          modifier =
                              Modifier.fillMaxSize()
                                  .background(
                                      androidx.compose.ui.graphics.Brush.verticalGradient(
                                          colors =
                                              listOf(
                                                  androidx.compose.ui.graphics.Color.Transparent,
                                                  androidx.compose.ui.graphics.Color.Black.copy(
                                                      alpha = 0.7f)),
                                          startY = 0f,
                                          endY = 1000f)))
                    } else {
                      Box(
                          modifier =
                              Modifier.fillMaxSize()
                                  .background(MaterialTheme.colorScheme.secondaryContainer),
                          contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription =
                                    stringResource(R.string.content_description_event_image),
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                          }
                    }
                  }

              // Action Buttons Section - Elevated Card Style
              Card(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(horizontal = 16.dp)
                          .offset(y = (-40).dp)
                          .testTag(EventViewTestTags.ACTION_BUTTONS_SECTION),
                  elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                  shape = RoundedCornerShape(20.dp),
                  colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                          EventActionButtons(
                              joined = isJoined,
                              isFull = isFull,
                              currentEvent = event,
                              eventViewModel = eventViewModel,
                              modifier = Modifier,
                              navController = navController)
                        }
                  }

              // Main Content with negative margin to overlap with card
              Column(
                  modifier =
                      Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-30).dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Countdown Card
                    CountdownCard(timeLeft = timeLeft, event = event, isJoined = isJoined)

                    // Info Section Card
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag(EventViewTestTags.INFO_SECTION),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface)) {
                          Column(
                              modifier = Modifier.fillMaxWidth().padding(20.dp),
                              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Tags
                                if (event is Event.Public && event.tags.isNotEmpty()) {
                                  EventTagsRow(tags = event.tags)
                                }

                                // Description
                                Text(
                                    text = stringResource(R.string.event_label_description),
                                    style = titleTextStyle())
                                Text(
                                    text = event.description,
                                    modifier = Modifier.testTag(EventViewTestTags.DESCRIPTION_TEXT),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                // Participants Info
                                ParticipantsInfo(
                                    event = event,
                                    participantCount = participantCount,
                                    onClick = {
                                      coroutineScope.launch { pagerState.scrollToPage(0) }
                                      coroutineScope.launch { eventViewModel.fetchAttendees() }
                                    })
                              }
                        }

                    // Poll notification for participants
                    if (isJoined &&
                        uiState.activePolls.isNotEmpty() &&
                        AuthenticationProvider.currentUser != event.ownerId) {
                      PollNotificationCard(
                          onVoteNowClick = {
                            navController.navigate(Route.pollsListScreen(event.uid))
                          },
                          onDismissClick = {},
                          modifier = Modifier.testTag(EventViewTestTags.POLL_NOTIFICATION_CARD))
                    }

                    // Chat Button
                    ChatButton()

                    Spacer(modifier = Modifier.height(20.dp))
                  }
            }
      }
}

@Composable
private fun CountdownCard(timeLeft: Long, event: Event, isJoined: Boolean) {
  val now = Timestamp.now()
  val eventHasStarted = now >= event.start

  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              // Countdown Timer
              when {
                eventHasStarted && timeLeft <= 0 -> {
                  val text =
                      if (isJoined) stringResource(R.string.event_hurry_up_started)
                      else stringResource(R.string.event_has_started)
                  Text(
                      modifier = Modifier.fillMaxWidth().testTag(EventViewTestTags.COUNTDOWN_TIMER),
                      color = MaterialTheme.colorScheme.primary,
                      text = text,
                      style = MaterialTheme.typography.displaySmall,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Center)
                }
                timeLeft > DAY_IN_SECONDS -> {
                  Text(
                      modifier = Modifier.fillMaxWidth().testTag(EventViewTestTags.COUNTDOWN_DAYS),
                      color = MaterialTheme.colorScheme.primary,
                      text = days(timeLeft) + " days left",
                      style = MaterialTheme.typography.displaySmall,
                      textAlign = TextAlign.Center)
                }
                else -> {
                  Box(
                      modifier =
                          Modifier.fillMaxWidth().testTag(EventViewTestTags.COUNTDOWN_TIMER)) {
                        CountDownDisplay(timeLeft)
                      }
                }
              }
            }
      }
}

@Composable
private fun AttendeesList(
    paddingValues: PaddingValues,
    pagerState: PagerState,
    uiState: EventUiState,
    navController: NavHostController,
) {
  val coroutineScope = rememberCoroutineScope()
  val isJoined = uiState.isJoined
  val attendees = uiState.attendees
  val user = uiState.currentUser
  val owner = uiState.owner

  Scaffold(
      modifier =
          Modifier.fillMaxSize().padding(paddingValues).testTag(EventViewTestTags.ATTENDEE_LIST),
      bottomBar = {
        Button(
            onClick = { coroutineScope.launch { pagerState.scrollToPage(1) } },
            content = { Text(stringResource(R.string.event_button_return)) },
            modifier =
                Modifier.fillMaxWidth()
                    .padding(screenPadding)
                    .testTag(EventViewTestTags.RETURN_TO_EVENT_BUTTON),
        )
      },
  ) { paddingValues ->
    LazyColumn(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        modifier = Modifier.fillMaxSize().padding(paddingValues),
    ) {
      if (isJoined && user != null && user != owner) {
        item {
          AttendeeItem(
              user,
              false,
              { navController.navigate(Route.visitorProfile(user.userId)) },
              modifier = Modifier.testTag(EventViewTestTags.ATTENDEE_LIST_CURRENT_USER))
        }
      }
      if (owner != null) {
        item {
          AttendeeItem(
              owner,
              true,
              { navController.navigate(Route.visitorProfile(owner.userId)) },
              modifier = Modifier.testTag(EventViewTestTags.ATTENDEE_LIST_OWNER))
        }
      }

      items(attendees) { a ->
        if (a != user && a != owner)
            AttendeeItem(
                a,
                false,
                { navController.navigate(Route.visitorProfile(a.userId)) },
                modifier = Modifier.testTag(EventViewTestTags.ATTENDEE_LIST_ITEM))
      }
    }
  }
}

/** Shows countdown, description, and attendance information for the given event. */
@Composable
private fun InfoEvent(
    timeLeft: Long,
    event: Event,
    isJoined: Boolean,
    participantCount: Int,
    onClickParticipants: () -> Unit,
    modifier: Modifier = Modifier
) {
  val now = Timestamp.now()
  val eventHasStarted = now >= event.start

  Column(
      verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
      modifier =
          modifier
              .fillMaxWidth()
              .padding(start = screenPadding, top = 6.dp, end = screenPadding, bottom = 6.dp)) {
        when {
          eventHasStarted && timeLeft <= 0 -> {
            val text =
                if (isJoined) stringResource(R.string.event_hurry_up_started)
                else stringResource(R.string.event_has_started)
            Text(
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxHeight()
                        .testTag(EventViewTestTags.COUNTDOWN_TIMER),
                color = MaterialTheme.colorScheme.primary,
                text = text,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
          }
          timeLeft > DAY_IN_SECONDS -> {
            Text(
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxHeight()
                        .testTag(EventViewTestTags.COUNTDOWN_DAYS),
                color = MaterialTheme.colorScheme.primary,
                text = days(timeLeft) + " days left",
                style = MaterialTheme.typography.displaySmall)
          }
          else -> {
            Box(
                modifier =
                    Modifier.testTag(EventViewTestTags.COUNTDOWN_TIMER)
                        .align(Alignment.CenterHorizontally)) {
                  CountDownDisplay(timeLeft)
                }
          }
        }
        if (event is Event.Public && event.tags.isNotEmpty()) {
          EventTagsRow(tags = event.tags)
        }
        Text(text = stringResource(R.string.event_label_description), style = titleTextStyle())
        Text(
            text = event.description,
            modifier = Modifier.testTag(EventViewTestTags.DESCRIPTION_TEXT))
        Spacer(modifier = Modifier.height(10.dp))
        ParticipantsInfo(
            event = event, participantCount = participantCount, onClick = onClickParticipants)
      }
}

/**
 * Displays the tags associated with a public event using pill-shaped chips.
 *
 * @param tags List of tag labels to display
 * @param modifier Modifier applied to the row container
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventTagsRow(tags: List<String>, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth().testTag(EventViewTestTags.TAGS_SECTION)) {
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
          tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
                  Text(
                      text = tag,
                      style = MaterialTheme.typography.bodyMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
          }
        }
  }
}

@Composable
private fun ParticipantsInfo(event: Event, participantCount: Int, onClick: () -> Unit) {
  val capacity =
      when (event) {
        is Event.Public -> event.maxCapacity
        is Event.Private -> event.maxCapacity
      }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .testTag(EventViewTestTags.PARTICIPANTS_INFO)
              .clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(
                  painter = painterResource(id = R.drawable.ic_group),
                  contentDescription = stringResource(R.string.content_description_participants),
                  modifier = Modifier.size(24.dp))
              val participantsText =
                  if (capacity != null) {
                    "$participantCount / $capacity"
                  } else {
                    stringResource(R.string.text_participants_count, participantCount)
                  }
              Text(text = participantsText, style = MaterialTheme.typography.bodyLarge)
            }
        capacity?.let { maxCap ->
          val progress = (participantCount.toFloat() / maxCap.toFloat()).coerceIn(0f, 1f)
          Spacer(modifier = Modifier.height(8.dp))
          LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)),
              color =
                  if (progress < 0.75f) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.error,
              trackColor = MaterialTheme.colorScheme.surfaceVariant,
              strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
          )
        }
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
            text = stringResource(R.string.event_chat_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(
            text = stringResource(R.string.event_chat_subtitle),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer)
      }
      Icon(
          painter = painterResource(id = R.drawable.ic_chat_bubble),
          contentDescription = stringResource(R.string.content_description_home),
          modifier = Modifier.size(24.dp),
          tint = MaterialTheme.colorScheme.onSecondaryContainer)
    }
  }
}

@Composable
fun EventActionButtons(
    joined: Boolean,
    isFull: Boolean,
    currentEvent: Event,
    eventViewModel: EventViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
  val context = LocalContext.current
  val currentUserId = AuthenticationProvider.currentUser
  val isOwner = currentUserId == currentEvent.ownerId

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    if (isOwner) {
      // First row: Blue textual buttons with icons (Create Poll, Scan, Edit)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { eventViewModel.showCreatePollDialog() },
            modifier =
                Modifier.weight(1f).height(48.dp).testTag(EventViewTestTags.CREATE_POLL_BUTTON),
            colors =
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
              Row(
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_poll),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.button_create_poll), maxLines = 1)
                  }
            }

        Button(
            onClick = { eventViewModel.showQrScanner() },
            modifier = Modifier.weight(1f).height(48.dp).testTag(EventViewTestTags.SCAN_QR_BUTTON),
            colors =
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
              Row(
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.button_scan), maxLines = 1)
                  }
            }

        val editRoute =
            when (currentEvent) {
              is Event.Public -> Route.editPublicEvent(currentEvent.uid)
              is Event.Private -> Route.editPrivateEvent(currentEvent.uid)
            }

        Button(
            onClick = { navController.navigate(editRoute) },
            modifier =
                Modifier.weight(1f).height(48.dp).testTag(EventViewTestTags.EDIT_EVENT_BUTTON),
            colors =
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
              Row(
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.button_edit), maxLines = 1)
                  }
            }
      }

      // Second row: Circular icon buttons (invite, location, website, share)
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically) {
            if (currentEvent is Event.Private) {
              ButtonIcon(
                  id = R.drawable.ic_group,
                  onClick = { eventViewModel.showInviteFriendsDialog() },
                  modifier = Modifier.testTag("event_view_invite_friends_button"))
            }

            CommonActionButtons(
                currentEvent = currentEvent, context = context, navController = navController)
          }
    } else {
      // Non-owner: Join/Leave button
      NonOwnerActionButtons(
          joined = joined,
          isFull = isFull,
          currentEvent = currentEvent,
          eventViewModel = eventViewModel)

      // Second row: Circular icon buttons (location, website, share)
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically) {
            CommonActionButtons(
                currentEvent = currentEvent, context = context, navController = navController)
          }
    }
  }
}

/** Owner-specific action buttons */
@Composable
private fun OwnerActionButtons(
    currentEvent: Event,
    eventViewModel: EventViewModel,
    navController: NavHostController
) {
  val context = LocalContext.current
  val editRoute =
      when (currentEvent) {
        is Event.Public -> Route.editPublicEvent(currentEvent.uid)
        is Event.Private -> Route.editPrivateEvent(currentEvent.uid)
      }

  if (currentEvent is Event.Private) {
    ButtonIcon(
        id = R.drawable.ic_group,
        onClick = { eventViewModel.showInviteFriendsDialog() },
        modifier = Modifier.testTag("event_view_invite_friends_button"))
  }

  Button(
      onClick = { eventViewModel.showCreatePollDialog() },
      modifier =
          Modifier.wrapContentSize().padding(2.dp).testTag(EventViewTestTags.CREATE_POLL_BUTTON)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_poll),
            contentDescription = stringResource(R.string.content_description_add_poll),
            modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(stringResource(R.string.button_create_poll))
      }

  Button(
      onClick = { eventViewModel.showQrScanner() },
      modifier =
          Modifier.wrapContentSize().padding(2.dp).testTag(EventViewTestTags.SCAN_QR_BUTTON)) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = stringResource(R.string.content_description_scan_icon),
            modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(stringResource(R.string.button_scan))
      }
  Button(
      onClick = { navController.navigate(editRoute) },
      modifier =
          Modifier.wrapContentSize().padding(2.dp).testTag(EventViewTestTags.EDIT_EVENT_BUTTON)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = stringResource(R.string.content_description_edit_icon),
            modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(stringResource(R.string.button_edit))
      }
}

/** Non-owner action buttons (Join/Leave) */
@Composable
private fun NonOwnerActionButtons(
    joined: Boolean,
    isFull: Boolean,
    currentEvent: Event,
    eventViewModel: EventViewModel
) {
  val now = Timestamp.now()
  val eventHasStarted = now >= currentEvent.start

  Button(
      onClick = {
        if (joined) {
          eventViewModel.showLeaveConfirmDialog()
        } else if (!isFull && !eventHasStarted) {
          eventViewModel.joinEvent(eventUid = currentEvent.uid)
        }
      },
      modifier =
          Modifier.fillMaxWidth()
              .height(56.dp)
              .testTag(
                  if (joined) EventViewTestTags.LEAVE_EVENT_BUTTON
                  else EventViewTestTags.JOIN_BUTTON),
      enabled = joined || (!eventHasStarted && !isFull),
      colors =
          ButtonDefaults.buttonColors(
              containerColor =
                  if (joined) MaterialTheme.colorScheme.error
                  else MaterialTheme.colorScheme.primary,
              disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              val showIcon = joined || (!eventHasStarted && !isFull)
              if (showIcon) {
                Icon(
                    painter =
                        if (joined) painterResource(id = R.drawable.ic_arrow_right)
                        else painterResource(id = R.drawable.ic_add),
                    contentDescription = stringResource(R.string.content_description_action_icon),
                    modifier = Modifier.size(iconSize))
                Spacer(modifier = Modifier.width(smallSpacing))
              }
              Text(
                  text =
                      when {
                        joined -> stringResource(R.string.button_leave)
                        isFull -> stringResource(R.string.button_full)
                        eventHasStarted -> stringResource(R.string.button_started)
                        else -> stringResource(R.string.button_join)
                      },
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold)
            }
      }
}

/** Common action buttons for all users (Location, Web, Share) */
@Composable
private fun CommonActionButtons(
    currentEvent: Event,
    context: Context,
    navController: NavHostController
) {
  // Only show location button if location exists
  if (currentEvent.location != null) {
    ButtonIcon(
        id = R.drawable.ic_location_pin,
        onClick = {
          currentEvent.location?.let { location ->
            // Navigate to map with location and event UID to automatically select and display the
            // event
            val route =
                Route.mapWithLocation(
                    location.latitude, location.longitude, eventUid = currentEvent.uid)
            navController.navigate(route)
          }
        },
        modifier = Modifier.testTag(EventViewTestTags.LOCATION_BUTTON))
  }

  // Only show website button if event is Public and has a non-empty website
  val publicEvent = currentEvent as? Event.Public
  val websiteUrl = publicEvent?.website
  if (!websiteUrl.isNullOrEmpty()) {
    ButtonIcon(
        id = R.drawable.ic_web,
        onClick = {
          currentEvent.website?.let { website ->
            val fixedUrl =
                when {
                  website.startsWith(HTTP_PROTOCOL) -> website
                  website.startsWith(HTTPS_PROTOCOL) -> website
                  else -> HTTPS_PROTOCOL + website
                }
            try {
              val intent = Intent(Intent.ACTION_VIEW, fixedUrl.toUri())
              context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
              Toast.makeText(
                      context, context.getString(R.string.toast_no_browser), Toast.LENGTH_LONG)
                  .show()
            }
          }
        },
        modifier = Modifier.testTag(EventViewTestTags.VISIT_WEBSITE_BUTTON))
  }

  ButtonIcon(
      id = R.drawable.ic_share,
      onClick = { DialogNotImplemented(context) },
      modifier = Modifier.testTag(EventViewTestTags.SHARE_EVENT_BUTTON))
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
            contentDescription = stringResource(R.string.content_description_action_button),
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer)
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
  Box(
      modifier =
          Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
      contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors =
                CardDefaults.cardColors(containerColor = getValidationContainerColor(result))) {
              Column(
                  modifier = Modifier.padding(24.dp).testTag(getValidationTestTag(result)),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ValidationIcon(result = result)
                    ValidationTitle(result = result)
                    ValidationMessage(result = result)

                    Button(onClick = onScanNext, modifier = Modifier.fillMaxWidth()) {
                      Text(stringResource(R.string.event_button_scan_next))
                    }

                    OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                      Text(stringResource(R.string.event_button_close_scanner))
                    }
                  }
            }
      }
}

@Composable
private fun ValidationIcon(result: TicketValidationResult) {
  val iconRes =
      when (result) {
        is TicketValidationResult.Valid -> R.drawable.ic_add
        is TicketValidationResult.Invalid -> R.drawable.ic_user
        is TicketValidationResult.Error -> R.drawable.ic_arrow_right
      }

  Icon(
      painter = painterResource(id = iconRes),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = getValidationContentColor(result))
}

@Composable
private fun ValidationTitle(result: TicketValidationResult) {
  val title =
      when (result) {
        is TicketValidationResult.Valid -> stringResource(R.string.event_validation_valid_ticket)
        is TicketValidationResult.Invalid ->
            stringResource(R.string.event_validation_invalid_ticket)
        is TicketValidationResult.Error -> stringResource(R.string.event_validation_error)
      }

  Text(
      text = title,
      style = MaterialTheme.typography.headlineMedium,
      color = getValidationContentColor(result))
}

@Composable
private fun ValidationMessage(result: TicketValidationResult) {
  val message =
      when (result) {
        is TicketValidationResult.Valid ->
            stringResource(R.string.event_validation_participant_id, result.participantId)
        is TicketValidationResult.Invalid ->
            stringResource(R.string.event_validation_user_not_registered, result.userId)
        is TicketValidationResult.Error -> result.message
      }

  Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = getValidationContentColor(result))
}

@Composable
private fun getValidationContainerColor(result: TicketValidationResult) =
    when (result) {
      is TicketValidationResult.Valid -> MaterialTheme.colorScheme.primaryContainer
      is TicketValidationResult.Invalid -> MaterialTheme.colorScheme.errorContainer
      is TicketValidationResult.Error -> MaterialTheme.colorScheme.secondaryContainer
    }

@Composable
private fun getValidationContentColor(result: TicketValidationResult) =
    when (result) {
      is TicketValidationResult.Valid -> MaterialTheme.colorScheme.onPrimaryContainer
      is TicketValidationResult.Invalid -> MaterialTheme.colorScheme.onErrorContainer
      is TicketValidationResult.Error -> MaterialTheme.colorScheme.onSecondaryContainer
    }

@Composable
private fun AttendeeItem(
    attendee: User,
    owner: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(start = screenPadding, end = screenPadding)
              .clickable(onClick = onClick),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
        painter = painterResource(R.drawable.ic_user),
        contentDescription = "attendee image",
        Modifier.size(48.dp),
    )
    Column {
      Text(
          attendee.firstName + " " + attendee.lastName,
          fontSize = MaterialTheme.typography.headlineMedium.fontSize)
      if (owner) Text(stringResource(R.string.event_label_owner))
    }
  }
}

private fun getValidationTestTag(result: TicketValidationResult) =
    when (result) {
      is TicketValidationResult.Valid -> EventViewTestTags.VALIDATION_RESULT_VALID
      is TicketValidationResult.Invalid -> EventViewTestTags.VALIDATION_RESULT_INVALID
      is TicketValidationResult.Error -> EventViewTestTags.VALIDATION_RESULT_ERROR
    }

@Composable
private fun PollNotificationCard(
    onVoteNowClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(start = screenPadding, end = screenPadding, top = 8.dp, bottom = 8.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.poll_notification_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.poll_notification_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
              }
              Button(
                  onClick = onVoteNowClick,
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.primary)) {
                    Text(stringResource(R.string.poll_notification_vote))
                  }
            }
      }
}
