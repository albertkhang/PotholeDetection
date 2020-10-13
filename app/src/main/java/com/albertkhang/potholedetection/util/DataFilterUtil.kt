package com.albertkhang.potholedetection.util

import android.content.Context
import android.util.Log
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.IPothole
import com.albertkhang.potholedetection.model.IUserPothole
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.database.ILocation
import com.google.gson.Gson
import java.util.*

class DataFilterUtil {
    companion object {
        private const val TAG = "DataFilterUtil"
        private const val showLog = true

        private const val minSpeed = 1.38889 // m/s = 5 km/h
        private const val isDeleteCacheFile = true // default: true
        private const val isWriteFilteredCacheFile = false // default: false

        private val mCloudDatabaseUtil = CloudDatabaseUtil()

        private fun read(context: Context, type: String): List<IDatabase> {
            when (type) {
                LocalDatabaseUtil.CACHE_AG_FILE_NAME -> {
                    return LocalDatabaseUtil.read(
                        context,
                        LocalDatabaseUtil.CACHE_AG_FILE_NAME, LocalDatabaseUtil.CACHE_AG_FILE_NAME
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

        // Filter level 1
        fun run(context: Context) {
            Thread {
                if (showLog)
                    Log.d(
                        TAG,
                        "start filter minute=${
                            Calendar.getInstance().get(Calendar.MINUTE)
                        }, second=${Calendar.getInstance().get(Calendar.SECOND)}"
                    )

                var isCacheEmpty = false

                // read IAGVector data from cache file
                val ag = read(context, LocalDatabaseUtil.CACHE_AG_FILE_NAME) as List<IAGVector>
                if (ag.isEmpty()) {
                    isCacheEmpty = true
                }

                // read ILocation data from cache file
                val l =
                    read(context, LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME) as List<ILocation>

                if (l.isEmpty()) {
                    isCacheEmpty = true
                }

                if (isCacheEmpty) {
                    if (showLog)
                        Log.d(TAG, "Cache file is empty, return thread.")

                    return@Thread
                } else {
                    if (showLog)
                        Log.d(TAG, "Read cache file.")
                }

                // remove AGVector Redundant
                // ================================================================
                // firstLocationTimestamp < list IAGVector < lastLocationTimestamp
                // ================================================================

                val location = LinkedList<ILocation>()
                location.addAll(l)

                val agVector = LinkedList<IAGVector>()
                agVector.addAll(ag)

                removeAGVectorRedundant(
                    location.first.timestamps,
                    location.last.timestamps,
                    agVector
                )

                if (showLog)
                    Log.d(TAG, "remove AG Vector Redundant")

                // now | firstLocationTimestamp < list IAGVector < lastLocationTimestamp |

                val mixed = getMixedData(agVector, location)

                val data = filter(mixed)

                if (showLog)
                    Log.d(TAG, "filtered")

                if (data.isNotEmpty()) {
                    if (NetworkUtil.isNetworkAvailable(context)) {
                        Log.d(TAG, "Network available. Preparing to upload.")

                        // convert data to json before upload
                        val userPothole = IUserPothole("albertkhang", Gson().toJson(data.toArray()))

                        // Upload data to firebase
                        mCloudDatabaseUtil.write(
                            userPothole
                        ) {
                            if (showLog) {
                                Log.d(TAG, "Uploaded ${it.result}")
                            }
                        }

                        // Delete cache files after upload
                        if (showLog)
                            if (isDeleteCacheFile) {
                                if (LocalDatabaseUtil.deleteAllCacheFile(context)) {
                                    Log.d(TAG, "Deleted cache files success.")
                                } else {
                                    Log.d(TAG, "Deleted cache files error.")
                                }
                            }

                        // be used for test
                        if (isWriteFilteredCacheFile) {
                            writeCacheFile(context, data)
                        }
                    } else {
                        Log.d(TAG, "Network unavailable. Write to filter cache file.")

                        // TODO: lưu dữ liệu xuống nếu không có mạng, khi có mạng thì upload sau
                        // TODO: xử lý netowrk status listener bên DetectingNotification
                        writeCacheFile(context, data)
                    }
                } else {
                    if (showLog)
                        Log.d(TAG, "Data is empty. Not upload!")
                }

                data.clear()
                mixed.clear()
                location.clear()
                agVector.clear()

                Log.d(TAG, "thread=${Thread.currentThread()} done.")
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