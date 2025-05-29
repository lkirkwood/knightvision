package com.knightvision

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.knightvision.ui.screens.SettingsViewModel
import com.knightvision.BoardState

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

class Evaluation(val bestMove: String, val ponder: String, val score: String)

suspend fun evaluateFen(fen: String, depth: Int): Evaluation = withContext(Dispatchers.Default) {
    StockfishBridge.runCmd("position fen " + fen)
    val outputLines = StockfishBridge.goBlocking(depth).split("\n").dropLast(1)
    val bestmoveParts = outputLines.last().split(" ")

    val lastInfo = outputLines.dropLast(2).last()
    var seenCpTag = false
    val cpEval = run breakval@ {
        for (part in lastInfo.split(" ")) {
            if (seenCpTag) {
                return@breakval part
            } else if (part == "cp") {
                seenCpTag = true
            }
        }
        Log.e("com.knightvision", "Couldnt find centipawn score in last info: " + lastInfo)
        "unknown"
    }

    if (bestmoveParts.size < 4) {
        Log.e("com.knightvision", "Unexpected bestmove output: ${bestmoveParts.joinToString(" ")}")
        Evaluation("unknown", "unknown", cpEval)
    } else {
        Log.e(
            "com.knightvision",
            "Successfully set evaluation; bestmove: ${bestmoveParts[1]}, ponder: ${bestmoveParts[3]}, eval: $cpEval"
        )
        Evaluation(bestmoveParts[1], bestmoveParts[3], cpEval)
    }
}

const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

class BoardEvaluationViewModel : ViewModel() {
    var boardState by mutableStateOf(BoardState(STARTING_FEN))


    var stockfishReady by mutableStateOf(false)

    fun setReady(readiness: Boolean) {
        stockfishReady = readiness
        runEvaluation()
    }

    var analysisComplete by mutableStateOf(false)

    fun setComplete(completeness: Boolean) {
        analysisComplete = completeness
        runEvaluation()
    }

    var boardEval by mutableStateOf(Evaluation("e2e4", "e7e5", "+0.99"))
    fun runEvaluation() {
        if (stockfishReady && !analysisComplete) {
            viewModelScope.launch {
                boardEval = evaluateFen(boardState.boardFen, 10)
                analysisComplete = true
            }
        }
    }

}
