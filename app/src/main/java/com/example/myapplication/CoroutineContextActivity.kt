package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityCoroutineContextBinding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class CoroutineContextActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCoroutineContextBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@CoroutineContextActivity, R.layout.activity_coroutine_context)

        binding.coroutineContextButton.setOnClickListener {
            coroutineContext()
        }

        binding.minusKeyButton.setOnClickListener {
            coroutineContextMinusKey()
        }
    }

    private fun coroutineContext() = runBlocking {
        val coroutineContext: CoroutineContext = newSingleThreadContext("MyThread") + CoroutineName("MyCoroutine")
        launch(context = coroutineContext) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
        }

        // CoroutineName.Key 를 사용하여 CoroutineName 구성요소에 접근
        Log.i("hsjeong", "result : ${coroutineContext[CoroutineName.Key]}")
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun coroutineContextMinusKey() = runBlocking {
        val coroutineName = CoroutineName("MyCoroutine")
        val dispatcher = Dispatchers.IO
        val job = Job()
        val coroutineContext: CoroutineContext = coroutineName + dispatcher + job
        launch(context = coroutineContext) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
        }

        // coroutineContext 에는 CoroutineName이 제거되지 않음
        val deleteCoroutineContext = coroutineContext.minusKey(CoroutineName)
        Log.i("hsjeong", "CoroutineName : ${deleteCoroutineContext[CoroutineName]}")       // CoroutineName 과 CoroutineName.key 동일
        Log.i("hsjeong", "CoroutineDispatcher : ${deleteCoroutineContext[CoroutineDispatcher]}")
        Log.i("hsjeong", "Job : ${deleteCoroutineContext[Job]}")
    }
}