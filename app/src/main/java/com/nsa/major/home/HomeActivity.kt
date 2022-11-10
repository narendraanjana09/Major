package com.nsa.major.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nsa.major.R
import com.nsa.major.databinding.ActivityHomeBinding
import com.nsa.major.util.Constants
import com.nsa.major.util.SharedPref
import com.nsa.major.util.Util

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private var classID=MutableLiveData<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_home)
        auth = Firebase.auth
        classID.value = intent.getStringExtra(Constants.class_id).toString()
    }
    fun getClassId(): MutableLiveData<String> {
        return classID
    }
    private fun showToast(message:String){
        Util.showToast(this,message)
    }

}
