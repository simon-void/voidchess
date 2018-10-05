package voidchess.player.ki

import voidchess.board.ChessGameInterface
import voidchess.board.MoveResult
import voidchess.helper.PawnPromotion
import voidchess.helper.Position
import voidchess.helper.RuntimeFacade
import voidchess.player.PlayerInterface
import voidchess.player.ki.concurrent.ConcurrencyStrategy
import voidchess.player.ki.concurrent.ConcurrencyStrategyFactory
import voidchess.player.ki.evaluation.EvaluatedMove
import voidchess.player.ki.openings.OpeningsLibrary
import voidchess.ui.ComputerPlayerUI
import voidchess.ui.TableInterface
import voidchess.ui.Thumb
import java.util.NavigableSet
import java.util.Random


class ComputerPlayer(private val table: TableInterface, private val game: ChessGameInterface, private val ui: ComputerPlayerUI) : PlayerInterface {
    private val dynamicEvaluation: DynamicEvaluation
    private var standardPruner: SearchTreePruner
    private val standardEvaluation: StaticEvaluationInterface
    private var concurrencyStrategy: ConcurrencyStrategy
    private var usesStandardEvaluation: Boolean = false
    private val openingsLibrary: OpeningsLibrary
    private var useLibrary: Boolean = false
    private val randomNumberGenerator: Random
    private var isWhite = false

    init {
        standardPruner = SimplePruner(1, 2, 2)
        standardEvaluation = StaticEvaluation
        dynamicEvaluation = DynamicEvaluation(standardPruner, standardEvaluation)
        concurrencyStrategy = ConcurrencyStrategyFactory.getConcurrencyStrategy(ui, 1)
        openingsLibrary = OpeningsLibrary("openings.txt")
        randomNumberGenerator = Random()
        reset()
    }

    override fun play() {
        ui.setBubbleText(null)
        ui.showThoughts(true)
        val chosenMove = nextMove()
        ui.setValue(chosenMove.value)
        table.move(chosenMove.move)

        RuntimeFacade.collectGarbage()
    }

    //lets see if the library contains a next move, else we compute the next move
    private fun nextMove() = lookUpNextMove() ?: computeNextMove()

    private fun lookUpNextMove(): EvaluatedMove? {
        fun wait(milliseconds: Int) = try {Thread.sleep(milliseconds.toLong())} catch (e: InterruptedException) {}

        if (useLibrary) {
            val history = game.getCompleteHistory()
            val possibleMoves = openingsLibrary.nextMove(history)
            if (possibleMoves.isNotEmpty()) {
                //display that the computer is working
                ui.setProgress(0, 1)
                //pick a random move
                val randomMove = possibleMoves[randomNumberGenerator.nextInt(possibleMoves.size)]
                //and evaluate it
                val isWhitePlayer = game.isWhiteTurn
                game.move(randomMove)
                val evaluation = standardEvaluation.getPrimaryEvaluation(game, isWhitePlayer)
                standardEvaluation.addSecondaryEvaluation(game, isWhitePlayer, evaluation)
                game.undo()

                //wait before playing so that the user can clearly see the computer's move
                wait(300)

                return EvaluatedMove(randomMove, evaluation)
            }
        }
        //the library has no more information on this sequence
        useLibrary = false
        return null
    }

    private fun computeNextMove(): EvaluatedMove {
        //the game starts with a static game evaluation that considers many things
        //but in the endgame, when only the king of one side can move than the only important thing is
        //how many possible moves the king has left. So the Evaluation strategy has to be changed.
        pickStaticSpaceEvaluationIfNecessary()

        //      TODO  real Timer
        //		long time = System.currentTimeMillis();

        val sortedEvaluatedMoves = concurrencyStrategy.evaluatePossibleMoves(game, dynamicEvaluation)

        //		final int calls = StaticEvaluation.getCallCounter();
        //		final int totalNumberOfMoves = evaluation.totalNumberOfMoves();
        //		long duration = System.currentTimeMillis()-time;
        //		double timePerMove = duration/(double)totalNumberOfMoves;
        //		double timePerCall = duration/(double)calls;
        //		System.out.println( "ComputerPlayer.play()" );
        //		System.out.println( "moves:       "+totalNumberOfMoves );
        //		System.out.println( "staticEval:  "+calls );
        //		System.out.println( "duration:    "+(duration)+"ms" );
        //		System.out.println( "timePerMove: "+(timePerMove)+"ms" );
        //		System.out.println( "timePerCall: "+(timePerCall)+"ms" );
        //		System.out.println();

        return pickNextMoveByEvaluation(sortedEvaluatedMoves)
    }

