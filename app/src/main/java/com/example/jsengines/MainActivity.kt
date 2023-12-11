package com.example.jsengines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jsengines.engines.DuktapeEngineImpl
import com.example.jsengines.engines.J2V8EngineImpl
import com.example.jsengines.engines.JavascriptSandboxImpl
import com.example.jsengines.engines.JavetEngineImpl
import com.example.jsengines.engines.QuickJSEngineImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

private val runtimes = mutableMapOf<String, MutableList<Long>>()
private val benchmarkScript =
    """
        function createLargeObject(depth, breadth) {
            if (depth == 0) {
                return "leaf";
            }
            var obj = {};
            for (var i = 0; i < breadth; i++) {
                obj["key" + i] = createLargeObject(depth - 1, breadth);
            }
            return obj;
        }
        JSON.stringify(createLargeObject(5, 5));
        """.trimIndent()

private fun runEngineTest(engine: JSEngine): Long {
    engine.init()
    val runtime = measureTimeMillis {
        for (i in 0..100) {
            engine.evaluate(benchmarkScript)
        }
    }
    engine.close()
    val engineName = engine::class.java.simpleName
    if (!runtimes.containsKey(engineName)) {
        runtimes[engineName] = mutableListOf(runtime)
    } else {
        runtimes[engineName]!!.add(runtime)
    }
    return runtime
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var runtime by remember { mutableLongStateOf(0L) }
            var isEnabled: Boolean by remember { mutableStateOf(true) }
            val coroutineScope = rememberCoroutineScope()

            @Composable
            fun BenchmarkButton(
                text: String,
                engine: () -> JSEngine
            ) {
                Button(
                    content = { Text(text = text) },
                    onClick = {
                        if (!isEnabled) return@Button
                        coroutineScope.launch(Dispatchers.IO) {
                            isEnabled = false
                            runtime = runEngineTest(engine())
                            isEnabled = true
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BenchmarkButton(
                    text = "Run J2V8",
                    engine = { J2V8EngineImpl() }
                )
                BenchmarkButton(
                    text = "Run Javet",
                    engine = { JavetEngineImpl() }
                )
                BenchmarkButton(
                    text = "Run QuickJS",
                    engine = { QuickJSEngineImpl() }
                )
                BenchmarkButton(
                    text = "Run JavascriptSandbox",
                    engine = { JavascriptSandboxImpl(this@MainActivity) }
                )
                BenchmarkButton(
                    text = "Run Duktape",
                    engine = { DuktapeEngineImpl() }
                )
                Spacer(modifier = Modifier.height(50.dp))

                if (runtime > 0) {
                    Text(text = "Runtime: $runtime ms")
                }
                if (!isEnabled) {
                    Box(
                        modifier = Modifier
                            .height(25.dp)
                            .fillMaxWidth()
                            .background(Color.Yellow)
                    )
                }

                if (runtimes.isNotEmpty()) {
                    Text(text = "Runtimes:")
                    runtimes.forEach { (engine, runtimes) ->
                        val avgRuntime = runtimes.average().roundToInt()
                        Text(text = "$engine (${runtimes.size}) Avg Runtime: $avgRuntime ms")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Greeting("Android")
}