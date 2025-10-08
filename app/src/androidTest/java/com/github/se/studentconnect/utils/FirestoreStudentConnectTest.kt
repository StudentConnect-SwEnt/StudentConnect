package com.github.se.studentconnect.utils

import android.util.Log
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryFirestore
import com.github.se.studentconnect.model.event.EventRepositoryFirestore.Companion.EVENTS_COLLECTION_PATH
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

open class FirestoreStudentConnectTest : StudentConnectTest() {

    suspend fun getEventsCount(): Int {
        val user = FirebaseEmulator.auth.currentUser ?: return 0
        return FirebaseEmulator.firestore
            .collection(EVENTS_COLLECTION_PATH)
            .whereEqualTo("ownerId", user.uid)
            .get()
            .await()
            .size()
    }

    private suspend fun clearTestCollection() {
        val user = FirebaseEmulator.auth.currentUser ?: return
        val todos =
            FirebaseEmulator.firestore
                .collection(EVENTS_COLLECTION_PATH)
                .whereEqualTo("ownerId", user.uid)
                .get()
                .await()

        val batch = FirebaseEmulator.firestore.batch()
        todos.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()

        val count = getEventsCount()
        assert(count == 0) {
            "Test collection is not empty after clearing, count: ${count}"
        }
    }

    override fun createInitializedRepository(): EventRepository {
        return EventRepositoryFirestore(db = FirebaseEmulator.firestore)
    }

    @Before
    override fun setUp() {
        super.setUp()
        runTest {
            val todosCount = getEventsCount()
            if (todosCount > 0) {
                Log.w(
                    "FirebaseEmulatedTest",
                    "Warning: Test collection is not empty at the beginning of the test, count: $todosCount",
                )
                clearTestCollection()
            }
        }
    }

    @After
    override fun tearDown() {
        runTest { clearTestCollection() }
        FirebaseEmulator.clearFirestoreEmulator()
        super.tearDown()
    }
}
