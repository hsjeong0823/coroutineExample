package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityFlowBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

class FlowActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlowBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@FlowActivity, R.layout.activity_flow)

        binding.asFlowButton.setOnClickListener {
            flowConvert()
        }

        binding.flowBuilderButton.setOnClickListener {
            flowBuilder()
        }
    }

    private fun flowConvert() = runBlocking {
        // asFlow를 사용해서 Flow로 바꿀수 있음.
        listOf(1,2,3,4,5).asFlow().collect {
            Log.i("hsjeong", "${Thread.currentThread().name} $it")
        }

        val function = suspend {
            // 중단함수를 람다식으로 만든 것
            delay(1000)
            "UserName"
        }

        function.asFlow().collect {
            Log.i("hsjeong", "${Thread.currentThread().name} $it")
        }

        //함수 참조값을 사용하여 적용
        ::getUserName.asFlow().collect {
            Log.i("hsjeong", "${Thread.currentThread().name} $it")
        }
    }

    suspend fun getUserName(): String {
        delay(1000)
        return "UserName"
    }

    private fun flowBuilder() = runBlocking {
        val startTime = System.currentTimeMillis()
        makeFlow().collect {
            Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} flowBuilder value : $it")
        }
    }

    private fun makeFlow() = flow {
        repeat(3) {
            delay(1000)
            emit(it)
        }
    }

    private fun getElapsedTime(startTime: Long): String = "지난 시간: ${System.currentTimeMillis() - startTime}ms"
}