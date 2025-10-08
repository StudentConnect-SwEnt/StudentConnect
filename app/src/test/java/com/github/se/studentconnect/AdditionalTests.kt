package com.github.se.studentconnect

import androidx.activity.ComponentActivity
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.*
import org.junit.Assert
import org.junit.Test

class AdditionalTests {

  @Test
  fun testTypographyConfiguration() {
    Assert.assertEquals(FontFamily.Companion.Default, AppTypography.bodyLarge.fontFamily)
    Assert.assertEquals(FontWeight.Companion.Normal, AppTypography.bodyLarge.fontWeight)
    Assert.assertEquals(16.sp, AppTypography.bodyLarge.fontSize)
    Assert.assertEquals(24.sp, AppTypography.bodyLarge.lineHeight)
    Assert.assertEquals(0.5.sp, AppTypography.bodyLarge.letterSpacing)
  }

  @Test
  fun testMainActivityExists() {
    val activityClass = MainActivity::class.java
    Assert.assertNotNull("MainActivity class should exist", activityClass)
    Assert.assertEquals(
        "Package name should match", "com.github.se.studentconnect", activityClass.packageName)
  }

  @Test
  fun testCTagConstants() {
    Assert.assertEquals("main_screen_greeting", C.Tag.greeting)
    Assert.assertEquals("main_screen_container", C.Tag.main_screen_container)
  }

  @Test
  fun testSampleAppThemeExists() {
    val themeClass = Class.forName("com.github.se.studentconnect.ui.theme.ThemeKt")
    Assert.assertNotNull("SampleAppTheme should exist", themeClass)
  }

  @Test
  fun testColorObjectsAreDistinct() {
    Assert.assertNotEquals(primaryLight, primaryDark)
    Assert.assertNotEquals(secondaryLight, secondaryDark)
    Assert.assertNotEquals(tertiaryLight, tertiaryDark)
    Assert.assertNotEquals(backgroundLight, backgroundDark)
    Assert.assertNotEquals(surfaceLight, surfaceDark)
  }

  @Test
  fun testCTagObjectStructure() {
    val tagClass = C.Tag::class.java
    val fields = tagClass.declaredFields

    Assert.assertTrue("Should have at least 2 tag constants", fields.size >= 2)

    val fieldNames = fields.map { it.name }.toSet()
    Assert.assertTrue("Missing greeting field", fieldNames.contains("greeting"))
    Assert.assertTrue(
        "Missing main_screen_container field", fieldNames.contains("main_screen_container"))
  }

  @Test
  fun testGreetingFunction() {
    val greetingClass = Class.forName("com.github.se.studentconnect.MainActivityKt")
    val methods = greetingClass.declaredMethods
    val greetingMethod = methods.find { it.name == "Greeting" }
    Assert.assertNotNull("Greeting function should exist", greetingMethod)
  }

  // Additional tests for MainActivity coverage
  @Test
  fun testMainActivityInheritance() {
    val activityClass = MainActivity::class.java
    Assert.assertTrue(
        "MainActivity should extend ComponentActivity",
        ComponentActivity::class.java.isAssignableFrom(activityClass))
  }

  @Test
  fun testGreetingFunctionSignature() {
    // Test that Greeting function exists with correct parameter types
    val greetingClass = Class.forName("com.github.se.studentconnect.MainActivityKt")
    val methods = greetingClass.declaredMethods
    val greetingMethods = methods.filter { it.name == "Greeting" }

    Assert.assertTrue("Should have Greeting methods", greetingMethods.isNotEmpty())
    // Verify parameter types exist (String and Modifier)
    val hasStringParam =
        greetingMethods.any { method -> method.parameterTypes.any { it == String::class.java } }
    Assert.assertTrue("Greeting should accept String parameter", hasStringParam)
  }

  // Tests for Theme.kt dark mode functionality
  @Test
  fun testDarkColorSchemeExists() {
    // Test that dark color scheme has expected properties
    val darkScheme =
        darkColorScheme(primary = primaryDark, secondary = secondaryDark, tertiary = tertiaryDark)

    Assert.assertNotNull("Dark color scheme should exist", darkScheme)
    Assert.assertEquals(
        "Dark scheme primary should be primaryDark", primaryDark, darkScheme.primary)
    Assert.assertEquals(
        "Dark scheme secondary should be secondaryDark", secondaryDark, darkScheme.secondary)
    Assert.assertEquals(
        "Dark scheme tertiary should be tertiaryDark", tertiaryDark, darkScheme.tertiary)
  }

