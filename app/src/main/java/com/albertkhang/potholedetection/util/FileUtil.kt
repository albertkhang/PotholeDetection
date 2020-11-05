package com.albertkhang.potholedetection.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.albertkhang.potholedetection.model.cloud_database.IPothole
import com.albertkhang.potholedetection.model.entry.AccelerometerEntry
import com.albertkhang.potholedetection.model.entry.LocalEntry
import com.albertkhang.potholedetection.model.entry.LocationEntry
import com.albertkhang.potholedetection.model.local_database.IAGVector
import com.albertkhang.potholedetection.model.local_database.IDatabase
import com.albertkhang.potholedetection.model.local_database.ILocation
import com.albertkhang.potholedetection.util.LocalDatabaseUtil.Companion.CACHE_ACCELEROMETER_FILE_NAME
import com.albertkhang.potholedetection.util.LocalDatabaseUtil.Companion.CACHE_LOCATION_FILE_NAME
import com.google.gson.Gson
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FileUtil {
    @SuppressLint("SimpleDateFormat")
    companion object {
        private const val TAG = "FileUtil"
        private const val POSTFIX = ".txt"
        private const val FOLDER = "potholedetection"

        private const val FILTERED_FOLDER = "filtered"

        /**
         * be used for test
         */
        fun writeFilteredCache(
            context: Context,
            fileName: String,
            datas: List<IPothole>
        ): Boolean {
            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val cacheFolder = File("${context.externalCacheDir}/$FOLDER/$FILTERED_FOLDER")
                cacheFolder.mkdirs()

                val f =
                    File("${context.externalCacheDir}/$FOLDER/$FILTERED_FOLDER/$fileName$POSTFIX")
                if (!f.exists()) {
                    f.createNewFile()
                }

                val fileWriter = FileWriter(f, true)

                val bw = BufferedWriter(fileWriter)
                val out = PrintWriter(bw)

                datas.forEach {
                    out.println(Gson().toJson(it))
                }

                out.close()
                fileWriter.close()

                return true
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, e.message.toString())
                return false
            }
        }

        /**
         * be used for test
         */
        fun readFilteredCache(context: Context, fileName: String): List<IPothole> {
            val folder = File("${context.externalCacheDir}/$FOLDER")
            folder.mkdirs()

            val cacheFolder = File("${context.externalCacheDir}/$FOLDER/$FILTERED_FOLDER")
            cacheFolder.mkdirs()

            val f =
                File("${context.externalCacheDir}/$FOLDER/$FILTERED_FOLDER/$fileName$POSTFIX")
            if (!f.exists()) {
                return emptyList()
            }

            val fileReader = FileReader(f)
            val dataString = fileReader.readLines()
            val datas = ArrayList<IPothole>()

            dataString.forEach {
                datas.add(Gson().fromJson(it, IPothole::class.java))
            }

            fileReader.close()

            return datas
        }

        fun writeRaw(context: Context, fileName: String, data: IDatabase): Boolean {
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

        fun readLocationCache(context: Context): LinkedList<LocationEntry> {
            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val f =
                    File("${context.externalCacheDir}/$FOLDER/$CACHE_LOCATION_FILE_NAME$POSTFIX")
                if (!f.exists()) {
                    f.createNewFile()
                    return LinkedList<LocationEntry>()
                }

                val fileReader = FileReader(f)
                val dataString = fileReader.readLines()
                val locationEntries = LinkedList<LocationEntry>()

                dataString.forEach {
                    locationEntries.add(Gson().fromJson(it, LocationEntry::class.java))
                }

                fileReader.close()

                return locationEntries
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                return LinkedList<LocationEntry>()
            }
        }

        fun writeLocationCache(context: Context, locationEntry: LocationEntry): Boolean {
            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val f =
                    File("${context.externalCacheDir}/$FOLDER/$CACHE_LOCATION_FILE_NAME$POSTFIX")
                if (!f.exists()) {
                    f.createNewFile()
                }

                val fileWriter = FileWriter(f, true)

                val bw = BufferedWriter(fileWriter)
                val out = PrintWriter(bw)
                out.println(Gson().toJson(locationEntry))
                out.close()
                fileWriter.close()

                return true
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                return false
            }
        }

        fun readAccelerometerCache(context: Context): LinkedList<AccelerometerEntry> {
            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val f =
                    File("${context.externalCacheDir}/$FOLDER/$CACHE_ACCELEROMETER_FILE_NAME$POSTFIX")
                if (!f.exists()) {
                    f.createNewFile()
                    return LinkedList<AccelerometerEntry>()
                }

                val fileReader = FileReader(f)
                val dataString = fileReader.readLines()
                val locationEntries = LinkedList<AccelerometerEntry>()

                dataString.forEach {
                    locationEntries.add(Gson().fromJson(it, AccelerometerEntry::class.java))
                }

                fileReader.close()

                return locationEntries
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                return LinkedList<AccelerometerEntry>()
            }
        }

        fun writeAccelerometerCache(
            context: Context,
            accelerometerEntry: AccelerometerEntry
        ): Boolean {
            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val f =
                    File("${context.externalCacheDir}/$FOLDER/$CACHE_ACCELEROMETER_FILE_NAME$POSTFIX")
                if (!f.exists()) {
                    f.createNewFile()
                }

                val fileWriter = FileWriter(f, true)

                val bw = BufferedWriter(fileWriter)
                val out = PrintWriter(bw)
                out.println(Gson().toJson(accelerometerEntry))
                out.close()
                fileWriter.close()

                return true
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                return false
            }
        }

        fun writeFilteredCache(
            context: Context,
            roads: LinkedList<LinkedList<LocalEntry>>
        ): Boolean {
            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val cacheFolder = File("${context.externalCacheDir}/$FOLDER/$FILTERED_FOLDER")
                cacheFolder.mkdirs()

                val fileName =
                    "${getCurrentTimeFormat()}_${LocalDatabaseUtil.CACHE_FILTERED_FILE_NAME}"

                val f =
                    File("${context.externalCacheDir}/$FOLDER/$FILTERED_FOLDER/$fileName$POSTFIX")
                if (!f.exists()) {
                    f.createNewFile()
                }

                val fileWriter = FileWriter(f, true)

                val bw = BufferedWriter(fileWriter)
                val out = PrintWriter(bw)
                out.println(Gson().toJson(roads))
                out.close()
                fileWriter.close()

                return true
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                return false
            }
        }

        @SuppressLint("SimpleDateFormat")
        private val sdf = SimpleDateFormat("yyyyMMdd_HHmm")
        private fun getCurrentTimeFormat(): String {
            return sdf.format(Date())
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
                    LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME -> IAGVector::class.java
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

        fun deleteAllCacheFile(context: Context): Boolean {
            try {
                val folder = File("${context.externalCacheDir}/$FOLDER")
                folder.mkdirs()

                val agName = LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME
                val locationName = LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME

                val f1 = File("${context.externalCacheDir}/$FOLDER/$agName$POSTFIX")
                val f2 = File("${context.externalCacheDir}/$FOLDER/$locationName$POSTFIX")

                if (f1.exists()) {
                    f1.delete()
                }

                if (f2.exists()) {
                    f2.delete()
                }

                return true
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, e.message.toString())
            }

            return false
        }
    }
}