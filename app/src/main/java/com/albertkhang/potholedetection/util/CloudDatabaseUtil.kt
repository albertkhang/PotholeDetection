package com.albertkhang.potholedetection.util

import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.database.ILocation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CloudDatabaseUtil {
    private val TAG = "CloudDatabase"
    val db: FirebaseFirestore = Firebase.firestore

    companion object {
        const val COLLECTION_AG_VECTOR = "ag-vector"
        const val COLLECTION_LOCATION = "location"
    }

    fun write(data: IDatabase, onCompleteListener: OnCompleteListener<DocumentReference>?) {
        when (data) {
            is IAGVector -> {
                db.collection(COLLECTION_AG_VECTOR)
                    .add(data)
                    .addOnCompleteListener {
                        onCompleteListener?.onComplete(it)
                    }
            }

            is ILocation -> {
                db.collection(COLLECTION_LOCATION)
                    .add(data)
                    .addOnCompleteListener {
                        onCompleteListener?.onComplete(it)
                    }
            }

            else -> return
        }
    }

    fun readAll(collectionName: String, onCompleteListener: OnCompleteListener<QuerySnapshot>) {
        when (collectionName) {
            COLLECTION_AG_VECTOR -> {
                db.collection(COLLECTION_AG_VECTOR)
                    .get()
                    .addOnCompleteListener {
                        onCompleteListener.onComplete(it)
                    }
            }

            COLLECTION_LOCATION -> {
                db.collection(COLLECTION_LOCATION)
                    .get()
                    .addOnCompleteListener {
                        onCompleteListener.onComplete(it)
                    }
            }

            else -> return
        }
    }
}