#include <android/log.h>
#include <dlfcn.h>
#include <fcntl.h>
#include <cstdarg>
#include <cstring>

#define TAG "VMCore-HOOK"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

typedef int (*orig_open_func_type)(const char *pathname, int flags, ...);
static orig_open_func_type orig_open = nullptr;

extern "C" int fake_open(const char *pathname, int flags, ...) {
    if (!orig_open) {
        orig_open = (orig_open_func_type) dlsym(RTLD_NEXT, "open");
    }

    // 简单演示：拦截特定路径，后续 LibOS 会在这里做真正的沙盒重定向
    if (strstr(pathname, "/data/data/com.example.targetapp/")) {
        LOGI("拦截到目标打开请求: %s", pathname);
        const char* fake_path = "/data/local/tmp/fake_sandbox_file.txt";
        return orig_open(fake_path, flags);
    }

    return orig_open(pathname, flags);
}
