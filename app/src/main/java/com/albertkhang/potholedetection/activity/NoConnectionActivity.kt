package com.albertkhang.potholedetection.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.albertkhang.potholedetection.BuildConfig
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.model.local_database.IAGVector
import com.albertkhang.potholedetection.model.local_database.ILocation
import com.albertkhang.potholedetection.util.LocalDatabaseUtil
import com.albertkhang.potholedetection.util.NetworkUtil
import com.albertkhang.potholedetection.util.SettingsUtil
import kotlinx.android.synthetic.main.activity_no_connection.*

class NoConnectionActivity : AppCompatActivity() {
    private val TAG = "NoConnectionActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_connection)

        addControl()
        addEvent()
    }

    private fun addControl() {

    }

    private fun addEvent() {
        btnTryAgain.setOnClickListener {
            if (NetworkUtil.isNetworkAvailable(this@NoConnectionActivity)) {
                val intent = Intent(this@NoConnectionActivity, SplashActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(baseContext, "Hãy kiểm tra lại mạng.", Toast.LENGTH_SHORT).show()
            }

            if (BuildConfig.DEBUG) {
                showDataSize()
            }
        }

        btnTryAgain.setOnLongClickListener {
            if (BuildConfig.DEBUG) {
                deleteData()
            }

            true
        }
    }

    private var deleteCount = 0

    private fun deleteData() {
        if (deleteCount == 2) {
            deleteLocalData()
            deleteCount = 0
        } else {
            deleteCount++
            Toast.makeText(
                this@NoConnectionActivity,
                "deleteCount=$deleteCount",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun deleteLocalData() {
        if (LocalDatabaseUtil.delete(
                this,
                LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME,
                13
            )
        ) {
            Toast.makeText(this@NoConnectionActivity, "Deleted Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@NoConnectionActivity, "Deleted False", Toast.LENGTH_SHORT).show()
        }

        if (LocalDatabaseUtil.delete(
                this,
                LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME,
                13
            )
        ) {
            Toast.makeText(this@NoConnectionActivity, "Deleted Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@NoConnectionActivity, "Deleted False", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDataSize() {
        // show data size added
        val agDatas = LocalDatabaseUtil.read(
            this,
            LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME, LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME
        ) as List<IAGVector>

        val locationDatas = LocalDatabaseUtil.read(
            this,
            LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME, LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME
        ) as List<ILocation>

        val agSize = agDatas.size
        val locationSize = locationDatas.size

        Toast.makeText(
            this@NoConnectionActivity,
            "ag: $agSize, location: $locationSize",
            Toast.LENGTH_SHORT
        ).show()

        Log.d(TAG, "ag: $agSize, location: $locationSize")

//        if (agDatas.isNotEmpty()) {
//            agDatas.forEach {
////                Log.d(TAG, it.toString())
//            }
//        }

//        if (locationDatas.isNotEmpty()) {
//            locationDatas.forEach {
//                Log.d(TAG, it.toString())
//            }
//        }
    }
}