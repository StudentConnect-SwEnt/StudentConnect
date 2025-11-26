package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.github.se.studentconnect.ui.profile.FriendsListViewModel
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers

/** Friends list screen showing all friends for a given user. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onFriendClick: (String) -> Unit,
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: FriendsListViewModel = viewModel {
      FriendsListViewModel(
          userRepository = userRepository,
          friendsRepository = FriendsRepositoryProvider.repository,
          userId = userId)
    }
) {
  val filteredFriends by viewModel.filteredFriends.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val error by viewModel.error.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = stringResource(R.string.title_friends_list)) },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_back))
              }
            })
      }) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadFriends() },
            modifier = Modifier.fillMaxSize().padding(paddingValues)) {
              Column(modifier = Modifier.fillMaxSize()) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    modifier =
                        Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.medium))

                when {
                  error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      Text(
                          text = error ?: stringResource(R.string.error_failed_to_load_friends),
                          color = MaterialTheme.colorScheme.error,
                          style = MaterialTheme.typography.bodyLarge)
                    }
                  }
                  filteredFriends.isEmpty() && !isLoading -> {
                    EmptyFriendsState(modifier = Modifier.fillMaxSize())
                  }
                  else -> {
                    FriendsList(friends = filteredFriends, onFriendClick = onFriendClick)
                  }
                }
              }
            }
      }
}

/** Search bar for filtering friends. */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  TextField(
      value = query,
      onValueChange = onQueryChange,
      modifier = modifier.padding(vertical = MaterialTheme.spacing.small),
      placeholder = { Text(text = stringResource(R.string.placeholder_search_friends)) },
      leadingIcon = {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(R.string.content_description_search))
      },
      shape = MaterialTheme.shapes.large,
      singleLine = true)
}

/** Empty state when user has no friends. */
@Composable
private fun EmptyFriendsState(modifier: Modifier = Modifier) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
          text = stringResource(R.string.text_no_friends_title),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface)
      Text(
          text = stringResource(R.string.text_no_friends_subtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

/** List of friends. */
@Composable
private fun FriendsList(
    friends: List<User>,
    onFriendClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)) {
        items(friends, key = { it.userId }) { friend -> FriendItem(friend, onFriendClick) }
      }
}

/** Single friend item. */
@Composable
private fun FriendItem(friend: User, onFriendClick: (String) -> Unit) {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val profileId = friend.profilePictureUrl
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, profileId, repository) {
        value =
            profileId?.let { id ->
              runCatching { repository.download(id) }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clickable { onFriendClick(friend.userId) }
              .padding(vertical = MaterialTheme.spacing.small),
      verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier.size(MaterialTheme.sizing.profilePictureSmall)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center) {
              if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription =
                        stringResource(R.string.content_description_friend_profile_picture),
                    modifier =
                        Modifier.size(MaterialTheme.sizing.profilePictureSmall).clip(CircleShape),
                    contentScale = ContentScale.Crop)
              } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription =
                        stringResource(R.string.content_description_friend_profile_picture),
                    modifier = Modifier.size(MaterialTheme.sizing.iconMedium),
                    tint = MaterialTheme.colorScheme.primary)
              }
            }

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = friend.getFullName(),
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface)

          Text(
              text = friend.username,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
}

/** Extension properties for Material spacing. */
private val MaterialTheme.spacing: Spacing
  @Composable get() = Spacing

private object Spacing {
  val extraSmall = 8.dp
  val small = 12.dp
  val medium = 16.dp
}

/** Extension properties for Material sizing. */
private val MaterialTheme.sizing: Sizing
  @Composable get() = Sizing

private object Sizing {
  val profilePictureSmall = 56.dp
  val iconMedium = 32.dp
}
