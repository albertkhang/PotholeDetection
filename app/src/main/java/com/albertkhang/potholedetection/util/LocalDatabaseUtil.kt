package com.albertkhang.potholedetection.util

import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.settings.ISettings
import io.paperdb.Paper
import java.lang.Exception

class LocalDatabaseUtil {
    companion object {
        const val DEBUG_SETTINGS_BOOK = "debug-settings-book"
        const val RELEASE_SETTINGS_BOOK = "release-settings-book"

        const val AG_VECTOR_BOOK = "ag-vector-book"
        const val LOCATION_BOOK = "location-book"

        /**
         * Initial settings for release version
         */
        fun init() {
            if(readSettings()==null){
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
            if (SettingsUtil.isDebugVersion) {
                return Paper.book().read(DEBUG_SETTINGS_BOOK, null)
            } else {
                return Paper.book().read(RELEASE_SETTINGS_BOOK, null)
            }
        }

        fun write(bookName: String, data: IDatabase) {
            when (bookName) {
                AG_VECTOR_BOOK -> Paper.book(AG_VECTOR_BOOK).write(data.timestamps.toString(), data)
                LOCATION_BOOK -> Paper.book(AG_VECTOR_BOOK).write(data.timestamps.toString(), data)
                else -> throw Exception("Write to local database false.")
            }
        }

        fun readAll(bookName: String): List<IDatabase>? {
            val datas = ArrayList<IDatabase>()
            val allKeys = readAllKeys(bookName)

            if (allKeys != null && allKeys.isNotEmpty()) {
                allKeys.forEach {
                    val data = Paper.book(bookName).read<IDatabase>(it)
                    datas.add(data)
                }
            }

            return datas
        }

        fun readAllKeys(bookName: String): List<String>? {
            return Paper.book(bookName).allKeys
        }
    }
}