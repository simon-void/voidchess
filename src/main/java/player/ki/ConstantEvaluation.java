package player.ki;

import board.ChessGameInterface;

/**
 * usefull for Profile purposes (only DynamicEvaluation remains)
 * @author Stephan Schröder
 */

public class ConstantEvaluation implements StaticEvaluationInterface
{
  @Override
  public float evaluate(ChessGameInterface game, boolean forWhite)
  {
    return 0;
  }
}