  @Test
  fun testLightColorSchemeExists() {
    // Test that light color scheme has expected properties
    val lightScheme =
        lightColorScheme(
            primary = primaryLight, secondary = secondaryLight, tertiary = tertiaryLight)

    Assert.assertNotNull("Light color scheme should exist", lightScheme)
    Assert.assertEquals(
        "Light scheme primary should be primaryLight", primaryLight, lightScheme.primary)
    Assert.assertEquals(
        "Light scheme secondary should be secondaryLight", secondaryLight, lightScheme.secondary)
    Assert.assertEquals(
        "Light scheme tertiary should be tertiaryLight", tertiaryLight, lightScheme.tertiary)
  }

  @Test
  fun testSampleAppThemeFunctionExists() {
    // Test that SampleAppTheme function exists
    val themeClass = Class.forName("com.github.se.studentconnect.ui.theme.ThemeKt")
    val methods = themeClass.declaredMethods
    val themeMethod = methods.find { it.name == "SampleAppTheme" }
    Assert.assertNotNull("SampleAppTheme function should exist", themeMethod)
  }

  @Test
  fun testThemeParameterTypes() {
    // Test that SampleAppTheme has correct parameter types
    val themeClass = Class.forName("com.github.se.studentconnect.ui.theme.ThemeKt")
    val methods = themeClass.declaredMethods
    val themeMethods = methods.filter { it.name == "SampleAppTheme" }

    Assert.assertTrue("Should have SampleAppTheme methods", themeMethods.isNotEmpty())
    // Verify boolean parameters exist (for darkTheme and dynamicColor)
    val hasBooleanParams =
        themeMethods.any { method -> method.parameterTypes.any { it == Boolean::class.java } }
    Assert.assertTrue("SampleAppTheme should accept boolean parameters", hasBooleanParams)
  }

  @Test
  fun testTypographyIntegration() {
    // Test that AppTypography object is accessible and has expected properties
    Assert.assertNotNull("AppTypography should exist", AppTypography)
    Assert.assertNotNull("AppTypography.bodyLarge should exist", AppTypography.bodyLarge)
    Assert.assertEquals(
        "AppTypography bodyLarge fontSize should be 16.sp", 16.sp, AppTypography.bodyLarge.fontSize)
  }

  @Test
  fun testColorSchemeDifferences() {
    val darkScheme =
        darkColorScheme(primary = primaryDark, secondary = secondaryDark, tertiary = tertiaryDark)

    val lightScheme =
        lightColorScheme(
            primary = primaryLight, secondary = secondaryLight, tertiary = tertiaryLight)

    // Verify that dark and light schemes have different colors
    Assert.assertNotEquals(
        "Dark and light primary colors should be different",
        darkScheme.primary,
        lightScheme.primary)
    Assert.assertNotEquals(
        "Dark and light secondary colors should be different",
        darkScheme.secondary,
        lightScheme.secondary)
    Assert.assertNotEquals(
        "Dark and light tertiary colors should be different",
        darkScheme.tertiary,
        lightScheme.tertiary)
  }

  @Test
  fun testAllLightThemeColorsExist() {
    // Test all light theme colors exist and are not null/unspecified
    val colors =
        listOf(
            primaryLight,
            onPrimaryLight,
            primaryContainerLight,
            onPrimaryContainerLight,
            secondaryLight,
            onSecondaryLight,
            secondaryContainerLight,
            onSecondaryContainerLight,
            tertiaryLight,
            onTertiaryLight,
            tertiaryContainerLight,
            onTertiaryContainerLight,
            errorLight,
            onErrorLight,
            errorContainerLight,
            onErrorContainerLight,
            backgroundLight,
            onBackgroundLight,
            surfaceLight,
            onSurfaceLight,
            surfaceVariantLight,
            onSurfaceVariantLight,
            outlineLight,
            outlineVariantLight,
            scrimLight,
            inverseSurfaceLight,
            inverseOnSurfaceLight,
            inversePrimaryLight,
            surfaceDimLight,
            surfaceBrightLight,
            surfaceContainerLowestLight,
            surfaceContainerLowLight,
            surfaceContainerLight,
            surfaceContainerHighLight,
            surfaceContainerHighestLight)

    colors.forEach { color ->
      Assert.assertNotEquals("Color should not be unspecified", Color.Unspecified, color)
      Assert.assertNotNull("Color should not be null", color)
    }

    // Verify specific color values
    Assert.assertEquals("Primary light should be correct", Color(0xFF5C5891), primaryLight)
    Assert.assertEquals("Background light should be correct", Color(0xFFFCF8FF), backgroundLight)
    Assert.assertEquals("Error light should be correct", Color(0xFFBA1A1A), errorLight)
  }

