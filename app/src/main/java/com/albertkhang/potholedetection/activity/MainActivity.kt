package com.albertkhang.potholedetection.activity

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.animation.AlphaAnimation
import com.albertkhang.potholedetection.broadcast.NetworkChangeReceiver
import com.albertkhang.potholedetection.model.IPothole
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.ILocation
import com.albertkhang.potholedetection.model.settings.ISettings
import com.albertkhang.potholedetection.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_one_button.*
import kotlinx.android.synthetic.main.legend_popup.*
import kotlinx.android.synthetic.main.legend_popup.title
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
// Checked permissions before go to this activity
class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var TAG = "MainActivity"

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val MAP_ZOOM = LocalDatabaseUtil.readSettings()!!.mapZoom

    private var mLegendView: View? = null
    private lateinit var mPreparingMapProgress: View

    private lateinit var mCloudDatabaseUtil: CloudDatabaseUtil

    private lateinit var mNetworkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addControl()
        addEvent()
    }

    private fun onMapReady() {
        onAddLinesReady {
            root_view.removeView(mPreparingMapProgress)
        }
    }

    private fun onAddLinesReady(objects: () -> Unit) {
        mCloudDatabaseUtil.read("albertkhang") {
            if (it.isSuccessful) {
                val documents = it.result.documents
                documents.forEach {
//                    val username = it.data!!.get("username")
                    val s = it.data!!["data"] as String
                    val data = Gson().fromJson(s, Array<IPothole>::class.java)

                    data.forEach {
                        // set min speed = 2.77778 m/s = 10 km/h
                        val polyline = mMap.addPolyline(
                            PolylineOptions()
                                .add(it.startLatLng)
                                .add(it.endLatLng)
                        )

                        polyline.tag = it.quality

                        stylePolyline(polyline)
                    }
                }

                objects.invoke()
            }
        }
    }

    private val POLYLINE_STROKE_WIDTH_PX = 12

    private fun stylePolyline(polyline: Polyline) {
        polyline.startCap = RoundCap()
        polyline.endCap = RoundCap()
        polyline.width = POLYLINE_STROKE_WIDTH_PX.toFloat()
        polyline.jointType = JointType.ROUND
        when (polyline.tag) {
            "G" -> polyline.color = ContextCompat.getColor(this, R.color.colorGoodLegend)
            "A" -> polyline.color = ContextCompat.getColor(this, R.color.colorAverageLegend)
            "B" -> polyline.color = ContextCompat.getColor(this, R.color.colorBadLegend)
        }
    }

    private var deleteCount = 0

    private fun deleteLocalData() {
        if (LocalDatabaseUtil.delete(
                this,
                LocalDatabaseUtil.CACHE_AG_FILE_NAME,
                13
            )
        ) {
            Toast.makeText(this@MainActivity, "Deleted Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Deleted False", Toast.LENGTH_SHORT).show()
        }

        if (LocalDatabaseUtil.delete(
                this,
                LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME,
                13
            )
        ) {
            Toast.makeText(this@MainActivity, "Deleted Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Deleted False", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addEvent() {
        btnMyLocation.setOnClickListener {
            moveToMyLocation()
            removeLegendView()
            showSettings()
        }

        btnLegend.setOnClickListener {
            if (mLegendView == null) {
                initLegendView()
            }

            addLegendView()
        }
    }

    private var settingsCount = 0

    private fun showSettings() {
        if (SettingsUtil.isDebugVersion) {
            settingsCount++

            if (settingsCount == 5) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Settings")
                builder.setMessage(LocalDatabaseUtil.readSettings().toString())
                builder.show()

                settingsCount = 0
            }
        }
    }

    private fun addLegendView() {
        if (mLegendView != null) {
            val params = initLegendLayoutParams()
            root_view.addView(mLegendView, params)
            mLegendView!!.visibility = View.INVISIBLE
            btnLegend.isClickable = false

            if (SettingsUtil.isDebugVersion) {
                mLegendView!!.setOnClickListener {
                    // show data size added
                    val agDatas = LocalDatabaseUtil.read(
                        this,
                        LocalDatabaseUtil.CACHE_AG_FILE_NAME, LocalDatabaseUtil.CACHE_AG_FILE_NAME
                    ) as List<IAGVector>

                    val locationDatas = LocalDatabaseUtil.read(
                        this,
                        LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME,
                        LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME
                    ) as List<ILocation>

                    Toast.makeText(
                        this@MainActivity,
                        "ag: ${agDatas.size}, location: ${locationDatas.size}",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (agDatas.isNotEmpty()) {
                        agDatas.forEach {
                            Log.d(TAG, it.toString())
                        }
                    }

                    if (locationDatas.isNotEmpty()) {
                        locationDatas.forEach {
                            Log.d(TAG, it.toString())
                        }
                    }
                }

                mLegendView!!.setOnLongClickListener {
                    if (deleteCount == 2) {
                        deleteLocalData()
                        deleteCount = 0
                    } else {
                        deleteCount++
                        Toast.makeText(
                            this@MainActivity,
                            "deleteCount=$deleteCount",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    true
                }
            }

            AlphaAnimation.showViewAnimation(mLegendView, object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                    mLegendView!!.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
        }
    }

    private fun removeLegendView() {
        if (mLegendView != null) {
            btnLegend.visibility = View.VISIBLE

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
        mLegendView = inflater.inflate(R.layout.legend_popup, root_view, false)

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
        initPreparingMapView()
        initNetworkChangeListener()

        (map as SupportMapFragment).getMapAsync(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mCloudDatabaseUtil = CloudDatabaseUtil()
    }

    private fun initNetworkChangeListener() {
        val snackbar = Snackbar.make(root_view, "", Snackbar.LENGTH_INDEFINITE)
        mNetworkChangeReceiver = NetworkChangeReceiver()
        mNetworkChangeReceiver.setOnNetworkChangeListener(object :
            NetworkChangeReceiver.OnNetworkChangeListener {
            override fun onNetworkOn() {
                if (snackbar.isShown) {
                    snackbar.setText("Đã kết nối lại kết nối.")
                    snackbar.setTextColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.colorConnected
                        )
                    )
                    snackbar.setDuration(Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onNetworkOff() {
                snackbar.setText("Mất kết nối.")
                snackbar.setTextColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorNoConnection
                    )
                )
                snackbar.setDuration(Snackbar.LENGTH_INDEFINITE).show()
            }
        })
    }

    private fun initPreparingMapView() {
        val inflater = LayoutInflater.from(this)
        mPreparingMapProgress = inflater.inflate(R.layout.view_preparing_map, root_view, false)
    }

    private fun addPreparingMapProgress() {
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        root_view.addView(mPreparingMapProgress, params)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        initGoogleMap()
        addPreparingMapProgress()
        moveToMyLocation()

    }

    private fun moveToMyLocation() {
        mFusedLocationClient.lastLocation.addOnSuccessListener {
            val current = LatLng(it.latitude, it.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM.toFloat()))
        }
    }

    private fun initGoogleMap() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.setOnCameraMoveListener { removeLegendView() }
        mMap.setOnMapClickListener { removeLegendView() }
        mMap.setOnMapLoadedCallback {
            onMapReady()
        }
    }

    override fun onResume() {
        super.onResume()
        mNetworkChangeReceiver.register(this)
    }

    override fun onStop() {
        super.onStop()
        mNetworkChangeReceiver.unregister(this)
    }
}