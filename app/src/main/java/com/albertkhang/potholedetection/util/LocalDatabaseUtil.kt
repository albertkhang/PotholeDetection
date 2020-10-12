package com.albertkhang.potholedetection.util

import android.content.Context
import com.albertkhang.potholedetection.model.IPothole
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

        const val CACHE_FILTERED_FILE_NAME = "cache_filtered"

        /**
         * Initial settings for release version
         */
        fun init() {
            if (readSettings() == null) {
                writeSettings(ISettings())
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
            return if (SettingsUtil.isDebugVersion) {
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
            return FileUtil.writeFilteredCache(
                context,
                CACHE_FILTERED_FILE_NAME,
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