package com.example.jsengines.engines

import android.content.Context
import androidx.javascriptengine.IsolateStartupParameters
import androidx.javascriptengine.JavaScriptIsolate
import androidx.javascriptengine.JavaScriptSandbox
import com.example.jsengines.JSEngine

class JavascriptSandboxImpl(
    private val context: Context,
    private val startupParameters: IsolateStartupParameters = IsolateStartupParameters()
) : JSEngine {
    companion object {
        var sandbox: JavaScriptSandbox? = null
    }

    private lateinit var runtime: JavaScriptIsolate
    override fun init() {
        if (sandbox == null) {
            sandbox = JavaScriptSandbox.createConnectedInstanceAsync(context).get()
        }
        runtime = sandbox!!.createIsolate(startupParameters)
    }

    override fun evaluate(script: String): String =
        runtime.evaluateJavaScriptAsync(script).get()

    override fun close() =
        runtime.close()
}