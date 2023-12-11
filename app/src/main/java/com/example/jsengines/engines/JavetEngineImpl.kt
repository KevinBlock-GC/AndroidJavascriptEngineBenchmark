package com.example.jsengines.engines

import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.example.jsengines.JSEngine

class JavetEngineImpl : JSEngine {

    private lateinit var runtime: V8Runtime

    override fun init() {
        runtime = V8Host.getV8Instance().createV8Runtime()
    }

    override fun evaluate(script: String): String =
        runtime.getExecutor(script).executeString()

    override fun close() =
        runtime.close()
}