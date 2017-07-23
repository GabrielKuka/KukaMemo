package com.katana.memo.memo.Activities

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.katana.memo.memo.R
import kotlinx.android.synthetic.main.location_activity.*
import java.io.IOException
import java.util.*


class Location : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener, LocationListener, GoogleMap.OnMapClickListener {


    override fun onClick(v: View?) {
        val intent = Intent(this, CreateNoteActivity::class.java)
        intent.putExtra("Long", currentLongtitude)
        intent.putExtra("Lat", currentLatitude)
        intent.putExtra("streetAddress", currentStreetAddress)
        setResult(2, intent)
        finish()
    }

    lateinit private var mMap: GoogleMap
    private var mCameraPosition: CameraPosition? = null
    private var mLastKnownLocation: Location? = null
    private var mLocationPermissionGranted = false

    private var currentLatitude: Double = 0.0
    private var currentLongtitude: Double = 0.0
    private var currentStreetAddress: String = ""

    companion object {
        private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
        private val DEFAULT_ZOOM: Int = 15

        private val KEY_CAMERA_POSITION: String = "camera_position"
        private val KEY_LOCATION: String = "location"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_activity)


        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (savedInstanceState != null) {
                mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
                mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
            }

            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)


            saveLocationButton.setOnClickListener(this)

        } else {
            showGPSDisabledAlertToUser()

        }


    }

    override fun onMapClick(latLng: LatLng) {

        val markerOptions: MarkerOptions = MarkerOptions()

        markerOptions.position(latLng)

        mMap.clear()

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.addMarker(markerOptions)

        currentLongtitude = latLng.longitude
        currentLatitude = latLng.latitude

        getStreetAddress()
    }

    fun getStreetAddress() {
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())

        try {
            val addresses: List<Address> = geocoder.getFromLocation(currentLatitude, currentLongtitude, 1)

            val returnedAddress: Address = addresses.get(0)
            val strReturnedAddress: StringBuilder = StringBuilder()
            for (i: Int in 0..returnedAddress.maxAddressLineIndex - 1) {
                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("")
            }
            currentStreetAddress = strReturnedAddress.toString()

        } catch (e: IOException) {
            e.printStackTrace()
            currentStreetAddress = "Canont get Address!"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {


        mMap = googleMap

        mMap.setOnMapClickListener(this)

        // Get the current location     xof the device and set the position of the map.
        getDeviceLocation()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()


    }


    private fun updateLocationUI() {

        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (canAccessLocation()) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        if (mLocationPermissionGranted) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        } else {
            mMap.isMyLocationEnabled = false
            mMap.uiSettings.isMyLocationButtonEnabled = false
            mLastKnownLocation = null
        }
    }


    private fun getDeviceLocation() {
        /*
     * Before getting the device location, you must check location
     * permission, as described earlier in the tutorial. Then:
     * Get the best and most recent location of the device, which may be
     * null in rare cases when a location is not available.
     */

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition))


        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(mLastKnownLocation?.latitude!!,
                            mLastKnownLocation?.longitude!!), DEFAULT_ZOOM.toFloat()))

        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM.toFloat()))
            mMap.uiSettings.isMyLocationButtonEnabled = false
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.cameraPosition)
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation)
            super.onSaveInstanceState(outState)
        }
    }

    fun canAccessLocation(): Boolean {
        return (hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION))
    }


    private fun hasPermission(perm: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm)
        } else {
            return false
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        mMap.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)))
        currentLatitude = location.latitude
        currentLongtitude = location.longitude
    }

    private fun showGPSDisabledAlertToUser() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        DialogInterface.OnClickListener { dialog, id ->
                            val callGPSSettingIntent = Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivity(callGPSSettingIntent)
                        })
        alertDialogBuilder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    run {
                        dialog.cancel()
                        this.finish()
                    }
                })
        val alert = alertDialogBuilder.create()
        alert.show()
    }

}




