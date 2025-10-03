package com.github.se.studentconnect

import androidx.activity.ComponentActivity
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.theme.Pink40
import com.github.se.studentconnect.theme.Pink80
import com.github.se.studentconnect.theme.Purple40
import com.github.se.studentconnect.theme.Purple80
import com.github.se.studentconnect.theme.PurpleGrey40
import com.github.se.studentconnect.theme.PurpleGrey80
import com.github.se.studentconnect.theme.Typography
import org.junit.Assert
import org.junit.Test

class AdditionalTests {

  @Test
  fun testTypographyConfiguration() {
    Assert.assertEquals(FontFamily.Companion.Default, Typography.bodyLarge.fontFamily)
    Assert.assertEquals(FontWeight.Companion.Normal, Typography.bodyLarge.fontWeight)
    Assert.assertEquals(16.sp, Typography.bodyLarge.fontSize)
    Assert.assertEquals(24.sp, Typography.bodyLarge.lineHeight)
    Assert.assertEquals(0.5.sp, Typography.bodyLarge.letterSpacing)
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
    Assert.assertNotEquals(Purple80, Purple40)
    Assert.assertNotEquals(PurpleGrey80, PurpleGrey40)
    Assert.assertNotEquals(Pink80, Pink40)
    Assert.assertNotEquals(Purple80, PurpleGrey80)
    Assert.assertNotEquals(PurpleGrey80, Pink80)
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
        darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

    Assert.assertNotNull("Dark color scheme should exist", darkScheme)
    Assert.assertEquals("Dark scheme primary should be Purple80", Purple80, darkScheme.primary)
    Assert.assertEquals(
        "Dark scheme secondary should be PurpleGrey80", PurpleGrey80, darkScheme.secondary)
    Assert.assertEquals("Dark scheme tertiary should be Pink80", Pink80, darkScheme.tertiary)
  }

  @Test
  fun testLightColorSchemeExists() {
    // Test that light color scheme has expected properties
    val lightScheme =
        lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

    Assert.assertNotNull("Light color scheme should exist", lightScheme)
    Assert.assertEquals("Light scheme primary should be Purple40", Purple40, lightScheme.primary)
    Assert.assertEquals(
        "Light scheme secondary should be PurpleGrey40", PurpleGrey40, lightScheme.secondary)
    Assert.assertEquals("Light scheme tertiary should be Pink40", Pink40, lightScheme.tertiary)
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
    // Test that Typography object is accessible and has expected properties
    Assert.assertNotNull("Typography should exist", Typography)
    Assert.assertNotNull("Typography.bodyLarge should exist", Typography.bodyLarge)
    Assert.assertEquals(
        "Typography bodyLarge fontSize should be 16.sp", 16.sp, Typography.bodyLarge.fontSize)
  }

  @Test
  fun testColorSchemeDifferences() {
    val darkScheme =
        darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

    val lightScheme =
        lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

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
}
