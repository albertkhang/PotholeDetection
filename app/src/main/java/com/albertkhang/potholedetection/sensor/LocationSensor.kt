package com.albertkhang.potholedetection.sensor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat

abstract class LocationSensor(context: Context) : LocationListener, BaseSensor {
    private var locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val mContext: Context = context

    companion object {
        private const val TAG = "LOCATION_LISTENER"

        /**
         * Minimum Time Update
         *
         * @Unit millisecond
         */
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 1000

        /**
         * Minimum Time Update
         *
         * @Unit meter
         */
        private const val MIN_DIST_BETWEEN_UPDATES = 1f
    }

    fun start() {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                (mContext as Activity),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
        } else {
            // This provider determines location using GNSS satellites
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DIST_BETWEEN_UPDATES, this
            )

            // This provider determines location based on nearby of cell tower and WiFi access points.
            // Results are retrieved by means of a network lookup.
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DIST_BETWEEN_UPDATES, this
            )

            // A special location provider for receiving locations without actually initiating a location fix.
            locationManager.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DIST_BETWEEN_UPDATES, this
            )
        }
    }

    fun stop() {
        locationManager.removeUpdates(this)
    }

    abstract fun onUpdate(location: Location?)

    override fun onLocationChanged(location: Location) {
        onUpdate(location)
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}
}