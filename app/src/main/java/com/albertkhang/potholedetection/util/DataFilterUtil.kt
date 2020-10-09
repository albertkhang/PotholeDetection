package com.albertkhang.potholedetection.util

import android.content.Context
import android.util.Log
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.database.ILocation
import com.google.android.gms.maps.model.LatLng
import java.util.*

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
                val ag = LocalDatabaseUtil.read(
                    context,
                    LocalDatabaseUtil.CACHE_AG_FILE_NAME,
                    13, LocalDatabaseUtil.CACHE_AG_FILE_NAME
                ) as List<IAGVector>

                val l = LocalDatabaseUtil.read(
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

                val firstLocationTimestamp = location.first.timestamps
                val lastLocationTimestamp = location.last.timestamps

                while (agVector.first.timestamps < firstLocationTimestamp) {
                    agVector.removeFirst()
                }

                while (agVector.last.timestamps > lastLocationTimestamp) {
                    agVector.removeLast()
                }

                // agVector=2329
                // location=3390
                // total=5719
                Log.d(TAG, "agVector=${agVector.size}, location=${location.size}")

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

                // mixed=5719

                Log.d(TAG, "agVector=${agVector.size}, location=${location.size}")
                Log.d(TAG, "mixed=${mixed.size}")

                data class UploadData(
                    val startLatLng: LatLng,
                    val endLatLng: LatLng,
                    var iri: Float
                )

                var start: ILocation = mixed.first as ILocation
                mixed.removeFirst()

                val tempIRI = LinkedList<Float>()
                val tempUpload = LinkedList<UploadData>()

                var highest = 0f
                var lowest = 1f

                while (mixed.isNotEmpty()) {
                    if (mixed.first is IAGVector) {
                        val data = mixed.first as IAGVector
                        val iri = IVector3D(data.ax, data.ay, data.ax).iri(
                            IVector3D(data.gx, data.gy, data.gz)
                        )
//                        Log.d(TAG, "iri=${iri}")
                        tempIRI.add(iri)
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
                            if (iri > highest) {
                                highest = iri
                            }
                            if (iri < lowest) {
                                lowest = iri
                            }
//                            Log.d(TAG, "average iri=${iri}")

                            // clear tempIRI
                            tempIRI.clear()

                            // add vào tempUpload
                            tempUpload.add(UploadData(start.latLng, end.latLng, iri))
                        }

                        start = end
                        mixed.removeFirst()
                    }
                }

                Log.d(TAG, "tempUpload=${tempUpload.size}")
                Log.d(TAG, "highest=$highest, lowest=$lowest")
                tempUpload.forEach {
                    it.iri -= lowest
                    Log.d(TAG, it.toString())
                }
            }.start()
        }
    }
}