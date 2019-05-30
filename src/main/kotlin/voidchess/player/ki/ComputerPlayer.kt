package voidchess.player.ki

import voidchess.board.ChessGameInterface
import voidchess.board.move.Move
import voidchess.board.move.MoveResult
import voidchess.board.move.PawnPromotion
import voidchess.board.move.Position
import voidchess.helper.RuntimeFacade
import voidchess.player.PlayerInterface
import voidchess.player.ki.concurrent.ConcurrencyStrategy
import voidchess.player.ki.concurrent.getConcurrencyStrategy
import voidchess.player.ki.evaluation.*
import voidchess.player.ki.openings.OpeningsLibrary
import voidchess.ui.ComputerPlayerUI
import voidchess.ui.TableInterface
import voidchess.ui.Thumb
import java.text.DecimalFormat
import java.util.*


class ComputerPlayer(private val table: TableInterface, private val game: ChessGameInterface, private val ui: ComputerPlayerUI) : PlayerInterface {
    private val evaluatingMinMax: EvaluatingMinMax
    private var standardPruner: SearchTreePruner
    private val standardEvaluation: EvaluatingStatically
    private var concurrencyStrategy: ConcurrencyStrategy
    private var usesStandardEvaluation: Boolean = false
    private val openingsLibrary: OpeningsLibrary
    private var useLibrary: Boolean = false
    private val randomNumberGenerator: Random
    private var isWhite = false

    init {
        standardPruner = PrunerWithIrreversibleMoves(1, 2, 2)
        standardEvaluation = EvaluatingAsIsNow
        evaluatingMinMax = EvaluatingMinMax(standardPruner, standardEvaluation)
        concurrencyStrategy = getConcurrencyStrategy(ui::setProgress, 1)
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
                val preliminaryEvaluation = standardEvaluation.getPreliminaryEvaluation(game, isWhitePlayer)
                val fullEvaluation = preliminaryEvaluation + standardEvaluation.getSecondaryEvaluation(game, isWhitePlayer)

                game.undo()

                //wait before playing so that the user can clearly see the computer's move
                wait(300)

                return EvaluatedMove(randomMove, Ongoing(preliminaryEvaluation, fullEvaluation))
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

        val sortedEvaluatedMoves = concurrencyStrategy.evaluateMovesBestMoveFirst(game, evaluatingMinMax)

        //		final int calls = EvaluatingAsIsNow.getCallCounter();
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
    private fun pickNextMoveByEvaluation(sortedEvaluatedMoves: List<EvaluatedMove>): EvaluatedMove {
        val bestMove: EvaluatedMove = sortedEvaluatedMoves.first()

        if (bestMove.value !is NumericalEvaluation) {
            return bestMove
        }

        fun pickEvaluateMoveBy(move: Move) = sortedEvaluatedMoves.first { it.move.equalsMove(move) }

        val okDistanceToBest = .2

        // the weight lies between (0-1]
        // with bestMove will have a weight of 1
        // and a move that is almost okDistanceToBest apart will have a weight of almost 0
        val moveAndLinearWeight: List<Pair<Move, Double>> = LinkedList<Pair<Move, Double>>().apply {
            val bestFullEvaluation = bestMove.value.fullEvaluation
            for((move, evaluation) in sortedEvaluatedMoves) {
                if(evaluation !is NumericalEvaluation) break
                val distanceToBest = bestFullEvaluation-evaluation.fullEvaluation
                if(distanceToBest>=okDistanceToBest) break
                add(Pair(move, (okDistanceToBest-distanceToBest)/okDistanceToBest))
            }
        }
        println("considering: ${moveAndLinearWeight.map { it.first.toString() }.joinToString()}")

        require(moveAndLinearWeight.isNotEmpty())
        if(moveAndLinearWeight.size==1) {
            return pickEvaluateMoveBy(moveAndLinearWeight.first().first)
        }

//        fun printMovesAndWeight(moveAndPercentage: List<Pair<Move, Double>>) {
//            val formatter = DecimalFormat().apply {
//                minimumFractionDigits = 2
//                maximumFractionDigits = 2
//            }
//
//            fun percentageToString(value: Double): String = formatter.format(value)
//            println(moveAndPercentage.joinToString { "${it.first}:${percentageToString(it.second)}" })
//        }
//        printMovesAndWeight(moveAndLinearWeight)

        // make it more than linear probable to pick a better move (the bigger the factor, the more preferable better solutions are)
        val moveAndWeight = moveAndLinearWeight.map { Pair(it.first, Math.pow(it.second, 1.8)) }
        val weightSum = moveAndWeight.map { it.second }.sum()
        // the sum of all percentages will be 1.0 (or close to it because of rounding errors)
        val moveAndPercentage = moveAndWeight.map { Pair(it.first, it.second/weightSum) }

//        fun printMovesAndPercentages(moveAndPercentage: List<Pair<Move, Double>>) {
//            val formatter = DecimalFormat().apply {
//                minimumFractionDigits = 1
//                maximumFractionDigits = 1
//            }
//
//            fun percentageToString(value: Double): String = "${formatter.format(100*value)}%"
//            println(moveAndPercentage.joinToString { "${it.first}:${percentageToString(it.second)}" })
//        }
//        printMovesAndPercentages(moveAndPercentage)


        var randomValueInbetween0and1 = randomNumberGenerator.nextDouble()
        val moveAndPercentageIter = moveAndPercentage.iterator()
        var moveWithPercentage = moveAndPercentageIter.next()
        while(moveAndPercentageIter.hasNext() && randomValueInbetween0and1>moveWithPercentage.second) {
            randomValueInbetween0and1 -= moveWithPercentage.second
            moveWithPercentage = moveAndPercentageIter.next()
        }

        return pickEvaluateMoveBy(moveWithPercentage.first)
    }

    /**
     * the game starts with a static game evaluation that considers many things
     * but in the endgame, when only the king of one side can move than the
     * only important thing is how many possible moves the king has left.
     * So the Evaluation strategy has to be changed.
     */
    private fun pickStaticSpaceEvaluationIfNecessary() {
        // TODO implement better endgame engine
//        if (usesStandardEvaluation && EvaluatingSpace.shouldUseStaticSpaceEvaluation(game)) {
//            // once per game
//            evaluatingMinMax.strategy = EvaluatingSpace()
//            evaluatingMinMax.pruner = FullMovePruner(3, 3, 3)
//            usesStandardEvaluation = false
//        }
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
        evaluatingMinMax.strategy = standardEvaluation
        evaluatingMinMax.pruner = standardPruner
        usesStandardEvaluation = true
        //use the library only if the figures are used in the classical way (no Chess960)
        useLibrary = game.isStandardGame
    }

    fun setSearchTreePruner(pruner: SearchTreePruner) {
        standardPruner = pruner
        evaluatingMinMax.pruner = pruner
    }

    fun setNumberOfCoresToUse(numberOfCoresToUse: Int) {
        concurrencyStrategy = getConcurrencyStrategy(ui::setProgress, numberOfCoresToUse)
    }
}
