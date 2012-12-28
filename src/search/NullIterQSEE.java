package search;

import opening.Opening;

import org.apache.commons.lang.ArrayUtils;
import movegen.MoveGenerator;
import util.BBUtils;
import eval.Evaluator;
import godot.Board;
import godot.Move;

/**
 * Searching based on null-move pruning, iterative deepening, quiescent
 * searching, static exchange evaluation, alpha-beta, PVS, and history
 * heuristics.
 * 
 * @author Ulysse
 * 
 */
public class NullIterQSEE {
	private static final boolean VERBOSE = false;
	
	private static final int NULLMOVE_REDUCTION = 4;
	private static final int NULLMOVE_THRESHOLD = 319;
	
	private static Evaluator evaluator;
	private static int max_search_depth = 5;
	private static int evals;
	private static int[][] white_heuristics;
	private static int[][] black_heuristics;
	private static int[][] triangularArray;
	private static int[] triangularLength;
	private static boolean follow_pv;
	private static boolean allow_null;
	private static int[] lastPV;
	
	public static void setDepth(int depth) {
		max_search_depth = depth;
	}
	
	public static void setEvaluator(Evaluator eval) {
		evaluator = eval;
	}
	
	/**
	 * Outputs the entire contents of the PV array, including "blank space".
	 */
	public static void printPV() {
		for (int i = 0; i < lastPV.length - 1; i++)
			System.out.print(BBUtils.moveToString(lastPV[i]) + " ");
		System.out.println();
	}
	
	/**
	 * Returns the best move in a position, at least according to the engine.
	 * 
	 * @param b
	 *            the position to consider
	 * @return the best move the engine can find.
	 */
	public static int getBestMove(Board b) {
		int bookmove = Opening.getBookMove(b);
		if (bookmove != Opening.MOVE_NOT_FOUND) {
			if (VERBOSE)
				System.out.println("(B) " + BBUtils.moveToString(bookmove));
			return bookmove;
		}
		
		evals = 0;
		white_heuristics = new int[64][64];
		black_heuristics = new int[64][64];
		lastPV = new int[Board.MAX_MOVES];
		
		long start = System.currentTimeMillis();
		
		for (int curr_depth = 1; curr_depth < max_search_depth; curr_depth++) {
			triangularArray = new int[Board.MAX_MOVES][Board.MAX_MOVES];
			triangularLength = new int[Board.MAX_MOVES];
			follow_pv = true;
			allow_null = true;
			alphabeta(b, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, curr_depth, 0);
			if (VERBOSE)
				System.out.println("(" + curr_depth + ") "
						+ ( (System.currentTimeMillis() - start) / 1000.0) + "s ("
						+ BBUtils.moveToString(lastPV[0]) + ") -- " + evals
						+ " nodes evaluated.");
		}
		
		if (VERBOSE)
			System.out.println(evals + " positions evaluated.");
		return lastPV[0];
	}
	
	private static int alphabeta(Board b, int alpha, int beta, int depth, int ply) {
		triangularLength[ply] = ply;
		evals++;
		if (depth <= 0) {
			follow_pv = false;
			return qsearch(b, alpha, beta, ply);
		}
		
		if (b.isEndOfGame()) {
			follow_pv = false;
			return evaluator.eval(b);
		}
		
		// attempt null move
		if (allow_null && !follow_pv && b.movingSideMaterial() > NULLMOVE_THRESHOLD
				&& !b.isCheck()) {
			allow_null = false;
			b.doNullMove();
			int val = -alphabeta(b, -beta, -beta + 1, depth - NULLMOVE_REDUCTION, ply);
			b.undoMove();
			allow_null = true;
			if (val >= beta)
				return val;
		}
		
		allow_null = true;
		
		int movesfound = 0;
		int val = 0;
		
		int[] moves = new int[Board.MAX_MOVES];
		int num_moves = MoveGenerator.getAllLegalMoves(b, moves);
		
		for (int i = 0; i < num_moves; i++) {
			putBestMoveFirst(ply, depth, i, moves, num_moves, b.white_to_move);
			b.makeMove(moves[i]);
			
			if (movesfound != 0) {
				val = -alphabeta(b, -alpha - 1, -alpha, depth - 1, ply + 1);
				
				if (val > alpha && val < beta)
					val = -alphabeta(b, -beta, -alpha, depth - 1, ply + 1);
			}
			else
				val = -alphabeta(b, -beta, -alpha, depth - 1, ply + 1);
			
			b.undoMove();
			
			if (val >= beta) {
				if (b.white_to_move)
					white_heuristics[Move.getFrom(moves[i])][Move.getTo(moves[i])] +=
							depth * depth;
				else
					black_heuristics[Move.getFrom(moves[i])][Move.getTo(moves[i])] +=
							depth * depth;
				
				return beta;
			}
			if (val > alpha) {
				alpha = val;
				movesfound++;
				
				triangularArray[ply][ply] = moves[i];
				
				for (int j = ply + 1; j < triangularLength[ply + 1]; j++)
					triangularArray[ply][j] = triangularArray[ply + 1][j];
				
				triangularLength[ply] = triangularLength[ply + 1];
				
				if (ply == 0)
					rememberPV();
			}
		}
		
		if (movesfound != 0) {
			if (b.white_to_move)
				white_heuristics[Move.getFrom(triangularArray[ply][ply])][Move
						.getTo(triangularArray[ply][ply])] += depth * depth;
			else
				black_heuristics[Move.getFrom(triangularArray[ply][ply])][Move
						.getTo(triangularArray[ply][ply])] += depth * depth;
		}
		
		return alpha;
	}
	
