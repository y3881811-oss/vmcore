#include <jni.h>
#include <string>

extern std::string load_elf_segments(const char* path);

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_vmcore_MainActivity_loadElfNative(JNIEnv* env, jobject, jstring apk_path) {
    const char* path = env->GetStringUTFChars(apk_path, nullptr);
    // 调用底层，获取字符串结果
    std::string result = load_elf_segments(path);
    env->ReleaseStringUTFChars(apk_path, path);
    
    // 将 C++ string 转成 Java String 返回
    return env->NewStringUTF(result.c_str());
}
