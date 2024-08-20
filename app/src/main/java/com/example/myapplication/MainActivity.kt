package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)

        binding.dispatcherButton.setOnClickListener {
            startActivity(Intent(this, DispatcherActivity::class.java))
        }

        binding.builderButton.setOnClickListener {
            startActivity(Intent(this, BuilderActivity::class.java))
        }

        binding.asyncButton.setOnClickListener {
            startActivity(Intent(this, AsyncAndDeferredActivity::class.java))
        }

        binding.contextButton.setOnClickListener {
            startActivity(Intent(this, CoroutineContextActivity::class.java))
        }

        binding.structuredConcurrencyButton.setOnClickListener {
            startActivity(Intent(this, StructuredConcurrencyActivity::class.java))
        }
    }
}