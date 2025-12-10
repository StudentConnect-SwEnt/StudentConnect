package com.github.se.studentconnect.model.organization

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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
        Organization(
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

  @Test
  fun getAllOrganizations_returns_list_of_organizations() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val querySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val doc1 = mockk<DocumentSnapshot>(relaxed = true)
    val doc2 = mockk<DocumentSnapshot>(relaxed = true)

    val org1Map =
        mapOf<String, Any?>(
            "id" to "org1",
            "name" to "Org1",
            "type" to "Company",
            "description" to "desc1",
            "logoUrl" to "url1",
            "location" to "loc1",
            "mainDomains" to listOf("Tech"),
            "ageRanges" to listOf("18-25"),
            "typicalEventSize" to "M",
            "roles" to emptyList<Map<String, Any>>(),
            "socialLinks" to emptyMap<String, String>(),
            "createdAt" to Timestamp.now(),
            "createdBy" to "creator1")

    val org2Map =
        mapOf<String, Any?>(
            "id" to "org2",
            "name" to "Org2",
            "type" to "NGO",
            "description" to "desc2",
            "logoUrl" to "url2",
            "location" to "loc2",
            "mainDomains" to listOf("Education"),
            "ageRanges" to listOf("26-35"),
            "typicalEventSize" to "S",
            "roles" to emptyList<Map<String, Any>>(),
            "socialLinks" to emptyMap<String, String>(),
            "createdAt" to Timestamp.now(),
            "createdBy" to "creator2")

    every { db.collection("organizations") } returns collection
    every { collection.get() } returns Tasks.forResult(querySnapshot)
    every { querySnapshot.documents } returns listOf(doc1, doc2)
    every { doc1.data } returns org1Map
    every { doc2.data } returns org2Map

    val repo = OrganizationRepositoryFirestore(db)
    val organizations = repo.getAllOrganizations()

    assertEquals(2, organizations.size)
    assertEquals("org1", organizations[0].id)
    assertEquals("Org1", organizations[0].name)
    assertEquals("org2", organizations[1].id)
    assertEquals("Org2", organizations[1].name)
  }

  @Test
  fun getAllOrganizations_returns_empty_list_when_no_organizations() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val querySnapshot = mockk<QuerySnapshot>(relaxed = true)

    every { db.collection("organizations") } returns collection
    every { collection.get() } returns Tasks.forResult(querySnapshot)
    every { querySnapshot.documents } returns emptyList()

    val repo = OrganizationRepositoryFirestore(db)
    val organizations = repo.getAllOrganizations()

    assertTrue(organizations.isEmpty())
  }

  @Test
  fun getAllOrganizations_skips_invalid_documents() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val querySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val validDoc = mockk<DocumentSnapshot>(relaxed = true)
    val invalidDoc = mockk<DocumentSnapshot>(relaxed = true)

    val validMap =
        mapOf<String, Any?>(
            "id" to "org1",
            "name" to "Org1",
            "type" to "Company",
            "description" to "desc",
            "logoUrl" to "url",
            "location" to "loc",
            "mainDomains" to listOf("Tech"),
            "ageRanges" to listOf("18-25"),
            "typicalEventSize" to "M",
            "roles" to emptyList<Map<String, Any>>(),
            "socialLinks" to emptyMap<String, String>(),
            "createdAt" to Timestamp.now(),
            "createdBy" to "creator")

    every { db.collection("organizations") } returns collection
    every { collection.get() } returns Tasks.forResult(querySnapshot)
    every { querySnapshot.documents } returns listOf(validDoc, invalidDoc)
    every { validDoc.data } returns validMap
    every { invalidDoc.data } returns emptyMap() // Invalid data

    val repo = OrganizationRepositoryFirestore(db)
    val organizations = repo.getAllOrganizations()

    // Should only return the valid organization, skipping the invalid one
    assertEquals(1, organizations.size)
    assertEquals("org1", organizations[0].id)
  }

  @Test
  fun sendMemberInvitation_callsSetOnDocument() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val docRef = mockk<DocumentReference>(relaxed = true)

    every { db.collection("organization_member_invitations") } returns collection
    every { collection.document("org-1_user-1") } returns docRef
    every { docRef.set(any()) } returns Tasks.forResult(null)

    val repo = OrganizationRepositoryFirestore(db)

    repo.sendMemberInvitation(
        organizationId = "org-1", userId = "user-1", role = "Member", invitedBy = "user-2")

    verify { docRef.set(any()) }
  }

  @Test
  fun acceptMemberInvitation_addsUserToMemberUidsAndDeletesInvitation() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val orgCollection = mockk<CollectionReference>(relaxed = true)
    val invCollection = mockk<CollectionReference>(relaxed = true)
    val orgDocRef = mockk<DocumentReference>(relaxed = true)
    val invDocRef = mockk<DocumentReference>(relaxed = true)
    val snapshot = mockk<DocumentSnapshot>(relaxed = true)

    val orgMap =
        mapOf<String, Any?>(
            "id" to "org-1",
            "name" to "Test Org",
            "type" to "Company",
            "createdBy" to "creator",
            "createdAt" to Timestamp.now(),
            "memberUids" to listOf<String>())

    every { db.collection("organizations") } returns orgCollection
    every { db.collection("organization_member_invitations") } returns invCollection
    every { orgCollection.document("org-1") } returns orgDocRef
    every { invCollection.document("org-1_user-1") } returns invDocRef
    every { orgDocRef.get() } returns Tasks.forResult(snapshot)
    every { snapshot.exists() } returns true
    every { snapshot.data } returns orgMap
    every { orgDocRef.update(any<String>(), any()) } returns Tasks.forResult(null)
    every { invDocRef.delete() } returns Tasks.forResult(null)

    val repo = OrganizationRepositoryFirestore(db)

    repo.acceptMemberInvitation(organizationId = "org-1", userId = "user-1")

    verify { orgDocRef.update("memberUids", listOf("user-1")) }
    verify { invDocRef.delete() }
  }

  @Test
  fun acceptMemberInvitation_doesNotAddDuplicateUser() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val orgCollection = mockk<CollectionReference>(relaxed = true)
    val invCollection = mockk<CollectionReference>(relaxed = true)
    val orgDocRef = mockk<DocumentReference>(relaxed = true)
    val invDocRef = mockk<DocumentReference>(relaxed = true)
    val snapshot = mockk<DocumentSnapshot>(relaxed = true)

    val orgMap =
        mapOf<String, Any?>(
            "id" to "org-1",
            "name" to "Test Org",
            "type" to "Company",
            "createdBy" to "creator",
            "createdAt" to Timestamp.now(),
            "memberUids" to listOf("user-1")) // User already exists

    every { db.collection("organizations") } returns orgCollection
    every { db.collection("organization_member_invitations") } returns invCollection
    every { orgCollection.document("org-1") } returns orgDocRef
    every { invCollection.document("org-1_user-1") } returns invDocRef
    every { orgDocRef.get() } returns Tasks.forResult(snapshot)
    every { snapshot.exists() } returns true
    every { snapshot.data } returns orgMap
    every { invDocRef.delete() } returns Tasks.forResult(null)

    val repo = OrganizationRepositoryFirestore(db)

    repo.acceptMemberInvitation(organizationId = "org-1", userId = "user-1")

    // Should not call update since user already exists
    verify(exactly = 0) { orgDocRef.update(any<String>(), any()) }
    verify { invDocRef.delete() }
  }

  @Test
  fun rejectMemberInvitation_deletesInvitation() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val docRef = mockk<DocumentReference>(relaxed = true)

    every { db.collection("organization_member_invitations") } returns collection
    every { collection.document("org-1_user-1") } returns docRef
    every { docRef.delete() } returns Tasks.forResult(null)

    val repo = OrganizationRepositoryFirestore(db)

    repo.rejectMemberInvitation(organizationId = "org-1", userId = "user-1")

    verify { docRef.delete() }
  }

  @Test
  fun getPendingInvitations_returnsListOfInvitations() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val query = mockk<com.google.firebase.firestore.Query>(relaxed = true)
    val querySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val doc = mockk<DocumentSnapshot>(relaxed = true)

    val invitationMap =
        mapOf(
            "organizationId" to "org-1",
            "userId" to "user-1",
            "role" to "Member",
            "invitedBy" to "user-2")

    every { db.collection("organization_member_invitations") } returns collection
    every { collection.whereEqualTo("organizationId", "org-1") } returns query
    every { query.get() } returns Tasks.forResult(querySnapshot)
    every { querySnapshot.documents } returns listOf(doc)
    every { doc.data } returns invitationMap

    val repo = OrganizationRepositoryFirestore(db)
    val invitations = repo.getPendingInvitations("org-1")

    assertEquals(1, invitations.size)
    assertEquals("org-1", invitations[0].organizationId)
    assertEquals("user-1", invitations[0].userId)
    assertEquals("Member", invitations[0].role)
  }

  @Test
  fun getUserPendingInvitations_returnsListOfInvitations() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val query = mockk<com.google.firebase.firestore.Query>(relaxed = true)
    val querySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val doc = mockk<DocumentSnapshot>(relaxed = true)

    val invitationMap =
        mapOf(
            "organizationId" to "org-1",
            "userId" to "user-1",
            "role" to "Admin",
            "invitedBy" to "user-2")

    every { db.collection("organization_member_invitations") } returns collection
    every { collection.whereEqualTo("userId", "user-1") } returns query
    every { query.get() } returns Tasks.forResult(querySnapshot)
    every { querySnapshot.documents } returns listOf(doc)
    every { doc.data } returns invitationMap

    val repo = OrganizationRepositoryFirestore(db)
    val invitations = repo.getUserPendingInvitations("user-1")

    assertEquals(1, invitations.size)
    assertEquals("org-1", invitations[0].organizationId)
    assertEquals("user-1", invitations[0].userId)
    assertEquals("Admin", invitations[0].role)
  }

  @Test
  fun addMemberToOrganization_addsUserToMemberUids() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val docRef = mockk<DocumentReference>(relaxed = true)
    val snapshot = mockk<DocumentSnapshot>(relaxed = true)

    val orgMap =
        mapOf<String, Any?>(
            "id" to "org-1",
            "name" to "Test Org",
            "type" to "Company",
            "createdBy" to "creator",
            "createdAt" to Timestamp.now(),
            "memberUids" to listOf<String>())

    every { db.collection("organizations") } returns collection
    every { collection.document("org-1") } returns docRef
    every { docRef.get() } returns Tasks.forResult(snapshot)
    every { snapshot.exists() } returns true
    every { snapshot.data } returns orgMap
    every { docRef.update(any<String>(), any()) } returns Tasks.forResult(null)

    val repo = OrganizationRepositoryFirestore(db)

    repo.addMemberToOrganization(organizationId = "org-1", userId = "user-1")

    verify { docRef.update("memberUids", listOf("user-1")) }
  }

  @Test
  fun addMemberToOrganization_doesNotAddDuplicateUser() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val docRef = mockk<DocumentReference>(relaxed = true)
    val snapshot = mockk<DocumentSnapshot>(relaxed = true)

    val orgMap =
        mapOf<String, Any?>(
            "id" to "org-1",
            "name" to "Test Org",
            "type" to "Company",
            "createdBy" to "creator",
            "createdAt" to Timestamp.now(),
            "memberUids" to listOf("user-1"))

    every { db.collection("organizations") } returns collection
    every { collection.document("org-1") } returns docRef
    every { docRef.get() } returns Tasks.forResult(snapshot)
    every { snapshot.exists() } returns true
    every { snapshot.data } returns orgMap

    val repo = OrganizationRepositoryFirestore(db)

    repo.addMemberToOrganization(organizationId = "org-1", userId = "user-1")

    verify(exactly = 0) { docRef.update(any<String>(), any()) }
  }

  @Test
  fun getPendingInvitations_returnsEmptyListOnException() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val query = mockk<com.google.firebase.firestore.Query>(relaxed = true)

    every { db.collection("organization_member_invitations") } returns collection
    every { collection.whereEqualTo("organizationId", "org-1") } returns query
    every { query.get() } returns Tasks.forException(Exception("Firestore error"))

    val repo = OrganizationRepositoryFirestore(db)
    val invitations = repo.getPendingInvitations("org-1")

    assertEquals(0, invitations.size)
  }

  @Test
  fun getUserPendingInvitations_returnsEmptyListOnException() = runBlocking {
    val db = mockk<FirebaseFirestore>(relaxed = true)
    val collection = mockk<CollectionReference>(relaxed = true)
    val query = mockk<com.google.firebase.firestore.Query>(relaxed = true)

    every { db.collection("organization_member_invitations") } returns collection
    every { collection.whereEqualTo("userId", "user-1") } returns query
    every { query.get() } returns Tasks.forException(Exception("Firestore error"))

    val repo = OrganizationRepositoryFirestore(db)
    val invitations = repo.getUserPendingInvitations("user-1")

    assertEquals(0, invitations.size)
  }
}
