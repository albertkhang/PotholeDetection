package com.albertkhang.potholedetection.util

import com.albertkhang.potholedetection.model.ISettings
import io.paperdb.Paper

class LocalDatabaseUtil {
    companion object {
        const val SETTINGS_BOOK = "settings-book"

        fun writeSettings(settings: ISettings) {
            Paper.book().write(SETTINGS_BOOK, settings)
        }

        fun readSettings(): ISettings? {
            return Paper.book().read(SETTINGS_BOOK, null)
        }

        fun init() {
            if (readSettings() == null) {
                writeSettings(ISettings())
            }
        }
    }
}