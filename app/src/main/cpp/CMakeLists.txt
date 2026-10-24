cmake_minimum_required(VERSION 3.22.1)
project("vmcore")

# 让 CMake 生成 compile_commands.json 供 clangd 读取
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

add_library(vmcore SHARED 
    native-lib.cpp
    elf_loader.cpp
    syscall_hook.cpp
)

find_library(log-lib log)
find_library(dl-lib dl)

target_link_libraries(vmcore ${log-lib} ${dl-lib})
