package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivitySuspendFunctionBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

class SuspendFunActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuspendFunctionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SuspendFunActivity, R.layout.activity_suspend_function)

        binding.suspendFuncButton.setOnClickListener {
            suspendFunction()
        }

        binding.suspendFuncCoroutineScopeButton.setOnClickListener {
            suspendFunctionWithCoroutineScope()
        }

        binding.suspendFuncSupervisorScopeButton.setOnClickListener {
            suspendFunctionWithSupervisorScope()
        }
    }

    private fun suspendFunction() = runBlocking {
        val startTime = System.currentTimeMillis()
        suspendKeywordFunc("Hello")
        suspendKeywordFunc("World")
        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)}")
    }

    // suspend fun 키워드로 선언된 함수는 일시 중단 지점을 포함 할 수 있는 기능을 한다.
    // 일시 중단 함수의 호출 가능 지점
    // 1. 코루틴 내부
    // 2. 일시 중단 함수
    private suspend fun suspendKeywordFunc(keyword: String) {
        delay(1000L)
        Log.i("hsjeong", "keyword : $keyword")
    }

    // 일시 중단 함수 내에 코루틴 빌더 사용인 경우 coroutineScope 를 사용해 구조화를 깨지 않고 객체를 생성할 수 있다.
    private fun suspendFunctionWithCoroutineScope() = runBlocking {
        val startTime = System.currentTimeMillis()
        val result = searchKeyword()
        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} result : ${result.toList()}")
    }

    private suspend fun searchKeyword(): Array<String> = coroutineScope {
        val helloSearchDeferred = async {
            Log.i("hsjeong", "${Thread.currentThread().name} helloSearchDeferred 실행")
            getKeyword("Hello")
        }

        val worldSearchDeferred = async {
            Log.i("hsjeong", "${Thread.currentThread().name} worldSearchDeferred 실행")
            getKeyword("World")
        }
        return@coroutineScope arrayOf(helloSearchDeferred.await(), worldSearchDeferred.await())
    }

    // supervisorScope 를 사용해 예외 전파를 제한 할 수 있다.
    private fun suspendFunctionWithSupervisorScope() = runBlocking {
        val startTime = System.currentTimeMillis()
        val result = searchKeywordWithSupervisorScope()
        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} result : ${result.toList()}")
    }

    private suspend fun searchKeywordWithSupervisorScope(): Array<String> = supervisorScope {
        val helloSearchDeferred = async {
            Log.i("hsjeong", "${Thread.currentThread().name} helloSearchDeferred 실행")
            throw Exception("helloSearchDeferred 예외 발생")
            getKeyword("Hello")
        }

        val worldSearchDeferred = async {
            Log.i("hsjeong", "${Thread.currentThread().name} worldSearchDeferred 실행")
            getKeyword("World")
        }

        val helloSearchResult = try {
            helloSearchDeferred.await()
        } catch (e: Exception) {
            Log.i("hsjeong", "Exception ${e.message}")
            "helloSearchResult error" // 예외 발생시 리턴값
        }

        val worldSearchResult = try {
            worldSearchDeferred.await()
        } catch (e: Exception) {
            Log.i("hsjeong", "Exception ${e.message}")
            "worldSearchResult error" // 예외 발생시 리턴값
        }

        return@supervisorScope arrayOf(helloSearchResult, worldSearchResult)
    }

    private suspend fun getKeyword(keyword: String): String {
        delay(1000L)
        return keyword
    }

    private fun getElapsedTime(startTime: Long): String = "지난 시간: ${System.currentTimeMillis() - startTime}ms"
}