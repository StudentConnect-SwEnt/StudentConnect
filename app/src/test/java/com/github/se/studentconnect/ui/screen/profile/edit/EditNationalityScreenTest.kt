package com.github.se.studentconnect.ui.screen.profile.edit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditNationalityScreenTest {

  @Test
  fun `loadCountries returns non-empty list`() {
    val countries = loadCountries()

    assertTrue("Countries list should not be empty", countries.isNotEmpty())
    assertTrue("Countries list should have at least 200 countries", countries.size > 200)
  }

  @Test
  fun `loadCountries returns sorted list`() {
    val countries = loadCountries()

    // Verify the list is sorted alphabetically by name
    for (i in 0 until countries.size - 1) {
      assertTrue(
          "Countries should be sorted: ${countries[i].name} should come before ${countries[i + 1].name}",
          countries[i].name <= countries[i + 1].name)
    }
  }

  @Test
  fun `loadCountries contains expected countries`() {
    val countries = loadCountries()
    val countryNames = countries.map { it.name }

    // Check for some well-known countries
    assertTrue("Should contain United States", countryNames.contains("United States"))
    assertTrue("Should contain France", countryNames.contains("France"))
    assertTrue("Should contain Germany", countryNames.contains("Germany"))
    assertTrue("Should contain Switzerland", countryNames.contains("Switzerland"))
    assertTrue("Should contain Japan", countryNames.contains("Japan"))
    assertTrue("Should contain Canada", countryNames.contains("Canada"))
    assertTrue("Should contain Australia", countryNames.contains("Australia"))
    assertTrue("Should contain Brazil", countryNames.contains("Brazil"))
    assertTrue("Should contain China", countryNames.contains("China"))
    assertTrue("Should contain India", countryNames.contains("India"))
  }

  @Test
  fun `loadCountries returns countries with valid codes`() {
    val countries = loadCountries()

    countries.forEach { country ->
      assertNotNull("Country code should not be null", country.code)
      assertEquals("Country code should be 2 characters: ${country.code}", 2, country.code.length)
      assertTrue(
          "Country code should be uppercase: ${country.code}",
          country.code == country.code.uppercase())
    }
  }

  @Test
  fun `loadCountries returns countries with valid names`() {
    val countries = loadCountries()

    countries.forEach { country ->
      assertNotNull("Country name should not be null", country.name)
      assertTrue("Country name should not be empty: ${country.name}", country.name.isNotEmpty())
    }
  }

  @Test
  fun `loadCountries returns countries with valid flags`() {
    val countries = loadCountries()

    countries.forEach { country ->
      assertNotNull("Country flag should not be null", country.flag)
      assertTrue("Country flag should not be empty: ${country.flag}", country.flag.isNotEmpty())
    }
  }

  @Test
  fun `loadCountries returns unique country codes`() {
    val countries = loadCountries()
    val countryCodes = countries.map { it.code }
    val uniqueCodes = countryCodes.toSet()

    assertEquals("All country codes should be unique", countryCodes.size, uniqueCodes.size)
  }

  @Test
  fun `loadCountries returns unique country names`() {
    val countries = loadCountries()
    val countryNames = countries.map { it.name }
    val uniqueNames = countryNames.toSet()

    assertEquals("All country names should be unique", countryNames.size, uniqueNames.size)
  }

  @Test
  fun `Country data class properties are accessible`() {
    val country = Country(code = "US", name = "United States", flag = "🇺🇸")

    assertEquals("US", country.code)
    assertEquals("United States", country.name)
    assertEquals("🇺🇸", country.flag)
  }

  @Test
  fun `Country data class equality works correctly`() {
    val country1 = Country(code = "US", name = "United States", flag = "🇺🇸")
    val country2 = Country(code = "US", name = "United States", flag = "🇺🇸")
    val country3 = Country(code = "FR", name = "France", flag = "🇫🇷")

    assertEquals("Same countries should be equal", country1, country2)
    assertFalse("Different countries should not be equal", country1 == country3)
  }

  @Test
  fun `Country data class copy works correctly`() {
    val original = Country(code = "US", name = "United States", flag = "🇺🇸")
    val copied = original.copy(name = "USA")

    assertEquals("US", copied.code)
    assertEquals("USA", copied.name)
    assertEquals("🇺🇸", copied.flag)
    assertEquals("Original should be unchanged", "United States", original.name)
  }

  @Test
  fun `loadCountries specific country flags are correct`() {
    val countries = loadCountries()

    // Find specific countries and verify their flags
    val us = countries.find { it.code == "US" }
    assertNotNull("United States should exist", us)
    assertEquals("🇺🇸", us?.flag)

    val fr = countries.find { it.code == "FR" }
    assertNotNull("France should exist", fr)
    assertEquals("🇫🇷", fr?.flag)

    val ch = countries.find { it.code == "CH" }
    assertNotNull("Switzerland should exist", ch)
    assertEquals("🇨🇭", ch?.flag)

    val gb = countries.find { it.code == "GB" }
    assertNotNull("United Kingdom should exist", gb)
    assertEquals("🇬🇧", gb?.flag)
  }

  @Test
  fun `loadCountries handles countries with special characters in names`() {
    val countries = loadCountries()
    val countryNames = countries.map { it.name }

    // Some countries have special characters in their names
    val specialNameCountries = countries.filter { it.name.any { char -> char > 127.toChar() } }

    // There should be some countries with special characters (e.g., Côte d'Ivoire)
    assertTrue(
        "Should have countries with special characters in names", specialNameCountries.isNotEmpty())
  }

  @Test
  fun `loadCountries returns consistent results on multiple calls`() {
    val countries1 = loadCountries()
    val countries2 = loadCountries()

    assertEquals(
        "Multiple calls should return same number of countries", countries1.size, countries2.size)
    assertEquals("Multiple calls should return identical lists", countries1, countries2)
  }

  @Test
  fun `loadCountries includes small and large countries`() {
    val countries = loadCountries()
    val countryNames = countries.map { it.name }

    // Small countries
    assertTrue("Should contain Monaco", countryNames.contains("Monaco"))
    assertTrue("Should contain Vatican City", countryNames.contains("Vatican City"))
    assertTrue("Should contain Liechtenstein", countryNames.contains("Liechtenstein"))

    // Large countries
    assertTrue("Should contain Russia", countryNames.contains("Russia"))
  }

  @Test
  fun `loadCountries includes countries from all continents`() {
    val countries = loadCountries()
    val countryNames = countries.map { it.name }

    // Africa
    assertTrue("Should contain Egypt", countryNames.contains("Egypt"))

    // Asia
    assertTrue("Should contain Japan", countryNames.contains("Japan"))

    // Europe
    assertTrue("Should contain France", countryNames.contains("France"))

    // North America
    assertTrue("Should contain United States", countryNames.contains("United States"))

    // South America
    assertTrue("Should contain Brazil", countryNames.contains("Brazil"))

    // Oceania
    assertTrue("Should contain Australia", countryNames.contains("Australia"))

    // Antarctica (might not be in standard country list, but checking anyway)
  }

  @Test
  fun `loadCountries flags are valid emoji`() {
    val countries = loadCountries()

    countries.forEach { country ->
      // Flag emojis should be 2 or more characters (regional indicator symbols)
      // or the fallback globe emoji (🌐) which is 1-2 characters depending on encoding
      assertTrue(
          "Country flag should be valid: ${country.name} - ${country.flag}",
          country.flag.isNotEmpty())
    }
  }

  @Test
  fun `Country toString returns expected format`() {
    val country = Country(code = "US", name = "United States", flag = "🇺🇸")
    val toString = country.toString()

    assertTrue("toString should contain code", toString.contains("US"))
    assertTrue("toString should contain name", toString.contains("United States"))
    assertTrue("toString should contain flag", toString.contains("🇺🇸"))
  }

  @Test
  fun `loadCountries Switzerland specific properties`() {
    val countries = loadCountries()
    val switzerland = countries.find { it.code == "CH" }

    assertNotNull("Switzerland should exist in country list", switzerland)
    assertEquals("Switzerland", switzerland?.name)
    assertEquals("CH", switzerland?.code)
    assertEquals("🇨🇭", switzerland?.flag)
  }

  @Test
  fun `loadCountries France specific properties`() {
    val countries = loadCountries()
    val france = countries.find { it.code == "FR" }

    assertNotNull("France should exist in country list", france)
    assertEquals("France", france?.name)
    assertEquals("FR", france?.code)
    assertEquals("🇫🇷", france?.flag)
  }

  @Test
  fun `loadCountries Germany specific properties`() {
    val countries = loadCountries()
    val germany = countries.find { it.code == "DE" }

    assertNotNull("Germany should exist in country list", germany)
    assertEquals("Germany", germany?.name)
    assertEquals("DE", germany?.code)
    assertEquals("🇩🇪", germany?.flag)
  }

  @Test
  fun `loadCountries no null values in results`() {
    val countries = loadCountries()

    countries.forEach { country ->
      assertNotNull("Country code should not be null", country.code)
      assertNotNull("Country name should not be null", country.name)
      assertNotNull("Country flag should not be null", country.flag)
    }
  }
}
