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
import com.albertkhang.potholedetection.model.response.SnapToRoadsResponse
import com.albertkhang.potholedetection.service.SnapToRoadsService
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

            return roads
        }

        private fun distanceFilter(roads: LinkedList<LinkedList<LocalEntry>>) {
            // TODO: set this to setting
            val maxDistance = 13 // meter

            val removeEntries = LinkedList<LinkedList<LocalEntry>>()

            var start: LocationEntry
            var end: LocationEntry

            roads.forEach {
                start = it.first as LocationEntry
                end = it.last as LocationEntry

                if (distanceBetween(start.location, end.location) > maxDistance) {
                    removeEntries.add(it)
                }
            }
            Log.d(TAG, "distanceFilter remove ${removeEntries.size} entry.")

            removeEntries.forEach {
                roads.remove(it)
            }
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

                distanceFilter(roads)
                Log.d(TAG, "roads size=${roads.size}")

                firstIRIFilter(roads)
                Log.d(TAG, "roads size=${roads.size}")

                if (NetworkUtil.isNetworkAvailable(context)) {
                    // Have Connection

//                    val points = getPoints(roads)
//                    Log.d(TAG, "points size=${points.size}")

                    // TODO: xử lý lấy snap to roads bất đồng bộ
                } else {
                    // No Connection

                    if (isWriteFilteredCacheFile) {
                        FileUtil.writeFilteredCache(context, roads)
                    }

                    if (isDeleteCacheFile) {
                        FileUtil.deleteAllCacheFile(context)
                    }
                }
            }.start()
        }

        private fun firstIRIFilter(roads: LinkedList<LinkedList<LocalEntry>>) {
            // TODO: set this to settings
            val minFirstIRIFilter = 0.2 * 0.8

            val iris = LinkedList<Float>()
            val removeEntries = LinkedList<LinkedList<LocalEntry>>()

            roads.forEach {
                it.forEach {
                    if (it is AccelerometerEntry) {
                        iris += it.iri
                    }
                }

                if (getAverageIRI(iris) < minFirstIRIFilter) {
                    removeEntries.add(it)
                }

                iris.clear()
            }
            Log.d(TAG, "firstIRIFilter remove ${removeEntries.size} entry.")
            Log.d(
                TAG,
                "firstIRIFilter count=${removeEntries.size}, " +
                        "size=${roads.size}, " +
                        "remain=${roads.size - removeEntries.size}, " +
                        "percentRemoved=${removeEntries.size * 100f / roads.size}%"
            )

            removeEntries.forEach {
                roads.remove(it)
            }
        }

        private fun getPoints(roads: LinkedList<LinkedList<LocalEntry>>): LinkedList<LatLng> {
            val points = LinkedList<LatLng>()

            roads.forEach {
                val locationEntryFirst = it.first as LocationEntry
                val locationEntryLast = it.last as LocationEntry

                points.add(locationEntryFirst.location)
                points.add(locationEntryLast.location)
            }

            return points
        }

        /**
         * @maxPoints 100
         */
        fun getOnSnapToRoads(points: LinkedList<LatLng>, callback: Callback<SnapToRoadsResponse>) {
            var s = ""
            val size = points.size
            for (i in points.indices) {
                s += "${points[i].latitude},${points[i].longitude}"

                if (i != size - 1) {
                    s += "|"
                }
            }

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(SnapToRoadsService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(SnapToRoadsService::class.java)

            service.get(s).enqueue(object : Callback<SnapToRoadsResponse> {
                override fun onResponse(
                    call: Call<SnapToRoadsResponse>,
                    response: Response<SnapToRoadsResponse>
                ) {
                    callback.onResponse(call, response)
                }

                override fun onFailure(call: Call<SnapToRoadsResponse>, throwable: Throwable) {
                    callback.onFailure(call, throwable)
                }

            })
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