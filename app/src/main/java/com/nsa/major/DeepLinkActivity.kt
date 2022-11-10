package com.nsa.major

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.nsa.major.home.HomeActivity
import com.nsa.major.login.LoginActivity
import com.nsa.major.util.Constants

class DeepLinkActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }
                Log.e(TAG, "onNewIntent: $deepLink", )
                var classID:String?=null
                deepLink?.let { uri ->
                    val path = uri.toString().substring(deepLink.toString().lastIndexOf("/") + 1)

                    when {
                        uri.toString().contains(Constants.class_id) -> {
                            // In my case, the ID is an Integer
                            classID = path
                            Log.e("TAG", "classID1 $classID: ", )
                        }
                        else -> {

                        }
                    }
                }
                Log.e("TAG", "classID2 $classID: ", )
                if(auth.currentUser!=null){
                    val intent= Intent(this, HomeActivity::class.java)
                    if(classID!=null){
                        intent.putExtra(Constants.class_id,classID)
                    }
                    startActivity(intent)
                    finish()
                }else{
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }



            }
            .addOnFailureListener(this) { e ->
                if(auth.currentUser!=null){
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                Log.e("TAG", "getDynamicLink:onFailure", e)

            }

    }
    private val TAG="deepLinkAct"

}