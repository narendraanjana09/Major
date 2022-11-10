package com.nsa.major.home.frags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nsa.major.R
import com.nsa.major.databinding.FragmentViewAttendanceBinding
import com.nsa.major.home.adapters.SingleTextAdapter
import com.nsa.major.home.models.ClassModel
import com.nsa.major.home.viewmodels.ClassViewModel
import com.nsa.major.util.CustomProgressDialog
import com.nsa.major.util.Util
import com.nsa.major.util.Util.getDate

class ViewAttendanceFragment : Fragment() {


    private lateinit var binding: FragmentViewAttendanceBinding
    val db = Firebase.firestore
    val args: ViewAttendanceFragmentArgs by navArgs()
    private lateinit var user: FirebaseUser
    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val classesList= arrayListOf<ClassModel>()
    val classViewModel: ClassViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_view_attendance, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        user= Firebase.auth.currentUser!!
        getAttendances()



    }

    private fun getAttendances() {
        classViewModel.getMessage().observe(viewLifecycleOwner){
            showToast(it)
        }
        classViewModel.getProgress().observe(viewLifecycleOwner){
            if(it){
                showProgress()
            }else{
                hideProgress()
            }
        }
        classViewModel.attendances_list.observe(viewLifecycleOwner){
            val list= arrayListOf<String>()
            it.forEach{ date->
                list.add(getDate(date.toLong()))
            }

            binding.attendancesRv.adapter=SingleTextAdapter(list,false,object :SingleTextAdapter.ClickCallback{
                override fun onClicked(position: Int) {
                    findNavController().navigate(
                        ViewAttendanceFragmentDirections.actionViewAttendanceFragmentToStudentsListFragment(
                            args.classID,
                            it[position]
                        )
                    )
                }
            })
        }
        classViewModel.getAttendancesList(args.classID)

    }

    private fun showToast(message:String){
        Util.showToast(requireContext(),message)
    }
    private fun showProgress(){
        progressDialog.start()
    }
    private fun hideProgress(){
        progressDialog.stop()
    }


}