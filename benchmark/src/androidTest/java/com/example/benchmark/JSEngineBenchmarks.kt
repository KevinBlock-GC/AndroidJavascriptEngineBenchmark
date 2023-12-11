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

    lateinit var context: Context

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
        benchmarkRule.measureRepeated {
            val runtime = runWithTimingDisabled { V8.createV8Runtime() }
            runtime.executeVoidScript(createLargeObjectFunction)
            for (i in 0..100) {
                runtime.executeStringScript(benchmarkScript())
            }
            runWithTimingDisabled {
                runtime.close()
                System.gc()
            }
        }
    }

    @Test
    fun benchmarkJavet() {
        benchmarkRule.measureRepeated {
            val runtime: V8Runtime =
                runWithTimingDisabled { V8Host.getV8Instance().createV8Runtime() }
            runtime.getExecutor(createLargeObjectFunction).executeVoid()
            for (i in 0..100) {
                runtime.getExecutor(benchmarkScript()).executeString()
            }
            runWithTimingDisabled {
                runtime.close()
                System.gc()
            }
        }
    }

    @OptIn(EngineApi::class)
    @Test
    fun benchmarkQuickJS() {
        benchmarkRule.measureRepeated {
            val runtime = runWithTimingDisabled { QuickJs.create() }
            runtime.evaluate(createLargeObjectFunction)
            for (i in 0..100) {
                runtime.evaluate(benchmarkScript())
            }
            runWithTimingDisabled {
                runtime.close()
                System.gc()
            }
        }
    }

    @Test
    fun benchmarkAndroidJavascript() {
        val sandbox = JavaScriptSandbox.createConnectedInstanceAsync(context).get()
        benchmarkRule.measureRepeated {
            val runtime = runWithTimingDisabled { sandbox.createIsolate() }
            runtime.evaluateJavaScriptAsync(createLargeObjectFunction).get()
            for (i in 0..100) {
                runtime.evaluateJavaScriptAsync(benchmarkScript()).get()
            }
            runWithTimingDisabled {
                runtime.close()
                System.gc()
            }
        }
    }

    @Test
    fun benchmarkDuktape() {
        benchmarkRule.measureRepeated {
            val runtime = runWithTimingDisabled { Duktape.create() }
            runtime.evaluate(createLargeObjectFunction)
            for (i in 0..100) {
                runtime.evaluate(benchmarkScript())
            }
            runWithTimingDisabled {
                runtime.close()
                System.gc()
            }
        }
    }
}