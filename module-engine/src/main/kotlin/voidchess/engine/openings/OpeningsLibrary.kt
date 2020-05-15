package voidchess.engine.openings

import voidchess.common.board.BasicChessGameImpl
import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig
import voidchess.common.helper.TreeNode
import voidchess.common.helper.getResourceStream
import voidchess.common.helper.splitAndTrim
import voidchess.common.engine.ProgressCallback
import voidchess.common.engine.EvaluatedMove
import voidchess.engine.concurrent.SingleThreadStrategy
import voidchess.engine.evaluation.AllMovesOrNonePruner
import voidchess.engine.evaluation.leaf.MiddleGameEval
import voidchess.engine.evaluation.MinMaxEval

import java.util.ArrayList


internal class OpeningsLibrary(
    openingSequences: List<String>
) {
    private val openingsRootNode: TreeNode<KnownMove, String> = parseOpenings(openingSequences)
    private val quickMinMaxEval: MinMaxEval = MinMaxEval(
        AllMovesOrNonePruner(1, 6, 1),
        MiddleGameEval
    )

    private val maxDepth: Int get() = openingsRootNode.depth

    suspend fun lookUpNextMove(
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

        var currentNode: TreeNode<KnownMove, String> = openingsRootNode
        for (move in moves) {
            currentNode = currentNode.getChild(move.toString()) ?: return null
        }

        val movesFound: List<Move> = currentNode.childData.filter { it.isRecommended }.map { knownMove -> Move.byCode(knownMove.move)}
        return movesFound.shuffled().firstOrNull()
    }

    private fun parseOpenings(openingSequences: List<String>): TreeNode<KnownMove, String> {
        val root = TreeNode.getRoot(KnownMove("root", false)) { knownMove ->
            knownMove.move
        }

        for (openingSequence in openingSequences) {
            var currentNode = root
            val knownMoves = splitAndCheckOpeningSequence(openingSequence)
            for (knownMove in knownMoves) {
                currentNode = currentNode.addChild(knownMove)
            }
        }

        return root
    }

    companion object {

        fun loadFromFile(relativePathToOpeningsFile: String): OpeningsLibrary {
            val openingSequence = try {
                getResourceStream(OpeningsLibrary::class.java, "module-engine", relativePathToOpeningsFile)
                    .bufferedReader().useLines { lines ->
                        lines.map { it.trim() }.filter { !(it.isEmpty() || it.startsWith('#')) }.toList()
                    }
            } catch (e: Exception) {
                println("OpeningsLibrary couldn't load $relativePathToOpeningsFile: $e")
                emptyList<String>()
            }
            return OpeningsLibrary(openingSequence)
        }

        fun splitAndCheckOpeningSequence(openingSequence: String): List<KnownMove> {
            if (openingSequence.isBlank()) {
                return emptyList()
            }

            val separator = ','
            require(!openingSequence.startsWith(separator)) { "opening sequence starts with separator: $openingSequence" }
            require(!openingSequence.endsWith(separator)) { "opening sequence ends with separator: $openingSequence" }

            val textMoves = openingSequence.splitAndTrim(separator)
            val checkedMoves = ArrayList<KnownMove>(textMoves.size)

            val game = BasicChessGameImpl(StartConfig.ClassicConfig)

            for (textMove in textMoves) {
                val knownMove = KnownMove.from(textMove)
                val move = Move.byCode(knownMove.move)
                val isMoveExecutable = game.isMovable(move.from, move.to)
                require(isMoveExecutable) { "illegal move '$textMove' in opening sequence: $openingSequence" }
                game.move(move)

                checkedMoves.add(knownMove)
            }
            return checkedMoves
        }
    }
}

data class KnownMove(
    val move: String,
    val isRecommended: Boolean
): Comparable<KnownMove> {
    companion object {
        private val moveFromBookRegex = """^([a-h][1-8]-[a-h][1-8])|(\([a-h][1-8]-[a-h][1-8]\))$""".toRegex()
        fun from(text: String): KnownMove {
            require(moveFromBookRegex matches text) {"text: $text doesn't match the expected pattern of either 'e1-d2' or '(e1-d2)'"}
            val isRecommended: Boolean
            val move: String
            if(text.startsWith('(')) {
                isRecommended = false
                move = text.substring(1, 6) // remove the outer brackets
            }else{
                isRecommended = true
                move = text
            }
            return KnownMove(move, isRecommended)
        }
    }

    override fun compareTo(other: KnownMove) = move.compareTo(other.move)
}