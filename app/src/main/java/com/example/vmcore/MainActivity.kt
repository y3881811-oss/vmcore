package com.example.vmcore

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File

class MainActivity : Activity() {
    companion object {
        init {
            System.loadLibrary("vmcore")
        }
    }

    // 注意返回值变成了 String
    external fun loadElfNative(apkPath: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
        }

        val tv = TextView(this).apply {
            text = "点击下方按钮，启动底层内存映射。"
            textSize = 16f
            setPadding(0, 0, 0, 40)
        }

        val btnTest = Button(this).apply {
            text = "执行底层 ELF 解析"
            setOnClickListener {
                // 读取系统底层库测试
                val targetSoPath = "/system/lib64/libc.so" 
                val file = File(targetSoPath)
                
                val resultText = if (file.exists()) {
                    loadElfNative(targetSoPath)
                } else {
                    loadElfNative(applicationInfo.nativeLibraryDir + "/libvmcore.so")
                }
                
                // 直接把底层返回的结果贴在屏幕上！
                tv.text = resultText
            }
        }

        val scrollView = ScrollView(this).apply {
            addView(tv)
        }

        layout.addView(btnTest)
        layout.addView(scrollView)
        setContentView(layout)
    }
}
