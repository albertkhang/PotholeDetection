package com.albertkhang.potholedetection.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.albertkhang.potholedetection.R


class RequestPermissionActivity : AppCompatActivity() {
    private val REQUEST_PERMISSION_REQUESTCODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_permission)

        showRequestPermissionDialog()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_REQUESTCODE && grantResults.isNotEmpty()) {
            var isGranted = true
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    isGranted = false
                }
            }

            if (!isGranted) {
                Toast.makeText(this@RequestPermissionActivity, "Bạn phải cấp đủ quyền để tiếp tục.",Toast.LENGTH_SHORT).show()
                showRequestPermissionDialog()
            } else {
                val intent = Intent(this@RequestPermissionActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showRequestPermissionDialog() {
        // ser round corner dialog
        val dialog = Dialog(this, R.style.RoundCornerDialog)
        dialog.setContentView(R.layout.dialog_two_button)
        // prevent click outside dialog
        dialog.setCanceledOnTouchOutside(false)
        dialog.findViewById<Button>(R.id.cancel).setOnClickListener {
            showDenyGrantPermissionDialog()
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.agree)
            .setOnClickListener {
                ActivityCompat.requestPermissions(
                    this@RequestPermissionActivity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    REQUEST_PERMISSION_REQUESTCODE
                )
                dialog.dismiss()
            }
        dialog.findViewById<TextView>(R.id.title).text = "Yêu Cầu Quyền Cho Ứng Dụng"
        dialog.findViewById<TextView>(R.id.description).text =
            "Ứng dụng yêu cầu vị trí và truy cập bộ nhớ\nđể tiếp tục."
        dialog.findViewById<Button>(R.id.cancel).text = "Từ Chối"
        dialog.findViewById<Button>(R.id.agree).text = "Đồng Ý"
        dialog.show()
    }

    private fun showDenyGrantPermissionDialog() {
        // ser round corner dialog
        val dialog = Dialog(this, R.style.RoundCornerDialog)
        dialog.setContentView(R.layout.dialog_one_button)
        // prevent click outside dialog
        dialog.setCanceledOnTouchOutside(false)
        dialog.findViewById<Button>(R.id.confirm).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.findViewById<TextView>(R.id.title).text = "Thông Báo"
        dialog.findViewById<TextView>(R.id.description).text =
            "Ứng dụng sẽ thoát vì\nkhông cung cấp đủ quyền để sử dụng."
        dialog.findViewById<Button>(R.id.confirm).text = "Xác Nhận"
        dialog.show()
    }
}