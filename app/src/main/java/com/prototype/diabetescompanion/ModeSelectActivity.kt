package com.prototype.diabetescompanion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.prototype.diabetescompanion.databinding.ActivityModeSelectBinding

class ModeSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModeSelectBinding
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityModeSelectBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ctx = this

        binding.apply {
            btnDoctor.setOnClickListener {
                Handler().post {
                    startActivity(
                        Intent(
                            ctx,
                            PatientsListActivity::class.java
                        )
                    )
                }
            }
        }
    }


}