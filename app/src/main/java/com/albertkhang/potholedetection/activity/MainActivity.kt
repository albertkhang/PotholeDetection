package com.albertkhang.potholedetection.activity

import android.animation.Animator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.animation.AlphaAnimation
import com.albertkhang.potholedetection.broadcast.NetworkChangeReceiver
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.IDatabase
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.database.ILocation
import com.albertkhang.potholedetection.sensor.AccelerometerSensor
import com.albertkhang.potholedetection.sensor.LocationSensor
import com.albertkhang.potholedetection.util.CloudDatabaseUtil
import com.albertkhang.potholedetection.util.DisplayUtil
import com.albertkhang.potholedetection.util.LocalDatabaseUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
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

    private lateinit var mAccelerometerSensor: AccelerometerSensor
    private lateinit var mLocationSensor: LocationSensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addControl()
        addEvent()

        // read all data from cloud db
//        readAll()

        mAccelerometerSensor = object : AccelerometerSensor(this@MainActivity) {
            override fun onUpdate(accelerometer: IVector3D?, gravity: IVector3D?) {
                if (accelerometer != null && gravity != null) {
                    val data = IAGVector(accelerometer, gravity)
                    data.timestamps = System.currentTimeMillis()

//                    LocalDatabaseUtil.add(LocalDatabaseUtil.AG_VECTOR_BOOK, data)
                }
            }

        }

        mLocationSensor = object : LocationSensor(this@MainActivity) {
            override fun onUpdate(location: Location?) {
                if (location !== null) {
                    val data = ILocation(location)
                    data.timestamps = System.currentTimeMillis()

//                    LocalDatabaseUtil.add(LocalDatabaseUtil.LOCATION_BOOK, data)
                }
            }

        }

        btnAddSensor.setOnClickListener {
//            writeData(agVectorDb)
//            writeData(locationDb)

//            LocalDatabaseUtil.add(LocalDatabaseUtil.AG_VECTOR_BOOK, agVectorDb)
//            LocalDatabaseUtil.add(LocalDatabaseUtil.LOCATION_BOOK, locationDb)
//            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//
//            val agDatas = LocalDatabaseUtil.readData(LocalDatabaseUtil.AG_VECTOR_BOOK, hour)
//            val locationDatas = LocalDatabaseUtil.readData(LocalDatabaseUtil.LOCATION_BOOK, hour)
//
//            Log.d(TAG, "agData size: ${agDatas!!.size}")
//            Log.d(TAG, "locationData: ${locationDatas!!.size}")

//            LocalDatabaseUtil.filter(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        }

//        btnAddSensor.setOnLongClickListener {
//            LocalDatabaseUtil.deleteData(
//                LocalDatabaseUtil.AG_VECTOR_BOOK,
//                Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//            )
//
//            LocalDatabaseUtil.deleteData(
//                LocalDatabaseUtil.LOCATION_BOOK,
//                Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//            )
//
//            Toast.makeText(this@MainActivity, "Deleted", Toast.LENGTH_SHORT).show()
//
//            true
//        }
    }

    private fun writeData(data: IDatabase) {
        mCloudDatabaseUtil.write(data) { documentReference ->
            if (documentReference.isComplete) {
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.result.id}")
                Toast.makeText(this@MainActivity, "Add Success", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "Error adding document", documentReference.exception)
                Toast.makeText(this@MainActivity, "Add Failure", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readAll() {
        mCloudDatabaseUtil.readAll(
            CloudDatabaseUtil.COLLECTION_AG_VECTOR
        ) { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            } else {
                Log.e(TAG, "Error getting documents.", task.exception)
            }
        }

        mCloudDatabaseUtil.readAll(
            CloudDatabaseUtil.COLLECTION_LOCATION
        ) { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            } else {
                Log.w(TAG, "Error getting documents.", task.exception)
            }
        }
    }

    private fun addEvent() {
        btnMyLocation.setOnClickListener {
            moveToMyLocation()
            removeLegendView()
        }

        btnLegend.setOnClickListener {
            if (mLegendView == null) {
                initLegendView()
            }

            addLegendView()
        }
    }

    private fun addLegendView() {
        if (mLegendView != null) {
            val params = initLegendLayoutParams()
            root_view.addView(mLegendView, params)
            mLegendView!!.visibility = View.INVISIBLE
            btnLegend.isClickable = false

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
                    snackbar.setTextColor(Color.parseColor("#2ECC71"))
                    snackbar.setDuration(Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onNetworkOff() {
                snackbar.setText("Mất kết nối.")
                snackbar.setTextColor(Color.parseColor("#E74C3C"))
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM!!.toFloat()))
        }
    }

    private fun initGoogleMap() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.setOnCameraMoveListener { removeLegendView() }
        mMap.setOnMapClickListener { removeLegendView() }
        mMap.setOnMapLoadedCallback {
            root_view.removeView(mPreparingMapProgress)
        }
    }

    override fun onResume() {
        super.onResume()
        mNetworkChangeReceiver.register(this)
        mAccelerometerSensor.start()
        mLocationSensor.start()
    }

    override fun onStop() {
        super.onStop()
        mNetworkChangeReceiver.unregister(this)
        mAccelerometerSensor.stop()
        mLocationSensor.stop()
    }
}