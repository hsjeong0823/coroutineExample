package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityAsyncDeferredBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class AsyncAndDeferredActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAsyncDeferredBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@AsyncAndDeferredActivity, R.layout.activity_async_deferred)

        binding.awaitButton.setOnClickListener {
            coroutineAsyncAwait()
        }

        binding.awaitAllButton.setOnClickListener {
            coroutineAsyncAwaitAll()
        }

        binding.withContextButton.setOnClickListener {
            coroutineWithContext()
        }
    }

    private fun coroutineAsyncAwait() = runBlocking {
        val deferred: Deferred<String> = async(Dispatchers.IO) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
            delay(1000L)
            return@async "Return Value"
        }

        // Deferred 객체도 Job 의 일종으로 join 이나 isActivie 같은 프로퍼티도 동일하게 사용가능함.
        val result = deferred.await()   // 결과가 반환될때까지 runBlocking 일시중지

        Log.i("hsjeong", "${Thread.currentThread().name} result : $result")
    }

    private fun coroutineAsyncAwaitAll() = runBlocking {
        val startTime = System.currentTimeMillis()
        val deferredArray = arrayListOf<Deferred<Array<String>>>()
        val deferred1: Deferred<Array<String>> = async(Dispatchers.IO) {
            Log.i("hsjeong", "${Thread.currentThread().name} deferred1 실행")
            delay(1000L)
            arrayOf("1", "2")
        }
        deferredArray.add(deferred1)

        val deferred2: Deferred<Array<String>> = async(Dispatchers.IO) {
            Log.i("hsjeong", "${Thread.currentThread().name} deferred2 실행")
            delay(1000L)
            arrayOf("3")
        }
        deferredArray.add(deferred2)

        // Collection 인터페이스에 대한 확장함수도 제공하여 아래 두가지 방식으로 awaitAll 사용가능
        //val results: List<Array<String>> = awaitAll(deferred1, deferred2)
        val results = deferredArray.awaitAll()

        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} result : ${listOf(*results[0], *results[1])}")
    }

    private fun coroutineWithContext() = runBlocking {
        // withContext 는 async-await 보다 깔끔하지만 여러 요청을 병렬로 처리해야하는 경우는 async_await를 사용해야한다.
        val result = withContext(Dispatchers.IO) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행")
            delay(1000L)
            return@withContext "Return Value"
        }

        Log.i("hsjeong", "${Thread.currentThread().name} result : $result")
    }

    private fun getElapsedTime(startTime: Long): String = "지난 시간: ${System.currentTimeMillis() - startTime}ms"
}