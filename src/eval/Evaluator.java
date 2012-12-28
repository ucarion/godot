package eval;

import godot.Board;

public interface Evaluator {
	/**
	 * Provides an estimation of the value of a position according to the side
	 * to move. All values should return a positive value for an advantage to
	 * the side to move, a 0 for an estimated draw, and negative values for
	 * positions which are disadvantageous to the side to move. Evaluators
	 * should (but are not required to) give a value of approximately 100 for a
	 * pawn, thus making the returned value of this method an approximation in
	 * centipawns (cp).
	 * 
	 * @param b
	 *            the board to evaluate
	 * @return the estimated value of the board according to the side to move.
	 */
	public int eval(Board b);
}
