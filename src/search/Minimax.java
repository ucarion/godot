package search;

import util.BBUtils;
import eval.Evaluator;
import eval.SimpleEvaluate;
import movegen.MoveGenerator;
import godot.Board;

public class Minimax {
	private static int DEPTH = 5;
	
	private static Evaluator evaluator;
	
	public static void setEvaluator(Evaluator eval) {
		evaluator = eval;
	}
	
	public static int getBestMove(Board b) {
		int[] moves = new int[Board.MAX_MOVES];
		int num_moves = MoveGenerator.getAllLegalMoves(b, moves);
		
		int max = Integer.MIN_VALUE;
		int bestmove = 0;
		
		for (int i = 0; i < num_moves; i++) {
			b.makeMove(moves[i]);
			int score = -minimax(b, DEPTH);
			b.undoMove();
			if (score > max) {
				max = score;
				bestmove = moves[i];
			}
		}
		
		return bestmove;
	}
	
	private static int minimax(Board b, int depth) {
		if (depth == 0 || b.isEndOfGame())
			return evaluator.eval(b);
		
		int[] moves = new int[Board.MAX_MOVES];
		int num_moves = MoveGenerator.getAllLegalMoves(b, moves);
		
		int max = Integer.MIN_VALUE;
		
		for (int i = 0; i < num_moves; i++) {
			b.makeMove(moves[i]);
			int score = -minimax(b, depth - 1);
			b.undoMove();
			if (score > max) {
				max = score;
			}
		}

		return max;
	}
}
