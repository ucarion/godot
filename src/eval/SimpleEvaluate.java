package eval;

import godot.Board;

public class SimpleEvaluate implements Evaluator {
	private static int PAWN_VALUE = 100;
	private static int KNIGHT_VALUE = 320;
	private static int BISHOP_VALUE = 325;
	private static int ROOK_VALUE = 500;
	private static int QUEEN_VALUE = 900;
	
	private static final int CAN_CASTLE_BONUS = 15;
	private static final int HAS_CASTLED_BONUS = 30;
	
	// @formatter:off
	private static final int[] pawnSquareBlack = { 
		 0,  0,  0,  0,  0,  0,  0,  0, 
		 50, 50, 50, 50, 50, 50, 50, 50, 
		 10, 10, 20, 30, 30, 20, 10, 10, 
		  5,  5, 10, 25, 25, 10,  5,  5, 
		  0,  0,  0, 20, 20,  0,  0,  0, 
		  5, -5,-10,  0,  0,-10, -5,  5, 
		  5, 10, 10,-20,-20, 10, 10,  5, 
		  0,  0,  0,  0,  0,  0,  0,  0 
	};
	
	private static final int[] pawnSquareWhite = {
		  0,  0,  0,  0,  0,  0,  0,  0, 
		  5, 10, 10,-20,-20, 10, 10,  5, 
		  5, -5,-10,  0,  0,-10, -5,  5, 
		  0,  0,  0, 20, 20,  0,  0,  0, 
		  5,  5, 10, 25, 25, 10,  5,  5, 
		 10, 10, 20, 30, 30, 20, 10, 10, 
		 50, 50, 50, 50, 50, 50, 50, 50, 
		 0,  0,  0,  0,  0,  0,  0,  0
	};

	private static final int[] knightSquareBlack = { 
		-50,-40,-30,-30,-30,-30,-40,-50, 
		-40,-20,  0,  0,  0,  0,-20,-40, 
		-30,  0, 10, 15, 15, 10,  0,-30, 
		-30,  5, 15, 20, 20, 15,  5,-30, 
		-30,  0, 15, 20, 20, 15,  0,-30, 
		-30,  5, 10, 15, 15, 10,  5,-30, 
		-40,-20,  0,  5,  5,  0,-20,-40, 
		-50,-40,-30,-30,-30,-30,-40,-50, 
	};
	
	private static final int[] knightSquareWhite = {
		-50,-40,-30,-30,-30,-30,-40,-50, 
		-40,-20,  0,  5,  5,  0,-20,-40, 
		-30,  5, 10, 15, 15, 10,  5,-30, 
		-30,  0, 15, 20, 20, 15,  0,-30, 
		-30,  5, 15, 20, 20, 15,  5,-30, 
		-30,  0, 10, 15, 15, 10,  0,-30, 
		-40,-20,  0,  0,  0,  0,-20,-40, 
		-50,-40,-30,-30,-30,-30,-40,-50, 
	};

	private static final int[] bishopSquareBlack = { 
		-20,-10,-10,-10,-10,-10,-10,-20, 
		-10,  0,  0,  0,  0,  0,  0,-10, 
		-10,  0,  5, 10, 10,  5,  0,-10, 
		-10,  5,  5, 10, 10,  5,  5,-10, 
		-10,  0, 10, 10, 10, 10,  0,-10, 
		-10, 10, 10, 10, 10, 10, 10,-10, 
		-10,  5,  0,  0,  0,  0,  5,-10, 
		-20,-10,-10,-10,-10,-10,-10,-20, 
	};
	
	private static final int[] bishopSquareWhite = {
		-20,-10,-10,-10,-10,-10,-10,-20, 
		-10,  5,  0,  0,  0,  0,  5,-10, 
		-10, 10, 10, 10, 10, 10, 10,-10, 
		-10,  0, 10, 10, 10, 10,  0,-10, 
		-10,  5,  5, 10, 10,  5,  5,-10, 
		-10,  0,  5, 10, 10,  5,  0,-10, 
		-10,  0,  0,  0,  0,  0,  0,-10, 
		-20,-10,-10,-10,-10,-10,-10,-20, 
	};

	private static final int[] rookSquareBlack = { 
		  0,  0,  0,  0,  0,  0,  0,  0, 
		  5, 10, 10, 10, 10, 10, 10,  5, 
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		  0,  0,  0,  5,  5,  0,  0,  0 
	};
	
	private static final int[] rookSquareWhite = {
		  0,  0,  0,  5,  5,  0,  0,  0 ,
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		 -5,  0,  0,  0,  0,  0,  0, -5, 
		  5, 10, 10, 10, 10, 10, 10,  5, 
		  0,  0,  0,  0,  0,  0,  0,  0, 
	};

	private static final int[] queenSquareBlack = { 
		-20,-10,-10, -5, -5,-10,-10,-20, 
		-10,  0,  0,  0,  0,  0,  0,-10, 
		-10,  0,  5,  5,  5,  5,  0,-10, 
		 -5,  0,  5,  5,  5,  5,  0, -5, 
		  0,  0,  5,  5,  5,  5,  0, -5, 
		-10,  5,  5,  5,  5,  5,  0,-10, 
		-10,  0,  5,  0,  0,  0,  0,-10, 
		-20,-10,-10, -5, -5,-10,-10,-20 
	};
	
