package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityStructuredConcurrencyBinding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class StructuredConcurrencyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStructuredConcurrencyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@StructuredConcurrencyActivity, R.layout.activity_structured_concurrency)

        binding.jobButton.setOnClickListener {
            coroutineJob()
        }

        binding.cancelButton.setOnClickListener {
            coroutineCancel()
        }

        binding.coroutineScopeButton.setOnClickListener {
            coroutineScope()
        }

        binding.coroutineScopeNewButton.setOnClickListener {
            coroutineScopeNew()
        }

        binding.coroutineScopeJobButton.setOnClickListener {
            coroutineScopeJob()
        }

        binding.coroutineScopeNewJobButton.setOnClickListener {
            coroutineScopeNewJob()
        }

        binding.coroutineScopeNewJobParentButton.setOnClickListener {
            coroutineScopeNewJobParent()
        }

        binding.coroutineRunBlockingAndLaunchButton.setOnClickListener {
            coroutineRunBlockingAndLaunch()
        }
    }

    private fun coroutineJob() = runBlocking {  // 부모 코루틴
        val runBlockingJob = coroutineContext[Job] // 부모의 Job 추출
        launch {    // 자식 코루틴
            val launchJob = coroutineContext[Job]   // 자식의 Job 추출
            if (runBlockingJob === launchJob) {
                Log.i("hsjeong", "runBlockingJob, launchJob 동일")
            } else {
                Log.i("hsjeong", "runBlockingJob, launchJob 다름")
            }
        }
    }

    private fun coroutineCancel() = runBlocking {
        val parentJob = launch(Dispatchers.IO) {
            val resultsDeferred: List<Deferred<String>> = listOf("1" ,"2", "3").map {
                async {
                    delay(1000L)
                    Log.i("hsjeong", "$it 실행")
                    return@async "Return : $it"
                }
            }

            val results = resultsDeferred.awaitAll()
            Log.i("hsjeong", "results : $results")
        }

        parentJob.cancel()      // 취소 시 아무것도 출력되지 않음(부모 코루틴 취소 시 자식 코루틴도 전분 취소됨)

        parentJob.invokeOnCompletion {  // invokeOnCompletion 을 통해 코루틴 실행 완료 시 콜백을 받음
            Log.i("hsjeong", "isCompleted : ${parentJob.isCompleted}")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun coroutineScope() {
        val newScope = CoroutineScope(CoroutineName("My Coroutine") + Dispatchers.IO)
        newScope.launch(CoroutineName("LaunchCoroutine")) {
            // 코루틴의 실행 환경이 newScope -> launch 로 상속된다.
            // lauch 코루틴의 CoroutineName 은 LaunchCoroutine 으로 덮어써지고 job 은 newScope 를 부모로한 새로운 job 이 생성된다.
            Log.i("hsjeong", "this.coroutineContext[CoroutineName] : ${this.coroutineContext[CoroutineName]}")
            Log.i("hsjeong", "this.coroutineContext[CoroutineDispatcher] : ${this.coroutineContext[CoroutineDispatcher]}")
            val launchJob = this.coroutineContext[Job]
            val newScopeJob = newScope.coroutineContext[Job]
        }
    }

    private fun coroutineScopeNew() = runBlocking {
        launch(CoroutineName("Coroutine1")) {
            launch(CoroutineName("Coroutine3")) {
                delay(200L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine3")
            }

            // Coroutine4는 CoroutineScope 객체를 생성하면서 runBlocking 코루틴과 관계가 없어진다.
            CoroutineScope(Dispatchers.IO).launch(CoroutineName("Coroutine4")) {
                delay(200L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine4")
            }
            Log.i("hsjeong", "${Thread.currentThread().name} Coroutine1")

            this.cancel()   // 취소 시 Coroutine3 은 로그가 찍히지 않는다
        }

        launch(CoroutineName("Coroutine2")) {
            Log.i("hsjeong", "${Thread.currentThread().name} Coroutine2")
        }
    }

    // CoroutineScope 를 이용하여 구조화 깨기
    private fun coroutineScopeJob() = runBlocking {
        // 모든 자식 코루틴이 newScope 에서 실행 되고 runBlocking은 자식 코루틴이 없는 구조가 된다.
        val newScope = CoroutineScope(Dispatchers.IO)
        newScope.launch(CoroutineName("Coroutine1")) {
            launch(CoroutineName("Coroutine3")) {
                delay(100L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine3")
            }

            launch(CoroutineName("Coroutine4")) {
                delay(100L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine4")
            }
        }

        newScope.launch(CoroutineName("Coroutine2")) {
            launch(CoroutineName("Coroutine5")) {
                delay(100L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine5")
            }
        }
    }

    // 새로운 Job 을 생성하여 구조화 깨기
    private fun coroutineScopeNewJob() = runBlocking {
        // 모든 자식 코루틴이 newScope 에서 실행 되고 runBlocking은 자식 코루틴이 없는 구조가 된다.
        val newJob = Job()  // 부모가 없는 루트 Job이 생성된다.
        launch(CoroutineName("Coroutine1") + newJob) {
            launch(CoroutineName("Coroutine3")) {
                delay(100L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine3")
            }

            launch(CoroutineName("Coroutine4")) {
                delay(100L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine4")
            }
        }

        launch(CoroutineName("Coroutine2") + newJob) {
            launch(CoroutineName("Coroutine5")) {
                delay(100L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine5")
            }
        }
    }

    // 생성된 Job의 부모를 명시적으로 설정하기
    private fun coroutineScopeNewJobParent() = runBlocking {
        launch(CoroutineName("Coroutine1")) {
            val newJob = Job(this.coroutineContext[Job])  // 부모를 명시하여 구조화가 깨지지 않는다
            launch(CoroutineName("Coroutine2") + newJob) {
                delay(100L)
                Log.i("hsjeong", "${Thread.currentThread().name} Coroutine2")
            }
            newJob.complete()   // Job 객체는 자식이 모두 실행 완료되더라도 프로세스가 종료되지 않아 complete를 명시적으로 호출해야한다.
        }
    }

    private fun coroutineRunBlockingAndLaunch() = runBlocking {
        // runBlocking 은 하위 코루틴 완료까지 스레드를 차단하지만 launch는 차단하지 않는다.
        val startTime = System.currentTimeMillis()
        launch {
            delay(100L)
            Log.i("hsjeong", "${Thread.currentThread().name} launch 실행")
        }
        // launch는 스레드를 차단하지 않아 launch 코루틴 완료 되지 않아도 아래 로그가 찍힘
        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)}")

        runBlocking {
            delay(100L)
            Log.i("hsjeong", "${Thread.currentThread().name} runBlocking 실행")
        }
        // runBlocking은 스레드를 차단하기 때문에 runBlocking 코루틴 완료 후 아래 로그가 찍힘
        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)}")
    }

    private fun getElapsedTime(startTime: Long): String = "지난 시간: ${System.currentTimeMillis() - startTime}ms"
}