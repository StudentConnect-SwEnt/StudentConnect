package com.github.se.studentconnect.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.google.firebase.Timestamp
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Background worker that checks for events starting soon and creates notifications
 *
 * This worker runs periodically to check for events that are starting within the next hour and
 * creates notifications for users who have joined those events
 */
class EventReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

  companion object {
    private const val TAG = "EventReminderWorker"
    private const val REMINDER_WINDOW_MINUTES = 60L // Check for events starting in the next hour
  }

  override suspend fun doWork(): Result {
    Log.d(TAG, "Event reminder worker started")

    try {
      val eventRepository = EventRepositoryProvider.repository
      val notificationRepository = NotificationRepositoryProvider.repository

      // Get all visible events
      val allEvents = eventRepository.getAllVisibleEvents()

      // Calculate time window (current time to 1 hour from now)
      val currentTime = Timestamp.now()
      val reminderWindowEnd =
          Timestamp(
              Date(currentTime.toDate().time + TimeUnit.MINUTES.toMillis(REMINDER_WINDOW_MINUTES)))

      // Filter events that are starting within the reminder window
      val upcomingEvents =
          allEvents.filter { event ->
            event.start >= currentTime && event.start <= reminderWindowEnd
          }

      Log.d(TAG, "Found ${upcomingEvents.size} events starting soon")

      // For each upcoming event, create notifications for all participants
      for (event in upcomingEvents) {
        try {
          // Get participants for this event
          val participants = eventRepository.getEventParticipants(event.uid)

          Log.d(TAG, "Event ${event.title} has ${participants.size} participants")

          // Create notification for each participant
          for (participant in participants) {
            try {
              // Check if notification already exists for this user and event
              // We'll use a composite ID to avoid duplicate notifications
              val notificationId = "event_${event.uid}_user_${participant.uid}"

              val notification =
                  Notification.EventStarting(
                      id = notificationId,
                      userId = participant.uid,
                      eventId = event.uid,
                      eventTitle = event.title,
                      eventStart = event.start,
                      timestamp = Timestamp.now(),
                      isRead = false)

              notificationRepository.createNotification(
                  notification,
                  onSuccess = { Log.d(TAG, "Created notification for user ${participant.uid}") },
                  onFailure = { e ->
                    // Notification may already exist, which is fine
                    Log.w(
                        TAG,
                        "Failed to create notification for user ${participant.uid}: ${e.message}")
                  })
            } catch (e: Exception) {
              Log.e(TAG, "Error creating notification for participant ${participant.uid}", e)
            }
          }
        } catch (e: Exception) {
          Log.e(TAG, "Error processing event ${event.uid}", e)
        }
      }

      Log.d(TAG, "Event reminder worker completed successfully")
      return Result.success()
    } catch (e: Exception) {
      Log.e(TAG, "Event reminder worker failed", e)
      return Result.retry()
    }
  }
}
