package com.renalize.android.util

import android.content.Context
import android.content.SharedPreferences
import com.renalize.android.util.Constants.PREFS_TOKEN_FILE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {

    private var prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_TOKEN_FILE, Context.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun putString(key: String, value: String) {
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String {
        return prefs.getString(key, null) ?: ""
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    object Keys {
        const val IS_LOGGED_IN = "logged_in"
        const val USER_TOKEN = "user_token"
        const val MOBILE_NUMBER = "mobile_number"

        const val AADHAR_NUMBER = "aadhar_number"
        const val AADHAR_NAME = "aadhar_name"
        const val DOB = "aadhar_dob"
        const val GENDER = "gender"
        const val ADDRESS_STREET = "address_street"
        const val ADDRESS_CITY = "address_city"
        const val ADDRESS_STATE = "address_state"
        const val ADDRESS_PINCODE = "address_pincode"

        const val PAN_NUMBER = "pan_number"
        const val PAN_NAME = "pan_name"

        const val ACCOUNT_NUMBER = "account_number"
        const val IFSC_CODE = "ifsc_code"
        const val BANK_NAME = "bank_name"
        const val BRANCH_NAME = "branch_name"
        const val ACCOUNT_HOLDER_NAME = "account_holder_name"

        const val UHID = "uhid"
    }
}

