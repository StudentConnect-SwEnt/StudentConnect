package com.github.se.studentconnect.ui.camera

import com.github.se.studentconnect.ui.screen.camera.CameraMode
import org.junit.Assert.assertEquals
import org.junit.Test

class CameraModeTest {

  @Test
  fun cameraMode_hasCorrectValues() {
    assertEquals(2, CameraMode.entries.size)
    assertEquals(CameraMode.STORY, CameraMode.entries[0])
    assertEquals(CameraMode.QR_SCAN, CameraMode.entries[1])
  }

  @Test
  fun cameraMode_hasCorrectOrdinals() {
    assertEquals(0, CameraMode.STORY.ordinal)
    assertEquals(1, CameraMode.QR_SCAN.ordinal)
  }

  @Test
  fun cameraMode_hasCorrectNames() {
    assertEquals("STORY", CameraMode.STORY.name)
    assertEquals("QR_SCAN", CameraMode.QR_SCAN.name)
  }

  @Test
  fun cameraMode_valueOf_returnsCorrectValues() {
    assertEquals(CameraMode.STORY, CameraMode.valueOf("STORY"))
    assertEquals(CameraMode.QR_SCAN, CameraMode.valueOf("QR_SCAN"))
  }

  @Test
  fun cameraMode_entriesIndexAccess_worksCorrectly() {
    val entries = CameraMode.entries
    assertEquals(CameraMode.STORY, entries[0])
    assertEquals(CameraMode.QR_SCAN, entries[1])
  }

  @Test
  fun cameraMode_ordinalCanBeUsedAsIndex() {
    val entries = CameraMode.entries
    assertEquals(CameraMode.STORY, entries[CameraMode.STORY.ordinal])
    assertEquals(CameraMode.QR_SCAN, entries[CameraMode.QR_SCAN.ordinal])
  }
}
