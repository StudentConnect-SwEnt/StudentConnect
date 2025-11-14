package com.github.se.studentconnect.resources

import com.github.se.studentconnect.R
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Test implementation that reads actual string values from strings.xml. This keeps the tests
 * aligned with the production resources and avoids duplicating message literals in test code.
 */
class TestResourceProvider(stringsFile: File = File("app/src/main/res/values/strings.xml")) {
  private val resourceIdsByName: Map<String, Int> =
      R.string::class.java.fields.associate { field -> field.name to field.getInt(null) }

  private val stringMap: Map<Int, String> = loadStrings(stringsFile)

  fun getString(resId: Int): String {
    return stringMap[resId] ?: "Unknown string resource: $resId"
  }

  fun getString(resId: Int, vararg formatArgs: Any): String {
    val template = stringMap[resId] ?: "Unknown string resource: $resId"
    return try {
      String.format(template, *formatArgs)
    } catch (_: Exception) {
      template
    }
  }

  private fun loadStrings(file: File): Map<Int, String> {
    if (!file.exists()) {
      return emptyMap()
    }

    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val document = builder.parse(file)
    document.documentElement.normalize()

    val strings = mutableMapOf<Int, String>()
    val nodes = document.getElementsByTagName("string")

    for (index in 0 until nodes.length) {
      val node = nodes.item(index)
      if (node.nodeType != Node.ELEMENT_NODE) continue

      val element = node as Element
      val name = element.getAttribute("name")
      val value = element.textContent.trim()
      val resId = resourceIdsByName[name] ?: continue
      strings[resId] = value
    }
    return strings
  }
}
