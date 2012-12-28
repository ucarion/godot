package search;

import org.apache.commons.lang.ArrayUtils;

import movegen.BBMagicAttacks;
import movegen.MoveGenerator;
import util.BBUtils;
import eval.Evaluator;
import eval.SimpleEvaluate;
import godot.Board;
import godot.Move;
import movegen.MoveGenerator;

import org.apache.commons.lang.ArrayUtils;

import eval.SimpleEvaluate;
import godot.Board;

public class IterativeQSEE {
	private static Evaluator evaluator;
	private static int max_search_depth = 5;
	private static int evals;
	private static int[][] white_heuristics;
	private static int[][] black_heuristics;
	private static int[][] triangularArray;
	private static int[] triangularLength;
	private static boolean follow_pv;
	private static int[] lastPV;
	
	public static void setDepth(int depth) {
		max_search_depth = depth;
	}
	
	public static void setEvaluator(Evaluator eval) {
		evaluator = eval;
	}
	
	public static void printPV() {
		for (int i = 0; i < lastPV.length - 1; i++)
			System.out.print(BBUtils.moveToString(lastPV[i]) + " ");
		System.out.println();
	}
	
	public static int getBestMove(Board b) {
		evals = 0;
		white_heuristics = new int[64][64];
		black_heuristics = new int[64][64];
		lastPV = new int[Board.MAX_MOVES];
		
		long start = System.currentTimeMillis();
		
		for (int curr_depth = 1; curr_depth < max_search_depth; curr_depth++) {
			triangularArray = new int[Board.MAX_MOVES][Board.MAX_MOVES];
			triangularLength = new int[Board.MAX_MOVES];
			follow_pv = true;
			alphabeta(b, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, curr_depth, 0);
//			System.out.println("(" + curr_depth + ") "
//					+ ((System.currentTimeMillis() - start) / 1000.0) + "s");
		}
		
		// System.out.println(evals + " positions evaluated.");
		return lastPV[0];
	}
	
	private static int alphabeta(Board b, int alpha, int beta, int depth, int ply) {
		triangularLength[ply] = ply;
		
		if (depth == 0) {
			evals++;
			follow_pv = false;
			return qsearch(b, alpha, beta, ply);
		}
		
		if (b.isEndOfGame()) {
			evals++;
			follow_pv = false;
			return evaluator.eval(b);
		}
		
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
	
	// Board b, int alpha, int beta, int depth, int ply
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
			
			// System.out.println(BitboardUtils.moveToString(captures[i]) + ": "
			// + val);
			
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
	
	public static void printCaptures(Board b) {
		int[] captures = new int[Board.MAX_MOVES];
		int num_captures = generateCaptures(b, captures);
		
		for (int i = 0; i < num_captures; i++) {
			BBUtils.printMove(captures[i]);
		}
	}
}
