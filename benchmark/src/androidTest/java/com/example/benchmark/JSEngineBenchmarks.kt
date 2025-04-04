package com.example.benchmark

import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.javascriptengine.JavaScriptSandbox
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.zipline.EngineApi
import app.cash.zipline.QuickJs
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.eclipsesource.v8.V8
import com.squareup.duktape.Duktape
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JSEngineBenchmarks {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    private val createLargeObjectFunction =
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
        """.trimIndent()

    private fun benchmarkScript(depth: Int = 3, breadth: Int = 5) =
        "JSON.stringify(createLargeObject($depth, $breadth))"

    @Test
    fun benchmarkJ2V8() {
        val runtime = V8.createV8Runtime()
        benchmarkRule.measureRepeated {
            runtime.executeVoidScript(createLargeObjectFunction)
            runtime.executeStringScript(benchmarkScript())
        }
        runtime.close()
    }

    @Test
    fun benchmarkJavet() {
        val runtime: V8Runtime = V8Host.getV8Instance().createV8Runtime()
        benchmarkRule.measureRepeated {
            runtime.getExecutor(createLargeObjectFunction).executeVoid()
            runtime.getExecutor(benchmarkScript()).executeString()
        }
        runtime.close()
    }

    @OptIn(EngineApi::class)
    @Test
    fun benchmarkQuickJS() {
        val runtime = QuickJs.create()
        benchmarkRule.measureRepeated {
            runtime.evaluate(createLargeObjectFunction)
            runtime.evaluate(benchmarkScript())
        }
        runtime.close()
    }

    @Test
    fun benchmarkAndroidJavascript() {
        val sandbox = JavaScriptSandbox.createConnectedInstanceAsync(context).get()
        val runtime = sandbox.createIsolate()
        benchmarkRule.measureRepeated {
            runtime.evaluateJavaScriptAsync(createLargeObjectFunction).get()
            runtime.evaluateJavaScriptAsync(benchmarkScript()).get()
        }
        runtime.close()
        sandbox.close()
    }

    @Test
    fun benchmarkDuktape() {
        val runtime = Duktape.create()
        benchmarkRule.measureRepeated {
            runtime.evaluate(createLargeObjectFunction)
            runtime.evaluate(benchmarkScript())
        }
        runtime.close()
    }
}