    /**
     * pick the move to make given an evaluated Set of possible moves.
     * (Taking always the best move would lead to a limited number of openings!)
     *
     * @param sortedEvaluatedMoves (set.first is the best move for the ki, set.last the worst)
     * @return the move the ki will make next
     */
    private fun pickNextMoveByEvaluation(sortedEvaluatedMoves: NavigableSet<EvaluatedMove>): EvaluatedMove {
        val evaluation = sortedEvaluatedMoves.descendingIterator()
        val bestMove = evaluation.next()

        //as long as the top moves are almost equally good, pick randomly one (with a higher chance for the better move)
        var chosenMove = bestMove
        while (evaluation.hasNext()) {
            if (Math.random() < 0.6) break
            val tempMove = evaluation.next()
            if (tempMove.value.isCloseToByCombined(bestMove.value)) {
                chosenMove = tempMove
            } else {
                break
            }
        }
        return chosenMove
    }

    /**
     * the game starts with a static game evaluation that considers many things
     * but in the endgame, when only the king of one side can move than the
     * only important thing is how many possible moves the king has left.
     * So the Evaluation strategy has to be changed.
     */
    private fun pickStaticSpaceEvaluationIfNecessary() {
        if (usesStandardEvaluation && StaticSpaceEvaluation.shouldUseStaticSpaceEvaluation(game)) {
            // once per game
            dynamicEvaluation.setEvaluationStrategy(StaticSpaceEvaluation())
            dynamicEvaluation.setSearchTreePruner(FullMovePruner(2, 2, 2))
            usesStandardEvaluation = false
        }
    }

    override fun askForPawnPromotionType(pawnPosition: Position) = PawnPromotion.QUEEN

    override fun gameStarts() {
        reset()
        ui.reset()
    }

    override fun gameEnds(endoption: MoveResult, lastMoveByWhite: Boolean) {
        ui.showThoughts(false)
        when (endoption) {
            MoveResult.DRAW -> ui.setBubbleText("draw")
            MoveResult.STALEMATE -> ui.setBubbleText("stalemate")
            MoveResult.CHECKMATE -> {
                val didIWin = lastMoveByWhite == isWhite
                if (didIWin) {
                    ui.setBubbleText("checkmate")
                    ui.setThumb(Thumb.DOWN)
                } else {
                    ui.setBubbleText("good game")
                    ui.setThumb(Thumb.UP)
                }
            }
            MoveResult.THREE_TIMES_SAME_POSITION -> ui.setBubbleText("draw because of\n3x repetition")
            MoveResult.FIFTY_MOVES_NO_HIT -> ui.setBubbleText("draw because of\n50-move rule")
            MoveResult.RESIGN -> ui.setBubbleText("you're already\nresigning ?!")
            else -> ui.setBubbleText(endoption.toString())
        }
    }

    override fun gaveCheck() {
        ui.setBubbleText("check")
    }

    override fun setColor(isWhite: Boolean) {
        this.isWhite = isWhite
    }

    // initializes the default EvaluationStrategy
    private fun reset() {
        dynamicEvaluation.setEvaluationStrategy(standardEvaluation)
        dynamicEvaluation.setSearchTreePruner(standardPruner)
        usesStandardEvaluation = true
        //use the library only if the figures are used in the classical way (no Chess960)
        useLibrary = game.isStandardGame
    }

    fun setSearchTreePruner(pruner: SearchTreePruner) {
        standardPruner = pruner
        dynamicEvaluation.setSearchTreePruner(pruner)
    }

    fun setNumberOfCoresToUse(numberOfCoresToUse: Int) {
        concurrencyStrategy = ConcurrencyStrategyFactory.getConcurrencyStrategy(ui, numberOfCoresToUse)
    }
}
