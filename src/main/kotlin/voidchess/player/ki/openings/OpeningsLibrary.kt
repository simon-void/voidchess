package voidchess.player.ki.openings

import voidchess.board.ChessGame
import voidchess.board.move.Move
import voidchess.helper.*

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.LinkedList


class OpeningsLibrary(relativePathToOpeningsFile: String) {
    private val openingsRootNode: TreeNode<String>

    init {
        val openingSequences = loadOpeningSequencesFromFile(relativePathToOpeningsFile)
        openingsRootNode = parseOpenings(openingSequences)
    }

    fun nextMove(history: String): List<Move> {
        var currentNode: TreeNode<String> = openingsRootNode
        if (history != "") {
            val moves = history.splitAndTrim(',')
            for (move in moves) {
                currentNode = currentNode.getChild(move) ?: return emptyList()
            }
        }
        val moveDescriptionsFound = currentNode.childData
        return moveDescriptionsFound.map {moveCode -> Move.byCode(moveCode)}
    }

    private fun loadOpeningSequencesFromFile(relativePathToOpeningsFile: String): List<String> {
        try {
            getResourceStream(relativePathToOpeningsFile).use { fileStream ->

                val openingSequences = LinkedList<String>()

                BufferedReader(InputStreamReader(fileStream)).use { reader ->
                    for(line: String in reader.readLines().trim()) {
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue
                        }
                        openingSequences.add(line)
                    }
                }

                return openingSequences
            }
        } catch (e: Exception) {
            return emptyList()
        }

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
            if (openingSequence.startsWith(separator)) {
                throw IllegalArgumentException("opening sequence starts with separator: $openingSequence")
            }
            if (openingSequence.endsWith(separator)) {
                throw IllegalArgumentException("opening sequence ends with separator: $openingSequence")
            }

            val textMoves = openingSequence.splitAndTrim(separator)
            val checkedMoves = ArrayList<String>(textMoves.size)

            val game = ChessGame()

            for (textMove in textMoves) {
                if (!Move.isValid(textMove)) {
                    throw IllegalArgumentException(
                            "illegal move format'$textMove' in opening sequence: $openingSequence")
                }
                val move = Move.byCode(textMove)
                val isMoveExecutable = game.isMovable(
                        move.from, move.to, game.isWhiteTurn
                )
                if (!isMoveExecutable) {
                    throw IllegalArgumentException(
                            "illegal move '$textMove' in opening sequence: $openingSequence")
                }
                game.move(move)

                checkedMoves.add(textMove)
            }
            return checkedMoves
        }
    }
}
