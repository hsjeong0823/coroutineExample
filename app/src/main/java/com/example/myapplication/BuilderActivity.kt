package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityBuilderBinding
import com.example.myapplication.databinding.ActivityDispatcherBinding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

/**
 * Copyright ⓒ 2024 Starbucks Coffee Company. All Rights Reserved.| Confidential
 *
 * @ Description :
 * @ Class : DispatcherActivity
 * @ Created by : jeonghwasu
 * @ Created Date : 2024. 08. 19.
 */

class BuilderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuilderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@BuilderActivity, R.layout.activity_builder)

        binding.singleThreadButton.setOnClickListener {
            singleThread()
        }

    }

    private fun singleThread() = runBlocking {
        val dispatcher: CoroutineDispatcher = newSingleThreadContext("SingleThread")
        launch(dispatcher) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
        }
    }

}