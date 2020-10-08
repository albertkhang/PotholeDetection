package com.albertkhang.potholedetection.util

import android.util.Log
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.database.ILocation
import com.albertkhang.potholedetection.model.settings.ISettings
import io.paperdb.Paper
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class LocalDatabaseUtil {
    companion object {
        const val TAG = "LocalDatabaseUtil"
        const val DEBUG_SETTINGS_BOOK = "debug-settings-book"
        const val RELEASE_SETTINGS_BOOK = "release-settings-book"

        const val AG_VECTOR_BOOK = "ag-vector-book"
        const val LOCATION_BOOK = "location-book"

        /**
         * Initial settings for release version
         */
        fun init() {
            if (readSettings() == null) {
                writeSettings(ISettings())
            }
        }

        fun filter(hour: Int) {
            // TODO: Cứ mỗi 1h là tự update, không quan tâm hiện tại là mấy giờ

            val agData: List<IAGVector>? = readData(AG_VECTOR_BOOK, hour) as List<IAGVector>?
            val locationData: List<ILocation>? = readData(LOCATION_BOOK, hour) as List<ILocation>?

            val filteredAGData = ArrayList<IAGVector>()
            val filteredLocationData = ArrayList<ILocation>()

            val filteredData = ArrayList<IDatabase>()

            // filter IRI > 0.3
            if (agData != null && agData.isNotEmpty()) {
                agData.forEach {
                    val iri =
                        IVector3D(it.ax, it.ay, it.az).project(IVector3D(it.gx, it.gy, it.gz))
                    if (iri > 0.3) {
                        filteredAGData.add(it)
                    }
                }
            }
            Log.d(TAG, "filteredAGData: ${filteredAGData.size}")

            if (locationData != null && locationData.isNotEmpty()) {
                // filter provider = gps && speed >= 1.38889 m/s = 5km/h
                locationData.forEach {
                    if (it.provider == ILocation.PROVIDER_NETWORK && it.speed >= 0.3) {
                        filteredLocationData.add(it)
                    }
                }
            }
            Log.d(TAG, "filteredLocationData: ${filteredLocationData.size}")

            // find start and end location
            //
            filteredLocationData.forEach {

            }
        }

        fun writeSettings(settings: ISettings) {
            if (SettingsUtil.isDebugVersion) {
                Paper.book().write(DEBUG_SETTINGS_BOOK, settings)
            } else {
                Paper.book().write(RELEASE_SETTINGS_BOOK, settings)
            }
        }

        fun readSettings(): ISettings? {
            if (SettingsUtil.isDebugVersion) {
                return Paper.book().read(DEBUG_SETTINGS_BOOK, null)
            } else {
                return Paper.book().read(RELEASE_SETTINGS_BOOK, null)
            }
        }

        private var agTemp: LinkedList<IDatabase>? = null

        fun add(bookName: String, data: IDatabase) {
            val currentHour = 13

            when (bookName) {
                AG_VECTOR_BOOK -> {
                    val newdatas: ArrayList<IDatabase> = ArrayList()
                    val datas = readData(bookName, currentHour)
                    if (datas != null && datas.isNotEmpty()) {
                        newdatas.addAll(datas)
                    }
                    newdatas.add(data)

                    Paper.book(bookName).write(currentHour.toString(), newdatas.toList())
//                    Log.d(TAG, data.toString())
                }
                LOCATION_BOOK -> {
                    val newdatas: ArrayList<IDatabase> = ArrayList()
                    val datas = readData(bookName, currentHour)
                    if (datas != null && datas.isNotEmpty()) {
                        newdatas.addAll(datas)
                    }
                    newdatas.add(data)

                    Paper.book(bookName).write(currentHour.toString(), newdatas.toList())
//                    Log.d(TAG, data.toString())
                }
                else -> throw Exception("Write to local database false.")
            }
        }

        /**
         * Read all data in a book key
         */
        fun readData(bookName: String, hour: Int): List<IDatabase>? {
            return Paper.book(bookName).read<List<IDatabase>>(hour.toString(), null)
        }

        /**
         * Delete data in a book key
         */
        fun deleteData(bookName: String, hour: Int) {
            Paper.book(bookName).delete(hour.toString())
        }

        /**
         * Read all key in a book
         */
        fun readAllKeys(bookName: String): List<String>? {
            return Paper.book(bookName).allKeys
        }
    }
}