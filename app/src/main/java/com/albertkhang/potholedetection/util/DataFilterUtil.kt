package com.albertkhang.potholedetection.util

import android.content.Context
import android.util.Log
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.UploadData
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.database.ILocation
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.util.*
import kotlin.collections.HashMap

class DataFilterUtil {
    companion object {
        private const val TAG = "DataFilterUtil"

        private var count = 0

        // Filter level 2
        fun filter() {
//            Thread {
//                Log.d(TAG, "${Thread.currentThread()}")
//                Log.d(TAG, "count=$count")
//                count++
//            }.start()
        }

        fun upload() {
//            Thread {
//                // TODO: upload to cloud firestore
//            }.start()
        }

        fun filter(context: Context) {
            Thread {
                val f = File("${context.externalCacheDir}/potholedetection/tmp.txt")
                if (f.exists()) {
                    return@Thread
                }

                var ag = LocalDatabaseUtil.read(
                    context,
                    LocalDatabaseUtil.CACHE_AG_FILE_NAME,
                    13, LocalDatabaseUtil.CACHE_AG_FILE_NAME
                ) as List<IAGVector>

                var l = LocalDatabaseUtil.read(
                    context,
                    LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME,
                    13, LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME
                ) as List<ILocation>

                // Loại bỏ những data vector dư thừa ở đầu và cuối danh sách
                // ================================================================
                // firstLocationTimestamp < list IAGVector < lastLocationTimestamp
                // ================================================================

                val location = LinkedList<ILocation>()
                location.addAll(l)

                val agVector = LinkedList<IAGVector>()
                agVector.addAll(ag)

                ag = emptyList()
                l = emptyList()

                val firstLocationTimestamp = location.first.timestamps
                val lastLocationTimestamp = location.last.timestamps

                while (agVector.first.timestamps < firstLocationTimestamp) {
                    agVector.removeFirst()
                }

                while (agVector.last.timestamps > lastLocationTimestamp) {
                    agVector.removeLast()
                }

//                Log.d(TAG, "agVector=${agVector.size}, location=${location.size}")

                // now | firstLocationTimestamp < list IAGVector < lastLocationTimestamp |

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

//                Log.d(TAG, "agVector=${agVector.size}, location=${location.size}")
//                Log.d(TAG, "mixed=${mixed.size}")


                var start: ILocation = mixed.first as ILocation
                mixed.removeFirst()

                val tempIRI = LinkedList<Float>()
                val tempUpload = LinkedList<UploadData>()

//                var highest = 0f
//                var lowest = 1f

//                val hash = HashMap<Int, Int>()

                while (mixed.isNotEmpty()) {
                    if (mixed.first is IAGVector) {
                        val data = mixed.first as IAGVector
                        val iri = IVector3D(data.ax, data.ay, data.ax).iri(
                            IVector3D(data.gx, data.gy, data.gz)
                        )
//                        Log.d(TAG, "iri=${iri}")

                        if (iri < 2) {
                            tempIRI.add(iri)
                        }

                        mixed.removeFirst()
                    }

                    if (mixed.first is ILocation) {
                        val end = mixed.first as ILocation

                        if (tempIRI.isNotEmpty()) {
                            // tính iri
//                            Log.d(TAG, "tempIRI=${tempIRI.size}")
                            var sum = 0f
                            tempIRI.forEach {
                                sum += it
                            }
                            val iri = sum / tempIRI.size
//                            if (iri > highest) {
//                                highest = iri
//                            }
//                            if (iri < lowest) {
//                                lowest = iri
//                            }
//                            Log.d(TAG, "average iri=${iri}")

                            // clear tempIRI
                            tempIRI.clear()

                            val averageSpeed = (start.speed + end.speed) / 2

//                            val t: Int = (iri / 0.1f).toInt()
////                            Log.d(TAG, "t=$t")
//                            var value = hash.get(t)
//                            if (value == null) {
//                                value = 1
//                            } else {
//                                value++
//                            }
//                            hash.put(t, value)

                            // add vào tempUpload
                            if (averageSpeed >= 1.38889) {
                                tempUpload.add(
                                    UploadData(
                                        start.latLng,
                                        end.latLng,
                                        iri,
                                        averageSpeed
                                    )
                                )
                            }
                        }

                        start = end
                        mixed.removeFirst()
                    }
                }

//                Log.d(TAG, "hash=${hash.size}")
//                hash.forEach {
//                    Log.d(TAG, "key=${it.key}, value=${it.value}")
//                }

//                while (tempUpload.first.speed < 1.38889) {
//                    tempUpload.removeFirst()
//                }
//
//                while (tempUpload.last.speed < 1.38889) {
//                    tempUpload.removeLast()
//                }

//                Log.d(TAG, "tempUpload=${tempUpload.size}")
//                Log.d(TAG, "highest=$highest, lowest=$lowest")
                tempUpload.forEach {
//                    it.iri -= lowest
//                    Log.d(TAG, it.toString())
                    write(context, it)
                }
                Log.d(TAG, "write done")
            }.start()
        }

        private fun write(context: Context, data: UploadData) {
            val folder = File("${context.externalCacheDir}/potholedetection")
            folder.mkdirs()

            val f = File("${context.externalCacheDir}/potholedetection/tmp.txt")
            if (!f.exists()) {
                f.createNewFile()
            }

            val fileWriter = FileWriter(f, true)

            val bw = BufferedWriter(fileWriter)
            val out = PrintWriter(bw)
            out.println(Gson().toJson(data))
            out.close()
            fileWriter.close()
        }
    }
}