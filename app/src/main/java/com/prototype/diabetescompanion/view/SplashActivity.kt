package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.prototype.diabetescompanion.SharedPreferences
import com.prototype.diabetescompanion.databinding.ActivitySplashBinding
import java.util.*

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var context: Context
    private val SPLASH_DELAY: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        context = this@SplashActivity

        val typeFace: Typeface = Typeface.createFromAsset(assets, "fonts/NeogreyMedium.otf")

        binding.apply {
            titleHalfLeft.text = "Diabetes".toUpperCase(Locale.getDefault())
            titleHalfRight.text = " Companion".toUpperCase(Locale.getDefault())
            titleHalfLeft.typeface = typeFace
            titleHalfRight.typeface = typeFace
        }

        val handler = Handler()
        handler.postDelayed(
            {
                when {
                    SharedPreferences.getSignedInProfile(context) == SharedPreferences.PROFILE_DOCTOR -> startActivity(
                        Intent(context, PatientsListActivity::class.java))
                    SharedPreferences.getSignedInProfile(context) == SharedPreferences.PROFILE_PATIENT -> startActivity(
                        Intent(context, PatientActivity::class.java))
                    else -> startActivity(Intent(context, ModeSelectActivity::class.java))
                }
            },
            SPLASH_DELAY
        )
    }
}