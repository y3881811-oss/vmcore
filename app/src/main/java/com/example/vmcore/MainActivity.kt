package com.example.vmcore

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "VMCore 链路已通"
            textSize = 24f
            setPadding(64, 64, 64, 64)
        }
        setContentView(tv)
    }
}
