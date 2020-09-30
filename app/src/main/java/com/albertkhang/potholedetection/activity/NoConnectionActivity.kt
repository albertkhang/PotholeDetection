package com.albertkhang.potholedetection.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.util.NetworkUtil
import com.albertkhang.potholedetection.util.PermissionUtil
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
            if (NetworkUtil.isNetworkAvailable(this@NoConnectionActivity)) {
                val intent: Intent
                if (!PermissionUtil.isGrantedPermissions(this@NoConnectionActivity)) {
                    // do not grant permission yet
                    intent = Intent(this@NoConnectionActivity, RequestPermissionActivity::class.java)
                } else {
                    // granted permission
                    intent = Intent(this@NoConnectionActivity, MainActivity::class.java)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(baseContext, "Hãy kiểm tra lại mạng.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}