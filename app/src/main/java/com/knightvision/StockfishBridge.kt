package com.knightvision

object StockfishBridge {
    init {
        System.loadLibrary("stockfish")
    }

    var enginePtr: Long? = null

    private external fun _initEngine(): Long
    private external fun runCmd(enginePtr: Long, cmd: String): String
    private external fun goBlocking(enginePtr: Long, depth: Int): String
    external fun validFen(fen: String): Boolean

    fun initEngine() {
        enginePtr = _initEngine()

        // runCmd("setoption name Threads value 1")
    }

    // TODO add thread safety to this object

    fun runCmd(cmd: String): String {
        if (this.enginePtr == null) {
            throw RuntimeException("Trying to run stockfish command before initialising engine.")
        }
        return runCmd(this.enginePtr!!, cmd)
    }

    fun goBlocking(depth: Int): String {
        if (this.enginePtr == null) {
            throw RuntimeException("Trying to search game tree before initialising engine.")
        }
        return goBlocking(this.enginePtr!!, depth)
    }
}
