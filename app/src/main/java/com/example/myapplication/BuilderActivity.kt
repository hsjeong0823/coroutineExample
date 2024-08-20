package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityBuilderBinding
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BuilderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuilderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@BuilderActivity, R.layout.activity_builder)

        binding.joinButton.setOnClickListener {
            coroutineJoin()
        }

        binding.joinAllButton.setOnClickListener {
            coroutineJoinAll()
        }

        binding.lazyButton.setOnClickListener {
            coroutineLazy()
        }

        binding.cancelButton.setOnClickListener {
            coroutineCancel()
        }

        binding.cancelAndJoinButton.setOnClickListener {
            coroutineCancelAndJoin()
        }

        binding.isActiveButton.setOnClickListener {
            coroutineIsActive()
        }
    }

    private fun coroutineJoin() = runBlocking {
        val updateJob = launch(Dispatchers.IO) {
            Log.i("hsjeong", "${Thread.currentThread().name} 업데이트 실행")
            delay(100L)
            Log.i("hsjeong", "${Thread.currentThread().name} 업데이트 완료")
        }

        updateJob.join()        // updateJob 완료까지 코루틴 일시 중단

        val callJob = launch(Dispatchers.IO) {
            Log.i("hsjeong", "${Thread.currentThread().name} 요청 실행")
        }
    }

    private fun coroutineJoinAll() = runBlocking {
        val job1 = launch(Dispatchers.Default) {
            Thread.sleep(100L)
            Log.i("hsjeong", "${Thread.currentThread().name} job 1 완료")
        }

        val job2 = launch(Dispatchers.Default) {
            Thread.sleep(100L)
            Log.i("hsjeong", "${Thread.currentThread().name} job 2 완료")
        }

        joinAll(job1, job2)  // job1, job2 완료까지 코루틴 일시 중단

        val workJob = launch(Dispatchers.IO) {
            Log.i("hsjeong", "${Thread.currentThread().name} job1,2 완료")
        }
    }

    private fun coroutineLazy() = runBlocking {
        var startTime = System.currentTimeMillis()
        val immediateJob = launch {
            Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} 즉시 실행")
        }

        startTime = System.currentTimeMillis()
        val lazyJob = launch(start = CoroutineStart.LAZY) {
            Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} 지연 실행")
        }
        showJobStateLog(lazyJob)

        delay(1000L)
        lazyJob.start() // 지연 시작
        showJobStateLog(lazyJob)
    }

    private fun coroutineCancel() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            repeat(10) { repeatTime ->
                delay(1000L)
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} 반복 횟수 $repeatTime")
            }
        }

        delay(3500L)
        job.cancel()    // 코루틴 취소 (cancel 호출 시 바로 취소 되지 않음, 일시 중단이나 대기 시점에 일반적으로 취소가 되기 때문에 cancel을 호출하여도 취소가 안될 수 있음)
        showJobStateLog(job)
    }

    private fun coroutineCancelAndJoin() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            repeat(10) { repeatTime ->
                delay(1000L)
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} 반복 횟수 $repeatTime")
            }
        }

        delay(3500L)
        job.cancelAndJoin()    // 코루틴 취소될때까지 코루틴 일시 중단 (cancel 했다고 바로 취소되는것이 아니기 때문에 cancel 만으로는 곧 바로 취소를 보장할 수 없다)
        showJobStateLog(job)

        //cancelAndJoin 호출해야 취소 후 다음 함수 실행이 보장됨.
        val workJob = launch(Dispatchers.IO) {
            Log.i("hsjeong", "${Thread.currentThread().name} worJob 실행")
        }
        showJobStateLog(workJob)
    }

    // 취소되도록 만드는 3가지 방법
    // 1. delay 사용
    // 2. yield 사용
    // 3. CoroutineScope.isActive 사용
    // 1,2 번으로 매번 delay 및 yield 시켜 중단되는 것은 비효율적
    private fun coroutineIsActive() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            while(this.isActive) {  // 취소 요청시 실제 취소되지 않아도 isActive가 false가 되고 isCancelled가 true로 반환됨
                Log.i("hsjeong", "${Thread.currentThread().name} 실행 중")
                // 아래 두가지는 취소 체크를 하기에 비효율적
                // delay(1)
                // yield()
            }
        }

        delay(100L)
        job.cancel()    // isAcitive값을 false로 바꿔 while문이 중단 된다.
        showJobStateLog(job)
    }

    private fun getElapsedTime(startTime: Long): String = "지난 시간: ${System.currentTimeMillis() - startTime}ms"

    private fun showJobStateLog(job: Job) {
        Log.i("hsjeong", "isActive : ${job.isActive}\nisCancelled : ${job.isCancelled}\nisCompleted : ${job.isCompleted}")
    }
}