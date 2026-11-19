cmake_minimum_required(VERSION 3.20)
project(NEXUS_ENGINE VERSION 0.1.0 LANGUAGES CXX)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(NEXUS_ENGINE_SOURCES
    Engine/Core/Engine.cpp
    Engine/Core/Logger.cpp
    Engine/Scene/Node2D.cpp
    Scripting/NXS/Lexer.cpp
    Scripting/NXS/Runtime.cpp
)
add_library(nexus_engine STATIC \${NEXUS_ENGINE_SOURCES})
target_include_directories(nexus_engine PUBLIC
    \${CMAKE_CURRENT_SOURCE_DIR}/Engine
    \${CMAKE_CURRENT_SOURCE_DIR}
)
target_compile_features(nexus_engine PUBLIC cxx_std_20)
if(MSVC)
    target_compile_options(nexus_engine PRIVATE /W4 /permissive-)
else()
    target_compile_options(nexus_engine PRIVATE -Wall -Wextra -Wpedantic)
endif()
if(ANDROID)
    add_library(nexus_android SHARED Platform/Android/AndroidEntry.cpp)
    target_link_libraries(nexus_android PRIVATE nexus_engine)
    target_compile_features(nexus_android PRIVATE cxx_std_20)
else()
    add_executable(nexus_demo Projects/Bootstrap/main.cpp)
    target_link_libraries(nexus_demo PRIVATE nexus_engine)
endif()
