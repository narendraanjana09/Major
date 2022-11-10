package com.nsa.major.home.frags

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nsa.major.R
import com.nsa.major.databinding.FragmentStudentsListBinding
import com.nsa.major.home.adapters.SingleTextAdapter
import com.nsa.major.home.models.ClassModel
import com.nsa.major.home.viewmodels.ClassViewModel
import com.nsa.major.util.Constants
import com.nsa.major.util.CustomProgressDialog
import com.nsa.major.util.Util
import com.nsa.major.util.Util.getDate

class StudentsListFragment : Fragment() {


    private lateinit var binding: FragmentStudentsListBinding
    val db = Firebase.firestore
    val args: StudentsListFragmentArgs by navArgs()
    private lateinit var user: FirebaseUser
    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val classesList= arrayListOf<ClassModel>()

    val classViewModel: ClassViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_students_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        user= Firebase.auth.currentUser!!

        if(args.attendancesID.equals(Constants.IS_NOT_ATTENDANCE)){
            binding.tv1.text="Students"
            getStudentsList()
        }else {
            binding.tv1.text = getDate(args.attendancesID.toLong()) + " Attendance"
            getAttendances()
        }
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




    }

    private fun getStudentsList() {

        classViewModel.getJoinedStudents(args.classID)

        classViewModel.joined_students.observe(viewLifecycleOwner){
            val list= arrayListOf<String>()
            it.forEach{ student->
                list.add(student.studentName.toString())
            }

            binding.studentsRv.adapter=SingleTextAdapter(list,false,object :SingleTextAdapter.ClickCallback{
                override fun onClicked(position: Int) {
                    showToast(it[position].studentName.toString())
                }
            })
        }
    }

    private fun getAttendances() {


        classViewModel.attendy_students_list.observe(viewLifecycleOwner){
            val list= arrayListOf<String>()
            it.forEach{ atten->
                list.add("<b>${atten.studentName.toString()}</b><br>dis:<font color=${Color.YELLOW}>${atten.distance} m</font>  " +
                        ", time:<font color=${Color.YELLOW}>${Util.getTime(atten.time!!)}</font>")
            }

            binding.studentsRv.adapter=SingleTextAdapter(list,true,object :SingleTextAdapter.ClickCallback{
                override fun onClicked(position: Int) {
                    showToast(it[position].studentName.toString())
                }
            })
        }
        classViewModel.getStudentsList(args.classID,args.attendancesID)

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