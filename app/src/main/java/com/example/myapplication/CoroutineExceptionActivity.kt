package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityCoroutineExceptionBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

class CoroutineExceptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCoroutineExceptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@CoroutineExceptionActivity, R.layout.activity_coroutine_exception)

        binding.supervisorJobButton.setOnClickListener {
            supervisorJob()
        }

        binding.supervisorJobWithCoroutineScopeButton.setOnClickListener {
            supervisorJobWithCoroutineScope()
        }

        binding.supervisorScopeButton.setOnClickListener {
            supervisorScope()
        }

        binding.coroutineExceptionHandlerButton.setOnClickListener {
            coroutineExceptionHandler()
        }

        binding.coroutineTryCatchButton.setOnClickListener {
            coroutineTryCatch()
        }

        binding.coroutineWithTimeOutButton.setOnClickListener {
            coroutineWithTimeOut()
        }

        binding.coroutineWithTimeOutOrNullButton.setOnClickListener {
            coroutineWithTimeOutOrNull()
        }
    }

    private fun supervisorJob() = runBlocking {
        // SupervisorJob 은 자식 코루틴의 예외를 전파받지 않는 특징을 가지며 아래 코드에서 Coroutine2 는 정상 실행 됨.(앱 크래시로 실제 로그 확인은 어려움)
        // 주의사항!!! lauch의 context 인자로 SupervisorJob 객체를 직접 넘기면 안됨.(해당 객체를 부모로하여 생성되기 때문)
        val supervisorJob = SupervisorJob(parent = coroutineContext[Job]) // 부모의 Job 객체를 넘겨 구조화를 깨지 않도록 함
        launch(CoroutineName("Coroutine1") + supervisorJob) {
            launch(CoroutineName("Coroutine3")) {
                Log.i("hsjeong", "${Thread.currentThread().name} 실행3")
                throw Exception("예외 발생")
            }
            delay(100L)
            Log.i("hsjeong", "${Thread.currentThread().name} 실행1")
        }

        launch(CoroutineName("Coroutine2") + supervisorJob) {
            delay(100L)
            Log.i("hsjeong", "${Thread.currentThread().name} 실행2")
        }

        supervisorJob.complete()
    }

    // CoroutineScope를 사용하여 SupervisorJob 사용
    private fun supervisorJobWithCoroutineScope() = runBlocking {
        val coroutineScope = CoroutineScope(SupervisorJob())
        coroutineScope.launch(CoroutineName("Coroutine1")) {
            launch(CoroutineName("Coroutine3")) {
//                throw Exception("예외 발생")
                Log.i("hsjeong", "${Thread.currentThread().name} 실행3")
            }
            delay(100L)
            Log.i("hsjeong", "${Thread.currentThread().name} 실행1")
        }

        coroutineScope.launch(CoroutineName("Coroutine2")) {
            delay(100L)
            Log.i("hsjeong", "${Thread.currentThread().name} 실행2")
        }
    }

    // supervisorScope를 사용하여 예외 전파 제한
    private fun supervisorScope() = runBlocking {
        supervisorScope {
            launch(CoroutineName("Coroutine1")) {
                launch(CoroutineName("Coroutine3")) {
                    throw Exception("예외 발생")
                    Log.i("hsjeong", "${Thread.currentThread().name} 실행3")
                }
                delay(100L)
                Log.i("hsjeong", "${Thread.currentThread().name} 실행1")
            }

            launch(CoroutineName("Coroutine2")) {
                delay(100L)
                Log.i("hsjeong", "${Thread.currentThread().name} 실행2")
            }
        }
    }

    private fun coroutineExceptionHandler() = runBlocking {
        // CoroutineExceptionHandler 은 마지막으로 예외를 전파받는 위치에 설정하여 처리해야한다.
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.i("hsjeong", "${coroutineContext[CoroutineName]} $throwable")
        }

        // 아래 코드는 exceptionHandler 가 동작하지 않는다.
//        launch(CoroutineName("Coroutine1") + exceptionHandler) {
//            throw Exception("예외 발생")
//        }

        // 아래 코드는 CoroutineScope 를 통해 루트 Job이 설정되어 해당 Job으로 마지막 으로 전파되기 때문에 예외처리가 가능하다.
        CoroutineScope(exceptionHandler).launch(CoroutineName("Coroutine1") + exceptionHandler) {
            throw Exception("예외 발생1")
        }

        // 아래 코드는 SupervisorJob을 사용하면 예외가 전파되지 않기 때문에 에러처리가 가능하다.
        val coroutineScope = CoroutineScope(SupervisorJob() + exceptionHandler)
        coroutineScope.launch(CoroutineName("Coroutine2")) {
            throw Exception("예외 발생2")
        }
    }

    private fun coroutineTryCatch() = runBlocking {
        // try catch 사용은 빌더 함수 내부에서 사용
        launch(CoroutineName("Coroutine1")) {
            try {
                throw Exception("예외 발생1")
            } catch (e: Exception) {
                Log.i("hsjeong", "Exception : ${Thread.currentThread().name} ${e.message}")
            }
        }

        // async 의 경우 await에 try catch 사용한다.
        // await 에 try catch를 사용하더라도 async 빌더 내에서 예외를 전파하기 때문에 supervisorScope 를 적절히 사용하여 예외가 전파되지 않도록 처리가 필요.
        supervisorScope {
            val deferred: Deferred<String> = async(CoroutineName("Coroutine2")) {
                throw Exception("예외 발생2")
            }
            try {
                deferred.await()
            } catch (e: Exception) {
                Log.i("hsjeong", "Exception : ${Thread.currentThread().name} ${e.message}")
            }
        }
    }

    private fun coroutineWithTimeOut() = runBlocking {
        launch(CoroutineName("Coroutine1")) {
            // 타임아웃으로 코루틴이 취소되어 아래 로그는 찍히지 않는다.(참고로 withTimeout을 try catch로 잡으면 Exception 로그가 출력되는것을 확인할 수 있다.)
            withTimeout(1000L) {
                delay(2000L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine1 실행")
            }
        }

        delay(2000L)
        Log.i("hsjeong", "${Thread.currentThread().name} runBlocking 실행")
    }

    private fun coroutineWithTimeOutOrNull() = runBlocking {
        launch(CoroutineName("Coroutine1")) {
            // 타임아웃으로 코루틴이 취소되어 아래 로그는 찍히지 않고 null을 반환한다.
            val result = withTimeoutOrNull(1000L) {
                delay(2000L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine1 실행")
            }
            Log.i("hsjeong", "${Thread.currentThread().name} Coroutine1 withTimeoutOrNull result $result")
        }

        delay(2000L)
        Log.i("hsjeong", "${Thread.currentThread().name} runBlocking 실행")
    }
}