package com.albertkhang.potholedetection.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.util.NetworkUtil
import kotlinx.android.synthetic.main.activity_no_connection.*

class NoConnectionActivity : AppCompatActivity() {
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
            if (NetworkUtil.isNetworkAvailable(baseContext)) {
                val intent = Intent(baseContext, RequestPermissionActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(baseContext, "Hãy kiểm tra lại mạng.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}