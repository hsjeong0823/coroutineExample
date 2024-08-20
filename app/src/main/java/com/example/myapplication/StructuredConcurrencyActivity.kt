package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityStructuredConcurrencyBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
}