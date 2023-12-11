package com.example.jsengines.engines

import app.cash.zipline.EngineApi
import app.cash.zipline.QuickJs
import com.example.jsengines.JSEngine

@OptIn(EngineApi::class)
class QuickJSEngineImpl : JSEngine {

    private lateinit var runtime: QuickJs

    override fun init() {
        runtime = QuickJs.create()
    }

    override fun evaluate(script: String): String =
        runtime.evaluate(script) as String

    override fun close() =
        runtime.close()
}