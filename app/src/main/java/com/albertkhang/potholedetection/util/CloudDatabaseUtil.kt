package com.albertkhang.potholedetection.util

import com.albertkhang.potholedetection.model.cloud_database.IUserPothole
import com.albertkhang.potholedetection.model.entry.CloudFirestoreEntry
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

class CloudDatabaseUtil {
    private val TAG = "CloudDatabase"

    companion object {
        const val COLLECTION_DATA = "data"
        const val COLLECTION_GEOPOINTS = "geopoints"

        private val db: FirebaseFirestore = Firebase.firestore
        private val settings = firestoreSettings {
            cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
            isPersistenceEnabled = true
        }
    }

    init {
        db.firestoreSettings = settings
    }

    fun write(data: IUserPothole, onCompleteListener: OnCompleteListener<DocumentReference>) {
        db.collection(COLLECTION_DATA)
            .add(data)
            .addOnCompleteListener {
                onCompleteListener.onComplete(it)
            }
    }

    fun write(
        cloudFirestoreEntry: CloudFirestoreEntry,
        onCompleteListener: OnCompleteListener<DocumentReference>
    ) {
        db.collection(COLLECTION_DATA)
            .add(cloudFirestoreEntry)
            .addOnCompleteListener {
                onCompleteListener.onComplete(it)
            }
    }

    fun read(
        username: String,
        onCompleteListener: OnCompleteListener<QuerySnapshot>
    ) {
        db.collection(COLLECTION_GEOPOINTS)
            .whereArrayContains("usernames", username)
            .get()
            .addOnCompleteListener {
                onCompleteListener.onComplete(it)
            }
    }

    fun readAll(
        onCompleteListener: OnCompleteListener<QuerySnapshot>
    ) {
        db.collection(COLLECTION_GEOPOINTS)
            .get()
            .addOnCompleteListener {
                onCompleteListener.onComplete(it)
            }
    }
}