package com.example.jsengines.engines

import com.eclipsesource.v8.V8
import com.example.jsengines.JSEngine

class J2V8EngineImpl : JSEngine {

    private lateinit var runtime: V8

    override fun init() {
        runtime = V8.createV8Runtime()
    }

    override fun evaluate(script: String): String =
        (runtime.executeScript(script) as? String) ?: ""

    override fun close() =
        runtime.close()
}