package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@Composable
fun DateTextField(
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    initialValue: String = "",
    onDateChange: (LocalDate?) -> Unit,
) {
  var dateString by remember { mutableStateOf(initialValue) }
  var hasInteractedWithField by remember { mutableStateOf(false) }

  val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  dateFormat.isLenient = false // strict parsing

  LaunchedEffect(initialValue) {
    dateString = initialValue
    hasInteractedWithField = false
  }

  val date =
      // make sure the format is matched exactly
      if (!Regex("""^\d{2}/\d{2}/\d{4}$""").matches(dateString)) null
      else
          try {
            val pos = ParsePosition(0)
            val dateAsDate = dateFormat.parse(dateString, pos)

            val dateAsLocalDate =
                dateAsDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

            // make sure the entire string was consumed (no extra characters at the end)
            if (pos.index == dateString.length) dateAsLocalDate else null
          } catch (_: Exception) {
            null // the date does not exist
          }

  LaunchedEffect(date, hasInteractedWithField) {
    if (!hasInteractedWithField) return@LaunchedEffect
    onDateChange(date)
  }

  FormTextField(
      modifier = modifier,
      value = dateString,
      onValueChange = {
        hasInteractedWithField = true
        dateString = it
      },
      label = label,
      placeholder = placeholder,
      errorText =
          if (dateString.isEmpty()) "Date cannot be empty"
          else if (date == null) "Date is invalid" else null // no error
      )
}
