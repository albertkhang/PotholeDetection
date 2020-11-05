package com.albertkhang.potholedetection.util

import android.content.Context
import android.location.Location
import android.util.Log
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.cloud_database.IPothole
import com.albertkhang.potholedetection.model.entry.AccelerometerEntry
import com.albertkhang.potholedetection.model.entry.LocalEntry
import com.albertkhang.potholedetection.model.entry.LocationEntry
import com.albertkhang.potholedetection.model.entry.RoadEntry
import com.albertkhang.potholedetection.model.local_database.IAGVector
import com.albertkhang.potholedetection.model.local_database.IDatabase
import com.albertkhang.potholedetection.model.local_database.ILocation
import com.google.android.gms.maps.model.LatLng
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

        private fun roadsDetect(mixedEntries: LinkedList<LocalEntry>): LinkedList<LinkedList<LocalEntry>> {
            val roads = LinkedList<LinkedList<LocalEntry>>()

            var start: LocationEntry? = null
            var end: LocationEntry?
            val accelerometerEntries = LinkedList<AccelerometerEntry>()

            mixedEntries.forEach {
                if (it is LocationEntry) {
                    if (start == null) {
                        start = it
                    } else {
                        end = it

                        if (accelerometerEntries.isNotEmpty()) {
                            roads.add(LinkedList<LocalEntry>())

                            roads.last.add(start!!)
                            roads.last.addAll(accelerometerEntries)
                            roads.last.add(end!!)
                        }

                        start = it
                        end = null
                        accelerometerEntries.clear()
                    }
                } else if (it is AccelerometerEntry) {
                    accelerometerEntries.add(it)
                }
            }

//            mixedEntries.forEach {
//                if (it is LocationEntry) {
//                    if (start == null) {
//                        start = it
//                    } else {
//                        end = it
//
//                        if (sumIRI != 0f) {
//                            val iri = sumIRI / sizeIRI
//
//                            if (iri > averageIRI) {
//                                roads.add(RoadEntry(start!!.location, end!!.location, "", iri))
//                            }
//
//                            sumIRI = 0f
//                            sizeIRI = 0
//                        }
//
//                        start = it
//                        end = null
//                    }
//                } else if (it is AccelerometerEntry) {
//                    sumIRI += it.iri
//                    sizeIRI++
//                }
//            }

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

        private fun readLocationCache(context: Context): LinkedList<LocationEntry> {
            return FileUtil.readLocationCache(context)
        }

        private fun readAccelerometerCache(context: Context): LinkedList<AccelerometerEntry> {
            return FileUtil.readAccelerometerCache(context)
        }

        private fun removeUnusedAccelerometerAtLast(
            locationEntries: LinkedList<LocationEntry>,
            accelerometerEntries: LinkedList<AccelerometerEntry>
        ) {
            val lastTimestamp = locationEntries.last.timestamp
            while (accelerometerEntries.last.timestamp >= lastTimestamp) {
                accelerometerEntries.removeLast()
            }
        }

        fun run(context: Context) {
            Thread {
                Log.d(TAG, "run new thread=${Thread.currentThread()}")
                // TODO: start new thread

                val locationEntries = readLocationCache(context)
                Log.d(TAG, "locationEntries size=${locationEntries.size}")

                if (locationEntries.size == 0) {
                    if (isDeleteCacheFile) {
                        FileUtil.deleteAllCacheFile(context)
                    }
                    return@Thread
                }

                val accelerometerEntries = readAccelerometerCache(context)
                removeUnusedAccelerometerAtLast(locationEntries, accelerometerEntries)
                Log.d(TAG, "accelerometerEntries size=${accelerometerEntries.size}")

                val mixedEntries = mixEntries(locationEntries, accelerometerEntries)
                Log.d(TAG, "mixedEntries size=${mixedEntries.size}")

                val roads = roadsDetect(mixedEntries)
                Log.d(TAG, "roads size=${roads.size}")

                val localEntries = roads.first
                localEntries.forEach {
                    Log.d(TAG, "$it")
                }
                
            }.start()
        }

        private fun mixEntries(
            locationEntries: LinkedList<LocationEntry>,
            accelerometerEntries: LinkedList<AccelerometerEntry>
        ): LinkedList<LocalEntry> {
            val mixedEntries = LinkedList<LocalEntry>()

            var j = 0
            val accelerometerSize = accelerometerEntries.size

            var i = 0
            val locationSize = locationEntries.size

            while (i < locationSize) {
                if (j == accelerometerSize) {
                    break
                }

                if (locationEntries[i].timestamp < accelerometerEntries[j].timestamp) {
                    mixedEntries.add(locationEntries[i])
                    i++
                } else {
                    mixedEntries.add(accelerometerEntries[j])
                    j++
                }
            }

            if (mixedEntries.last is AccelerometerEntry) {
                mixedEntries.add(locationEntries[i])
            }

            return mixedEntries
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