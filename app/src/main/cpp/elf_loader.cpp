#include <android/log.h>
#include <elf.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <cstring>

#define TAG "VMCore-ELF"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

void load_elf_segments(const char* path) {
    int fd = open(path, O_RDONLY);
    if (fd < 0) {
        LOGE("无法打开 ELF 文件: %s", path);
        return;
    }

    Elf64_Ehdr ehdr;
    if (pread(fd, &ehdr, sizeof(ehdr), 0) != sizeof(ehdr)) {
        LOGE("读取 ELF Header 失败");
        close(fd);
        return;
    }

    LOGI("========== 开始解析 ELF ==========");
    LOGI("目标架构: %s", ehdr.e_machine == EM_AARCH64 ? "ARM64" : "未知");
    LOGI("段数量 (Program Headers): %d", ehdr.e_phnum);

    Elf64_Phdr* phdrs = (Elf64_Phdr*) malloc(sizeof(Elf64_Phdr) * ehdr.e_phnum);
    pread(fd, phdrs, sizeof(Elf64_Phdr) * ehdr.e_phnum, ehdr.e_phoff);

    size_t page_size = sysconf(_SC_PAGESIZE);

    for (int i = 0; i < ehdr.e_phnum; i++) {
        Elf64_Phdr ph = phdrs[i];
        
        if (ph.p_type == PT_LOAD) {
            LOGI("发现 PT_LOAD 段: vaddr=0x%lx, filesz=%ld, memsz=%ld", 
                 ph.p_vaddr, ph.p_filesz, ph.p_memsz);
                 
            size_t map_size = (ph.p_memsz + page_size - 1) & ~(page_size - 1);

            void* map_addr = mmap(NULL, map_size, PROT_READ | PROT_WRITE | PROT_EXEC, 
                                  MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
                                  
            if (map_addr == MAP_FAILED) {
                LOGE("内存映射失败 (mmap)");
                continue;
            }

            pread(fd, map_addr, ph.p_filesz, ph.p_offset);
            
            LOGI("成功将数据段映射到虚拟地址: %p", map_addr);
        }
    }

    free(phdrs);
    close(fd);
    LOGI("========== ELF 解析结束 ==========");
}
