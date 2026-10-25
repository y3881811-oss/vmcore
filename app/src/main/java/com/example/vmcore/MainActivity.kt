package com.example.vmcore

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import dalvik.system.DexClassLoader
import java.io.File

class MainActivity : Activity() {
    companion object {
        init {
            System.loadLibrary("vmcore")
        }
    }

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

        val btnElf = Button(this).apply {
            text = "1. 执行底层 ELF 解析"
            setOnClickListener {
                tv.text = loadElfNative("/system/lib64/libc.so")
            }
        }

        val btnDex = Button(this).apply {
            text = "2. 测试 Dex 动态加载"
            setOnClickListener {
                try {
                    val apkPath = applicationInfo.sourceDir
                    val dexOutputDir = getDir("dex", 0).absolutePath
                    val classLoader = DexClassLoader(apkPath, dexOutputDir, null, classLoader)
                    val targetClass = classLoader.loadClass("com.example.vmcore.MainActivity")
                    tv.text = "========== Dex 加载成功 ==========\n成功在沙盒环境中找到类:\n${targetClass.name}"
                } catch (e: Exception) {
                    tv.text = "Dex 加载失败: ${e.message}"
                }
            }
        }

        val btnStub = Button(this).apply {
            text = "3. 启动代理 StubActivity"
            setOnClickListener {
                val intent = Intent(this@MainActivity, StubActivity::class.java)
                startActivity(intent)
            }
        }

        // 新增：沙盒资源劫持测试
        val btnStealResource = Button(this).apply {
            text = "4. 越权读取外部 App 资源"
            setOnClickListener {
                try {
                    // 目标：系统设置 (如果找不到，就用系统UI)
                    val targetPkg = "com.android.settings"
                    val pm = packageManager
                    
                    // 1. 获取目标 App 的 APK 路径
                    val targetInfo = pm.getPackageInfo(targetPkg, 0)
                    val targetApkPath = targetInfo.applicationInfo.sourceDir
                    
                    // 2. 核心：用反射强行创建目标 App 的 AssetManager
                    val assetManager = AssetManager::class.java.newInstance()
                    val addAssetPath = AssetManager::class.java.getMethod("addAssetPath", String::class.java)
                    addAssetPath.invoke(assetManager, targetApkPath)

                    // 3. 用伪造的 AssetManager 创建目标 App 的 Resources
                    val targetResources = Resources(assetManager, resources.displayMetrics, resources.configuration)
                    
                    // 4. 读取目标 App 在系统里的名字（不需要安装包也能读！）
                    val appName = targetInfo.applicationInfo.loadLabel(pm).toString()
                    
                    tv.text = "========== 资源越权读取成功 ==========\n" +
                            "目标包名: $targetPkg\n" +
                            "目标应用名: $appName\n" +
                            "目标 APK 路径: $targetApkPath\n" +
                            "当前壳 App 包名: ${packageName}\n\n" +
                            "【下一步】我们可以把 $appName 的界面，直接渲染在我们的 StubActivity 里！"
                } catch (e: Exception) {
                    tv.text = "资源越权读取失败: ${e.message}"
                }
            }
        }

        val scrollView = ScrollView(this).apply {
            addView(tv)
        }

        layout.addView(btnElf)
        layout.addView(btnDex)
        layout.addView(btnStub)
        layout.addView(btnStealResource)
        layout.addView(scrollView)
        
        setContentView(layout)
    }
}
