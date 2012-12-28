package tournament;

import eval.CompleteEvaluator;
import eval.Evaluator;
import eval.SimpleEvaluate;
import search.NullIterQSEE;
import util.BBUtils;
import util.SANUtils;
import godot.Board;

/**
 * Used to generate tournaments between two engines at different time controls.
 * 
 * @author Ulysse
 * 
 */
public class Tournament {
	private int depth1;
	private int depth2;
	private Evaluator eval1;
	private Evaluator eval2;
	private int num_minutes;
	private int num_games;
	
	private static final int P1_WIN = 1;
	private static final int P2_WIN = 2;
	private static final int DRAW = 3;
	private static final int CHECKMATE = 4;
	private static final int TIME = 5;
	
	public Tournament(int depth1, int depth2, Evaluator eval1, Evaluator eval2,
			int num_minutes, int num_games) {
		this.depth1 = depth1;
		this.depth2 = depth2;
		this.eval1 = eval1;
		this.eval2 = eval2;
		this.num_minutes = num_minutes;
		this.num_games = num_games;
	}
	
	public static void main(String[] args) {
		Tournament t =
				new Tournament(7, 7, new CompleteEvaluator(), new SimpleEvaluate(), 10, 2);
		t.run();
	}
	
	public void run() {
		System.out.println("STARTING TOURNAMENT ...");
		int p1 = 0;
		int p2 = 0;
		int draw_result = 0;
		int time = 0;
		int checkmate = 0;
		int draw_reason = 0;
		for (int i = 0; i < num_games; i++) {
			System.out.println("[Game] Playing game " + i + ": ");
			int[] result;
			if (i % 2 == 0)
				result = playGame(true);
			else
				result = playGame(false);
			System.out.println(result[0] + " " + result[1]);
			switch (result[0]) {
				case P1_WIN:
					p1++;
					break;
				case P2_WIN:
					p2++;
					break;
				case DRAW:
					draw_result++;
					break;
			}
			
			switch (result[1]) {
				case DRAW:
					draw_reason++;
					break;
				case CHECKMATE:
					checkmate++;
					break;
				case TIME:
					time++;
					break;
			}
		}
		
		System.out.println("... FINISHED.");
		System.out.println("P1: " + p1 + " -- P2: " + p2 + " Draw: " + draw_result);
		System.out
				.println("Draw: " + draw_reason + " #: " + checkmate + " Time: " + time);
	}
	
	/**
	 * Plays a game.
	 * 
	 * @return P1_WIN, P2_WIN, or DRAW
	 */
	private int[] playGame(boolean p1_is_white) {
		if (p1_is_white)
			System.out.println("W: " + depth1 + " B: " + depth2);
		else
			System.out.println("W: " + depth2 + " B: " + depth1);
		Board b = new Board();
		b.readFromFEN(BBUtils.START_FEN);
		
		long time1 = num_minutes * 60000;
		long time2 = num_minutes * 60000;
		
		while (time1 > 0 && time2 > 0 && !b.isEndOfGame()) {
			if ( (b.white_to_move && p1_is_white) || ( !b.white_to_move && !p1_is_white)) {
				NullIterQSEE.setDepth(depth1);
				NullIterQSEE.setEvaluator(eval1);
				long start = System.currentTimeMillis();
				int move = NullIterQSEE.getBestMove(b);
				if (b.moveNumber % 2 != 0)
					System.out.print( (b.moveNumber / 2 + 1) + ". ");
				System.out.print(SANUtils.getSAN(b, move) + " ");
				b.makeMove(move);
				long stop = System.currentTimeMillis();
				time1 -= (stop - start);
			}
			else {
				NullIterQSEE.setDepth(depth2);
				NullIterQSEE.setEvaluator(eval2);
				long start = System.currentTimeMillis();
				int move = NullIterQSEE.getBestMove(b);
				if (b.moveNumber % 2 != 0)
					System.out.print( (b.moveNumber / 2 + 1) + ". ");
				System.out.print(SANUtils.getSAN(b, move) + " ");
				b.makeMove(move);
				long stop = System.currentTimeMillis();
				time2 -= (stop - start);
			}
			
			if (time1 <= 0) {
				int[] arr = { P2_WIN, TIME };
				return arr;
			}
			if (time2 <= 0) {
				int[] arr = { P1_WIN, TIME };
				return arr;
			}
			if (b.isEndOfGame()) {
				if (b.isMate() && b.white_to_move) {
					if (p1_is_white) {
						int[] arr = { P2_WIN, CHECKMATE };
						return arr;
					}
					else {
						int[] arr = { P1_WIN, CHECKMATE };
						return arr;
					}
				}
				if (b.isMate() && !b.white_to_move) {
					if (p1_is_white) {
						int[] arr = { P2_WIN, CHECKMATE };
						return arr;
					}
					else {
						int[] arr = { P1_WIN, CHECKMATE };
						return arr;
					}
				}
				if (b.isDraw()) {
					int[] arr = { DRAW, DRAW };
					return arr;
				}
			}
		}
		return null;
	}
}