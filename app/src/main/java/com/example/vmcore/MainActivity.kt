package com.example.vmcore

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import dalvik.system.DexClassLoader

class MainActivity : Activity() {
    companion object {
        init {
            System.loadLibrary("vmcore")
        }
    }

    // JNI 接口
    external fun loadElfNative(apkPath: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
        }

        val tv = TextView(this).apply {
            text = "LibOS 核心功能测试台\n请按顺序点击下方按钮"
            textSize = 16f
            setPadding(0, 0, 0, 30)
        }

        // 按钮 1：底层 ELF 解析
        val btnElf = Button(this).apply {
            text = "1. 执行底层 ELF 解析"
            setOnClickListener {
                tv.text = loadElfNative("/system/lib64/libc.so")
            }
        }

        // 按钮 2：Dex 动态加载
        val btnDex = Button(this).apply {
            text = "2. 测试 Dex 动态加载"
            setOnClickListener {
                try {
                    val apkPath = applicationInfo.sourceDir
                    val dexOutputDir = getDir("dex", 0).absolutePath
                    
                    val classLoader = DexClassLoader(apkPath, dexOutputDir, null, classLoader)
                    val targetClass = classLoader.loadClass("com.example.vmcore.MainActivity")
                    
                    tv.text = "========== Dex 加载成功 ==========\n" +
                            "目标 APK: $apkPath\n" +
                            "成功在沙盒环境中找到类:\n" +
                            targetClass.name
                } catch (e: Exception) {
                    tv.text = "Dex 加载失败: ${e.message}"
                }
            }
        }

        // 按钮 3：启动代理 StubActivity
        val btnStub = Button(this).apply {
            text = "3. 启动代理 StubActivity"
            setOnClickListener {
                val intent = Intent(this@MainActivity, StubActivity::class.java)
                startActivity(intent)
            }
        }

        val scrollView = ScrollView(this).apply {
            addView(tv)
        }

        layout.addView(btnElf)
        layout.addView(btnDex)
        layout.addView(btnStub)
        layout.addView(scrollView)
        
        setContentView(layout)
    }
}