	private static final int[] queenSquareWhite = {
		-20,-10,-10, -5, -5,-10,-10,-20,
		-10,  0,  5,  0,  0,  0,  0,-10, 
		-10,  5,  5,  5,  5,  5,  0,-10, 
		  0,  0,  5,  5,  5,  5,  0, -5, 
		 -5,  0,  5,  5,  5,  5,  0, -5, 
		-10,  0,  5,  5,  5,  5,  0,-10, 
		-10,  0,  0,  0,  0,  0,  0,-10, 
		-20,-10,-10, -5, -5,-10,-10,-20, 
	};

	private static final int[] kingSquareOpeningBlack = { 
		-30,-40,-40,-50,-50,-40,-40,-30, 
		-30,-40,-40,-50,-50,-40,-40,-30, 
		-30,-40,-40,-50,-50,-40,-40,-30, 
		-30,-40,-40,-50,-50,-40,-40,-30, 
		-20,-30,-30,-40,-40,-30,-30,-20, 
		-10,-20,-20,-20,-20,-20,-20,-10, 
		 20, 20,  0,  0,  0,  0, 20, 20, 
		 20, 30, 10,  0,  0, 10, 30, 20 
	};
	
	private static final int[] kingSquareOpeningWhite = {
		 20, 30, 10,  0,  0, 10, 30, 20,
		 20, 20,  0,  0,  0,  0, 20, 20, 
		-10,-20,-20,-20,-20,-20,-20,-10, 
		-20,-30,-30,-40,-40,-30,-30,-20, 
		-30,-40,-40,-50,-50,-40,-40,-30, 
		-30,-40,-40,-50,-50,-40,-40,-30, 
		-30,-40,-40,-50,-50,-40,-40,-30, 
		-30,-40,-40,-50,-50,-40,-40,-30,
	};

	private static final int[] kingSquareEndGameBlack = { 
		-50,-40,-30,-20,-20,-30,-40,-50, 
		-30,-20,-10,  0,  0,-10,-20,-30, 
		-30,-10, 20, 30, 30, 20,-10,-30, 
		-30,-10, 30, 40, 40, 30,-10,-30, 
		-30,-10, 30, 40, 40, 30,-10,-30, 
		-30,-10, 20, 30, 30, 20,-10,-30, 
		-30,-30,  0,  0,  0,  0,-30,-30, 
		-50,-30,-30,-30,-30,-30,-30,-50 
	};
	
	private static final int[] kingSquareEndGameWhite = {
		-50,-30,-30,-30,-30,-30,-30,-50,
		-30,-30,  0,  0,  0,  0,-30,-30, 
		-30,-10, 20, 30, 30, 20,-10,-30, 
		-30,-10, 30, 40, 40, 30,-10,-30, 
		-30,-10, 30, 40, 40, 30,-10,-30, 
		-30,-10, 20, 30, 30, 20,-10,-30, 
		-30,-20,-10,  0,  0,-10,-20,-30, 
		-50,-40,-30,-20,-20,-30,-40,-50, 
	};
	// @formatter:on
	
	public int eval(Board b) {
		if (b.isMate()) {
			return Integer.MIN_VALUE + b.moveNumber;
		}
		if (b.isDraw())
			return 0;
		
		int white_score = 0;
		int black_score = 0;
		
		for (int square = 0; square < 64; square++) {
			switch (b.getPieceAt(square)) {
				case 'p':
					black_score += PAWN_VALUE;
					black_score += pawnSquareBlack[square];
					break;
				case 'P':
					white_score += PAWN_VALUE;
					white_score += pawnSquareWhite[square];
					break;
				case 'n':
					black_score += KNIGHT_VALUE;
					black_score += knightSquareBlack[square];
					break;
				case 'N':
					white_score += KNIGHT_VALUE;
					white_score += knightSquareWhite[square];
					break;
				case 'b':
					black_score += BISHOP_VALUE;
					black_score += bishopSquareBlack[square];
					break;
				case 'B':
					white_score += BISHOP_VALUE;
					white_score += bishopSquareWhite[square];
					break;
				case 'r':
					black_score += ROOK_VALUE;
					black_score += rookSquareBlack[square];
					break;
				case 'R':
					white_score += ROOK_VALUE;
					white_score += rookSquareWhite[square];
					break;
				case 'q':
					black_score += QUEEN_VALUE;
					black_score += queenSquareBlack[square];
					break;
				case 'Q':
					white_score += QUEEN_VALUE;
					white_score += queenSquareWhite[square];
					break;
				case 'k':
					if (b.isEndGame())
						black_score += kingSquareEndGameBlack[square];
					else
						black_score += kingSquareOpeningBlack[square];
					break;
				case 'K':
					if (b.isEndGame())
						white_score += kingSquareEndGameWhite[square];
					else
						white_score += kingSquareOpeningWhite[square];
					break;
			}
		}
		
		if (b.white_has_castled)
			white_score += HAS_CASTLED_BONUS;
		if (b.black_has_castled)
			black_score += HAS_CASTLED_BONUS;
		if (b.white_castle_k || b.white_castle_q)
			white_score += CAN_CASTLE_BONUS;
		if (b.black_castle_k || b.black_castle_q)
			black_score += CAN_CASTLE_BONUS;
		
		if (b.white_to_move)
			return white_score - black_score;
		else
			return black_score - white_score;
	}
}
