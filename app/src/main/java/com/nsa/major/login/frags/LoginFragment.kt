package com.nsa.major.login.frags

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nsa.major.R
import com.nsa.major.databinding.FragmentLoginBinding
import com.nsa.major.home.HomeActivity
import com.nsa.major.home.bottom_sheets.NormalTextBottomSheet
import com.nsa.major.login.models.UserRegisterModel
import com.nsa.major.util.Constants
import com.nsa.major.util.CustomProgressDialog
import com.nsa.major.util.SharedPref
import com.nsa.major.util.Util


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding


    private lateinit var auth: FirebaseAuth
    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }

    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_login, container, false)
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth


        binding.switchBtn.setOnCheckedChangeListener(object :CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                if(p1){
                    binding.nameLayout.visibility=View.VISIBLE
                    binding.loginTeacherCheckLayout.visibility=View.VISIBLE
                    binding.switchBtn.text="Sign Up"
                    binding.loginBtn.text="Sign Up"
                }else{
                    binding.nameLayout.visibility=View.GONE
                    binding.loginTeacherCheckLayout.visibility=View.GONE
                    binding.switchBtn.text="Login"
                    binding.loginBtn.text="Login"
                }
            }
        })


        binding.loginBtn.setOnClickListener {


            showProgress()
            var name=binding.nameEd.text.toString().trim()
            val email=binding.emailEd.text.toString().trim()
            val password=binding.pwdEd.text.toString().trim()
            if(!binding.switchBtn.isChecked){
                name="dbfxvfjbjsy"
            }
            if(validate(name,email,password)){
                if(binding.switchBtn.isChecked){
                    startSignUpProcess(name,email,password)
                }else{
                    startWithSignIn(email,password)
                }
            }else{
                hideProgress()
            }


            // findNavController().navigate(R.id.action_signUpFragment_to_detailsFragment)
        }

    }
    private fun startWithSignIn( email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task->

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.e("TAG", "signInWithEmail:success")
                    val user = auth.currentUser
                    if(user!!.isEmailVerified){
                        showToast("Sign In Successfull.")
                        checkIsTeacher(user)
                    }else{
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener {

                                val normalTextBottomSheet=NormalTextBottomSheet(
                                    "Email isn't verified!"
                                ,"You haven't verified your email yet, we have sent a verification link to your email"+
                                            " please verify to continue.(please also check your spam folder)"
                                ,object :NormalTextBottomSheet.ClickCallBack{
                                        override fun onClick() {

                                        }
                                    })
                                normalTextBottomSheet.show(childFragmentManager,"sdbcjd")

                                hideProgress()
                                auth.signOut()
                            }
                    }
                    hideProgress()
                    //  updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e("TAG", "signInWithEmail:failure", task.exception)
                    val normalTextBottomSheet=NormalTextBottomSheet(
                        "Login Failed!"
                        ,"We couldn't found a account with this email. If you are a new user try signup(click on switch at top)"
                        ,object :NormalTextBottomSheet.ClickCallBack{
                            override fun onClick() {

                            }
                        })
                    normalTextBottomSheet.show(childFragmentManager,"sdbcjdsfd")
                    hideProgress()

                }
            }


    }

    private fun checkIsTeacher(user: FirebaseUser) {

        db.collection("teachers").document(user.uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if(document != null) {
                    SharedPref(requireContext()).setBoolean(Constants.is_teacher,document.exists())
                    hideProgress()
                    startActivity(Intent(requireActivity(),HomeActivity::class.java))
                    requireActivity().finish()
                }
            } else {
                hideProgress()
                showToast("error")
                Log.d("TAG", "Error: ", task.exception)
            }
        }

    }

    private fun startSignUpProcess(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {task->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.e("TAG", "createUserWithEmail:success")
                    val user = auth.currentUser

                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }

                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                var dbRef: CollectionReference? =null
                                if(binding.teacherCheckBox.isChecked){
                                    dbRef= db.collection("teachers")
                                }else{
                                    dbRef= db.collection("students")
                                }
                                dbRef.document(user.uid).set(UserRegisterModel(name,user.uid,email,password))
                                    .addOnSuccessListener {

                                        user?.sendEmailVerification()
                                            ?.addOnCompleteListener {

                                                val normalTextBottomSheet=NormalTextBottomSheet(
                                                    "Registered Successfully!"
                                                    ,"We have sent a verification link to your email"+
                                                            " please verify to continue.(please also check your spam folder)"
                                                    ,object :NormalTextBottomSheet.ClickCallBack{
                                                        override fun onClick() {

                                                        }
                                                    })
                                                normalTextBottomSheet.show(childFragmentManager,"sdbcjefsd")


                                                hideProgress()
                                                auth.signOut()
                                            }


                                        Log.e("TAG", "DocumentSnapshot added")
                                    }
                                    .addOnFailureListener { e ->
                                        hideProgress()
                                        showToast("Data save error")
                                        auth.signOut()
                                        Log.e("TAG", "Error adding document", e)
                                    }



                                Log.e("TAG", "User profile updated.")
                            }else{
                                showToast("Profile update error")
                            }
                        }


                } else {
                    // If sign in fails, display a message to the user.
                    hideProgress()
                    Log.e("TAG", "createUserWithEmail:failure"+task.exception!!.message.toString())
                    val normalTextBottomSheet=NormalTextBottomSheet(
                        "Registration Failed!"
                        ,"${task.exception!!.message.toString()}"
                        ,object :NormalTextBottomSheet.ClickCallBack{
                            override fun onClick() {

                            }
                        })
                    normalTextBottomSheet.show(childFragmentManager,"sdbcjefsd")


                }

            }

    }
    private fun showSnack(message: String){
        val snackbar= Snackbar.make(binding.root,
            message, Snackbar.LENGTH_INDEFINITE)
        snackbar.setTextColor(resources.getColor(R.color.black))
        snackbar.setActionTextColor(resources.getColor(R.color.black))
        snackbar.setAction("Ok"){
            snackbar.dismiss()
        }
        snackbar.setBackgroundTint(resources.getColor(R.color.white_light))
        snackbar.duration=7000
        snackbar.show()
    }


    private fun validate(name: String, email: String, password: String): Boolean {
        if(name.isEmpty()){
            showToast("Name can't be empty")
            return false
        }

        if(!Util.checkEmail(email)){
            showToast("Email is not valid")
            return false
        }

        if(password.length<6){
            showToast("Password must contain 6 characters")
            return false
        }

        return true

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