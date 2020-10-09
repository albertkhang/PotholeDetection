package com.albertkhang.potholedetection.util

import android.content.Context
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.settings.ISettings
import io.paperdb.Paper

class LocalDatabaseUtil {
    companion object {
        const val TAG = "LocalDatabaseUtil"
        const val DEBUG_SETTINGS_BOOK = "debug-settings-book"
        const val RELEASE_SETTINGS_BOOK = "release-settings-book"

        const val CACHE_AG_FILE_NAME = "cache_ag"
        const val CACHE_LOCATION_FILE_NAME = "cache_location"

        /**
         * Initial settings for release version
         */
        fun init() {
            if (readSettings() == null) {
                writeSettings(ISettings())
            }
        }

//        fun filter(hour: Int) {
//            // TODO: Cứ mỗi 1h là tự update, không quan tâm hiện tại là mấy giờ
//
//            val agData: List<IAGVector>? = readData(AG_VECTOR_BOOK, hour) as List<IAGVector>?
//            val locationData: List<ILocation>? = readData(LOCATION_BOOK, hour) as List<ILocation>?
//
//            val filteredAGData = ArrayList<IAGVector>()
//            val filteredLocationData = ArrayList<ILocation>()
//
//            val filteredData = ArrayList<IDatabase>()
//
//            // filter IRI > 0.3
//            if (agData != null && agData.isNotEmpty()) {
//                agData.forEach {
//                    val iri =
//                        IVector3D(it.ax, it.ay, it.az).project(IVector3D(it.gx, it.gy, it.gz))
//                    if (iri > 0.3) {
//                        filteredAGData.add(it)
//                    }
//                }
//            }
//            Log.d(TAG, "filteredAGData: ${filteredAGData.size}")
//
//            if (locationData != null && locationData.isNotEmpty()) {
//                // filter provider = gps && speed >= 1.38889 m/s = 5km/h
//                locationData.forEach {
//                    if ((it.provider == ILocation.PROVIDER_GPS || it.provider == ILocation.PROVIDER_PASSIVE) && it.speed >= readSettings()!!.detectNotification.minLocalWriteSpeed) {
//                        filteredLocationData.add(it)
//                    }
//                }
//            }
//            Log.d(TAG, "filteredLocationData: ${filteredLocationData.size}")
//
//            // find start and end location
//            //
//            filteredLocationData.forEach {
//
//            }
//        }

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

        /**
         * Add new data at bottom of cache file
         */
        fun add(context: Context, fileName: String, data: IDatabase) {
            val currentHour = 13

            FileUtil.write(
                context,
                "${fileName}_$currentHour", data
            )
        }

        /**
         * Read all data in cache file
         */
        fun read(
            context: Context,
            fileName: String,
            hour: Int,
            type: String
        ): List<IDatabase> {
            return FileUtil.read(context, "${fileName}_$hour", type)
        }

        /**
         * Delete data in cache file
         */
        fun delete(context: Context, fileName: String, hour: Int): Boolean {
            return FileUtil.delete(context, "${fileName}_$hour")
        }
    }
}