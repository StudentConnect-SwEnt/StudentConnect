package com.github.se.studentconnect.ui.screen.signup.regularuser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.components.CountryListSurface
import com.github.se.studentconnect.ui.components.filterCountries
import com.github.se.studentconnect.ui.components.loadCountries
import com.github.se.studentconnect.ui.screen.signup.SignUpBackButton
import com.github.se.studentconnect.ui.screen.signup.SignUpLargeSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpMediumSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpPrimaryButton
import com.github.se.studentconnect.ui.screen.signup.SignUpScreenConstants
import com.github.se.studentconnect.ui.screen.signup.SignUpSmallSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpSubtitle
import com.github.se.studentconnect.ui.screen.signup.SignUpTitle
import com.github.se.studentconnect.ui.theme.AppTheme

@Composable
fun NationalityScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    viewModel: SignUpViewModel = viewModel()
) {
  val signUpState by viewModel.state
  var query by rememberSaveable { mutableStateOf("") }
  val countries = remember { loadCountries() }
  val filteredCountries = remember(query, countries) { filterCountries(query, countries) }

  var selectedCode by remember { mutableStateOf(signUpState.nationality) }

  LaunchedEffect(signUpState.nationality) { selectedCode = signUpState.nationality }

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(
                  horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                  vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING),
      horizontalAlignment = Alignment.Start) {
        SignUpBackButton(onClick = onBack)

        SignUpMediumSpacer()

        SignUpTitle(text = stringResource(R.string.instruction_where_are_you_from))
        SignUpSmallSpacer()
        SignUpSubtitle(text = stringResource(R.string.instruction_where_are_you_from_subtitle))

        SignUpLargeSpacer()

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.placeholder_search_countries)) },
            singleLine = true,
            trailingIcon = {
              Icon(
                  Icons.Filled.Search,
                  contentDescription = stringResource(R.string.content_description_search))
            })

        SignUpMediumSpacer()

        CountryListSurface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            filteredCountries = filteredCountries,
            selectedCode = selectedCode,
            onCountrySelect = { country ->
              selectedCode = country.code
              viewModel.setNationality(country.code)
            })

        SignUpLargeSpacer()

        SignUpPrimaryButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.button_continue),
            iconRes = R.drawable.ic_arrow_forward,
            onClick = onContinue,
            enabled = !selectedCode.isNullOrBlank())
      }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NationalityScreenPreview() {
  AppTheme { NationalityScreen(onContinue = {}, onBack = {}) }
}
