package com.prototype.diabetescompanion

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.prototype.diabetescompanion.databinding.ActivitySplashBinding
import java.util.*

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var ctx: Context
    private val SPLASH_DELAY: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ctx = this

        val typeFace: Typeface = Typeface.createFromAsset(assets, "fonts/NeogreyMedium.otf")

        binding.apply {
            titleHalfLeft.setText("Diabetes".toUpperCase(Locale.getDefault()))
            titleHalfRight.setText(" Companion".toUpperCase(Locale.getDefault()))
            titleHalfLeft.setTypeface(typeFace)
            titleHalfRight.setTypeface(typeFace)
        }

        val handler = Handler()
        handler.postDelayed(
            { startActivity(Intent(ctx, ModeSelectActivity::class.java)) },
            SPLASH_DELAY
        )
    }
}