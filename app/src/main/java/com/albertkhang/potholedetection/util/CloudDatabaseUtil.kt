package com.albertkhang.potholedetection.util

import android.util.Log
import com.albertkhang.potholedetection.model.IPothole
import com.albertkhang.potholedetection.model.IUserPothole
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.database.ILocation
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

    fun read(
        username: String,
        onCompleteListener: OnCompleteListener<QuerySnapshot>
    ) {
        db.collection(COLLECTION_DATA)
            .whereEqualTo("username", username)
            .get()
            .addOnCompleteListener {
                onCompleteListener.onComplete(it)
            }
    }
}