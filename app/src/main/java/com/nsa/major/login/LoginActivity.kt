package com.nsa.major.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nsa.major.R
import com.nsa.major.databinding.ActivityLoginBinding
import com.nsa.major.home.HomeActivity
import com.nsa.major.util.CustomProgressDialog

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = Firebase.auth

        if(auth.currentUser!=null){
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

    }


}