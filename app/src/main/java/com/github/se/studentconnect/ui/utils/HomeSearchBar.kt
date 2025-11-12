package com.github.se.studentconnect.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.resources.C

/**
 * The search bar on the home screen.
 *
 * @param modifier the modifier for the search bar
 * @param query the query string in the search bar
 * @param onQueryChange callback for when the query changes
 * @param enabled whether the search bar text field should be enabled or not
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    enabled: Boolean = true,
) {
  SearchBar(
      modifier = modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 5.dp),
      inputField = {
        SearchBarDefaults.InputField(
            modifier = Modifier.testTag(C.Tag.search_input_field),
            leadingIcon = {
              Icon(
                  painterResource(R.drawable.ic_search),
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = MaterialTheme.colorScheme.onSurface,
              )
            },
            query = query,
            onQueryChange = onQueryChange,
            placeholder = { Text("Search") },
            onSearch = {},
            expanded = false,
            onExpandedChange = {},
            enabled = enabled,
        )
      },
      expanded = false,
      onExpandedChange = {},
      colors =
          SearchBarColors(
              MaterialTheme.colorScheme.surfaceContainer,
              MaterialTheme.colorScheme.onSurface,
          ),
  ) {}
}
