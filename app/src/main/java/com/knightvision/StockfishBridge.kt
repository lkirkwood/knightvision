package com.knightvision

object StockfishBridge {
    init {
        System.loadLibrary("stockfish")
    }

    var enginePtr: Long? = null
    var bestmoveOutputPtr: Long? = null

    private external fun _initEngine(): LongArray
    private external fun runCmd(enginePtr: Long, cmd: String): String
    private external fun bestmove(enginePtr: Long, outputPtr: Long): String
    private external fun goBlocking(enginePtr: Long, outputPtr: Long, depth: Int): String

    fun initEngine() {
        val pointers = _initEngine()
        enginePtr = pointers[0]
        bestmoveOutputPtr = pointers[1]

        // runCmd("setoption name Threads value 1")
    }

    // TODO add thread safety to this object

    fun runCmd(cmd: String): String {
        if (this.enginePtr == null) {
            throw RuntimeException("Trying to run stockfish command before initialising engine.")
        }
        return runCmd(this.enginePtr!!, cmd)
    }

    fun bestmove(): String {
        if (this.enginePtr == null || this.bestmoveOutputPtr == null) {
            throw RuntimeException("Trying to get best move from stockfish before initialising engine.")
        }
        return bestmove(this.enginePtr!!, this.bestmoveOutputPtr!!)
    }

    fun goBlocking(depth: Int): String {
        if (this.enginePtr == null || this.bestmoveOutputPtr == null) {
            throw RuntimeException("Trying to search game tree before initialising engine.")
        }
        return goBlocking(this.enginePtr!!, this.bestmoveOutputPtr!!, depth)
    }
}
