#include <android/log.h>
#include <elf.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <cstring>
#include <cstdlib>
#include <string>
#include <sstream>

#define TAG "VMCore-ELF"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// 返回值改成 std::string，方便传回给 Kotlin
std::string load_elf_segments(const char* path) {
    std::stringstream result;
    int fd = open(path, O_RDONLY);
    if (fd < 0) {
        result << "无法打开 ELF 文件: " << path << "\n";
        return result.str();
    }

    Elf64_Ehdr ehdr;
    if (pread(fd, &ehdr, sizeof(ehdr), 0) != sizeof(ehdr)) {
        result << "读取 ELF Header 失败\n";
        close(fd);
        return result.str();
    }

    result << "========== 开始解析 ELF ==========\n";
    result << "目标路径: " << path << "\n";
    result << "目标架构: " << (ehdr.e_machine == EM_AARCH64 ? "ARM64" : "未知") << "\n";
    result << "段数量: " << (int)ehdr.e_phnum << "\n";

    Elf64_Phdr* phdrs = (Elf64_Phdr*) malloc(sizeof(Elf64_Phdr) * ehdr.e_phnum);
    pread(fd, phdrs, sizeof(Elf64_Phdr) * ehdr.e_phnum, ehdr.e_phoff);

    size_t page_size = sysconf(_SC_PAGESIZE);
    int success_count = 0;

    for (int i = 0; i < ehdr.e_phnum; i++) {
        Elf64_Phdr ph = phdrs[i];
        
        if (ph.p_type == PT_LOAD) {
            size_t map_size = (ph.p_memsz + page_size - 1) & ~(page_size - 1);

            void* map_addr = mmap(NULL, map_size, PROT_READ | PROT_WRITE | PROT_EXEC, 
                                  MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
                                  
            if (map_addr != MAP_FAILED) {
                pread(fd, map_addr, ph.p_filesz, ph.p_offset);
                result << "[+] 段 " << i << " 映射成功, 虚拟地址: " << map_addr << "\n";
                success_count++;
            } else {
                result << "[-] 段 " << i << " 映射失败\n";
            }
        }
    }

    free(phdrs);
    close(fd);
    result << "========== 解析结束, 成功映射 " << success_count << " 个段 ==========\n";
    return result.str();
}
