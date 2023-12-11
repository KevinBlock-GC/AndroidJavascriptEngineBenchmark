package com.example.jsengines

interface JSEngine {
    fun init()
    fun evaluate(script: String): String
    fun close()
}