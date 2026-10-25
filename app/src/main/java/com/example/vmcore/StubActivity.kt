package com.example.vmcore

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class StubActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "我是空壳 Activity (StubActivity)\n未来，这里将显示第三方 App 的界面！"
            textSize = 20f
            setPadding(64, 64, 64, 64)
        }
        setContentView(tv)
    }
}
