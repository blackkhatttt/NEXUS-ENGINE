cmake_minimum_required(VERSION 3.20)
project(NEXUS_ENGINE VERSION 0.1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_library(nexus_engine
    Engine/Core/Engine.cpp
    Engine/Core/Logger.cpp
    Engine/Scene/Node2D.cpp
)
target_include_directories(nexus_engine PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/Engine)
target_compile_features(nexus_engine PUBLIC cxx_std_20)

if(MSVC)
    target_compile_options(nexus_engine PRIVATE /W4 /permissive-)
else()
    target_compile_options(nexus_engine PRIVATE -Wall -Wextra -Wpedantic)
endif()

add_executable(nexus_demo Projects/Bootstrap/main.cpp)
target_link_libraries(nexus_demo PRIVATE nexus_engine)
