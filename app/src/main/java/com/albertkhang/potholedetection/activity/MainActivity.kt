package com.albertkhang.potholedetection.activity

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.albertkhang.potholedetection.BuildConfig
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.animation.AlphaAnimation
import com.albertkhang.potholedetection.broadcast.NetworkChangeReceiver
import com.albertkhang.potholedetection.model.local_database.IAGVector
import com.albertkhang.potholedetection.model.local_database.ILocation
import com.albertkhang.potholedetection.util.*
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

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
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var gso: GoogleSignInOptions

    private val polylines = ArrayList<Polyline>()

    private val RC_SIGN_IN = 1

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
        getAndDrawRoad()
        objects.invoke()
    }

    private fun getAndDrawRoad() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        var username = "anonymous"

        if (account != null) {
            username = account.email.toString()
        }

        if (username == "anonymous")
            return

        mCloudDatabaseUtil.read(username) {
            if (it.isSuccessful) {
                val documents = it.result.documents

                var iri: Float
                var placeId: String

                var startMap: HashMap<*, *>
                var startLat: Double
                var startLng: Double
                var startLocation: LatLng

                var endMap: HashMap<*, *>
                var endLat: Double
                var endLng: Double
                var endLocation: LatLng

                var polyline: Polyline

                documents.forEach {
                    val data = it.data

                    if (data != null) {
//                        Log.d(TAG, "id=${it.id}, data=$data")

                        iri = (data["iri"] as Double).toFloat()
                        placeId = data["placeId"] as String

                        startMap = data["startLocation"] as HashMap<*, *>
                        startLat = startMap["latitude"] as Double
                        startLng = startMap["longitude"] as Double
                        startLocation = LatLng(startLat, startLng)

                        endMap = data["endLocation"] as HashMap<*, *>
                        endLat = endMap["latitude"] as Double
                        endLng = endMap["longitude"] as Double
                        endLocation = LatLng(endLat, endLng)

                        polyline = mMap.addPolyline(
                            PolylineOptions()
                                .add(startLocation)
                                .add(endLocation)
                        )

                        polylines.add(polyline)

                        if (iri >= 0.2) {
                            polyline.tag = "A"
                        }

                        if (iri >= 0.3) {
                            polyline.tag = "B"
                        }

                        stylePolyline(polyline)
                    }
                }
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
                LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME,
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

        btnAccount.setOnClickListener {
            signIn()
        }

        btnAccount.setOnLongClickListener {
            showConfirmSignOutDialog()
            true
        }
    }

    private fun showConfirmSignOutDialog() {
        val dialog = Dialog(this, R.style.RoundCornerDialog)
        dialog.setContentView(R.layout.dialog_two_button)
        dialog.findViewById<Button>(R.id.cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.agree)
            .setOnClickListener {
                signOut()
                dialog.dismiss()
            }
        dialog.findViewById<TextView>(R.id.title).text = "Đăng Xuất"
        dialog.findViewById<TextView>(R.id.description).text =
            "Bạn có chắc muốn đăng xuất?"
        dialog.findViewById<Button>(R.id.cancel).text = "Không"
        dialog.findViewById<Button>(R.id.agree).text = "Đăng xuất"
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Toast.makeText(this, "SignIn", Toast.LENGTH_SHORT).show()
            Log.d(
                TAG,
                "id=${account.id}, email=${account.email}, displayName=${account.displayName}"
            )

            // Signed in successfully, show authenticated UI.
            updateUI(account)

            getAndDrawRoad()

            Toast.makeText(this, "Đang lấy dữ liệu đoạn đường của bạn.", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private var settingsCount = 0

    private fun showSettings() {
        if (BuildConfig.DEBUG) {
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

            if (BuildConfig.DEBUG) {
                mLegendView!!.setOnClickListener {
                    // show data size added
                    val agDatas = LocalDatabaseUtil.read(
                        this,
                        LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME,
                        LocalDatabaseUtil.CACHE_ACCELEROMETER_FILE_NAME
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
            val averageColor = ContextCompat.getColor(this, R.color.colorAverageLegend)
            val badColor = ContextCompat.getColor(this, R.color.colorBadLegend)

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

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Log.d(
                TAG,
                "id=${account.id}, email=${account.email}, displayName=${account.displayName}"
            )
        }
        updateUI(account)
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        Log.d(TAG, "account=$account")
        if (account == null) {
            // SignIn UI
            btnAccount.setImageResource(R.drawable.ic_sign_in)
        } else {
            Glide.with(this)
                .load(account.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_white_circle)
                .into(btnAccount)
        }
    }

    private fun signIn() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun removePolylines() {
        if (polylines.isNotEmpty()) {
            Log.d(TAG, "polylines size=${polylines.size}")
            polylines.forEach {
                it.remove()
            }

            polylines.clear()
        }
    }

    private fun signOut() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            mGoogleSignInClient.signOut()
            FilterUtil.resetUsername()
            Toast.makeText(this, "SignOut", Toast.LENGTH_SHORT).show()
            updateUI(null)

            removePolylines()
        }
    }

    private fun initNetworkChangeListener() {
        val snackbar = Snackbar.make(root_view, "", Snackbar.LENGTH_INDEFINITE)
        mNetworkChangeReceiver = NetworkChangeReceiver()
        mNetworkChangeReceiver.setOnNetworkChangeListener(object :
            NetworkChangeReceiver.OnNetworkOnOffListener {
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

    private fun moveToLocation(location: LatLng) {
        mFusedLocationClient.lastLocation.addOnSuccessListener {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    location,
                    MAP_ZOOM.toFloat()
                )
            )
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
        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.style_json
            )
        )
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