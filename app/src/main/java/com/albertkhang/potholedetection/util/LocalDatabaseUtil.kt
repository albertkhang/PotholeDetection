package com.albertkhang.potholedetection.util

import android.content.Context
import com.albertkhang.potholedetection.BuildConfig
import com.albertkhang.potholedetection.model.cloud_database.IPothole
import com.albertkhang.potholedetection.model.local_database.IDatabase
import com.albertkhang.potholedetection.model.response.SettingsResponse
import io.paperdb.Paper
import java.text.SimpleDateFormat
import java.util.*

class LocalDatabaseUtil {
    companion object {
        const val TAG = "LocalDatabaseUtil"
        const val DEBUG_SETTINGS_BOOK = "debug-settings-book"
        const val RELEASE_SETTINGS_BOOK = "release-settings-book"

        const val CACHE_ACCELEROMETER_FILE_NAME = "cache_accelerometer"
        const val CACHE_LOCATION_FILE_NAME = "cache_location"
        const val CACHE_FILTERED_FILE_NAME = "cache_filtered"

        /**
         * Initial settings for release version
         */
        fun init() {
            if (readSettings() == null) {
                writeSettings(SettingsResponse())
            }
        }

        fun writeSettings(settingsResponse: SettingsResponse) {
            if (BuildConfig.DEBUG) {
                Paper.book().write(DEBUG_SETTINGS_BOOK, settingsResponse)
            } else {
                Paper.book().write(RELEASE_SETTINGS_BOOK, settingsResponse)
            }
        }

        fun readSettings(): SettingsResponse? {
            return if (BuildConfig.DEBUG) {
                Paper.book().read(DEBUG_SETTINGS_BOOK, null)
            } else {
                Paper.book().read(RELEASE_SETTINGS_BOOK, null)
            }
        }

        /**
         * Add new data at bottom of cache file
         */
        fun addRaw(context: Context, fileName: String, data: IDatabase) {
            FileUtil.writeRaw(context, fileName, data)
        }

        fun writeFilteredList(context: Context, data: List<IPothole>): Boolean {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmm")
            val currentHour = sdf.format(Date())
//            Log.d(TAG,"$currentDate")

            return FileUtil.writeFilteredCache(
                context,
                currentHour,
                data
            )
        }

        /**
         * Read all data in cache file
         */
        fun read(
            context: Context,
            fileName: String,
            type: String
        ): List<IDatabase> {
            return FileUtil.read(context, fileName, type)
        }

        /**
         * Delete data in cache file
         */
        fun delete(context: Context, fileName: String, hour: Int): Boolean {
            return FileUtil.delete(context, "${fileName}_$hour")
        }

        fun deleteAllCacheFile(context: Context): Boolean {
            return FileUtil.deleteAllCacheFile(context)
        }
    }
}