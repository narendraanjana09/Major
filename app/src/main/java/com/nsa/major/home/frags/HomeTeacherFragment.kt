package com.nsa.major.home.frags

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nsa.major.R
import com.nsa.major.databinding.FragmentHomeTeacherBinding
import com.nsa.major.home.HomeActivity
import com.nsa.major.home.adapters.ClassesAdapter
import com.nsa.major.home.bottom_sheets.ClassShareBottomSheet
import com.nsa.major.home.bottom_sheets.CreateClassBottomSheet
import com.nsa.major.home.models.ClassModel
import com.nsa.major.login.LoginActivity
import com.nsa.major.util.Constants.class_id
import com.nsa.major.util.Constants.is_teacher
import com.nsa.major.util.CustomProgressDialog
import com.nsa.major.util.SharedPref
import com.nsa.major.util.Util
import java.util.UUID

class HomeTeacherFragment : Fragment() {


    private lateinit var binding: FragmentHomeTeacherBinding
    val db = Firebase.firestore
    private lateinit var user: FirebaseUser
    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }
    private val classesList= arrayListOf<ClassModel>()
    private lateinit var classesAdapter: ClassesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_home_teacher, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        classesAdapter= ClassesAdapter(classesList,object :ClassesAdapter.ClassCallback{
            override fun onClassClicked(position: Int) {
                findNavController().navigate(
                    HomeTeacherFragmentDirections.actionHomeTeacherFragmentToTeacherClassFragment(
                        classesList[position]
                    )
                )
            }
        })
        binding.classesRv.adapter=classesAdapter

        user= Firebase.auth.currentUser!!

        binding.logoutBtn.setOnClickListener {
            Util.logout()
            showToast("Logout Success!")
            startActivity(Intent(requireActivity(),LoginActivity::class.java))
            requireActivity().finish()
        }
        binding.createClassBtn.setOnClickListener {
            val modalBottomSheet = CreateClassBottomSheet(object : CreateClassBottomSheet.ClickCallBack{
                override fun onClick(className: String, classDesc: String) {
                    var dbRef= db.collection("classes")
                    showProgress()

                    val classModel=ClassModel(
                        className = className,
                        teacherName = user.displayName,
                        classDesc = classDesc,
                        teacherId = user.uid,
                        createDate = System.currentTimeMillis(),
                        classId = UUID.randomUUID().toString()
                        )

                    dbRef.document(classModel.classId!!).set(classModel)
                        .addOnSuccessListener {
                            db.collection("teachers").document(user.uid)
                                .collection("classes")
                                .document(classModel.classId!!)
                                .set(classModel)
                                .addOnSuccessListener {

                                    val link="https://major.page.link/?link=" +
                                            "https://www.major.com/${class_id}/${classModel.classId!!}"//(/&apn=com.nsa.major&ibn=com.major.ios")
                                    val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
                                        longLink = Uri.parse(link)
                                    }.addOnSuccessListener {

                                        hideProgress()
                                        val shareBottomSheet = ClassShareBottomSheet(className,classModel.classId!!,object : ClassShareBottomSheet.ClickCallBack{
                                            override fun onClick() {

                                                val sendIntent: Intent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, "Join your $className class with this link\n\n${it.shortLink}")
                                                    type = "text/plain"
                                                }
                                                val shareIntent = Intent.createChooser(sendIntent, "Share Class")
                                                startActivity(shareIntent)
                                            }
                                        })
                                        shareBottomSheet.show(requireActivity().supportFragmentManager, "bottom")
//

                                    }.addOnFailureListener {
                                        Log.e("TAG", "create link error $it: ")
                                        showToast("Create link error")
                                        hideProgress()
                                    }
                                    getClasses()
                                }.addOnFailureListener {
                                    hideProgress()
                                    showToast("Class save error")
                                    Log.e("TAG", "Error adding document")
                                }
                            Log.e("TAG", "DocumentSnapshot added")

                        }
                        .addOnFailureListener { e ->
                            hideProgress()
                            showToast("Class save error")
                            Log.e("TAG", "Error adding document", e)
                        }

                }

            })
            modalBottomSheet.show(requireActivity().supportFragmentManager, "bottom")
        }

        if(SharedPref(requireContext()).getBoolean(is_teacher)==false){
            (requireActivity() as HomeActivity).getClassId().observe(viewLifecycleOwner){classID->
                findNavController().navigate(HomeTeacherFragmentDirections.actionHomeTeacherFragmentToHomeStudentFragment(
                    classID
                ))
            }

        }else{
            showProgress()
            getClasses()
        }


    }

    private fun getClasses() {
        db.collection("teachers").document(user.uid)
            .collection("classes")
            .get()
            .addOnSuccessListener { result ->
                hideProgress()
                if(result.isEmpty){
                    showToast("No classes")
                }else{
                    classesList.clear()
                for (document in result) {
                   val classModel= document.toObject(ClassModel::class.java)
                    classesList.add(classModel)
                }
                    classesAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting documents: ", exception)
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