  @Test
  fun testAllDarkThemeColorsExist() {
    // Test all dark theme colors exist and are not null/unspecified
    val colors =
        listOf(
            primaryDark,
            onPrimaryDark,
            primaryContainerDark,
            onPrimaryContainerDark,
            secondaryDark,
            onSecondaryDark,
            secondaryContainerDark,
            onSecondaryContainerDark,
            tertiaryDark,
            onTertiaryDark,
            tertiaryContainerDark,
            onTertiaryContainerDark,
            errorDark,
            onErrorDark,
            errorContainerDark,
            onErrorContainerDark,
            backgroundDark,
            onBackgroundDark,
            surfaceDark,
            onSurfaceDark,
            surfaceVariantDark,
            onSurfaceVariantDark,
            outlineDark,
            outlineVariantDark,
            scrimDark,
            inverseSurfaceDark,
            inverseOnSurfaceDark,
            inversePrimaryDark,
            surfaceDimDark,
            surfaceBrightDark,
            surfaceContainerLowestDark,
            surfaceContainerLowDark,
            surfaceContainerDark,
            surfaceContainerHighDark,
            surfaceContainerHighestDark)

    colors.forEach { color ->
      Assert.assertNotEquals("Color should not be unspecified", Color.Unspecified, color)
      Assert.assertNotNull("Color should not be null", color)
    }

    // Verify specific color values
    Assert.assertEquals("Primary dark should be correct", Color(0xFFC5C0FF), primaryDark)
    Assert.assertEquals("Background dark should be correct", Color(0xFF131318), backgroundDark)
    Assert.assertEquals("Error dark should be correct", Color(0xFFFFB4AB), errorDark)
  }

  @Test
  fun testMediumContrastColorsExist() {
    // Test medium contrast theme colors exist
    val lightColors =
        listOf(
            primaryLightMediumContrast,
            onPrimaryLightMediumContrast,
            secondaryLightMediumContrast,
            onSecondaryLightMediumContrast,
            tertiaryLightMediumContrast,
            onTertiaryLightMediumContrast,
            errorLightMediumContrast,
            onErrorLightMediumContrast,
            backgroundLightMediumContrast,
            onBackgroundLightMediumContrast)

    val darkColors =
        listOf(
            primaryDarkMediumContrast,
            onPrimaryDarkMediumContrast,
            secondaryDarkMediumContrast,
            onSecondaryDarkMediumContrast,
            tertiaryDarkMediumContrast,
            onTertiaryDarkMediumContrast,
            errorDarkMediumContrast,
            onErrorDarkMediumContrast,
            backgroundDarkMediumContrast,
            onBackgroundDarkMediumContrast)

    (lightColors + darkColors).forEach { color ->
      Assert.assertNotEquals(
          "Medium contrast color should not be unspecified", Color.Unspecified, color)
      Assert.assertNotNull("Medium contrast color should not be null", color)
    }
  }

  @Test
  fun testHighContrastColorsExist() {
    // Test high contrast theme colors exist
    val lightColors =
        listOf(
            primaryLightHighContrast,
            onPrimaryLightHighContrast,
            secondaryLightHighContrast,
            onSecondaryLightHighContrast,
            tertiaryLightHighContrast,
            onTertiaryLightHighContrast,
            errorLightHighContrast,
            onErrorLightHighContrast,
            backgroundLightHighContrast,
            onBackgroundLightHighContrast)

    val darkColors =
        listOf(
            primaryDarkHighContrast,
            onPrimaryDarkHighContrast,
            secondaryDarkHighContrast,
            onSecondaryDarkHighContrast,
            tertiaryDarkHighContrast,
            onTertiaryDarkHighContrast,
            errorDarkHighContrast,
            onErrorDarkHighContrast,
            backgroundDarkHighContrast,
            onBackgroundDarkHighContrast)

    (lightColors + darkColors).forEach { color ->
      Assert.assertNotEquals(
          "High contrast color should not be unspecified", Color.Unspecified, color)
      Assert.assertNotNull("High contrast color should not be null", color)
    }
  }

