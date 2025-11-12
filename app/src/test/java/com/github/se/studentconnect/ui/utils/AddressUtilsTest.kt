package com.github.se.studentconnect.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class AddressUtilsTest {

  @Test
  fun formatShortAddress_withNull_returnsLocationNotSpecified() {
    assertEquals("Location not specified", formatShortAddress(null))
  }

  @Test
  fun formatShortAddress_withEmptyParts_handlesEmptyString() {
    val result = formatShortAddress("")
    assertEquals("", result)
  }

  @Test
  fun formatShortAddress_withOnlyCommas_handlesEmptyParts() {
    val result = formatShortAddress(",,,")
    assertEquals(",,,", result)
  }

  @Test
  fun formatShortAddress_withSinglePart_usesFallback() {
    val result = formatShortAddress("OnlyOnePart")
    assertEquals("OnlyOnePart", result)
  }

  @Test
  fun formatShortAddress_withLongAddress_truncates() {
    val longAddress = "A".repeat(60)
    val result = formatShortAddress(longAddress)
    assertEquals(50, result.length)
    assert(result.endsWith("..."))
  }

  @Test
  fun formatShortAddress_withThreePartsLongerThan50_truncates() {
    val result =
        formatShortAddress("Very Long Road Name, Very Long Neighbourhood, Very Long City Name")
    assertEquals(50, result.length)
    assert(result.endsWith("..."))
  }

  @Test
  fun formatShortAddress_withNominatimFormat_returnsFirstThreeParts() {
    val result =
        formatShortAddress(
            "Rue de la Gare, Quartier du Centre, Lausanne, District, Vaud, 1000, Switzerland")
    assertEquals("Rue de la Gare, Quartier du Centre, Lausanne", result)
  }
}
