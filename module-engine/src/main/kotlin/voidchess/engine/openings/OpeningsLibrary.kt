package voidchess.engine.openings

import voidchess.common.board.BasicChessGameImpl
import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig
import voidchess.common.helper.TreeNode
import voidchess.common.helper.getResourceStream
import voidchess.common.helper.splitAndTrim
import voidchess.common.helper.trim
import voidchess.common.player.ki.ProgressCallback
import voidchess.common.player.ki.evaluation.EvaluatedMove
import voidchess.engine.concurrent.SingleThreadStrategy
import voidchess.engine.evaluation.AllMovesOrNonePruner
import voidchess.engine.evaluation.leaf.MiddleGameEval
import voidchess.engine.evaluation.MinMaxEval

import java.util.ArrayList
import kotlin.random.Random


internal class OpeningsLibrary(relativePathToOpeningsFile: String) {
    private val quickMinMaxEval: MinMaxEval = MinMaxEval(
        AllMovesOrNonePruner(1, 6, 1),
        MiddleGameEval
    )
    private val openingsRootNode: TreeNode<String>

    init {
        val openingSequences = loadOpeningSequencesFromFile(relativePathToOpeningsFile)
        openingsRootNode = parseOpenings(openingSequences)
    }

    private val maxDepth: Int get() = openingsRootNode.depth



    fun lookUpNextMove(
        startConfig: StartConfig,
        movesSoFar: List<Move>,
        progressCallback: ProgressCallback
    ): EvaluatedMove? {

        val chess960StartIndex: Int? = when (startConfig) {
            is StartConfig.ClassicConfig -> startConfig.chess960Index
            is StartConfig.Chess960Config -> startConfig.chess960Index
            is StartConfig.ManualConfig -> null
        }

        if (chess960StartIndex == null || movesSoFar.size >= maxDepth) {
            return null
        }

        return nextMove(chess960StartIndex, movesSoFar)?.let { libraryMove ->
            progressCallback(0, 1)
            SingleThreadStrategy.evaluateMove(
                StartConfig.Chess960Config(chess960StartIndex),
                movesSoFar,
                libraryMove,
                quickMinMaxEval
            ).also {
                val milliSecondsToWait = 200L
                runCatching { Thread.sleep(milliSecondsToWait) }
                progressCallback(1, 1)
            }
        }
    }

    private fun nextMove(
        chess960StartIndex: Int,
        moves: List<Move>
    ): Move? {
        // assume that the whole library is based on classic chess
        if(chess960StartIndex!=518) return null

        var currentNode: TreeNode<String> = openingsRootNode
        for (move in moves) {
            currentNode = currentNode.getChild(move.toString()) ?: return null
        }

        val movesFound: List<Move> = currentNode.childData.map { moveCode -> Move.byCode(moveCode)}
        return movesFound[Random.nextInt(movesFound.size)]
    }

    private fun loadOpeningSequencesFromFile(relativePathToOpeningsFile: String): List<String> = try {
        getResourceStream(javaClass, "module-engine", relativePathToOpeningsFile)
            .bufferedReader().useLines { lines ->
            lines.map { it.trim() }.filter { !(it.isEmpty() || it.startsWith('#')) }.toList()
        }
    } catch (e: Exception) {
        println("OpeningsLibrary couldn't load $relativePathToOpeningsFile: $e")
        emptyList()
    }

    private fun parseOpenings(openingSequences: List<String>): TreeNode<String> {
        val root = TreeNode.getRoot("root")

        for (openingSequence in openingSequences) {
            var currentNode = root
            val moves = splitAndCheckOpeningSequence(openingSequence)
            for (move in moves.trim()) {
                currentNode = currentNode.addChild(move)
            }
        }

        return root
    }

    companion object {

        fun splitAndCheckOpeningSequence(openingSequence: String): List<String> {
            if (openingSequence.isBlank()) {
                return emptyList()
            }

            val separator = ','
            require(!openingSequence.startsWith(separator)) { "opening sequence starts with separator: $openingSequence" }
            require(!openingSequence.endsWith(separator)) { "opening sequence ends with separator: $openingSequence" }

            val textMoves = openingSequence.splitAndTrim(separator)
            val checkedMoves = ArrayList<String>(textMoves.size)

            val game = BasicChessGameImpl(StartConfig.ClassicConfig)

            for (textMove in textMoves) {
                require(Move.isValid(textMove)) { "illegal move format'$textMove' in opening sequence: $openingSequence" }
                val move = Move.byCode(textMove)
                val isMoveExecutable = game.isMovable(
                        move.from, move.to
                )
                require(isMoveExecutable) { "illegal move '$textMove' in opening sequence: $openingSequence" }
                game.move(move)

                checkedMoves.add(textMove)
            }
            return checkedMoves
        }
    }
}
