package com.example.countryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint



@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

        if (currentFragment is ChooseCountryFragment) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            finish()
        } else {
            super.onBackPressed()
        }
    }
}

