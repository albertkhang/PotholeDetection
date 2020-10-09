package com.albertkhang.potholedetection.util

import android.content.Context
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.database.ILocation
import com.google.gson.Gson
import java.io.*
import java.lang.Exception

class FileUtil {
    companion object {
        private const val TAG = "FileUtil"
        private const val POSTFIX = ".txt"

        fun write(context: Context, fileName: String, data: IDatabase) {
            val f = File("${context.externalCacheDir}/$fileName$POSTFIX")
            if (!f.exists()) {
                f.createNewFile()
            }

            val fileWriter = FileWriter(f, true)

            val bw = BufferedWriter(fileWriter)
            val out = PrintWriter(bw)
            out.println(Gson().toJson(data))
            out.close()
            fileWriter.close()
            // TODO: can optimize here
        }

        fun read(context: Context, fileName: String, type: String): List<IDatabase> {
            val f = File("${context.externalCacheDir}/$fileName$POSTFIX")
            if (!f.exists()) {
                f.createNewFile()
                return ArrayList<IDatabase>().toList()
            }

            val fileReader = FileReader(f)
            val dataString = fileReader.readLines()
            val datas = ArrayList<IDatabase>()

            val typeClass = when (type) {
                LocalDatabaseUtil.CACHE_AG_FILE_NAME -> IAGVector::class.java
                LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME -> ILocation::class.java
                else -> return datas.toList()
            }

            dataString.forEach {
                datas.add(Gson().fromJson(it, typeClass))
            }

            fileReader.close()

            return datas.toList()
        }

        fun delete(context: Context, fileName: String): Boolean {
            val f = File("${context.externalCacheDir}/$fileName$POSTFIX")
            if (f.exists()) {
                f.delete()
                return true
            }
            return false
        }
    }
}