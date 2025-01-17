package test.taylor.com.taylorcode.kotlin.coroutine.flow

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlowActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * case: collectLatest
         */
        lifecycleScope.launch {
            (1..10)
                .asFlow()
                .onEach {
                    delay(2000)
                    Log.v("ttaylor", "[collectLatest] emit $it")
                }
                .collectLatest {
                    /**
                     * every upstream value will be printed
                     */
                    Log.d("ttaylor", "[collectLatest]FlowActivity2.collectLatest[value=$it]: ")
                    delay(2001)
                    /**
                     * if the suspend function up,suspend long enough(longer than produce), all the value will be dropped except for the last one
                     */
                    Log.v("ttaylor", "[collectLatest] collect $it")
                }
        }

        /**
         * case: collectIndexed will block the faster producer
         */
        lifecycleScope.launch {
            (1..10)
                .asFlow()
                .onEach {
                    delay(2000)
                    Log.v("ttaylor", "[collectIndexed] emit $it")
                }
                .transformWhile {
                    repeat(20) { multiple -> emit(it * multiple) }
                    true
                }
                .collect { value ->
                    delay(2000)
                    Log.v("ttaylor", "[collectIndexed] collect $value")
                }
        }

        /**
         * case: this is not allowed(IllegalStateException: Flow invariant is violated), producer and collector is in the different thread
         */
//        lifecycleScope.launch {
//            flow {
//                withContext(Dispatchers.IO){
//                    repeat(10){emit(it)
//                    Log.v("ttaylor","[producer in another thread] emit $it")}
//
//                }
//            }.collect{
//                Log.v("ttaylor","[producer in another thread] $it thread=${Thread.currentThread().name}")
//            }
//        }

        /**
         * case: work around for IllegalStateException: Flow invariant is violated, just to wrap value generating logic to a suspend function
         */
        lifecycleScope.launch {
            flow {
                repeat(10) { emit(generateValues(it)) }
            }.buffer().collect {
                Log.v(
                    "ttaylor",
                    "[producer in another thread,work around] $it thread=${Thread.currentThread().name}"
                )
            }
        }

        /**
         * case: flowOn() will create a new coroutine for the upstream to produce the value and keep the downstream context unchanged
         */
        lifecycleScope.launch {
            flow {
                repeat(10) {
                    emit(it)
                    Log.v("ttaylor", "[flowOn] thread=${Thread.currentThread().name}")
                }
            }
                .flowOn(Dispatchers.Default)
                .collect {
                    Log.v(
                        "ttaylor",
                        "[flowOn] $it thread=${Thread.currentThread().name}"
                    )
                }
        }
    }

    private suspend fun generateValues(value: Int) = withContext(Dispatchers.Default) {
        value
    }
}