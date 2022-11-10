package com.nsa.major.home.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nsa.major.R
import com.nsa.major.databinding.ClassShareBottomSheetBinding


class ClassShareBottomSheet(
    private val className:String,
    private val classId:String,
    private val clickCallBack: ClickCallBack
                      ) : BottomSheetDialogFragment() {

    interface ClickCallBack{
        fun onClick()
    }

    private lateinit var binding: ClassShareBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater, R.layout.class_share_bottom_sheet,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.classNameTv.text=className

        binding.shareBtn.setOnClickListener {
            clickCallBack.onClick()
            dismiss()
        }


    }


}