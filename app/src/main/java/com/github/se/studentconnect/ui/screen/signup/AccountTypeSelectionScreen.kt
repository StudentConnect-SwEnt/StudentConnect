package com.github.se.studentconnect.ui.screen.signup

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import kotlinx.coroutines.delay

/**
 * Represents the available account types that users can select during onboarding.
 *
 * Each account type has associated UI resources for display:
 * - [titleRes]: The main title displayed on the account type card
 * - [featureRes]: List of feature descriptions shown when the card is expanded
 * - [contentDescriptionRes]: Accessibility content description for screen readers
 * - [iconRes]: Emoji or icon resource displayed on the card
 *
 * @property titleRes String resource ID for the account type title
 * @property featureRes List of string resource IDs describing the account type features
 * @property contentDescriptionRes String resource ID for accessibility content description
 * @property iconRes String resource ID for the account type icon/emoji
 */
enum class AccountTypeOption(
    @StringRes val titleRes: Int,
    val featureRes: List<Int>,
    @StringRes val contentDescriptionRes: Int,
    @StringRes val iconRes: Int
) {
  /**
   * Regular user account type for individual students. Allows users to see, join, and save events,
   * add friends, and create public/private events.
   */
  RegularUser(
      titleRes = R.string.account_type_regular_user,
      featureRes =
          listOf(
              R.string.account_type_regular_user_feature_events,
              R.string.account_type_regular_user_feature_friends,
              R.string.account_type_regular_user_feature_create_event),
      contentDescriptionRes = R.string.content_description_select_regular_user,
      iconRes = R.string.account_type_regular_user_icon),

  /**
   * Organization account type for institutions, clubs, or companies. Provides features for event
   * promotion, analytics, staff management, and centralized operations.
   */
  Organization(
      titleRes = R.string.account_type_organization,
      featureRes =
          listOf(
              R.string.account_type_organization_feature_promote,
              R.string.account_type_organization_feature_analytics,
              R.string.account_type_organization_feature_hire,
              R.string.account_type_organization_feature_operations),
      contentDescriptionRes = R.string.content_description_select_organization,
      iconRes = R.string.account_type_organization_icon)
}

/**
 * Animated account type selection screen for onboarding. Users can toggle between a "Regular User"
 * and "Organization" card with satisfying spring-based transitions and contextual details.
 *
 * @param onContinue Invoked when the user confirms their choice.
 * @param onBack Invoked when the back button is pressed.
 */
