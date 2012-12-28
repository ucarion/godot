package eval;

import godot.Board;

public class MaterialEvaluate implements Evaluator {
	private static final int PAWN = 100;
	private static final int KNIGHT = 320;
	private static final int BISHOP = 330;
	private static final int ROOK = 500;
	private static final int QUEEN = 800;
	
	public int eval(Board b) {
		if (b.isMate()) {
			return Integer.MIN_VALUE + 2;
		}
		if (b.isDraw())
			return 0;
		
		if (b.white_to_move) {
			int pawns = Long.bitCount(b.white_pawns) - Long.bitCount(b.black_pawns);
			int knights = Long.bitCount(b.white_knights) - Long.bitCount(b.black_knights);
			int bishops = Long.bitCount(b.white_bishops) - Long.bitCount(b.black_bishops);
			int rooks = Long.bitCount(b.white_rooks) - Long.bitCount(b.black_rooks);
			int queens = Long.bitCount(b.white_queens) - Long.bitCount(b.black_queens);
			
			return pawns * PAWN + knights * KNIGHT + bishops * BISHOP + rooks * ROOK
					+ queens * QUEEN;
		}
		else {
			int pawns = Long.bitCount(b.black_pawns) - Long.bitCount(b.white_pawns);
			int knights = Long.bitCount(b.black_knights) - Long.bitCount(b.white_knights);
			int bishops = Long.bitCount(b.black_bishops) - Long.bitCount(b.white_bishops);
			int rooks = Long.bitCount(b.black_rooks) - Long.bitCount(b.white_rooks);
			int queens = Long.bitCount(b.black_queens) - Long.bitCount(b.white_queens);
			
			return pawns * PAWN + knights * KNIGHT + bishops * BISHOP + rooks * ROOK
					+ queens * QUEEN;
		}
	}
}
