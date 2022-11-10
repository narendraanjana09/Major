package com.nsa.major.home.bottom_sheets

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nsa.major.R
import com.nsa.major.databinding.CreateClassBottomSheetBinding
import com.nsa.major.util.Util


class CreateClassBottomSheet(private val clickCallBack: ClickCallBack
                      ) : BottomSheetDialogFragment() {

    interface ClickCallBack{
        fun onClick(className:String,classDesc:String)
    }

    private lateinit var binding: CreateClassBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater, R.layout.create_class_bottom_sheet,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.descEd.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                var length:Int
                if(p0==null){
                    length=0
                }else{
                    length=p0.length
                }
                binding.descTextCountTv.text="$length/150"
            }
        })
        binding.okBtn.setOnClickListener {
            val name=binding.nameEd.text.toString().trim()
            val desc=binding.descEd.text.toString().trim()
            if(name.length<3){
                Util.showToast(requireContext(),"Class name can't be less than 3 chars");
            }else{
                clickCallBack.onClick(name,desc)
                dismiss()
            }
        }

    }


}