@Composable
fun AccountTypeSelectionScreen(onContinue: (AccountTypeOption) -> Unit, onBack: () -> Unit) {
  var selectedOption by remember { mutableStateOf<AccountTypeOption?>(null) }

  val metrics = rememberAccountTypeMetrics()
  val accentColor = MaterialTheme.colorScheme.primary

  Column(
      modifier =
          Modifier.fillMaxSize()
              .background(MaterialTheme.colorScheme.background)
              .padding(
                  horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                  vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.SpaceBetween) {
        Column {
          SignUpBackButton(onClick = onBack)
          SignUpMediumSpacer()
          SignUpTitle(text = stringResource(R.string.signup_account_type_title))
          SignUpSmallSpacer()
          SignUpSubtitle(text = stringResource(R.string.signup_account_type_subtitle))
          SignUpLargeSpacer()

          AccountTypeOption.entries.forEachIndexed { index, option ->
            AccountTypeAnimatedCard(
                option = option,
                isSelected = selectedOption == option,
                otherSelected = selectedOption != null && selectedOption != option,
                onSelect = { selectedOption = it }, // Updates local UI only
                metrics = metrics,
                accentColor = accentColor)
            if (index != AccountTypeOption.entries.lastIndex) {
              Spacer(Modifier.height(metrics.interCardSpacing))
            }
          }
        }

        SignUpPrimaryButton(
            text = stringResource(R.string.button_continue),
            onClick = { selectedOption?.let { onContinue(it) } },
            enabled = selectedOption != null,
            modifier = Modifier.align(Alignment.CenterHorizontally))
      }
}

@Composable
private fun AccountTypeAnimatedCard(
    option: AccountTypeOption,
    isSelected: Boolean,
    otherSelected: Boolean,
    onSelect: (AccountTypeOption) -> Unit,
    metrics: AccountTypeMetrics,
    accentColor: Color
) {
  val interactionSource = remember { MutableInteractionSource() }
  val shape =
      if (otherSelected) CircleShape
      else
          RoundedCornerShape(
              if (isSelected) metrics.selectedCornerRadius else metrics.defaultCornerRadius)
  val targetHeight =
      when {
        isSelected -> metrics.expandedHeight
        otherSelected -> metrics.collapsedSize
        else -> metrics.mediumHeight
      }
  val alpha by
      animateFloatAsState(
          targetValue = if (otherSelected) 0.9f else 1f,
          animationSpec = tween(400),
          label = "alpha")
  val scale by
      animateFloatAsState(
          targetValue = if (isSelected) 1f else if (otherSelected) 0.9f else 0.96f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
          label = "scale")

  BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
    val desiredWidth =
        when {
          isSelected -> maxWidth
          otherSelected -> metrics.collapsedSize
          else -> maxWidth
        }
    val animatedWidth by
        animateDpAsState(
            targetValue = desiredWidth,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "width")
    val animatedHeight by
        animateDpAsState(
            targetValue = targetHeight,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "height")

    val cardContentDescription = stringResource(option.contentDescriptionRes)

    Surface(
        modifier =
            Modifier.width(animatedWidth)
                .height(animatedHeight)
                .graphicsLayer {
                  this.alpha = alpha
                  scaleX = scale
                  scaleY = scale
                }
                .clip(shape)
                .semantics { contentDescription = cardContentDescription }
                .clickable(enabled = true, interactionSource = interactionSource) {
                  onSelect(option)
                },
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color =
            if (isSelected) accentColor.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant,
        contentColor =
            if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface) {
          if (otherSelected) {
            SmallIconOnlyContent(option = option, metrics = metrics)
          } else {
            ExpandedCardContent(
                option = option,
                isSelected = isSelected,
                metrics = metrics,
                accentColor = accentColor)
          }
        }
  }
}

@Composable
private fun SmallIconOnlyContent(option: AccountTypeOption, metrics: AccountTypeMetrics) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = stringResource(option.iconRes), fontSize = metrics.compactIconSize)
  }
}

@Composable
private fun ExpandedCardContent(
    option: AccountTypeOption,
    isSelected: Boolean,
    metrics: AccountTypeMetrics,
    accentColor: Color
) {
  val iconScale by
      animateFloatAsState(
          targetValue = if (isSelected) 1.1f else 1f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
          label = "iconScale")
  val featureAnimations = remember(option) { option.featureRes.map { Animatable(0f) } }

  LaunchedEffect(isSelected) {
    if (isSelected) {
      featureAnimations.forEachIndexed { index, anim ->
        delay(50L * index)
        anim.animateTo(1f, animationSpec = tween(durationMillis = 450))
      }
    } else {
      featureAnimations.forEach { anim ->
        anim.animateTo(0f, animationSpec = tween(durationMillis = 200))
      }
    }
  }

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(
                  horizontal = metrics.cardContentHorizontalPadding,
                  vertical = metrics.cardContentVerticalPadding),
      verticalArrangement = Arrangement.spacedBy(metrics.featureSpacing)) {
        Text(
            text = stringResource(option.iconRes),
            fontSize = metrics.heroIconSize,
            modifier = Modifier.scale(iconScale))
        Text(
            text = stringResource(option.titleRes),
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold, color = accentColor),
            textAlign = TextAlign.Start)

        featureAnimations.forEachIndexed { index, anim ->
          val alpha = anim.value
          val offsetY = (1f - alpha) * 12f
          Row(
              modifier =
                  Modifier.fillMaxWidth().graphicsLayer { translationY = offsetY }.alpha(alpha)) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.padding(top = metrics.featureIconTopPadding))
                Spacer(Modifier.width(metrics.featureIconTextSpacing))
                Text(
                    text = stringResource(option.featureRes[index]),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
              }
        }
      }
}

