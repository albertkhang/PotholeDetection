package com.albertkhang.potholedetection.util

import android.content.Context
import android.util.Log
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.IPotholeDtected
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.database.ILocation
import java.util.*

class DataFilterUtil {
    companion object {
        private const val TAG = "DataFilterUtil"

        private const val minSpeed = 1.38889 // m/s = 5 km/h

        private val mCloudDatabaseUtil = CloudDatabaseUtil()

        private fun read(context: Context, type: String, hour: Int): List<IDatabase> {
            when (type) {
                LocalDatabaseUtil.CACHE_AG_FILE_NAME -> {
                    return LocalDatabaseUtil.read(
                        context,
                        LocalDatabaseUtil.CACHE_AG_FILE_NAME,
                        hour, LocalDatabaseUtil.CACHE_AG_FILE_NAME
                    )
                }

                LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME -> {
                    return LocalDatabaseUtil.read(
                        context,
                        LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME,
                        hour, LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME
                    )
                }

                else -> return emptyList()
            }
        }

        // Filter level 1
        fun run(context: Context) {
            Thread {
                Log.d(
                    TAG,
                    "start filter minute=${
                        Calendar.getInstance().get(Calendar.MINUTE)
                    }, second=${Calendar.getInstance().get(Calendar.SECOND)}"
                )

                val ag = read(context, LocalDatabaseUtil.CACHE_AG_FILE_NAME, 13) as List<IAGVector>
                val l =
                    read(context, LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME, 13) as List<ILocation>

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

                // now | firstLocationTimestamp < list IAGVector < lastLocationTimestamp |

                val mixed = getMixedData(agVector, location)

                val potholeDetecteds = filter(mixed)

                mCloudDatabaseUtil.write(
                    potholeDetecteds.get(0)
                ) {
                    Log.d(TAG, "Uploaded done!")
                }



                // Wrtie to cache file
//                if (potholeDetecteds.isNotEmpty()) {
//                    if (LocalDatabaseUtil.writeFilteredList(context, potholeDetecteds.toList())) {
//                        Log.d(TAG, "filter done")
//                    } else {
//                        Log.d(TAG, "write filter error")
//                    }
//                }
            }.start()
        }

        private fun filter(mixed: LinkedList<IDatabase>): LinkedList<IPotholeDtected> {
            var start: ILocation = mixed.first as ILocation
            mixed.removeFirst()

            val tempIRI = LinkedList<Float>()
            val tempUpload = LinkedList<IPotholeDtected>()

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
                            IPotholeDtected(
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