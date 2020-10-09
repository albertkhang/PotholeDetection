package com.albertkhang.potholedetection.util

import android.content.Context
import android.util.Log
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.settings.ISettings
import io.paperdb.Paper
import java.text.SimpleDateFormat
import java.util.*

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

        // Filter level 2
        // TODO: chạy ở background thread
        //
//        fun filter() {
//            // TODO: Cứ mỗi 1h là tự update, không quan tâm hiện tại là mấy giờ
//
//            val year = Calendar.getInstance().get(Calendar.YEAR)
//            val month = Calendar.getInstance().get(Calendar.MONTH) + 1
//            val day = Calendar.getInstance().get(Calendar.DATE)
//            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//
//            val parseFormat = SimpleDateFormat("yyyyMMdd HH:MM")
//            val s = parseFormat.format(Date())
//
////            Log.d(TAG, "$year$month${DecimalFormat("##").format(day)}$hour")
//            Log.d(TAG, s)
//        }

        fun writeSettings(settings: ISettings) {
            if (SettingsUtil.isDebugVersion) {
                Paper.book().write(DEBUG_SETTINGS_BOOK, settings)
            } else {
                Paper.book().write(RELEASE_SETTINGS_BOOK, settings)
            }
        }

        fun readSettings(): ISettings? {
            return if (SettingsUtil.isDebugVersion) {
                Paper.book().read(DEBUG_SETTINGS_BOOK, null)
            } else {
                Paper.book().read(RELEASE_SETTINGS_BOOK, null)
            }
        }

        /**
         * Add new data at bottom of cache file
         */
        fun add(context: Context, fileName: String, data: IDatabase) {
            val currentHour = 13

            FileUtil.write(context, "${fileName}_$currentHour", data)
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