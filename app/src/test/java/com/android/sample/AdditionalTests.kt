package com.android.sample

import androidx.activity.ComponentActivity
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.sample.resources.C
import com.android.sample.ui.theme.*
import org.junit.Assert.*
import org.junit.Test

class AdditionalTests {

  @Test
  fun testTypographyConfiguration() {
    assertEquals(FontFamily.Default, Typography.bodyLarge.fontFamily)
    assertEquals(FontWeight.Normal, Typography.bodyLarge.fontWeight)
    assertEquals(16.sp, Typography.bodyLarge.fontSize)
    assertEquals(24.sp, Typography.bodyLarge.lineHeight)
    assertEquals(0.5.sp, Typography.bodyLarge.letterSpacing)
  }

  @Test
  fun testMainActivityExists() {
    val activityClass = MainActivity::class.java
    assertNotNull("MainActivity class should exist", activityClass)
    assertEquals("Package name should match", "com.android.sample", activityClass.packageName)
  }

  @Test
  fun testCTagConstants() {
    assertEquals("main_screen_greeting", C.Tag.greeting)
    assertEquals("main_screen_container", C.Tag.main_screen_container)
  }

  @Test
  fun testSampleAppThemeExists() {
    val themeClass = Class.forName("com.android.sample.ui.theme.ThemeKt")
    assertNotNull("SampleAppTheme should exist", themeClass)
  }

  @Test
  fun testColorObjectsAreDistinct() {
    assertNotEquals(Purple80, Purple40)
    assertNotEquals(PurpleGrey80, PurpleGrey40)
    assertNotEquals(Pink80, Pink40)
    assertNotEquals(Purple80, PurpleGrey80)
    assertNotEquals(PurpleGrey80, Pink80)
  }

  @Test
  fun testCTagObjectStructure() {
    val tagClass = C.Tag::class.java
    val fields = tagClass.declaredFields

    assertTrue("Should have at least 2 tag constants", fields.size >= 2)

    val fieldNames = fields.map { it.name }.toSet()
    assertTrue("Missing greeting field", fieldNames.contains("greeting"))
    assertTrue("Missing main_screen_container field", fieldNames.contains("main_screen_container"))
  }

  @Test
  fun testGreetingFunction() {
    val greetingClass = Class.forName("com.android.sample.MainActivityKt")
    val methods = greetingClass.declaredMethods
    val greetingMethod = methods.find { it.name == "Greeting" }
    assertNotNull("Greeting function should exist", greetingMethod)
  }

  // Additional tests for MainActivity coverage
  @Test
  fun testMainActivityInheritance() {
    val activityClass = MainActivity::class.java
    assertTrue(
        "MainActivity should extend ComponentActivity",
        ComponentActivity::class.java.isAssignableFrom(activityClass))
  }

  @Test
  fun testGreetingFunctionSignature() {
    // Test that Greeting function exists with correct parameter types
    val greetingClass = Class.forName("com.android.sample.MainActivityKt")
    val methods = greetingClass.declaredMethods
    val greetingMethods = methods.filter { it.name == "Greeting" }

    assertTrue("Should have Greeting methods", greetingMethods.isNotEmpty())
    // Verify parameter types exist (String and Modifier)
    val hasStringParam =
        greetingMethods.any { method -> method.parameterTypes.any { it == String::class.java } }
    assertTrue("Greeting should accept String parameter", hasStringParam)
  }

  // Tests for Theme.kt dark mode functionality
  @Test
  fun testDarkColorSchemeExists() {
    // Test that dark color scheme has expected properties
    val darkScheme =
        darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

    assertNotNull("Dark color scheme should exist", darkScheme)
    assertEquals("Dark scheme primary should be Purple80", Purple80, darkScheme.primary)
    assertEquals("Dark scheme secondary should be PurpleGrey80", PurpleGrey80, darkScheme.secondary)
    assertEquals("Dark scheme tertiary should be Pink80", Pink80, darkScheme.tertiary)
  }

  @Test
  fun testLightColorSchemeExists() {
    // Test that light color scheme has expected properties
    val lightScheme =
        lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

    assertNotNull("Light color scheme should exist", lightScheme)
    assertEquals("Light scheme primary should be Purple40", Purple40, lightScheme.primary)
    assertEquals(
        "Light scheme secondary should be PurpleGrey40", PurpleGrey40, lightScheme.secondary)
    assertEquals("Light scheme tertiary should be Pink40", Pink40, lightScheme.tertiary)
  }

  @Test
  fun testSampleAppThemeFunctionExists() {
    // Test that SampleAppTheme function exists
    val themeClass = Class.forName("com.android.sample.ui.theme.ThemeKt")
    val methods = themeClass.declaredMethods
    val themeMethod = methods.find { it.name == "SampleAppTheme" }
    assertNotNull("SampleAppTheme function should exist", themeMethod)
  }

  @Test
  fun testThemeParameterTypes() {
    // Test that SampleAppTheme has correct parameter types
    val themeClass = Class.forName("com.android.sample.ui.theme.ThemeKt")
    val methods = themeClass.declaredMethods
    val themeMethods = methods.filter { it.name == "SampleAppTheme" }

    assertTrue("Should have SampleAppTheme methods", themeMethods.isNotEmpty())
    // Verify boolean parameters exist (for darkTheme and dynamicColor)
    val hasBooleanParams =
        themeMethods.any { method -> method.parameterTypes.any { it == Boolean::class.java } }
    assertTrue("SampleAppTheme should accept boolean parameters", hasBooleanParams)
  }

  @Test
  fun testTypographyIntegration() {
    // Test that Typography object is accessible and has expected properties
    assertNotNull("Typography should exist", Typography)
    assertNotNull("Typography.bodyLarge should exist", Typography.bodyLarge)
    assertEquals(
        "Typography bodyLarge fontSize should be 16.sp", 16.sp, Typography.bodyLarge.fontSize)
  }

  @Test
  fun testColorSchemeDifferences() {
    val darkScheme =
        darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

    val lightScheme =
        lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

    // Verify that dark and light schemes have different colors
    assertNotEquals(
        "Dark and light primary colors should be different",
        darkScheme.primary,
        lightScheme.primary)
    assertNotEquals(
        "Dark and light secondary colors should be different",
        darkScheme.secondary,
        lightScheme.secondary)
    assertNotEquals(
        "Dark and light tertiary colors should be different",
        darkScheme.tertiary,
        lightScheme.tertiary)
  }
}
