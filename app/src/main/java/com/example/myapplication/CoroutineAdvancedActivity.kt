package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityCoroutineAdvancedBinding
import com.example.myapplication.databinding.ActivitySuspendFunctionBinding
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.locks.ReentrantLock

class CoroutineAdvancedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCoroutineAdvancedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@CoroutineAdvancedActivity, R.layout.activity_coroutine_advanced)

        binding.volatileButton.setOnClickListener {
            volatileTest()
        }

        binding.mutexButton.setOnClickListener {
            mutexTest()
        }

        binding.reentrantLockButton.setOnClickListener {
            reentrantLockTest()
        }

        binding.coroutineStartDefaultButton.setOnClickListener {
            coroutineStartDefault()
        }

        binding.coroutineStartAtomicButton.setOnClickListener {
            coroutineStartAtomic()
        }

        binding.coroutineStartUndispatchedButton.setOnClickListener {
            coroutineStartUndispatched()
        }
    }

    @Volatile
    private var count = 0   // 변수가 @Volatile 로 선언 시 메인 메모리에만 저장 (메인 메모리에만 저장하더라도 메인 메모리에 여러 스레드가 동시에 접근할 수 있으므로 동시 접근을 제한할 필요가 있음)
    private fun volatileTest() = runBlocking {
        count = 0
        withContext(Dispatchers.Default) {
            repeat(10_000) {
                launch {
                    count += 1
                    Log.i("hsjeong", "${Thread.currentThread().name} count : $count")
                }
            }
        }
        Log.i("hsjeong", "${Thread.currentThread().name} count : $count")
    }

    private var mutexCount = 0
    private val mutex = Mutex() // Mutex 객체를 사용해 여러 스레드가 메인 메모리에 동시 접근할 수 없도록 임계영역을 만들 수 있음.(lock-unlock을 쌍으로 호출해야함)
    private fun mutexTest() = runBlocking {
        val startTime = System.currentTimeMillis()
        mutexCount = 0
        withContext(Dispatchers.Default) {
            repeat(10_000) {
                launch {
                    mutex.lock()        // 임계 영역 시작 지점
                    mutexCount += 1
                    //Log.i("hsjeong", "${Thread.currentThread().name} count : $mutexCount")
                    mutex.unlock()      // 임계 영역 종료 시점
                }
            }
        }
        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} mutex.lock() count : $mutexCount")

        mutexCount = 0
        withContext(Dispatchers.Default) {
            repeat(10_000) {
                launch {
                    mutex.withLock {    // withLock 사용 시 람다식이 모두 실행되면 unlock을 호출하기 때문에 좀더 안전하게 사용할 수 있다.
                        mutexCount += 1
                        //Log.i("hsjeong", "${Thread.currentThread().name} count : $mutexCount")
                    }
                }
            }
        }
        Log.i("hsjeong", "${Thread.currentThread().name}  ${getElapsedTime(startTime)} mutex.withLock count : $mutexCount")
    }

    private var reentrantLockCount = 0
    private val reentrantLock = ReentrantLock()  // ReentrantLock() 을 사용해도 동일하지만 Mutex() 와 차이점은 ReentrantLock() 스레드를 블로킹하기 때문에 다른 코루틴이 사용할 수 없으므로 Mutex() 사용을 권장함.
    private fun reentrantLockTest() = runBlocking {
        val startTime = System.currentTimeMillis()
        reentrantLockCount = 0
        withContext(Dispatchers.Default) {
            repeat(10_000) {
                launch {
                    reentrantLock.lock()        // 임계 영역 시작 지점
                    reentrantLockCount += 1
                    //Log.i("hsjeong", "${Thread.currentThread().name} count : $mutexCount")
                    reentrantLock.unlock()      // 임계 영역 종료 시점
                }
            }
        }
        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} reentrantLock.lock() count : $reentrantLockCount")
    }

    // CoroutineStart.DEFAULT (별도 설정이 없으면 기본값으로 적용된다. job.cancel 시 대기중인 코루틴이 취소되어 "실행 1" 로그만 출력)
    private fun coroutineStartDefault() = runBlocking {
        val job = launch {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행 2")
        }
        job.cancel()
        Log.i("hsjeong", "${Thread.currentThread().name} 실행 1")
    }

    // CoroutineStart.ATOMIC (job.cancel 시 대기중인 코루틴이 취소되지 않아 "실행 1" 로그만 출력)
    @OptIn(ExperimentalCoroutinesApi::class)    // 실험버전임을 알리는 어노테이션
    private fun coroutineStartAtomic() = runBlocking {
        val job = launch(start = CoroutineStart.ATOMIC) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행 2")
        }
        job.cancel()
        Log.i("hsjeong", "${Thread.currentThread().name} 실행 1")
    }

    // CoroutineStart.UNDISPATCHED
    private fun coroutineStartUndispatched() = runBlocking {
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            Log.i("hsjeong", "${Thread.currentThread().name} 실행 2")
        }
        job.cancel()
        Log.i("hsjeong", "${Thread.currentThread().name} 실행 1")
    }


    private fun getElapsedTime(startTime: Long): String = "지난 시간: ${System.currentTimeMillis() - startTime}ms"
}