package com.albertkhang.potholedetection.model.entry

/**
 * @Type Cloud Database Entry
 *
 * @Description data to upload to cloud firestore
 */
class CloudFirestoreEntry(
    val username: String,
    val roads: List<RoadEntry>
)