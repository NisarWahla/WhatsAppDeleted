package dzm.wamr.recover.deleted.messages.photo.media.util

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import java.lang.reflect.Type

class PrefManager(val context: Context) {
    var pref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    // Shared preferences file name
    private val PREF_NAME = "androidhive-welcome"

    private val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    private val WA_STATUS = "WA_STATUS_"
    // shared pref mode
    var PRIVATE_MODE = 0


    init {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref?.edit()
    }


    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        editor?.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
        editor?.commit()
    }

    fun isFirstTimeLaunch(): Boolean {
        return pref!!.getBoolean(IS_FIRST_TIME_LAUNCH, true)
    }

    fun getWATree(): String? {
        return pref!!.getString(WA_STATUS, "")
    }

    fun setWATree(uriPath: String) {
        editor?.putString(WA_STATUS, uriPath)
        editor?.commit()
    }

    fun setAcceptTermOfUse(isFirstTime: Boolean, key: String) {
        editor?.putBoolean(key, isFirstTime)
        editor?.commit()
    }

    fun getAcceptTermOfUse(key: String): Boolean {
        return pref!!.getBoolean(key, false)
    }


    fun getStatusList(context: Context,key: String?,defValue: String?) :ArrayList<Status>{
        val list: ArrayList<Status> = ArrayList()
        val gson = Gson()
        val json: String? = PrefManager(context).pref?.getString(key, "")
        if (json!!.isEmpty()) {
            Utils.showToast(context,context.getString(R.string.there_is_something_error))
        } else {
            val type: Type = object : TypeToken<List<Status?>?>() {}.type
            val arrPackageData = gson.fromJson<List<Status>>(json, type)
            for (data in arrPackageData) {
                list.add(data)
            }
        }

        return list
    }

    fun setStatusList(context: Context,key: String?,list: ArrayList<Status>){
        val gson = Gson()
        val json = gson.toJson(list)
        PrefManager(context).editor?.putString(key,json)
    }
}