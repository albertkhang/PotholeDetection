package com.albertkhang.potholedetection.activity

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.contains
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.util.DisplayUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.legend_popup.*

@SuppressLint("MissingPermission")
// Checked permissions before go to this activity
class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val MAP_SCALE_VALUE = 16f

    private var mLegendView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addControl()
        addEvent()
    }

    private fun addEvent() {
        btnMyLocation.setOnClickListener {
            moveToMyLocation()
            removeLegendView()
        }

        btnLegend.setOnClickListener {
            addLegendView()
        }
    }

    private fun addLegendView() {
        initLegendView()

        if (mLegendView != null) {
            val params = initLegendLayoutParams()
            root_view.addView(mLegendView, params)
        }

        btnLegend.isClickable = false
    }

    private fun removeLegendView() {
        if (mLegendView != null) {
            root_view.removeView(mLegendView)
            mLegendView = null

            btnLegend.isClickable = true
        }
    }

    private fun initLegendLayoutParams(): ViewGroup.LayoutParams {
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)

        val density = DisplayUtil.getDensity(this)
        params.bottomMargin = (35 * density).toInt()
        params.leftMargin = (25 * density).toInt()

        return params
    }

    private fun initLegendView() {
        val inflater = LayoutInflater.from(this)
        mLegendView = inflater.inflate(R.layout.legend_popup, null, false)

        if (mLegendView != null) {
            val goodColor = ContextCompat.getColor(this, R.color.colorGoodLegend)
            val averageColor = ContextCompat.getColor(this, R.color.colorAverageLegend)
            val badColor = ContextCompat.getColor(this, R.color.colorBadLegend)

            mLegendView!!.findViewById<FrameLayout>(R.id.circleGood).background.colorFilter =
                PorterDuffColorFilter(goodColor, PorterDuff.Mode.SRC)

            mLegendView!!.findViewById<FrameLayout>(R.id.circleAverage).background.colorFilter =
                PorterDuffColorFilter(averageColor, PorterDuff.Mode.SRC)

            mLegendView!!.findViewById<FrameLayout>(R.id.circleBad).background.colorFilter =
                PorterDuffColorFilter(badColor, PorterDuff.Mode.SRC)
        }
    }

    private fun addControl() {
        (map as SupportMapFragment).getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // TODO: add loading screen when getting current location
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnCameraMoveListener { removeLegendView() }
        initGoogleMap()
        moveToMyLocation()
    }

    private fun moveToMyLocation() {
        mFusedLocationClient.lastLocation.addOnSuccessListener {
            val current = LatLng(it.latitude, it.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_SCALE_VALUE))
        }
    }

    private fun initGoogleMap() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
    }
}