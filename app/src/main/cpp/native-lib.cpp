#include <jni.h>
#include <android/log.h>

extern void load_elf_segments(const char* path);

extern "C" JNIEXPORT void JNICALL
Java_com_example_vmcore_MainActivity_loadElfNative(JNIEnv* env, jobject, jstring apk_path) {
    const char* path = env->GetStringUTFChars(apk_path, nullptr);
    load_elf_segments(path);
    env->ReleaseStringUTFChars(apk_path, path);
}