/**
 * Container for all sizing and spacing measurements used in the animated account type selection
 * cards.
 *
 * "Metrics" here refers to the dimensional measurements (sizes, spacing, padding, etc.) that
 * control the layout and animation behavior of the account type cards. These values are calculated
 * responsively based on screen dimensions to ensure consistent proportions across different device
 * sizes.
 *
 * Used by [AccountTypeAnimatedCard], [ExpandedCardContent], and [SmallIconOnlyContent] to determine
 * card heights, corner radii, spacing, and icon sizes during the animated transitions between
 * selected and unselected states.
 *
 * @property collapsedSize Size of the card when it's collapsed (when another card is selected)
 * @property mediumHeight Default height of an unselected card
 * @property expandedHeight Height of the card when it's selected and expanded
 * @property selectedCornerRadius Corner radius for the selected card
 * @property defaultCornerRadius Corner radius for unselected cards
 * @property interCardSpacing Vertical spacing between account type cards
 * @property cardContentHorizontalPadding Horizontal padding inside the card content
 * @property cardContentVerticalPadding Vertical padding inside the card content
 * @property featureSpacing Vertical spacing between feature list items
 * @property featureIconTopPadding Top padding for feature icons
 * @property featureIconTextSpacing Horizontal spacing between feature icon and text
 * @property compactIconSize Font size for the icon when card is collapsed
 * @property heroIconSize Font size for the icon when card is expanded
 */
private data class AccountTypeMetrics(
    val collapsedSize: Dp,
    val mediumHeight: Dp,
    val expandedHeight: Dp,
    val selectedCornerRadius: Dp,
    val defaultCornerRadius: Dp,
    val interCardSpacing: Dp,
    val cardContentHorizontalPadding: Dp,
    val cardContentVerticalPadding: Dp,
    val featureSpacing: Dp,
    val featureIconTopPadding: Dp,
    val featureIconTextSpacing: Dp,
    val compactIconSize: TextUnit,
    val heroIconSize: TextUnit
)

/**
 * Calculates responsive sizing and spacing metrics for account type selection cards.
 *
 * All measurements are computed as percentages of screen dimensions to ensure consistent
 * proportions across different device sizes. These metrics are used to control the animated
 * transitions between card states (collapsed, medium, expanded).
 */
@Composable
private fun rememberAccountTypeMetrics(): AccountTypeMetrics {
  val windowInfo = LocalWindowInfo.current
  val density = LocalDensity.current
  val widthDp =
      with(density) { windowInfo.containerSize.width.coerceAtLeast(1).toDp().coerceAtLeast(1.dp) }
  val heightDp =
      with(density) { windowInfo.containerSize.height.coerceAtLeast(1).toDp().coerceAtLeast(1.dp) }
  val screenWidth = widthDp.value
  val screenHeight = heightDp.value

  val collapsed = (screenWidth * 0.2f).dp
  val medium = (screenHeight * 0.24f).dp
  val expanded = (screenHeight * 0.42f).dp
  val selectedCorner = (screenWidth * 0.08f).dp
  val defaultCorner = (screenWidth * 0.07f).dp
  val cardSpacing = (screenHeight * 0.02f).dp
  val horizontalPadding = (screenWidth * 0.06f).dp
  val verticalPadding = (screenHeight * 0.02f).dp
  val featureSpacing = (screenHeight * 0.015f).dp
  val featureIconTopPadding = (screenHeight * 0.002f).dp
  val featureTextSpacing = (screenWidth * 0.03f).dp
  val compactIcon = with(density) { (screenWidth * 0.09f).dp.toSp() }
  val heroIcon = with(density) { (screenWidth * 0.14f).dp.toSp() }

  return AccountTypeMetrics(
      collapsedSize = collapsed,
      mediumHeight = medium,
      expandedHeight = expanded,
      selectedCornerRadius = selectedCorner,
      defaultCornerRadius = defaultCorner,
      interCardSpacing = cardSpacing,
      cardContentHorizontalPadding = horizontalPadding,
      cardContentVerticalPadding = verticalPadding,
      featureSpacing = featureSpacing,
      featureIconTopPadding = featureIconTopPadding,
      featureIconTextSpacing = featureTextSpacing,
      compactIconSize = compactIcon,
      heroIconSize = heroIcon)
}
