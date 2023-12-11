package com.example.jsengines.engines

import com.example.jsengines.JSEngine
import com.squareup.duktape.Duktape

class DuktapeEngineImpl : JSEngine {

    private lateinit var runtime: Duktape

    override fun init() {
        runtime = Duktape.create()
    }

    override fun evaluate(script: String): String =
        runtime.evaluate(script) as String

    override fun close() =
        runtime.close()
}