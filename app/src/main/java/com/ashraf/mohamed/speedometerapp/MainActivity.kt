package com.ashraf.mohamed.speedometerapp

import android.Manifest
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private val TAG = "MainActivity"
    private val LOCATION_PERM = 124
    private var speedUpStartTime = 0L
    private var speedUpEndTime = 0L
    private var speedDownStartTime = 0L
    private var speedDownEndTime = 0L
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var isDone :Boolean by Delegates.observable(false){property, oldValue, newValue ->
        if(newValue == true){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        askForLocationPermission()

        createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (!isDone) {
                    val speedToInt = locationResult.lastLocation.speed.toInt()
                    calcSpeed(speedToInt)
                    currentSpeedId.text = speedToInt.toString()
                }
            }
        }

    }

    fun calcSpeed(speed: Int){

        if(speed == 2){
            //get current time as a start time
            speedUpStartTime = System.currentTimeMillis()
            // get current time as a finish time
            speedDownEndTime = System.currentTimeMillis()
            //check if speed is decreasing so get substract initial time with final time
            if(speedDownStartTime != 0L) {
                val speedDownTime = speedDownEndTime - speedDownStartTime
                thirtyToTenId.text = (speedDownTime/1000).toString()
                speedDownStartTime = 0L
            }
        }else if (speed >= 10){
            //check if increasing time is not zero, subtrack initial time with final time
            if(speedUpStartTime != 0L) {
                speedUpEndTime = System.currentTimeMillis()
                val speedUpTime = speedUpEndTime - speedUpStartTime
                tenToThirtyId.text = (speedUpTime/1000).toString()
                //by setting to true, activity will stop listening to updates
                speedUpStartTime = 0L
            }
            //get decreasing speed time
            speedDownStartTime = System.currentTimeMillis()
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    private fun hasLocationPermissions(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun askForLocationPermission() {
        if (hasLocationPermissions()) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    //textView.text = location?.speed.toString()
                }
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                this,
                "need permission to find your location and calc speed",
                LOCATION_PERM,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
//        val builder = LocationSettingsRequest.Builder()
//            .addLocationRequest(locationRequest)
//        val client: SettingsClient = LocationServices.getSettingsClient(this)
//        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
//
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            val yes = "Allow"
            val no = "Deny"
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(this, "onActivityResult", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    override fun onRationaleAccepted(requestCode: Int) {
    }


}
