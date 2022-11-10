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
import com.nsa.major.databinding.NormalTextBottomSheetBinding
import com.nsa.major.databinding.TakeAttendanceBottomSheetBinding
import com.nsa.major.util.Util


class NormalTextBottomSheet(
    val title:String,
    val message:String,
    val clickCallBack: ClickCallBack
    ) : BottomSheetDialogFragment() {

    interface ClickCallBack{
        fun onClick()
    }

    private lateinit var binding: NormalTextBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater, R.layout.normal_text_bottom_sheet,container,false)
        return binding.root
    }
    private fun showToast(message:String){
        Util.showToast(requireContext(),message)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
     binding.title.text=title
        binding.messageTv.text=message
        binding.okBtn.setOnClickListener {
            dismiss()
        }

    }


}