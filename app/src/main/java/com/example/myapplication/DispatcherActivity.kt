package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
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

class DispatcherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDispatcherBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@DispatcherActivity, R.layout.activity_dispatcher)

        binding.singleThreadButton.setOnClickListener {
            singleThread()
        }

        binding.multiThreadButton.setOnClickListener {
            multiThread()
        }

        binding.multiThreadChildButton.setOnClickListener {
            multiThreadChild()
        }

        binding.dispatchersIOButton.setOnClickListener {
            dispatchersIO()
        }

        binding.dispatchersDefaultButton.setOnClickListener {
            dispatchersDefault()
        }

        binding.dispatchersDefaultLimitedButton.setOnClickListener {
            dispatchersDefaultLimited()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun singleThread() = runBlocking {
        val dispatcher: CoroutineDispatcher = newSingleThreadContext("SingleThread")
        launch(dispatcher) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun multiThread() = runBlocking {
        val dispatcher: CoroutineDispatcher = newFixedThreadPoolContext(2, "MultiThread")
        launch(dispatcher) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
        }
        launch(dispatcher) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun multiThreadChild() = runBlocking {
        val dispatcher: CoroutineDispatcher = newFixedThreadPoolContext(2, "MultiThread")
        launch(dispatcher) {
            Log.i("hsjeong", "${Thread.currentThread().name} 부모 실행")
            launch(dispatcher) {
                Log.i("hsjeong", "${Thread.currentThread().name} 자식 실행")
            }
            launch(dispatcher) {
                Log.i("hsjeong", "${Thread.currentThread().name} 자식 실행")
            }
        }
    }

    private fun dispatchersIO() = runBlocking {
        launch(Dispatchers.IO) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
        }
    }

    private fun dispatchersDefault() = runBlocking {
        launch(Dispatchers.Default) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun dispatchersDefaultLimited() = runBlocking {
        launch(Dispatchers.Default.limitedParallelism(2)) {
            repeat(10) {
                launch {
                    Log.i("hsjeong", "${Thread.currentThread().name} 실행")
                }
            }
        }
    }
}