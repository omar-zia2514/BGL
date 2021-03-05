package com.prototype.diabetescompanion

import android.app.Activity
import android.content.Context

class SharedPreferences {
    companion object {
        private const val SPREF_NAME = "Fofkfsdx"
        private const val signedInProfile = "dsdjkdsk"
        const val PROFILE_DOCTOR = 0
        const val PROFILE_PATIENT = 1


        fun setSignedInProfile(ctx: Context, profile: Int) {
            val sEditor =
                ctx.getSharedPreferences(SPREF_NAME,
                    Activity.MODE_PRIVATE).edit()
            sEditor.putInt(signedInProfile,
                profile)
            sEditor.apply()
        }

        fun getSignedInProfile(ctx: Context): Int {
            val sPref =
                ctx.getSharedPreferences(SPREF_NAME,
                    Activity.MODE_PRIVATE)
            return sPref.getInt(signedInProfile,
                -1)
        }
    }
}