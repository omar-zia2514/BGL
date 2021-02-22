package com.prototype.diabetescompanion

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prototype.diabetescompanion.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ctx = this

        /*    val snackbar: Snackbar = Snackbar
                .make(view, "Archived", Snackbar.LENGTH_SHORT)
                .setAction("UNDO", View.OnClickListener { })
            snackbar.show()*/
    }
}