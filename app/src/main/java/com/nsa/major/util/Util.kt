package com.nsa.major.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.regex.Pattern


object Util {
     fun getColoredText(text: String, color: String): String{
        return "<font color=$color>$text</font>"
    }
    fun showToast(context: Context,text: String){
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show()
    }
    fun loadImage(context: Context, imageLink: String, imageView: ImageView) {
        Glide.with(context)
            .load(imageLink)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(com.google.android.material.R.drawable.mtrl_ic_error)
            .into(imageView)
    }

    fun isValidEmail(email: String?): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pat: Pattern = Pattern.compile(emailRegex)
        return if (email == null) false else pat.matcher(email).matches()
    }

    fun getScreenShot(view: View): Bitmap? {
        val view1 = view.rootView
//        View view1 = getWindow().getDecorView().getRootView();

        val bitmap = Bitmap.createBitmap(view1.getWidth(), view1.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view1.draw(canvas)

        return bitmap
    }
    fun logout(){
        Firebase.auth.signOut()
    }
    fun checkEmail(email: String): Boolean {
        return if(isValidEmail(email)){
            email.endsWith("@indoreinstitute.com")
        }else{
            false
        }
    }

    fun getDate(createDate: Long?): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        return simpleDateFormat.format(createDate)
    }
    fun getTime(date:Long):String{
        val simpleDateFormat = SimpleDateFormat("hh:mm a")
        return simpleDateFormat.format(date)
    }
}