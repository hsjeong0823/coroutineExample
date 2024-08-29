package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityChannelBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

class ChannelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChannelBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@ChannelActivity, R.layout.activity_channel)

        binding.channelTestButton.setOnClickListener {
            channelTest()
        }

        binding.channelProduceTestButton.setOnClickListener {
            channelWithProduce()
        }

        binding.channelUnlimitedButton.setOnClickListener {
            channelUnlimited()
        }

        binding.channelCapacityButton.setOnClickListener {
            channelCapacity()
        }

        binding.channelRendezvousdButton.setOnClickListener {
            channelRendezvous()
        }

        binding.channelConflateddButton.setOnClickListener {
            channelConflated()
        }

        binding.bufferOverFlowDropOldestButton.setOnClickListener {
            channelBufferOverflow(BufferOverflow.DROP_OLDEST)
        }

        binding.bufferOverFlowDropLatestButton.setOnClickListener {
            channelBufferOverflow(BufferOverflow.DROP_LATEST)
        }

        binding.bufferOverFlowSuspendButton.setOnClickListener {
            channelBufferOverflow(BufferOverflow.SUSPEND)
        }

        binding.channelOnUndeliveredElementButton.setOnClickListener {
            channelOnUndeliveredElement()
        }

        binding.coroutineSelectButton.setOnClickListener {
            coroutineSelect()
        }

        binding.coroutineSelectCallOnReceiveButton.setOnClickListener {
            coroutineSelectCallOnReceive()
        }

        binding.coroutineSelectCallOnSendButton.setOnClickListener {
            coroutineSelectCallOnSend()
        }
    }

    private fun channelTest() = runBlocking {
        val channel = Channel<Int>()
        // 송신
        launch {
            repeat(5) { index ->
                delay(1000L)
                Log.i("hsjeong", "${Thread.currentThread().name} send index : $index")
                channel.send(index * 2)
            }
            channel.close() // 원소를 모두 보내고 채널 닫음
        }

        // 수신
        launch {
            for (element in channel) {
                Log.i("hsjeong", "${Thread.currentThread().name} receive element : $element")
            }

            // consumeEach 또한 for루프를 사용하지만 모든 원소를 가지온 다음(채널이 닫힌 뒤) 채널을 취소한다,
//            channel.consumeEach { element ->
//                Log.i("hsjeong", "${Thread.currentThread().name} receive element : $element")
//            }
        }
    }

    // 예외가 발생했을때 채널을 닫지 못할 수 있기 때문에 produce 빌더를 사용하며 코루틴이 종료되면 채널을 닫는다
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun channelWithProduce() = runBlocking {
        // 송신
        val channel = produce {
            repeat(5) { index ->
                delay(1000L)
                Log.i("hsjeong", "${Thread.currentThread().name} send index : $index")
                channel.send(index * 2)
            }
        }

        // 수신
        for (element in channel) {
            Log.i("hsjeong", "${Thread.currentThread().name} receive element : $element")
        }
    }

    // 채널 용량이 무제한이면 모든 원소를 보내고 수신자가 하나씩 가져간다.
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun channelUnlimited() = runBlocking {
        val startTime = System.currentTimeMillis()
        // 송신
        val channel = produce(capacity = Channel.UNLIMITED) {
            repeat(5) { index ->
                channel.send(index * 2)
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} send index : $index")
                delay(100L)
            }
        }

        // 수신
        delay(1000)
        for (element in channel) {
            Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)}  receive element : $element")
            delay(1000)
        }
    }

    // 채널 용량이 정해져 있으면 버퍼가 가득 찰 때까지 원소가 생성되고 수신자가 원소를 소비하기를 기다림.
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun channelCapacity() = runBlocking {
        val startTime = System.currentTimeMillis()
        // 송신
        val channel = produce(capacity = 3) {
            repeat(5) { index ->
                channel.send(index * 2)
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} send index : $index")
                delay(100L)
            }
        }

        // 수신
        delay(1000)
        for (element in channel) {
            Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)}  receive element : $element")
            delay(1000)
        }
    }

    // Channel.RENDEZVOUS 용량은 0이며 송신자는 항상 수신자를 기다림
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun channelRendezvous() = runBlocking {
        val startTime = System.currentTimeMillis()
        // 송신
        val channel = produce(capacity = Channel.RENDEZVOUS) {
            repeat(5) { index ->
                channel.send(index * 2)
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} send index : $index")
                delay(100L)
            }
        }

        // 수신
        delay(1000)
        for (element in channel) {
            Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)}  receive element : $element")
            delay(1000)
        }
    }

    // Channel.CONFLATED 원소를 저장하지 않으면 먼저 보내진 원소는 유실되고 최근 원소만 받을 수 있음.
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun channelConflated() = runBlocking {
        val startTime = System.currentTimeMillis()
        // 송신
        val channel = produce(capacity = Channel.CONFLATED) {
            repeat(5) { index ->
                channel.send(index * 2)
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} send index : $index")
                delay(100L)
            }
        }

        // 수신
        delay(1000)
        for (element in channel) {
            Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)}  receive element : $element")
            delay(1000)
        }
    }

    // BufferOverflow.SUSPEND 버퍼가 가득 찼을 때 send 메서드가 중단됨.(기본값)
    // BufferOverflow.DROP_OLDEST 버퍼가 가득 찼을 때 가장 오래된 원소가 제거됨.
    // BufferOverflow.DROP_LATEST 버퍼가 가득 찼을 때 가장 최근의 원소가 제거됨.
    // Channel.CONFLATED 타입은 capacity가 1 onBufferOverflow = BufferOverflow.DROP_OLDEST 로 설정된것도 동일하다
    private fun channelBufferOverflow(bufferOverflow: BufferOverflow) = runBlocking {
        val startTime = System.currentTimeMillis()
        val channel = Channel<Int>(capacity = 2, onBufferOverflow = bufferOverflow)
        // 송신
        launch {
            repeat(5) { index ->
                channel.send(index * 2)
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} send index : $index")
                delay(100L)
            }
            channel.close()
        }

        // 수신
        delay(1000)
        for (element in channel) {
            Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)}  receive element : $element")
            delay(1000)
        }
    }

    // onUndeliveredElement 는 원소가 어떠한 이유로 처리되지 않을때 호출됨.
    private fun channelOnUndeliveredElement() = runBlocking {
        val startTime = System.currentTimeMillis()
        val channel = Channel<Int>(onUndeliveredElement = {
            value -> Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} onUndeliveredElement value : $value")
        })

        // 송신
        launch {
            repeat(5) { index ->
                channel.send(index * 2)
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} send index : $index")
                delay(100L)
            }
            channel.close()
        }

        // 수신
        delay(1000)
        launch {
            channel.consumeEach {
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)}  receive element : $it")
                return@launch   // return으로 코푸틴을 종료하면서 원소가 정상 처리 되지 않아 onUndeliveredElement 호출함.
            }
        }
    }

    // select 는 가장 먼저 완료되는 코루틴을 기다리는 함수
    val scope = CoroutineScope(SupervisorJob())
    private fun coroutineSelect() = runBlocking {
        val startTime = System.currentTimeMillis()
        val data = askMultipleForData()
        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} askMultipleForData : $data")
    }

    private suspend fun askMultipleForData(): String = coroutineScope {
        select {
            async { requestData1() }.onAwait { it }
            async { requestData2() }.onAwait { it }
        }.also {
            // select가 값을 생성하고 나서 코루틴을 취소하도록하면 1초 후 Data2 로그가 찍는것을 확인할 수 있음.
            // 아래 코루틴 취소하지 않으면 모든 코루틴이 종료될때까지 기다리기때문에 3초후에 Data2 로그가 찍힘
            coroutineContext.cancelChildren()
        }

        // 아래 코드는 1초 후 Data2가 로그가 찍힘 scope로 별도로 생성하기 때문에 기존 구조화가 깨짐
//        select {
//            scope.async { requestData1() }.onAwait { it }
//            scope.async { requestData2() }.onAwait { it }
//        }
    }

    private suspend fun requestData1(): String {
        delay(3000)
        return "Data1"
    }

    private suspend fun requestData2(): String {
        delay(1000)
        return "Data2"
    }

    // 채널에서 select 사용 예제
    private fun coroutineSelectCallOnReceive() = runBlocking {
        val startTime = System.currentTimeMillis()
        val fooChannel = Channel<String>()
        val barChannel = Channel<String>()
        launch {
            while(true) {
                delay(210L)
                Log.i("hsjeong", "${Thread.currentThread().name} foo")
                fooChannel.send("foo")
            }
        }

        launch {
            while(true) {
                delay(500L)
                Log.i("hsjeong", "${Thread.currentThread().name} BAR")
                barChannel.send("BAR")
            }
        }

        repeat(7) {
            select {
                fooChannel.onReceive {
                    Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} From fooChannel : $it")
                }

                barChannel.onReceive {
                    Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} From barChannel : $it")
                }
            }
        }

        coroutineContext.cancelChildren()
        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} coroutineSelectCallOnReceive")
    }

    // 채널에서 select 사용하여 버퍼에 공간이 있는 채널을 선택하여 데이터를 전송하는 예제
    private fun coroutineSelectCallOnSend() = runBlocking {
        val startTime = System.currentTimeMillis()
        val c1 = Channel<Char>(capacity = 2)
        val c2 = Channel<Char>(capacity = 2)
        launch {
            for (c in 'A'..'Z') {
                delay(210L)
                select<Unit> { 
                    c1.onSend(c) {
                        Log.i("hsjeong", "${Thread.currentThread().name} Sent $c to 1")
                    }
                    c2.onSend(c) {
                        Log.i("hsjeong", "${Thread.currentThread().name} Sent $c to 2")
                    }
                }
            }
        }

        launch {
            while(true) {
                delay(1000)
                val c = select {
                    c1.onReceive {
                        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} From c1 : $it")
                    }

                    c2.onReceive {
                        Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} From c2 : $it")
                    }
                }
                Log.i("hsjeong", "${Thread.currentThread().name} ${getElapsedTime(startTime)} received $c")
            }
        }
        
        delay(10000)
        coroutineContext.cancelChildren()
    }
    
    private fun getElapsedTime(startTime: Long): String = "지난 시간: ${System.currentTimeMillis() - startTime}ms"
}