	private static void putBestMoveFirst(int ply, int depth, int next_index, int[] moves,
			int num_moves, boolean white_to_move) {
		// if applicable, make next move the PV
		if (follow_pv && depth > 1) {
			for (int i = next_index; i < num_moves; i++) {
				if (moves[i] == lastPV[ply]) {
					int temp = moves[i];
					moves[i] = moves[next_index];
					moves[next_index] = temp;
					return;
				}
			}
		}
		
		// get best heuristic
		if (white_to_move) {
			int best =
					white_heuristics[Move.getFrom(moves[next_index])][Move
							.getTo(moves[next_index])];
			int best_loc = next_index;
			
			for (int i = next_index + 1; i < num_moves; i++) {
				if (white_heuristics[Move.getFrom(moves[i])][Move.getTo(moves[i])] > best) {
					best = white_heuristics[Move.getFrom(moves[i])][Move.getTo(moves[i])];
					best_loc = i;
				}
			}
			
			if (best_loc > next_index) {
				int temp = moves[best_loc];
				moves[best_loc] = moves[next_index];
				moves[next_index] = temp;
			}
		}
		else {
			int best =
					black_heuristics[Move.getFrom(moves[next_index])][Move
							.getTo(moves[next_index])];
			int best_loc = next_index;
			
			for (int i = next_index + 1; i < num_moves; i++) {
				if (black_heuristics[Move.getFrom(moves[i])][Move.getTo(moves[i])] > best) {
					best = black_heuristics[Move.getFrom(moves[i])][Move.getTo(moves[i])];
					best_loc = i;
				}
			}
			
			if (best_loc > next_index) {
				int temp = moves[best_loc];
				moves[best_loc] = moves[next_index];
				moves[next_index] = temp;
			}
		}
	}
	
	private static void rememberPV() {
		for (int i = 0; i < triangularLength[0]; i++) {
			lastPV[i] = triangularArray[0][i];
		}
	}
	
	private static int qsearch(Board b, int alpha, int beta, int ply) {
		triangularLength[ply] = ply;
		
		if (b.isCheck())
			return alphabeta(b, alpha, beta, 1, ply);
		
		int stand_pat = evaluator.eval(b);
		
		if (stand_pat >= beta)
			return stand_pat;
		if (stand_pat > alpha)
			alpha = stand_pat;
		
		int[] captures = new int[Board.MAX_MOVES];
		int num_captures = generateCaptures(b, captures);
		
		for (int i = 0; i < num_captures; i++) {
			b.makeMove(captures[i]);
			int val = -qsearch(b, -beta, -alpha, ply + 1);
			b.undoMove();
			
			if (val >= beta)
				return val;
			if (val > alpha) {
				alpha = val;
				triangularArray[ply][ply] = captures[i];
				for (int j = ply + 1; j < triangularLength[ply + 1]; j++)
					triangularArray[ply][j] = triangularArray[ply + 1][j];
				triangularLength[ply] = triangularLength[ply + 1];
			}
		}
		
		return alpha;
	}
	
	private static int generateCaptures(Board b, int[] captures) {
		int[] capturevals = new int[Board.MAX_MOVES];
		int num_captures = MoveGenerator.getAllCapturesAndPromotions(b, captures);
		
		for (int i = 0; i < num_captures; i++) {
			int val = b.see(captures[i]);
			capturevals[i] = val;
			
			if (val < 0) { // not worth it
				ArrayUtils.remove(captures, i);
				ArrayUtils.remove(capturevals, i);
				num_captures--;
				i--;
			}
			
			int insertloc = i;
			
			while (insertloc >= 0 && capturevals[i] > capturevals[insertloc]) {
				int tempcap = captures[i];
				captures[i] = captures[insertloc];
				captures[insertloc] = tempcap;
				
				int tempval = capturevals[i];
				capturevals[i] = capturevals[insertloc];
				capturevals[insertloc] = tempval;
				
				insertloc--;
			}
		}
		
		return num_captures;
	}
}
