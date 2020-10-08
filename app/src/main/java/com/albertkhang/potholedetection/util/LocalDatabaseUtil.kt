package com.albertkhang.potholedetection.util

import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.settings.ISettings
import io.paperdb.Paper
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

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
            if (SettingsUtil.isDebugVersion) {
                return Paper.book().read(DEBUG_SETTINGS_BOOK, null)
            } else {
                return Paper.book().read(RELEASE_SETTINGS_BOOK, null)
            }
        }

        fun write(bookName: String, data: IDatabase) {
            val currentHour =
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY) // 24 hours

            when (bookName) {
                AG_VECTOR_BOOK -> {
                    val newdatas: ArrayList<IDatabase> = ArrayList()
                    val datas = readDatas(bookName, currentHour)
                    if (datas != null && datas.isNotEmpty()) {
                        newdatas.addAll(datas)
                    }
                    newdatas.add(data)

                    Paper.book(bookName).write(currentHour.toString(), newdatas.toList())
                }
                LOCATION_BOOK -> {
                    val newdatas: ArrayList<IDatabase> = ArrayList()
                    val datas = readDatas(bookName, currentHour)
                    if (datas != null && datas.isNotEmpty()) {
                        newdatas.addAll(datas)
                    }
                    newdatas.add(data)

                    Paper.book(bookName).write(currentHour.toString(), newdatas.toList())
                }
                else -> throw Exception("Write to local database false.")
            }
        }

        /**
         * Read all data in a key of a book
         */
        fun readDatas(bookName: String, hour: Int): List<IDatabase>? {
            return Paper.book(bookName).read<List<IDatabase>>(hour.toString(), null)
        }

        /**
         * Read all key in a book
         */
        fun readAllKeys(bookName: String): List<String>? {
            return Paper.book(bookName).allKeys
        }
    }
}