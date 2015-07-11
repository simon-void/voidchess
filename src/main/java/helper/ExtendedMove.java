package helper;

import figures.Figure;

/**
 * @author stephan
 */
final public class ExtendedMove extends Move
{
    final private Figure pawnHitByEnpassantOrRochadeRock;
    final private Figure hitFigure;
    final private boolean white;
    final private boolean isRochade;
    final private boolean hitsEnpassent;
    final private boolean pawnTransformation;
    final private boolean hasHitFigure;

    public ExtendedMove(Position from,
                        Position to,
                        Figure hitFigure,
                        Figure pawnHitByEnpassantOrRochadeRock,
                        boolean white,
                        boolean isRochade,
                        boolean hitsEnpassent,
                        boolean pawnTrasformation) {
        super(from, to);
        this.hitFigure = hitFigure;
        this.pawnHitByEnpassantOrRochadeRock = pawnHitByEnpassantOrRochadeRock;
        this.white = white;
        this.isRochade = isRochade;
        this.hitsEnpassent = hitsEnpassent;
        this.pawnTransformation = pawnTrasformation;
        hasHitFigure = hitFigure != null || hitsEnpassent;
    }

    public Figure getFigure() {
        return hitFigure;
    }

    public Figure getEnpassentPawnOrRochadeRock() {
        return pawnHitByEnpassantOrRochadeRock;
    }

    public boolean getColorOfMove() {
        return white;
    }

    public boolean isRochade() {
        return isRochade;
    }

    public boolean isEnpassent() {
        return hitsEnpassent;
    }

    public boolean pawnTransformed() {
        return pawnTransformation;
    }

    public boolean hasHitFigure() {
        return hasHitFigure;
    }
}
