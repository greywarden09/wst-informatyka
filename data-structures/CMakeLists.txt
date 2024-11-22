cmake_minimum_required(VERSION 3.31)
project(data_structures)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_executable(data_structures src/main/main.cpp)

add_library(data_structures_lib
        src/main/common/list.hpp
        src/main/linked_list/linked_list.hpp
)
set_target_properties(data_structures_lib PROPERTIES LINKER_LANGUAGE CXX)

target_link_libraries(data_structures PRIVATE data_structures_lib)

# Testing -> Google Test
include(FetchContent)
include(GoogleTest)

enable_testing()

FetchContent_Declare(
        googletest
        URL https://github.com/google/googletest/archive/35d0c365609296fa4730d62057c487e3cfa030ff.zip
)

set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

file(GLOB_RECURSE TEST_SOURCES "src/test/**/*.cpp")

add_executable(data_structures_test ${TEST_SOURCES})
target_link_libraries(data_structures_test
        PRIVATE
        data_structures_lib
        GTest::gtest_main)

gtest_discover_tests(data_structures_test)
