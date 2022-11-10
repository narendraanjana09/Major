package com.nsa.major.home.frags

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nsa.major.R
import com.nsa.major.databinding.FragmentHomeStudentBinding
import com.nsa.major.home.adapters.ClassesAdapter
import com.nsa.major.home.bottom_sheets.JoinClassBottomSheet
import com.nsa.major.home.viewmodels.StudentHomeViewModel
import com.nsa.major.login.LoginActivity
import com.nsa.major.util.Constants
import com.nsa.major.util.CustomProgressDialog
import com.nsa.major.util.Util


class HomeStudentFragment : Fragment() {


    private lateinit var binding: FragmentHomeStudentBinding
    val args: HomeStudentFragmentArgs by navArgs()
    val db = Firebase.firestore
    private lateinit var user: FirebaseUser
    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }
    private var classExist=false
    private var argsChecked=false//for that back progress issue

    val homeViewModel: StudentHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_home_student, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user= Firebase.auth.currentUser!!



        binding.logoutBtn.setOnClickListener {
            Util.logout()
            showToast("Logout Success!")
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(Constants.CLASS_LEAVE)
            ?.observe(viewLifecycleOwner) {
                if(it){
                    homeViewModel.loadClasses()
                }
            }

        homeViewModel.getProgress().observe(viewLifecycleOwner){
            if(it){
                showProgress()
            }else{
                hideProgress()
            }
        }
        homeViewModel.getMessage().observe(viewLifecycleOwner){
            if(!it.isNullOrEmpty()){
               showToast(it)
                homeViewModel.getMessage().value=""
            }
        }
        homeViewModel.getClasses().observe(viewLifecycleOwner){
            binding.classesRv.adapter=ClassesAdapter(it,object :ClassesAdapter.ClassCallback{
                override fun onClassClicked(position: Int) {
                    findNavController().navigate(
                        HomeStudentFragmentDirections.actionHomeStudentFragmentToStudentClassFragment(
                            it[position]
                        )
                    )
                }
            })
        }
        getExtras()
    }

    private fun getExtras() {
        val classID = args.classID
        if(argsChecked){
            return
        }
        argsChecked=true

        if(classID!="null"){
            homeViewModel.checkClass(classID)
            homeViewModel.classExist.observe(viewLifecycleOwner){
                classExist=it
                Log.e("TAG", "classExist: $it ", )
            }
            homeViewModel.classModel.observe(viewLifecycleOwner){
                Log.e("TAG", "classModel: $it  ", )
                if(classExist){
                    findNavController().navigate(
                        HomeStudentFragmentDirections.actionHomeStudentFragmentToStudentClassFragment(
                            it
                        )
                    )
                }else{
                    val joinClassBottomSheet=JoinClassBottomSheet(classModel = it,object :JoinClassBottomSheet.ClickCallBack{
                        override fun onClick() {
                            homeViewModel.joinClass(it)
                        }
                    })
                    joinClassBottomSheet.show(childFragmentManager,"joinclassfr")
                }
            }
        }

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