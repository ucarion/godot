package perft;

import movegen.MoveGenerator;
import godot.Board;

/**
 * Perft is a useful way of testing to see if your move generator works
 * correctly. Given a position, the goal here is to see if you are generating
 * the correct moves, and if you are doing so quickly.
 * 
 * @author Ulysse
 * 
 */
public class Perft {
	/*
	 * This test starts from the initial position. The theoretical results should be:
	 * 
	 * 1 : 20
	 * 2 : 400
	 * 3 : 8902
	 * 4 : 197281
	 * 5 : 4865609
	 * 6 : 119060324
	 */
	private static String test1 = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	/*
	 * 1 : 48
	 * 2 : 2039
	 * 3 : 97862
	 * 4 : 4085603
	 * 5 : 193690690
	 */
	private static String test2 = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
	
	public static void main(String[] args) {
		Board b = new Board();
		b.readFromFEN(test1);
		test(test2);
		int i;
		long start = System.currentTimeMillis();
		i = perft(b, 5);
		long stop = System.currentTimeMillis();
		System.out.println("Found " + i + " nodes in " + (stop - start) + "ms.");
	}
	
	private static void test(String fen) {
		Board b = new Board();
		b.readFromFEN(fen);
		for (int i = 1; i <= 6; i++)
			System.out.println(i + ": " + perft(b, i));
	}
	
	private static int perft(Board b, int depth) {
		if (depth == 0)
			return 1;
			
		int[] moves = new int[Board.MAX_MOVES];
		int num_moves = MoveGenerator.getAllLegalMoves(b, moves);
		
		int count = 0;
		
		for (int i = 0; i < num_moves; i++) {
			b.makeMove(moves[i]);
			count += perft(b, depth - 1);
			b.undoMove();
		}
		
		return count;
	}
}
