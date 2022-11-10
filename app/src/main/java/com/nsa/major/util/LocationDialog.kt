package com.nsa.major.util

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.*
import com.nsa.major.databinding.ProgressDialogViewBinding
import com.nsa.major.home.bottom_sheets.TakeAttendanceBottomSheet
import java.util.concurrent.TimeUnit

class LocationDialog(
    val locationListener: LocationListener
): DialogFragment() {

    interface LocationListener{
        fun gotLocation(location: Location)
        fun message(message:String)
    }



    private lateinit var binding: ProgressDialogViewBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult != null) {
                super.onLocationResult(locationResult)
            }
            Log.e("Locating", "got location: ", )
            locationResult?.lastLocation?.let {
                Log.e("Locating", "got location: $it", )
                val currentLocation = it
                removeLocationUpdates()
                locationListener.gotLocation(currentLocation)
                dismiss()
                // use latitude and longitude as per your need
            } ?: run {
                removeLocationUpdates()
                dismiss()
                Log.e("TAG", "Location information isn't available.")
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProgressDialogViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("Locating", "getting location: ", )
        binding.cpTitle.text="Getting Location..."

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationRequest = LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(2)
            fastestInterval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(5)
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val displayHeight = displayMetrics.heightPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog!!.window!!.attributes)
        layoutParams.width = (displayWidth * 1)
        layoutParams.height = (displayHeight * 1)
        dialog!!.window!!.attributes = layoutParams
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {

            } else -> {
                locationListener.message("Permission not granted")
            dismiss()
            // No location access granted.
        }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Log.e("TAG", "getCurrentLocation: ", )
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }
    private fun removeLocationUpdates(){

        val removeTask = fusedLocationClient.removeLocationUpdates(locationCallback)
        removeTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.e("TAG", "Location Callback removed.")
            } else {
                Log.e("TAG", "Failed to remove Location Callback.")
            }
        }
    }


}