package voidchess.player.ki;

/**
 * @author stephan
 */
public class SimplePruner implements SearchTreePruner {
    final static public int MAX_SEARCH_DEPTH = 6;
    final private int NO_HIT_RADIUS, FIGURE_HIT_RADIUS, CHESS_RADIUS;

    SimplePruner() {
        this(1, 2, 2);   //vergrößern dieser Variablen->bessere KI aber mehr Zeit nötig
    }

    public SimplePruner(int no_hit_radius, int figure_hit_radius, int chess_radius) {
        if (no_hit_radius < 1) throw new IllegalArgumentException("no_hit_radius has to be at least one");
        if (no_hit_radius > MAX_SEARCH_DEPTH) throw new IllegalArgumentException("no_hit_radius can't be bigger than MAX_SEARCH_DEPTH");
        if (figure_hit_radius < no_hit_radius)
            throw new IllegalArgumentException("figure_hit_radius cann't be smaller than no_hit_radius");
        if (figure_hit_radius > MAX_SEARCH_DEPTH) throw new IllegalArgumentException("figure_hit_radius can't be bigger than MAX_SEARCH_DEPTH");
        if (chess_radius < no_hit_radius)
            throw new IllegalArgumentException("chess_radius cann't be smaller than no_hit_radius");
        if (chess_radius > MAX_SEARCH_DEPTH) throw new IllegalArgumentException("chess_radius can't be bigger than MAX_SEARCH_DEPTH");

        NO_HIT_RADIUS = no_hit_radius;
        FIGURE_HIT_RADIUS = figure_hit_radius;
        CHESS_RADIUS = chess_radius;
    }

    public boolean stopMinDynamicEvaluation(int depth,
                                            boolean thisMove_isChess,
                                            boolean thisMove_hasHitFigure,
                                            boolean lastMove_isChess,
                                            boolean lastMove_hasHitFigure) {
        //return false;
        return !(depth < NO_HIT_RADIUS || thisMove_hasHitFigure || thisMove_isChess || lastMove_isChess);
    }

    public boolean stopMaxDynamicEvaluation(int depth,
                                            boolean thisMove_isChess,
                                            boolean thisMove_hasHitFigure,
                                            boolean lastMove_isChess,
                                            boolean lastMove_hasHitFigure) {
        if (depth < NO_HIT_RADIUS) return false;                                    // keine Verluste
        if (depth < FIGURE_HIT_RADIUS &&
                (thisMove_hasHitFigure)                                             // eine Figure wurde während des
        )
            return false;                                                           // letzten Halbzuges geschlagen
        if (depth < CHESS_RADIUS &&
                (thisMove_isChess || lastMove_isChess)                              // Schach gesetzt während des
        )
            return false;                                                           // letzten Halbzuges

        return true;
    }
}
