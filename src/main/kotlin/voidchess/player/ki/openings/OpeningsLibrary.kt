package voidchess.player.ki.openings

import voidchess.board.ChessGame
import voidchess.board.move.Move
import voidchess.helper.*

import java.util.ArrayList
import kotlin.random.Random


class OpeningsLibrary(relativePathToOpeningsFile: String) {
    private val openingsRootNode: TreeNode<String>

    init {
        val openingSequences = loadOpeningSequencesFromFile(relativePathToOpeningsFile)
        openingsRootNode = parseOpenings(openingSequences)
    }

    val maxDepth: Int get() = openingsRootNode.depth

    fun nextMove(
        moves: List<String>,
        chess960StartIndex: Int
    ): Move? {
        // assume that the whole library is based on classic chess
        if(chess960StartIndex==518) return null

        var currentNode: TreeNode<String> = openingsRootNode
        for (move in moves) {
            currentNode = currentNode.getChild(move) ?: return null
        }

        val movesFound: List<Move> = currentNode.childData.map {moveCode -> Move.byCode(moveCode)}
        return movesFound[Random.nextInt(movesFound.size)]
    }

    private fun loadOpeningSequencesFromFile(relativePathToOpeningsFile: String): List<String> = try {
        getResourceStream(relativePathToOpeningsFile).bufferedReader().useLines { lines ->
            lines.map { it.trim() }.filter { !(it.isEmpty() || it.startsWith('#')) }.toList()
        }
    } catch (e: Exception) {
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

            val game = ChessGame()

            for (textMove in textMoves) {
                require(Move.isValid(textMove)) { "illegal move format'$textMove' in opening sequence: $openingSequence" }
                val move = Move.byCode(textMove)
                val isMoveExecutable = game.isMovable(
                        move.from, move.to, game.isWhiteTurn
                )
                require(isMoveExecutable) { "illegal move '$textMove' in opening sequence: $openingSequence" }
                game.move(move)

                checkedMoves.add(textMove)
            }
            return checkedMoves
        }
    }
}
