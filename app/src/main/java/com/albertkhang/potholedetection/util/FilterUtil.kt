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
import com.albertkhang.potholedetection.model.response.SnapToRoadsResponse
import com.albertkhang.potholedetection.service.SnapToRoadsService
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap

class FilterUtil {
    companion object {
        private const val TAG = "FilterUtil"
        private const val showLog = true

        private const val minSpeed = 1.38889 // m/s = 5 km/h
        private const val isDeleteCacheFile = false // default: true

        private val mCloudDatabaseUtil = CloudDatabaseUtil()

        // TODO: set this to setting
        private const val maxLatLngPoint = 50

        private var mRetrofit: Retrofit? = null
        private var mSnapToRoadsService: SnapToRoadsService? = null

        private var mSnapToRoadsCallback: SnapToRoadsCallback? = null

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
                Log.d(TAG, "roads ${roads[0]}")

                if (NetworkUtil.isNetworkAvailable(context)) {
                    // Have Connection

                    val points = getPoints(roads)
                    Log.d(TAG, "points size=${points.size}")
                    for (i in points.indices) {
                        Log.d(TAG, "[$i] size=${points[i].size}")
                    }

                    snapToRoads(context, points, object : OnSnapToRoadsFinish {
                        override fun onSnapToRoadsFinish(
                            isSuccess: Boolean,
                            snappedPoints: LinkedList<LinkedList<SnapToRoadsResponse.SnappedPointResponse>>
                        ) {
                            if (isSuccess) {
                                Log.d(TAG, "snapToRoads success size=${snappedPoints.size}")

                                val roadsEntries = getRoadEntries(roads, points, snappedPoints)
                                Log.d(TAG, "roadsEntries size=${roadsEntries.size}")
                            } else {
                                Log.d(TAG, "snapToRoads failure")
                            }
                        }
                    })
                } else {
                    // No Connection

                    FileUtil.writeFilteredCache(context, roads)

                    if (isDeleteCacheFile) {
                        FileUtil.deleteAllCacheFile(context)
                    }
                }
            }.start()
        }

        private fun getRoadEntries(
            roads: LinkedList<LinkedList<LocalEntry>>,
            points: LinkedList<LinkedList<LatLng>>,
            snappedPoints: LinkedList<LinkedList<SnapToRoadsResponse.SnappedPointResponse>>
        ): LinkedList<LinkedList<RoadEntry>> {
            val newPoints = fillMissedPoints(points, snappedPoints)

            val roadsEntries = LinkedList<LinkedList<RoadEntry>>()
            for (i in roads.indices) {
                if (newPoints[i].size == 2) {
                    roadsEntries.add(LinkedList<RoadEntry>())

                    val start = newPoints[i].first.location
                    val end = newPoints[i].last.location
                    val placeID = newPoints[i].first.placeId
                    val iris = LinkedList<Float>()
                    roads[i].forEach {
                        if (it is AccelerometerEntry) {
                            iris.add(it.iri)
                        }
                    }

                    val iri = getAverageIRI(iris)

                    roadsEntries.last.add(RoadEntry(start, end, placeID, iri))
                }
            }

            return roadsEntries
        }

        private fun fillMissedPoints(
            points: LinkedList<LinkedList<LatLng>>,
            snappedPoints: LinkedList<LinkedList<SnapToRoadsResponse.SnappedPointResponse>>
        ): LinkedList<LinkedList<SnapToRoadsResponse.SnappedPointResponse>> {
            val newPoints = LinkedList<LinkedList<SnapToRoadsResponse.SnappedPointResponse>>()

            val hashMap = HashMap<Int, LinkedList<SnapToRoadsResponse.SnappedPointResponse>>()

            var key: Int
            snappedPoints.forEach { list ->
                key = -1

                list.forEach {
                    if (it.originalIndex != -1) {
                        key = it.originalIndex
                        hashMap[key] = LinkedList<SnapToRoadsResponse.SnappedPointResponse>()
                    }

                    hashMap[key]!!.add(it)
                }
                Log.d(TAG, "[${snappedPoints.indexOf(list)}] snappedPoints size=${list.size}")

                for (i in points[snappedPoints.indexOf(list)].indices) {
                    if (hashMap[i] == null) {
                        hashMap[i] = LinkedList<SnapToRoadsResponse.SnappedPointResponse>()
                    }
                }

                for (i in points[snappedPoints.indexOf(list)].indices step 2) {
                    newPoints.add(LinkedList<SnapToRoadsResponse.SnappedPointResponse>())
                    when {
                        hashMap[i].isNullOrEmpty() -> {
                            newPoints.last.add(SnapToRoadsResponse.SnappedPointResponse())
                        }
                        hashMap[i + 1].isNullOrEmpty() -> {
                            newPoints.last.add(SnapToRoadsResponse.SnappedPointResponse())
                        }
                        else -> {
                            newPoints.last.addAll(hashMap[i]!!)
                            newPoints.last.add(hashMap[i + 1]!!.first)
                        }
                    }
                }
            }

            hashMap.clear()

            return newPoints
        }

        private fun snapToRoads(
            context: Context,
            points: LinkedList<LinkedList<LatLng>>,
            callback: OnSnapToRoadsFinish
        ) {
            val snappedPoints: Array<LinkedList<SnapToRoadsResponse.SnappedPointResponse>?> =
                arrayOfNulls(points.size)

            var isSuccess = true

            val requestAmount = points.size
            var responseAmount = 0

            var failureCount = 0

            if (mSnapToRoadsCallback == null) {
                mSnapToRoadsCallback = object : SnapToRoadsCallback {
                    override fun onResponse(id: Int, response: Response<SnapToRoadsResponse>) {
                        //                        Log.d(
//                            TAG,
//                            "onResponse received: ${responseAmount + 1}/${requestAmount}"
//                        )

                        if (response.code() == 200) {
                            if (response.body() != null) {
                                val snappedResponse =
                                    response.body()!!.snappedPoints

                                val linkedListPoints =
                                    LinkedList<SnapToRoadsResponse.SnappedPointResponse>()

                                snappedResponse.forEach {
                                    linkedListPoints.add(it)
                                }

                                snappedPoints[id] = linkedListPoints

                                Log.d(
                                    TAG,
                                    "[$id] size=${snappedResponse.size}, received: ${responseAmount + 1}/${requestAmount}"
                                )
                            } else {
                                Log.d(TAG, "[$id] body == null")
                            }
                        } else {
                            Log.d(TAG, "[$id] code=${response.code()}")
                            isSuccess = false
                        }

                        responseAmount++
                        if (requestAmount == responseAmount) {
                            val data =
                                LinkedList<LinkedList<SnapToRoadsResponse.SnappedPointResponse>>()

                            snappedPoints.forEach {
                                data.add(it!!)
                            }

                            callback.onSnapToRoadsFinish(isSuccess, data)
                        }
                    }

                    override fun onFailure(id: Int, throwable: Throwable) {
                        failureCount++

                        Log.d(
                            TAG,
                            "[$id] onFailure received: ${responseAmount}/${requestAmount}, $throwable"
                        )

                        if (NetworkUtil.isNetworkAvailable(context)) {
                            getOnSnapToRoads(id, points[id], this)
                        } else {
                            isSuccess = false
                            return
                        }
                    }
                }
            }

            for (i in points.indices) {
                getOnSnapToRoads(i, points[i], mSnapToRoadsCallback!!)
            }
        }

        private interface SnapToRoadsCallback {
            fun onResponse(id: Int, response: Response<SnapToRoadsResponse>)
            fun onFailure(id: Int, throwable: Throwable)
        }

        private interface OnSnapToRoadsFinish {
            fun onSnapToRoadsFinish(
                isSuccess: Boolean,
                snappedPoints: LinkedList<LinkedList<SnapToRoadsResponse.SnappedPointResponse>>
            )
        }

        /**
         * @maxPoints 100 per LatLng is 2 points
         */
        private fun getOnSnapToRoads(
            id: Int,
            points: LinkedList<LatLng>,
            callback: SnapToRoadsCallback
        ) {
            getOnSnapToRoads(points, object : Callback<SnapToRoadsResponse> {
                override fun onResponse(
                    call: Call<SnapToRoadsResponse>,
                    response: Response<SnapToRoadsResponse>
                ) {
                    callback.onResponse(id, response)
                }

                override fun onFailure(call: Call<SnapToRoadsResponse>, t: Throwable) {
                    callback.onFailure(id, t)
                }
            })
        }

        private fun firstIRIFilter(roads: LinkedList<LinkedList<LocalEntry>>) {
            // TODO: set this to settings
            val minFirstIRIFilter = 0.2

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

        private fun getPoints(roads: LinkedList<LinkedList<LocalEntry>>): LinkedList<LinkedList<LatLng>> {
            val points = LinkedList<LinkedList<LatLng>>()

            var count = 0
            points.add(LinkedList<LatLng>())

            roads.forEach {
                val locationEntryFirst = it.first as LocationEntry
                val locationEntryLast = it.last as LocationEntry

                if (count == maxLatLngPoint) {
                    points.add(LinkedList<LatLng>())
                    count = 0
                }
                points.last.add(locationEntryFirst.location)
                points.last.add(locationEntryLast.location)
                count++
            }

            return points
        }

        private fun getSnapToRoadsService(): SnapToRoadsService {
            if (mRetrofit == null) {
                mRetrofit = Retrofit.Builder()
                    .baseUrl(SnapToRoadsService.URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }

            if (mSnapToRoadsService == null) {
                mSnapToRoadsService = mRetrofit!!.create(SnapToRoadsService::class.java)
            }

            return mSnapToRoadsService!!
        }

        /**
         * @maxPoints 100 per LatLng is 2 points
         */
        private fun getOnSnapToRoads(
            points: LinkedList<LatLng>,
            callback: Callback<SnapToRoadsResponse>
        ) {
            var s = ""
            val size = points.size
            for (i in points.indices) {
                s += "${points[i].latitude},${points[i].longitude}"

                if (i != size - 1) {
                    s += "|"
                }
            }
//                Log.d(TAG, "[$id] sent $size points.")

            val service = getSnapToRoadsService()

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