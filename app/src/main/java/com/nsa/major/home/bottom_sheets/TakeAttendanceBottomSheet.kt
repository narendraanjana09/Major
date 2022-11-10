package com.nsa.major.home.bottom_sheets

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nsa.major.R
import com.nsa.major.databinding.TakeAttendanceBottomSheetBinding
import com.nsa.major.util.Util


class TakeAttendanceBottomSheet(
    val currentLocation: Location,
    val clickCallBack: ClickCallBack
    ) : BottomSheetDialogFragment() {

    interface ClickCallBack{
        fun onClick(distance:Double,timer:Int)
    }

    private lateinit var binding: TakeAttendanceBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater, R.layout.take_attendance_bottom_sheet,container,false)
        return binding.root
    }
    private fun showToast(message:String){
        Util.showToast(requireContext(),message)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.locationTxt.text="Your Location is ${currentLocation.latitude},${currentLocation.longitude}."
        binding.viewMapBtn.setOnClickListener {
            val urlAddress =
                "http://maps.google.com/maps?q=" + currentLocation.latitude + "," + currentLocation.longitude + "(Your Location)&iwloc=A&hl=es"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
            startActivity(intent)
        }
        binding.startBtn.setOnClickListener {
            val minDistance=binding.distanceEd.text.toString()
            val timer=binding.timerEd.text.toString()
            if(minDistance.isEmpty() || minDistance.toDouble()<20){
                showToast("Min Distance can't be less than 20 meters")
                return@setOnClickListener
            }
            if(timer.isEmpty() || timer.toInt()>30){
                showToast("Timer can't be more than 30 minutes")
                return@setOnClickListener
            }
            clickCallBack.onClick(minDistance.toDouble(),timer.toInt())
            dismiss()
        }

    }


}