  @Test
  fun testColorSchemeCreation() {
    // Test that we can create complete color schemes with all our theme colors
    val lightScheme =
        lightColorScheme(
            primary = primaryLight,
            onPrimary = onPrimaryLight,
            primaryContainer = primaryContainerLight,
            onPrimaryContainer = onPrimaryContainerLight,
            secondary = secondaryLight,
            onSecondary = onSecondaryLight,
            secondaryContainer = secondaryContainerLight,
            onSecondaryContainer = onSecondaryContainerLight,
            tertiary = tertiaryLight,
            onTertiary = onTertiaryLight,
            tertiaryContainer = tertiaryContainerLight,
            onTertiaryContainer = onTertiaryContainerLight,
            error = errorLight,
            onError = onErrorLight,
            errorContainer = errorContainerLight,
            onErrorContainer = onErrorContainerLight,
            background = backgroundLight,
            onBackground = onBackgroundLight,
            surface = surfaceLight,
            onSurface = onSurfaceLight,
            surfaceVariant = surfaceVariantLight,
            onSurfaceVariant = onSurfaceVariantLight,
            outline = outlineLight,
            outlineVariant = outlineVariantLight,
            scrim = scrimLight,
            inverseSurface = inverseSurfaceLight,
            inverseOnSurface = inverseOnSurfaceLight,
            inversePrimary = inversePrimaryLight)

    val darkScheme =
        darkColorScheme(
            primary = primaryDark,
            onPrimary = onPrimaryDark,
            primaryContainer = primaryContainerDark,
            onPrimaryContainer = onPrimaryContainerDark,
            secondary = secondaryDark,
            onSecondary = onSecondaryDark,
            secondaryContainer = secondaryContainerDark,
            onSecondaryContainer = onSecondaryContainerDark,
            tertiary = tertiaryDark,
            onTertiary = onTertiaryDark,
            tertiaryContainer = tertiaryContainerDark,
            onTertiaryContainer = onTertiaryContainerDark,
            error = errorDark,
            onError = onErrorDark,
            errorContainer = errorContainerDark,
            onErrorContainer = onErrorContainerDark,
            background = backgroundDark,
            onBackground = onBackgroundDark,
            surface = surfaceDark,
            onSurface = onSurfaceDark,
            surfaceVariant = surfaceVariantDark,
            onSurfaceVariant = onSurfaceVariantDark,
            outline = outlineDark,
            outlineVariant = outlineVariantDark,
            scrim = scrimDark,
            inverseSurface = inverseSurfaceDark,
            inverseOnSurface = inverseOnSurfaceDark,
            inversePrimary = inversePrimaryDark)

    Assert.assertNotNull("Light scheme should be created", lightScheme)
    Assert.assertNotNull("Dark scheme should be created", darkScheme)

    // Verify the schemes use our colors
    Assert.assertEquals("Light scheme primary", primaryLight, lightScheme.primary)
    Assert.assertEquals("Light scheme background", backgroundLight, lightScheme.background)
    Assert.assertEquals("Dark scheme primary", primaryDark, darkScheme.primary)
    Assert.assertEquals("Dark scheme background", backgroundDark, darkScheme.background)
  }

  @Test
  fun testColorFamilyStructure() {
    // Test ColorFamily data class
    val testFamily =
        ColorFamily(
            color = primaryLight,
            onColor = onPrimaryLight,
            colorContainer = primaryContainerLight,
            onColorContainer = onPrimaryContainerLight)

    Assert.assertEquals("ColorFamily color should match", primaryLight, testFamily.color)
    Assert.assertEquals("ColorFamily onColor should match", onPrimaryLight, testFamily.onColor)
    Assert.assertEquals(
        "ColorFamily colorContainer should match", primaryContainerLight, testFamily.colorContainer)
    Assert.assertEquals(
        "ColorFamily onColorContainer should match",
        onPrimaryContainerLight,
        testFamily.onColorContainer)

    // Test unspecified scheme
    Assert.assertEquals("Unspecified scheme color", Color.Unspecified, unspecified_scheme.color)
    Assert.assertEquals("Unspecified scheme onColor", Color.Unspecified, unspecified_scheme.onColor)
    Assert.assertEquals(
        "Unspecified scheme colorContainer", Color.Unspecified, unspecified_scheme.colorContainer)
    Assert.assertEquals(
        "Unspecified scheme onColorContainer",
        Color.Unspecified,
        unspecified_scheme.onColorContainer)
  }
}
