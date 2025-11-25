package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.organization.OrganizationModel
import com.github.se.studentconnect.model.organization.OrganizationType
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class OrganizationRepositoryFirestoreTest {

  @Test
  fun saveOrganization_calls_set_on_document() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val docRef = mockk<DocumentReference>(relaxed = true)

    every { db.collection("organizations") } returns collection
    every { collection.document("org1") } returns docRef
    every { docRef.set(any()) } returns Tasks.forResult(null)

    val repo = OrganizationRepositoryFirestore(db)

    val model =
        OrganizationModel(
            id = "org1", name = "Name", type = OrganizationType.Company, createdBy = "creator")

    repo.saveOrganization(model)

    verify { docRef.set(model.toMap()) }
  }

  @Test
  fun getOrganizationById_returns_model_when_document_exists() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val docRef = mockk<DocumentReference>(relaxed = true)
    val snapshot = mockk<DocumentSnapshot>(relaxed = true)

    val inputMap =
        mapOf<String, Any?>(
            "id" to "orgX",
            "name" to "OrgX",
            "type" to "NGO",
            "description" to "desc",
            "logoUrl" to "u",
            "location" to "here",
            "mainDomains" to listOf("A"),
            "ageRanges" to listOf("18-25"),
            "typicalEventSize" to "S",
            "roles" to listOf(mapOf("name" to "R", "description" to "D")),
            "socialLinks" to mapOf("website" to "w"),
            "createdAt" to Timestamp.now(),
            "createdBy" to "creator")

    every { db.collection("organizations") } returns collection
    every { collection.document("orgX") } returns docRef
    every { docRef.get() } returns Tasks.forResult(snapshot)
    every { snapshot.exists() } returns true
    every { snapshot.data } returns inputMap

    val repo = OrganizationRepositoryFirestore(db)
    val model = repo.getOrganizationById("orgX")

    assertNotNull(model)
    assertEquals("orgX", model?.id)
    assertEquals("OrgX", model?.name)
    assertEquals("creator", model?.createdBy)
    assertTrue(model?.roles?.isNotEmpty() == true)
  }

  @Test
  fun getOrganizationById_returns_null_when_missing() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val docRef = mockk<DocumentReference>(relaxed = true)
    val snapshot = mockk<DocumentSnapshot>(relaxed = true)

    every { db.collection("organizations") } returns collection
    every { collection.document("nope") } returns docRef
    every { docRef.get() } returns Tasks.forResult(snapshot)
    every { snapshot.exists() } returns false

    val repo = OrganizationRepositoryFirestore(db)
    val model = repo.getOrganizationById("nope")

    assertNull(model)
  }

  @Test
  fun getNewOrganizationId_returns_document_id() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val generatedDoc = mockk<DocumentReference>(relaxed = true)

    every { db.collection("organizations") } returns collection
    every { collection.document() } returns generatedDoc
    every { generatedDoc.id } returns "generated-42"

    val repo = OrganizationRepositoryFirestore(db)
    val id = repo.getNewOrganizationId()

    assertEquals("generated-42", id)
  }
}
