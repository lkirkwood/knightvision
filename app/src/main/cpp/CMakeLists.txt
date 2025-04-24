cmake_minimum_required(VERSION 3.10)
project(stockfish)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

file(GLOB_RECURSE STOCKFISH_SRC stockfish/src/*.h stockfish/src/*.cpp stockfish/src/*.nnue)
set(WRAPPER_SRC stockfish/src/uci_wrapper.cpp)

add_library(stockfish SHARED ${STOCKFISH_SRC} ${WRAPPER_SRC})
