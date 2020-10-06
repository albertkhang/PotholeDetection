package com.albertkhang.potholedetection.util

import com.albertkhang.potholedetection.model.IAGVector
import com.albertkhang.potholedetection.model.IDatabase
import com.albertkhang.potholedetection.model.IGps
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CloudDatabase {
    private val TAG = "CloudDatabase"
    val db: FirebaseFirestore = Firebase.firestore

    companion object {
        const val COLLECTION_AG_VECTOR = "ag-vector"
        const val COLLECTION_GPS = "gps"
    }

    fun write(data: IDatabase, onCompleteListener: OnCompleteListener<DocumentReference>) {
        when (data) {
            is IAGVector -> {
                db.collection(COLLECTION_AG_VECTOR)
                    .add(data)
                    .addOnCompleteListener {
                        onCompleteListener.onComplete(it)
                    }
            }

            is IGps -> {
                db.collection(COLLECTION_GPS)
                    .add(data)
                    .addOnCompleteListener {
                        onCompleteListener.onComplete(it)
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

            COLLECTION_GPS -> {
                db.collection(COLLECTION_GPS)
                    .get()
                    .addOnCompleteListener {
                        onCompleteListener.onComplete(it)
                    }
            }

            else -> return
        }
    }
}