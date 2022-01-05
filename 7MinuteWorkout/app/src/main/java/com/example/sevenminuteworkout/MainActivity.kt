package com.example.sevenminuteworkout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import com.example.sevenminuteworkout.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use view-binding, instead of findViewById
        // no more variables are needed for each item in xml
        /**
         * Add to build.gradle -> android
         *     buildFeatures{
         *          viewBinding true
         *     }
         */
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        // Remove for View-Binding
        // setContentView(R.layout.activity_main)

        binding?.flStart?.setOnClickListener {
            val intent = Intent(this, ExerciseActivity::class.java)
            startActivity(intent)
        }
    }

    // needed for view-binding, to avoid memory leak
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}