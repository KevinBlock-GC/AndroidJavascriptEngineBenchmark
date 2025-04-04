package com.example.jsengines

import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jsengines.engines.DuktapeEngineImpl
import com.example.jsengines.engines.J2V8EngineImpl
import com.example.jsengines.engines.JavascriptSandboxImpl
import com.example.jsengines.engines.JavetEngineImpl
import com.example.jsengines.engines.QuickJSEngineImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

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
        JSON.stringify(createLargeObject(3, 5));
        """.trimIndent()

private enum class Engine {
    J2V8,
    JAVET,
    QUICKJS,
    JAVASCRIPTSANDBOX,
    DUKTAPE
}

private val engines: Map<Engine, (Context) -> JSEngine> = mapOf(
    Engine.J2V8 to { J2V8EngineImpl() },
    Engine.JAVET to { JavetEngineImpl() },
    Engine.QUICKJS to { QuickJSEngineImpl() },
    Engine.JAVASCRIPTSANDBOX to { JavascriptSandboxImpl(it) },
    Engine.DUKTAPE to { DuktapeEngineImpl() }
)

class MainActivity : ComponentActivity() {

    private val runtimes = MutableStateFlow<Map<String, List<Long>>>(emptyMap())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var runtime by remember { mutableLongStateOf(0L) }
            var isEnabled: Boolean by remember { mutableStateOf(true) }
            val coroutineScope = rememberCoroutineScope()
            val context = this

            val averageRuntimes by runtimes.collectAsStateWithLifecycle()

            @Composable
            fun BenchmarkButton(
                text: String,
                engine: Engine
            ) {
                Button(
                    content = { Text(text = text) },
                    onClick = {
                        if (!isEnabled) return@Button
                        coroutineScope.launch(Dispatchers.IO) {
                            isEnabled = false
                            val jsEngine = engines[engine]!!(context)
                            runtime = runEngineTest(jsEngine)
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
                    engine = Engine.J2V8
                )
                BenchmarkButton(
                    text = "Run Javet",
                    engine = Engine.JAVET
                )
                BenchmarkButton(
                    text = "Run QuickJS",
                    engine = Engine.QUICKJS
                )
                BenchmarkButton(
                    text = "Run JavascriptSandbox",
                    engine = Engine.JAVASCRIPTSANDBOX
                )
                BenchmarkButton(
                    text = "Run Duktape",
                    engine = Engine.DUKTAPE
                )
                Button(
                    content = {
                        Text(text = "Run All Engines")
                    },
                    onClick = {
                        runAllEngines(coroutineScope)
                    }
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

                if (averageRuntimes.isNotEmpty()) {
                    Text(text = "Runtimes:")
                    averageRuntimes.forEach { (engine, runtimes) ->
                        val avgRuntime = runtimes.average().roundToInt()
                        Text(text = "$engine (${runtimes.size}) Avg Runtime: $avgRuntime ms")
                    }
                }
            }
        }
    }

    private fun runEngineTest(engine: JSEngine): Long {
        engine.init()
        val runtime = measureTimeMillis {
            for (i in 0..25) {
                engine.evaluate(benchmarkScript)
            }
        }
        engine.close()
        val engineName = engine::class.java.simpleName
        runtimes.update {
            val currentRuntimes = it[engineName] ?: emptyList()
            it + (engineName to (currentRuntimes + runtime))
        }
        return runtime
    }

    private fun runAllEngines(scope: CoroutineScope) {
        engines.forEach { (_, engineFactory) ->
            scope.launch(Dispatchers.IO) {
                val jsEngine = engineFactory(this@MainActivity)
                runEngineTest(jsEngine)
            }
        }
    }
}