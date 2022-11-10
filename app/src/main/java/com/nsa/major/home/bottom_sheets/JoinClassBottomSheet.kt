package com.nsa.major.home.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nsa.major.R
import com.nsa.major.databinding.JoinClassBottomSheetBinding
import com.nsa.major.home.models.ClassModel
import com.nsa.major.util.Util


class JoinClassBottomSheet(
    private val classModel: ClassModel,
    private val clickCallBack: ClickCallBack
                      ) : BottomSheetDialogFragment() {

    interface ClickCallBack{
        fun onClick()
    }

    private lateinit var binding: JoinClassBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater, R.layout.join_class_bottom_sheet,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.okBtn.setOnClickListener {
            clickCallBack.onClick()
            dismiss()
        }

        binding.desTxt.text="Class Name :- ${classModel.className}\nTeacher Name :- ${classModel.teacherName}\nFormed date :- ${Util.getDate(classModel.createDate)}"


    }


}