package com.albertkhang.potholedetection.util

import android.content.Context
import android.location.Location
import android.util.Log
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.cloud_database.IPothole
import com.albertkhang.potholedetection.model.entry.AccelerometerEntry
import com.albertkhang.potholedetection.model.entry.LocalEntry
import com.albertkhang.potholedetection.model.entry.LocationEntry
import com.albertkhang.potholedetection.model.local_database.IAGVector
import com.albertkhang.potholedetection.model.local_database.IDatabase
import com.albertkhang.potholedetection.model.local_database.ILocation
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import java.util.*

class FilterUtil {
    companion object {
        private const val TAG = "FilterUtil"
        private const val showLog = true

        private const val minSpeed = 1.38889 // m/s = 5 km/h
        private const val isDeleteCacheFile = false // default: true
        private const val isWriteFilteredCacheFile = false // default: falseee

        private val mCloudDatabaseUtil = CloudDatabaseUtil()

        /**
         * @unit meter
         */
        private fun distanceBetween(start: LatLng, end: LatLng): Float {
            val results = FloatArray(4)
            Location.distanceBetween(
                start.latitude,
                start.longitude,
                end.latitude,
                end.longitude,
                results
            )

            return results[0]
        }

        private fun roadsDetect(locationEntries: LinkedList<LocationEntry>): LinkedList<LinkedList<LocationEntry>> {
            val roads = LinkedList<LinkedList<LocationEntry>>()

            // TODO: set setting this
            val stopRecordingInterval = 1000 * 60 * 5 // 5 min
            val minDistanceBetween2Points = 30 // 30 meter

            if (locationEntries.size > 2) {
                roads.add(LinkedList<LocationEntry>())
                roads.last.add(locationEntries.first)

                var i = 1
                var timestampInterval: Long
                var distance: Float
                var start: LocationEntry
                var end: LocationEntry

                while (i < locationEntries.size) {
                    start = locationEntries[i - 1]
                    end = locationEntries[i]

                    timestampInterval = end.timestamp - start.timestamp
                    distance = distanceBetween(start.location, end.location)

                    if (timestampInterval >= stopRecordingInterval || distance > minDistanceBetween2Points) {
                        roads.add(LinkedList<LocationEntry>())
                    }

                    roads.last.add(locationEntries[i])

                    i++
                }
            }

            return roads
        }

        private fun read(context: Context, type: String): List<IDatabase> {
            when (type) {
                LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME -> {
                    return LocalDatabaseUtil.read(
                        context,
                        LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME,
                        LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME
                    )
                }

                LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME -> {
                    return LocalDatabaseUtil.read(
                        context,
                        LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME,
                        LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME
                    )
                }

                else -> return emptyList()
            }
        }

        private fun readLocationCache(context: Context): LinkedList<LinkedList<LocationEntry>> {
            val locationEntries = FileUtil.readLocationCache(context)
            return roadsDetect(locationEntries)
        }

        private fun readAccelerometerCache(context: Context): LinkedList<AccelerometerEntry> {
            return FileUtil.readAccelerometerCache(context)
        }

        private fun removeUnusedAccelerometerAtLast(
            roads: LinkedList<LinkedList<LocationEntry>>,
            accelerometerEntries: LinkedList<AccelerometerEntry>
        ) {
            val lastTimestamp = roads.last.last.timestamp
            while (accelerometerEntries.last.timestamp >= lastTimestamp) {
                accelerometerEntries.removeLast()
            }
        }

        fun run(context: Context) {
            Thread {
                // TODO: start new thread

                val roads = readLocationCache(context)
                Log.d(TAG, "roads size=${roads.size}")

                val accelerometerEntries = readAccelerometerCache(context)
                Log.d(TAG, "accelerometerEntries size=${accelerometerEntries.size}")

                if (roads.size == 0) {
                    if (isDeleteCacheFile) {
                        FileUtil.deleteAllCacheFile(context)
                    }
                    return@Thread
                }

                removeUnusedAccelerometerAtLast(roads, accelerometerEntries)

                val mixedEntries = LinkedList<LocalEntry>()
                roads.forEach {
                    Log.d(TAG, "index=${roads.indexOf(it)}, size=${it.size}")
                    mixedEntries.clear()

                    mixedEntries.add(it.first)
                }

//                var isCacheEmpty = false
//
//                // read IAGVector data from cache file
//                val ag = read(
//                    context,
//                    LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME
//                ) as List<IAGVector>
//                if (ag.isEmpty()) {
//                    isCacheEmpty = true
//                }
//
//                // read ILocation data from cache file
//                val l =
//                    read(context, LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME) as List<ILocation>
//
//                if (l.isEmpty()) {
//                    isCacheEmpty = true
//                }
//
//                if (isCacheEmpty) {
//                    if (showLog)
//                        Log.d(TAG, "Cache file is empty, return thread.")
//
//                    return@Thread
//                } else {
//                    if (showLog)
//                        Log.d(TAG, "Read cache file.")
//                }
//
//                // remove AGVector Redundant
//                // ================================================================
//                // firstLocationTimestamp < list IAGVector < lastLocationTimestamp
//                // ================================================================
//
//                val location = LinkedList<ILocation>()
//                location.addAll(l)
//
//                val agVector = LinkedList<IAGVector>()
//                agVector.addAll(ag)
//
//                removeAGVectorRedundant(
//                    location.first.timestamps,
//                    location.last.timestamps,
//                    agVector
//                )
//
//                if (showLog)
//                    Log.d(TAG, "remove AG Vector Redundant")
//
//                // now | firstLocationTimestamp < list IAGVector < lastLocationTimestamp |
//
//                val mixed = getMixedData(agVector, location)
//
//                val data = filter(mixed)
//
//                if (showLog)
//                    Log.d(TAG, "filtered")
//
//                if (data.isNotEmpty()) {
//                    if (NetworkUtil.isNetworkAvailable(context)) {
//                        Log.d(TAG, "Network available. Preparing to upload.")
//
//                        // convert data to json before upload
//                        val userPothole = IUserPothole("albertkhang", Gson().toJson(data.toArray()))
//
//                        // Upload data to firebase
//                        mCloudDatabaseUtil.write(
//                            userPothole
//                        ) {
//                            if (showLog) {
//                                Log.d(TAG, "Uploaded ${it.result}")
//                            }
//                        }
//
//                        // Delete cache files after upload
//                        if (showLog)
//                            if (isDeleteCacheFile) {
//                                if (LocalDatabaseUtil.deleteAllCacheFile(context)) {
//                                    Log.d(TAG, "Deleted cache files success.")
//                                } else {
//                                    Log.d(TAG, "Deleted cache files error.")
//                                }
//                            }
//
//                        // be used for test
//                        if (isWriteFilteredCacheFile) {
//                            writeCacheFile(context, data)
//                        }
//                    } else {
//                        Log.d(TAG, "Network unavailable. Write to filter cache file.")
//
//                        // TODO: lưu dữ liệu xuống nếu không có mạng, khi có mạng thì upload sau
//                        // TODO: xử lý netowrk status listener bên DetectingNotification
//                        writeCacheFile(context, data)
//                    }
//                } else {
//                    if (showLog)
//                        Log.d(TAG, "Data is empty. Not upload!")
//                }
//
//                data.clear()
//                mixed.clear()
//                location.clear()
//                agVector.clear()
//
//                Log.d(TAG, "thread=${Thread.currentThread()} done.")
            }.start()
        }

        /**
         * Be used for test
         */
        private fun writeCacheFile(context: Context, data: LinkedList<IPothole>) {
            if (data.isNotEmpty()) {
                if (LocalDatabaseUtil.writeFilteredList(context, data.toList())) {
                    if (showLog)
                        Log.d(TAG, "write filter done")
                } else {
                    if (showLog)
                        Log.d(TAG, "write filter error")
                }
            }
        }

        private fun filter(mixed: LinkedList<IDatabase>): LinkedList<IPothole> {
            var start: ILocation = mixed.first as ILocation
            mixed.removeFirst()

            val tempIRI = LinkedList<Float>()
            val tempUpload = LinkedList<IPothole>()

            while (mixed.isNotEmpty()) {
                if (mixed.first is IAGVector) {
                    val data = mixed.first as IAGVector
                    val iri = IVector3D(data.ax, data.ay, data.ax).iri(
                        IVector3D(data.gx, data.gy, data.gz)
                    )

                    if (iri < 2) {
                        tempIRI.add(iri)
                    }

                    mixed.removeFirst()
                }

                if (mixed.first is ILocation) {
                    val end = mixed.first as ILocation

                    val iri = getAverageIRI(tempIRI)
                    val averageSpeed = getAverageSpeed(start, end, tempIRI)
                    tempIRI.clear()

                    // add vào tempUpload
                    if (averageSpeed >= minSpeed) {
                        tempUpload.add(
                            IPothole(
                                start.latLng,
                                end.latLng,
                                iri,
                                averageSpeed
                            )
                        )
                    }

                    start = end
                    mixed.removeFirst()
                }
            }

            return tempUpload
        }

        private fun getAverageSpeed(
            start: ILocation,
            end: ILocation,
            tempIRI: LinkedList<Float>
        ): Float {
            return if (tempIRI.isNotEmpty()) {
                ((start.speed + end.speed) / 2)

            } else {
                0f
            }
        }

        private fun getAverageIRI(tempIRI: LinkedList<Float>): Float {
            var sum = 0f
            tempIRI.forEach {
                sum += it
            }

            return sum / tempIRI.size
        }

        private fun getMixedData(
            agVector: LinkedList<IAGVector>,
            location: LinkedList<ILocation>
        ): LinkedList<IDatabase> {
            val mixed = LinkedList<IDatabase>()

            // add smaller timestamp to mixed to focus timeline
            while (agVector.isNotEmpty()) {
                if (location.first.timestamps < agVector.first.timestamps) {
                    mixed.add(location.first)
                    location.removeFirst()
                } else {
                    mixed.add(agVector.first)
                    agVector.removeFirst()
                }
            }

            mixed.addAll(location)
            agVector.clear()
            location.clear()

            return mixed
        }

        private fun removeAGVectorRedundant(
            startTimestamps: Long,
            endTimestamps: Long,
            agVector: LinkedList<IAGVector>
        ) {
            while (agVector.first.timestamps < startTimestamps) {
                agVector.removeFirst()
            }

            while (agVector.last.timestamps > endTimestamps) {
                agVector.removeLast()
            }
        }
    }
}