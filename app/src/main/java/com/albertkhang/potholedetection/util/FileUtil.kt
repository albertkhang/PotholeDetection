package com.albertkhang.potholedetection.util

import android.content.Context
import android.util.Log
import android.widget.Toast
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
        private const val FOLDER = "potholedetection"


        fun write(context: Context, fileName: String, data: IDatabase): Boolean {
            // TODO: can optimize here

            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val f = File("${context.externalCacheDir}/$FOLDER/$fileName$POSTFIX")
                if (!f.exists()) {
                    f.createNewFile()
                }

                val fileWriter = FileWriter(f, true)

                val bw = BufferedWriter(fileWriter)
                val out = PrintWriter(bw)
                out.println(Gson().toJson(data))
                out.close()
                fileWriter.close()

                return true
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, e.message.toString())
                return false
            }
        }

        fun read(context: Context, fileName: String, type: String): List<IDatabase> {
            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val f = File("${context.externalCacheDir}/$FOLDER/$fileName$POSTFIX")
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

                return datas
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, e.message.toString())
                return emptyList()
            }
        }

        fun delete(context: Context, fileName: String): Boolean {
            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val f = File("${context.externalCacheDir}/$FOLDER/$fileName$POSTFIX")
                if (f.exists()) {
                    f.delete()
                    return true
                }
                return false
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, e.message.toString())
                return false
            }
        }
    }
}