package com.douglas.postalcodes.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.douglas.postalcodes.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
        setContentView(R.layout.activity_main)
    }
}
