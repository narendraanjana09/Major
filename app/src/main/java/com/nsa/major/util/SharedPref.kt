package com.nsa.major.util

import android.content.Context
import android.content.SharedPreferences
import com.nsa.major.R


class SharedPref(val context: Context) {

    object Keys{
        val ONBOARDING_DONE="ONboarding_done"
    }

    private var sharedpreferences: SharedPreferences? = null
    init {
        sharedpreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    fun setText(key: String?, text: String?) {
        sharedpreferences!!.edit().putString(key, text).apply()
    }
    fun setBoolean(key: String?, value: Boolean?) {
        sharedpreferences!!.edit().putBoolean(key,value!!).apply()
    }

    fun getBoolean(key: String?): Boolean? {
        return sharedpreferences!!.getBoolean(key, false)
    }

    fun getText(key: String?): String? {
        return sharedpreferences!!.getString(key, "")
    }

    fun remove(key: String?) {
        sharedpreferences!!.edit().remove(key).commit()
    }

}