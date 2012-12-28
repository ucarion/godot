package eval;

import movegen.BBMagicAttacks;
import util.BBUtils;
import godot.Board;

/**
 * The complete and final evaluator. Takes into consideration material, pawn
 * structure, king safety, piece placement, and rook-pawn cooperation (viz.,
 * rooks on open files, rooks supporting passed pawns).
 * 
 * @author Ulysse
 * 
 */
public class CompleteEvaluator implements Evaluator {
	private static final boolean VERBOSE = false;
	
	// From:
	// http://home.comcast.net/~danheisman/Articles/evaluation_of_material_imbalance.htm
	private static final int PAWN_VALUE = 100;
	private static final int KNIGHT_VALUE = 325;
	private static final int BISHOP_VALUE = 325;
	private static final int ROOK_VALUE = 500;
	private static final int QUEEN_VALUE = 975;
	
	// most values by Stef Luijten or Alonso Ruibal
	private static final int PENALTY_DOUBLED_PAWN = 10;
	private static final int PENALTY_ISOLATED_PAWN = 10;
	private static final int PENALTY_BACKWARD_PAWN = 8;
	private static final int BONUS_PASSED_PAWN = 20;
	private static final int BONUS_BISHOP_PAIR = 50; // also by Heisman
	private static final int BONUS_ROOK_DEFENDING_PASSED_PAWN = 20;
	private static final int BONUS_ROOK_ON_OPEN_FILE = 20;
	private static final int BONUS_TWO_ROOKS_ON_OPEN_FILE = 10;
	private static final int BONUS_STRONG_SHIELD = 9;
	private static final int BONUS_WEAK_SHIELD = 4;
	
	// by Stef Luijten -- only nearby pawns can give defense bonus
	private static final int[] OWN_PAWN_SAFETY_BONUS = { 0, 8, 4, 2, 0, 0, 0, 0 };
	private static final int[] OPP_PAWN_SAFETY_PENALTY = { 0, 2, 1, 0, 0, 0, 0, 0 };
	private static final int[] KNIGHT_SAFETY = { 0, 4, 4, 0, 0, 0, 0, 0 };
	private static final int[] BISHOP_SAFETY = { 0, 5, 4, 3, 2, 1, 0, 0 };
	private static final int[] ROOK_SAFETY = { 0, 7, 5, 4, 3, 0, 0, 0 };
	private static final int[] QUEEN_SAFETY = { 0, 10, 8, 5, 4, 0, 0, 0 };
	
	// @formatter:off
	// BITMAPS
	// (These are defined in the static block.)
	// pawns
	private static final long[] PASSED_WHITE; 	// no pawns ahead that are in same or adjacent file.
	private static final long[] ISOLATED_WHITE; // there are pawns in adjacent columns
	private static final long[] BACKWARD_WHITE; // there are pawns that can defend you
	private static final long[] PASSED_BLACK;
	private static final long[] ISOLATED_BLACK;
	private static final long[] BACKWARD_BLACK;
	// king safety
	private static final long[] STRONG_SAFE_WHITE; 	// three nearest squares in file in front
	private static final long[] WEAK_SAFE_WHITE;	// three nearest squares in file two in front
	private static final long[] STRONG_SAFE_BLACK;
	private static final long[] WEAK_SAFE_BLACK;
	
	
	private static final int[][] DISTANCES; // Chebyshev distance
	
	// Piece-square tables by Stef Luijten
	private static final int[] PAWN_POS_BLACK = {
		0,   0,   0,   0,   0,   0,   0,   0,
        5,  10,  15,  20,  20,  15,  10,   5,
        4,   8,  12,  16,  16,  12,   8,   4,
        3,   6,   9,  12,  12,   9,   6,   3,
        2,   4,   6,   8,   8,   6,   4,   2,
        1,   2,   3, -10, -10,   3,   2,   1,
        0,   0,   0, -40, -40,   0,   0,   0,
        0,   0,   0,   0,   0,   0,   0,   0
	};
	
	private static final int[] KNIGHT_POS_BLACK = {
		-10, -10, -10, -10, -10, -10, -10, -10,
	    -10,   0,   0,   0,   0,   0,   0, -10,
	    -10,   0,   5,   5,   5,   5,   0, -10,
	    -10,   0,   5,  10,  10,   5,   0, -10,
	    -10,   0,   5,  10,  10,   5,   0, -10,
	    -10,   0,   5,   5,   5,   5,   0, -10,
	    -10,   0,   0,   0,   0,   0,   0, -10,
	    -10, -30, -10, -10, -10, -10, -30, -10
	};
	
	private static final int[] BISHOP_POS_BLACK = {
		-10, -10, -10, -10, -10, -10, -10, -10,
	    -10,   0,   0,   0,   0,   0,   0, -10,
	    -10,   0,   5,   5,   5,   5,   0, -10,
	    -10,   0,   5,  10,  10,   5,   0, -10,
	    -10,   0,   5,  10,  10,   5,   0, -10,
	    -10,   0,   5,   5,   5,   5,   0, -10,
	    -10,   0,   0,   0,   0,   0,   0, -10,
	    -10, -10, -20, -10, -10, -20, -10, -10
	};
	
	private static final int[] ROOK_POS_BLACK = {
		  0,   0,   0,   0,   0,   0,   0,   0,
	     15,  15,  15,  15,  15,  15,  15,  15,
	      0,   0,   0,   0,   0,   0,   0,   0,
	      0,   0,   0,   0,   0,   0,   0,   0,
	      0,   0,   0,   0,   0,   0,   0,   0,
	      0,   0,   0,   0,   0,   0,   0,   0,
	      0,   0,   0,   0,   0,   0,   0,   0,
	    -10,   0,   0,  10,  10,   0,   0, -10
	};
	
	private static final int[] QUEEN_POS_BLACK = {
		-10, -10, -10, -10, -10, -10, -10, -10,
	    -10,   0,   0,   0,   0,   0,   0, -10,
	    -10,   0,   5,   5,   5,   5,   0, -10,
	    -10,   0,   5,  10,  10,   5,   0, -10,
	    -10,   0,   5,  10,  10,   5,   0, -10,
	    -10,   0,   5,   5,   5,   5,   0, -10,
	    -10,   0,   0,   0,   0,   0,   0, -10,
	    -10, -10, -20, -10, -10, -20, -10, -10
	};
	
	private static final int[] KING_POS_M_BLACK = {
		-40, -40, -40, -40, -40, -40, -40, -40,
	    -40, -40, -40, -40, -40, -40, -40, -40,
	    -40, -40, -40, -40, -40, -40, -40, -40,
	    -40, -40, -40, -40, -40, -40, -40, -40,
	    -40, -40, -40, -40, -40, -40, -40, -40,
	    -40, -40, -40, -40, -40, -40, -40, -40,
	    -20, -20, -20, -20, -20, -20, -20, -20,
	      0,  20,  40, -20,   0, -20,  40,  20
	};
	
	private static final int[] KING_POS_E_BLACK = {
		  0,  10,  20,  30,  30,  20,  10,   0,
	     10,  20,  30,  40,  40,  30,  20,  10,
	     20,  30,  40,  50,  50,  40,  30,  20,
	     30,  40,  50,  60,  60,  50,  40,  30,
	     30,  40,  50,  60,  60,  50,  40,  30,
	     20,  30,  40,  50,  50,  40,  30,  20,
	     10,  20,  30,  40,  40,  30,  20,  10,
	      0,  10,  20,  30,  30,  20,  10,   0
	};
	
	private static final int[] PAWN_POS_WHITE;
	private static final int[] KNIGHT_POS_WHITE;
	private static final int[] BISHOP_POS_WHITE;
	private static final int[] ROOK_POS_WHITE;
	private static final int[] QUEEN_POS_WHITE;
	private static final int[] KING_POS_M_WHITE;
	private static final int[] KING_POS_E_WHITE;
	
	private static final int[] MIRROR = {
		56,  57,  58,  59,  60,  61,  62,  63,
		48,  49,  50,  51,  52,  53,  54,  55,
	    40,  41,  42,  43,  44,  45,  46,  47,
	    32,  33,  34,  35,  36,  37,  38,  39,
	    24,  25,  26,  27,  28,  29,  30,  31,
	    16,  17,  18,  19,  20,  21,  22,  23,
	    8,   9,  10,  11,  12,  13,  14,  15,  
	    0,   1,   2,   3,   4,   5,   6,   7
	};
	
	// @formatter:on
	
	static {
		PASSED_WHITE = new long[64];
		ISOLATED_WHITE = new long[64];
		BACKWARD_WHITE = new long[64];
		
		for (int i = 0; i < 64; i++) {
			int rank = BBUtils.getLocRow(i);
			int file = BBUtils.getLocCol(i);
			
			for (int j = rank; j < 8; j++) {
				if (file > 0)
					PASSED_WHITE[i] |= BBUtils.getSquare(j, file - 1);
				PASSED_WHITE[i] |= BBUtils.getSquare(j, file);
				if (file < 7)
					PASSED_WHITE[i] |= BBUtils.getSquare(j, file + 1);
			}
			
			for (int j = 0; j < 8; j++) {
				if (file > 0)
					ISOLATED_WHITE[i] |= BBUtils.getSquare(j, file - 1);
				if (file < 7)
					ISOLATED_WHITE[i] |= BBUtils.getSquare(j, file + 1);
			}
			
			for (int j = 0; j < rank; j++) {
				if (file > 0)
					BACKWARD_WHITE[i] |= BBUtils.getSquare(j, file - 1);
				if (file < 7)
					BACKWARD_WHITE[i] |= BBUtils.getSquare(j, file + 1);
			}
		}
		
		STRONG_SAFE_WHITE = new long[64];
		WEAK_SAFE_WHITE = new long[64];
		
		// pawn shields for white only first three ranks
		for (int i = 0; i < 8 * 3; i++) {
			int file = BBUtils.getLocCol(i);
			STRONG_SAFE_WHITE[i] |= BBUtils.getSquare[i + 8];
			WEAK_SAFE_WHITE[i] |= BBUtils.getSquare[i + 8];
			
			if (file > 0)
				STRONG_SAFE_WHITE[i] |= BBUtils.getSquare[i + 7];
			else
				STRONG_SAFE_WHITE[i] |= BBUtils.getSquare[i + 10];
			if (file < 7)
				STRONG_SAFE_WHITE[i] |= BBUtils.getSquare[i + 9];
			else
				STRONG_SAFE_WHITE[i] |= BBUtils.getSquare[i + 6];
			
			WEAK_SAFE_WHITE[i] = STRONG_SAFE_WHITE[i] << 8;
		}
		
		DISTANCES = new int[64][64];
		
		for (int i = 0; i < 64; i++) {
			int rank_from = BBUtils.getLocCol(i);
			int file_from = BBUtils.getLocRow(i);
			
			for (int j = 0; j < 64; j++) {
				int rank_to = BBUtils.getLocCol(j);
				int file_to = BBUtils.getLocRow(j);
				
				if (Math.abs(rank_from - rank_to) > Math.abs(file_from - file_to))
					DISTANCES[i][j] = Math.abs(rank_from - rank_to);
				else
					DISTANCES[i][j] = Math.abs(file_from - file_to);
			}
		}
		
		PAWN_POS_WHITE = new int[64];
		KNIGHT_POS_WHITE = new int[64];
		BISHOP_POS_WHITE = new int[64];
		ROOK_POS_WHITE = new int[64];
		QUEEN_POS_WHITE = new int[64];
		KING_POS_M_WHITE = new int[64];
		KING_POS_E_WHITE = new int[64];
		PASSED_BLACK = new long[64];
		ISOLATED_BLACK = new long[64];
		BACKWARD_BLACK = new long[64];
		STRONG_SAFE_BLACK = new long[64];
		WEAK_SAFE_BLACK = new long[64];
		
		for (int i = 0; i < 64; i++) {
			PAWN_POS_WHITE[i] = PAWN_POS_BLACK[MIRROR[i]];
			KNIGHT_POS_WHITE[i] = KNIGHT_POS_BLACK[MIRROR[i]];
			BISHOP_POS_WHITE[i] = BISHOP_POS_BLACK[MIRROR[i]];
			ROOK_POS_WHITE[i] = ROOK_POS_BLACK[MIRROR[i]];
			QUEEN_POS_WHITE[i] = QUEEN_POS_BLACK[MIRROR[i]];
			KING_POS_M_WHITE[i] = KING_POS_M_BLACK[MIRROR[i]];
			KING_POS_E_WHITE[i] = KING_POS_E_BLACK[MIRROR[i]];
			
			for (int j = 0; j < 64; j++) {
				if ( (PASSED_WHITE[i] & BBUtils.getSquare[j]) != 0L)
					PASSED_BLACK[MIRROR[i]] |= BBUtils.getSquare[MIRROR[j]];
				if ( (ISOLATED_WHITE[i] & BBUtils.getSquare[j]) != 0L)
					ISOLATED_BLACK[MIRROR[i]] |= BBUtils.getSquare[MIRROR[j]];
				if ( (BACKWARD_WHITE[i] & BBUtils.getSquare[j]) != 0L)
					BACKWARD_BLACK[MIRROR[i]] |= BBUtils.getSquare[MIRROR[j]];
				if ( (STRONG_SAFE_WHITE[i] & BBUtils.getSquare[j]) != 0L)
					STRONG_SAFE_BLACK[MIRROR[i]] |= BBUtils.getSquare[MIRROR[j]];
				if ( (WEAK_SAFE_WHITE[i] & BBUtils.getSquare[j]) != 0L)
					WEAK_SAFE_BLACK[MIRROR[i]] |= BBUtils.getSquare[MIRROR[j]];
			}
		}
	}
	
	@Override
	public int eval(Board b) {
		if (b.isMate())
			return Integer.MIN_VALUE + b.moveNumber;
		if (b.isDraw())
			return 0;
		
		int score = 0; // from white's side; account for turn at the end
		long temp;
		
		int whiteKingLoc = BBUtils.getLocFromBitboard(b.white_king);
		int blackKingLoc = BBUtils.getLocFromBitboard(b.black_king);
		
		boolean endgame = b.isEndGame();
		
		long whitePassedPawns = 0L;
		long blackPassedPawns = 0L;
		
		int numWhitePawns = Long.bitCount(b.white_pawns);
		int numBlackPawns = Long.bitCount(b.black_pawns);
		int numWhitePieces =
				Long.bitCount(b.white_knights) + Long.bitCount(b.white_bishops)
						+ Long.bitCount(b.white_rooks) + Long.bitCount(b.white_queens);
		int numBlackPieces =
				Long.bitCount(b.black_knights) + Long.bitCount(b.black_bishops)
						+ Long.bitCount(b.black_rooks) + Long.bitCount(b.black_queens);
		
		int numwhites = numWhitePieces + numWhitePawns;
		int numblacks = numBlackPieces + numBlackPawns;
		
		// eval material imbalance, by Stef Luijten
		if (b.whitePieceMaterial() + 100 * numWhitePawns > b.blackPieceMaterial() + 100
				* numBlackPawns) {
			score += 45 + 3 * numwhites - 6 * numblacks;
			if (VERBOSE)
				print("Piece imbalance for white: "
						+ (45 + 3 * numwhites - 6 * numblacks));
		}
		else {
			score -= 45 + 3 * numblacks - 6 * numwhites;
			if (VERBOSE)
				print("Piece imbalance for black: "
						+ (45 + 3 * numblacks - 6 * numwhites));
		}
		
		if (VERBOSE)
			System.out.println("Position is endgame? " + b.isEndGame());
		
		// white pawns
		temp = b.white_pawns;
		while (temp != 0L) {
			score += PAWN_VALUE;
			long pawn = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(pawn);
			
			if (VERBOSE)
				print("Pawn on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score += PAWN_POS_WHITE[loc];
			if (VERBOSE)
				print("\tPSQ: " + PAWN_POS_WHITE[loc]);
			
			score += OPP_PAWN_SAFETY_PENALTY[DISTANCES[loc][blackKingLoc]];
			if (VERBOSE)
				print("\tOpp king safety: "
						+ OPP_PAWN_SAFETY_PENALTY[DISTANCES[loc][blackKingLoc]]);
			
			if (endgame) {
				score += OWN_PAWN_SAFETY_BONUS[DISTANCES[loc][whiteKingLoc]];
				if (VERBOSE)
					print("\tOwn king safety: "
							+ OWN_PAWN_SAFETY_BONUS[DISTANCES[loc][whiteKingLoc]]);
			}
			
			if ( (ISOLATED_WHITE[loc] & b.white_pawns) == 0) {
				score -= PENALTY_ISOLATED_PAWN;
				if (VERBOSE)
					print("\tIsolated: " + PENALTY_ISOLATED_PAWN);
			}
			else {
				if ( (BBMagicAttacks.whitePawn[loc + 8] & b.black_pawns) != 0
						&& (BACKWARD_WHITE[loc] & b.white_pawns) == 0) {
					score -= PENALTY_BACKWARD_PAWN;
					if (VERBOSE)
						print("Backward: " + PENALTY_BACKWARD_PAWN);
				}
			}
			
			if ( (PASSED_WHITE[loc] & b.black_pawns) == 0) {
				score += BONUS_PASSED_PAWN;
				if (VERBOSE)
					print("\tPassed: " + BONUS_PASSED_PAWN);
				whitePassedPawns |= pawn;
			}
			
			if ( (BBUtils.maskFile[BBUtils.getLocCol(loc)] & (b.white_pawns ^ pawn)) != 0) {
				score -= PENALTY_DOUBLED_PAWN;
				if (VERBOSE)
					print("\tDoubled: " + PENALTY_DOUBLED_PAWN);
			}
			
			temp &= ~pawn;
		}
		
		// black pawns
		temp = b.black_pawns;
		while (temp != 0L) {
			score -= PAWN_VALUE;
			long pawn = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(pawn);
			
			if (VERBOSE)
				print("Pawn on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score -= PAWN_POS_BLACK[loc];
			if (VERBOSE)
				print("\tPSQ: " + PAWN_POS_BLACK[loc]);
			
			score -= OPP_PAWN_SAFETY_PENALTY[DISTANCES[loc][whiteKingLoc]];
			if (VERBOSE)
				print("\tOpp king safety: "
						+ OPP_PAWN_SAFETY_PENALTY[DISTANCES[loc][whiteKingLoc]]);
			
			if (endgame) {
				score -= OWN_PAWN_SAFETY_BONUS[DISTANCES[loc][blackKingLoc]];
				if (VERBOSE)
					print("\tOwn king safety: "
							+ OWN_PAWN_SAFETY_BONUS[DISTANCES[loc][blackKingLoc]]);
			}
			
			if ( (ISOLATED_BLACK[loc] & b.black_pawns) == 0) {
				score += PENALTY_ISOLATED_PAWN;
				if (VERBOSE)
					print("\tIsolated: " + PENALTY_ISOLATED_PAWN);
			}
			else {
				if ( (BBMagicAttacks.blackPawn[loc - 8] & b.white_pawns) != 0
						&& (BACKWARD_BLACK[loc] & b.black_pawns) == 0) {
					score += PENALTY_BACKWARD_PAWN;
					if (VERBOSE)
						print("\tBackward: " + PENALTY_BACKWARD_PAWN);
				}
			}
			
			if ( (PASSED_BLACK[loc] & b.white_pawns) == 0) {
				score += BONUS_PASSED_PAWN;
				if (VERBOSE)
					print("\tPassed: " + BONUS_PASSED_PAWN);
				blackPassedPawns |= pawn;
			}
			
			if ( (BBUtils.maskFile[BBUtils.getLocCol(loc)] & (b.black_pawns ^ pawn)) != 0) {
				score += PENALTY_DOUBLED_PAWN;
				if (VERBOSE)
					print("\tDoubled: " + PENALTY_DOUBLED_PAWN);
			}
			
			temp &= ~pawn;
		}
		
		// white knights
		temp = b.white_knights;
		while (temp != 0L) {
			score += KNIGHT_VALUE;
			long knight = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(knight);
			
			if (VERBOSE)
				print("Knight on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score += KNIGHT_POS_WHITE[loc];
			if (VERBOSE)
				print("\tPSQ: " + KNIGHT_POS_WHITE[loc]);
			
			score += KNIGHT_SAFETY[DISTANCES[loc][blackKingLoc]];
			if (VERBOSE)
				print("\tKing safety: " + KNIGHT_SAFETY[DISTANCES[loc][blackKingLoc]]);
			
			temp &= ~knight;
		}
		
		// black knights
		temp = b.black_knights;
		while (temp != 0L) {
			score -= KNIGHT_VALUE;
			long knight = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(knight);
			
			if (VERBOSE)
				print("Knight on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score -= KNIGHT_POS_BLACK[loc];
			if (VERBOSE)
				print("\tPSQ: " + KNIGHT_POS_BLACK[loc]);
			
			score -= KNIGHT_SAFETY[DISTANCES[loc][whiteKingLoc]];
			if (VERBOSE)
				print("\tKing safety: " + KNIGHT_SAFETY[DISTANCES[loc][whiteKingLoc]]);
			
			temp &= ~knight;
		}
		
		// white bishops
		temp = b.white_bishops;
		while (temp != 0L) {
			score += BISHOP_VALUE;
			long bishop = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(bishop);
			
			if (VERBOSE)
				print("Bishop on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score += BISHOP_POS_WHITE[loc];
			if (VERBOSE)
				print("\tPSQ: " + BISHOP_POS_WHITE[loc]);
			
			score += BISHOP_SAFETY[DISTANCES[loc][blackKingLoc]];
			if (VERBOSE)
				print("\tKing safety: " + BISHOP_SAFETY[DISTANCES[loc][blackKingLoc]]);
			
			temp &= ~bishop;
		}
		
		// black bishops
		temp = b.black_bishops;
		while (temp != 0L) {
			score -= BISHOP_VALUE;
			long bishop = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(bishop);
			
			if (VERBOSE)
				print("Bishop on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score -= BISHOP_POS_BLACK[loc];
			if (VERBOSE)
				print("\tPSQ: " + BISHOP_POS_BLACK[loc]);
			
			score -= BISHOP_SAFETY[DISTANCES[loc][whiteKingLoc]];
			if (VERBOSE)
				print("\tKing safety: " + BISHOP_SAFETY[DISTANCES[loc][whiteKingLoc]]);
			
			temp &= ~bishop;
		}
		
		// bishop pairs
		if (Long.bitCount(b.white_bishops) > 1) {
			score += BONUS_BISHOP_PAIR;
			if (VERBOSE)
				print("Bonus bishop pair (white): " + BONUS_BISHOP_PAIR);
		}
		if (Long.bitCount(b.black_bishops) > 1) {
			score -= BONUS_BISHOP_PAIR;
			if (VERBOSE)
				print("Bonus bishop pair (black): " + BONUS_BISHOP_PAIR);
		}
		
		// white rooks
		temp = b.white_rooks;
		while (temp != 0L) {
			score += ROOK_VALUE;
			long rook = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(rook);
			
			if (VERBOSE)
				print("Rook on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score += ROOK_POS_WHITE[loc];
			if (VERBOSE)
				print("\tPSQ: " + ROOK_POS_WHITE[loc]);
			
			score += ROOK_SAFETY[DISTANCES[loc][blackKingLoc]];
			if (VERBOSE)
				print("\tKing safety: " + ROOK_SAFETY[DISTANCES[loc][blackKingLoc]]);
			
			if ( (BBUtils.maskFile[BBUtils.getLocCol(loc)] & whitePassedPawns) != 0) {
				long pawnOnSameFile =
						Long.highestOneBit(BBUtils.maskFile[BBUtils.getLocCol(loc)]
								& whitePassedPawns);
				if (BBUtils.getLocFromBitboard(rook) < BBUtils
						.getLocFromBitboard(pawnOnSameFile)) {
					score += BONUS_ROOK_DEFENDING_PASSED_PAWN;
					if (VERBOSE)
						print("\tBehind passed: " + BONUS_ROOK_DEFENDING_PASSED_PAWN);
				}
			}
			
			if ( (BBUtils.maskFile[BBUtils.getLocCol(loc)] & b.black_pawns) == 0L) {
				score += BONUS_ROOK_ON_OPEN_FILE;
				if (VERBOSE)
					print("\tOpen file: " + BONUS_ROOK_ON_OPEN_FILE);
				
				if ( (BBUtils.maskFile[BBUtils.getLocCol(loc)] & (b.white_rooks & ~rook)) != 0L) {
					score += BONUS_TWO_ROOKS_ON_OPEN_FILE;
					
					if (VERBOSE)
						print("\tTwo on open file: " + BONUS_TWO_ROOKS_ON_OPEN_FILE);
				}
			}
			
			temp &= ~rook;
		}
		
		// black rooks
		temp = b.black_rooks;
		while (temp != 0L) {
			score -= ROOK_VALUE;
			long rook = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(rook);
			
			if (VERBOSE)
				print("Rook on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score -= ROOK_POS_BLACK[loc];
			if (VERBOSE)
				print("\tPSQ: " + ROOK_POS_BLACK[loc]);
			
			score -= ROOK_SAFETY[DISTANCES[loc][whiteKingLoc]];
			if (VERBOSE)
				print("\tKing safety: " + ROOK_SAFETY[DISTANCES[loc][whiteKingLoc]]);
			
			if ( (BBUtils.maskFile[BBUtils.getLocCol(loc)] & blackPassedPawns) != 0) {
				long pawnOnSameFile =
						Long.highestOneBit(BBUtils.maskFile[BBUtils.getLocCol(loc)]
								& blackPassedPawns);
				if (BBUtils.getLocFromBitboard(rook) < BBUtils
						.getLocFromBitboard(pawnOnSameFile)) {
					score -= BONUS_ROOK_DEFENDING_PASSED_PAWN;
					if (VERBOSE)
						print("\tBehind passed: " + BONUS_ROOK_DEFENDING_PASSED_PAWN);
				}
			}
			
			if ( (BBUtils.maskFile[BBUtils.getLocCol(loc)] & b.white_pawns) == 0L) {
				score -= BONUS_ROOK_ON_OPEN_FILE;
				if (VERBOSE)
					print("\tOpen file: " + BONUS_ROOK_ON_OPEN_FILE);
				
				if ( (BBUtils.maskFile[BBUtils.getLocCol(loc)] & (b.black_rooks & ~rook)) != 0L) {
					score -= BONUS_TWO_ROOKS_ON_OPEN_FILE;
					
					if (VERBOSE)
						print("\tTwo on open file: " + BONUS_TWO_ROOKS_ON_OPEN_FILE);
				}
			}
			
			temp &= ~rook;
		}
		
		// white queens
		temp = b.white_queens;
		while (temp != 0L) {
			score += QUEEN_VALUE;
			long queen = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(queen);
			
			if (VERBOSE)
				print("Queen on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score += QUEEN_POS_WHITE[loc];
			if (VERBOSE)
				print("\tPSQ: " + QUEEN_POS_WHITE[loc]);
			
			score += QUEEN_SAFETY[DISTANCES[loc][blackKingLoc]];
			if (VERBOSE)
				print("\tKing safety: " + QUEEN_SAFETY[DISTANCES[loc][blackKingLoc]]);
			
			temp &= ~queen;
		}
		
		// black queens
		temp = b.black_queens;
		while (temp != 0L) {
			score -= QUEEN_VALUE;
			long queen = Long.lowestOneBit(temp);
			int loc = BBUtils.getLocFromBitboard(queen);
			
			if (VERBOSE)
				print("Queen on " + BBUtils.intToAlgebraicLoc(loc) + ":");
			
			score -= QUEEN_POS_BLACK[loc];
			if (VERBOSE)
				print("\tPSQ: " + QUEEN_POS_BLACK[loc]);
			
			score -= QUEEN_SAFETY[DISTANCES[loc][whiteKingLoc]];
			if (VERBOSE)
				print("\tKing safety: " + QUEEN_SAFETY[DISTANCES[loc][whiteKingLoc]]);
			
			temp &= ~queen;
		}
		
		if (endgame) {
			score += KING_POS_E_WHITE[whiteKingLoc];
			score -= KING_POS_E_BLACK[blackKingLoc];
			if (VERBOSE) {
				print("King on " + BBUtils.intToAlgebraicLoc(whiteKingLoc) + ":");
				print("\tPSQ: " + KING_POS_E_WHITE[whiteKingLoc]);
				print("King on " + BBUtils.intToAlgebraicLoc(blackKingLoc) + ":");
				print("\tPSQ: " + KING_POS_E_BLACK[blackKingLoc]);
			}
		}
		else {
			// white king
			if (VERBOSE)
				print("King on " + BBUtils.intToAlgebraicLoc(whiteKingLoc) + ":");
			
			score += KING_POS_M_WHITE[whiteKingLoc];
			if (VERBOSE)
				print("\tPSQ: " + KING_POS_M_WHITE[whiteKingLoc]);
			
			score +=
					BONUS_STRONG_SHIELD
							* Long.bitCount(STRONG_SAFE_WHITE[whiteKingLoc]
									& b.white_pawns);
			if (VERBOSE)
				print("\tStrong shield: " + BONUS_STRONG_SHIELD
						* Long.bitCount(STRONG_SAFE_WHITE[whiteKingLoc] & b.white_pawns));
			
			score +=
					BONUS_WEAK_SHIELD
							* Long.bitCount(WEAK_SAFE_WHITE[whiteKingLoc] & b.white_pawns);
			if (VERBOSE)
				print("\tWeak shield: " + BONUS_WEAK_SHIELD
						* Long.bitCount(WEAK_SAFE_WHITE[whiteKingLoc] & b.white_pawns));
			
			// black king
			if (VERBOSE)
				print("King on " + BBUtils.intToAlgebraicLoc(blackKingLoc) + ":");
			
			score -= KING_POS_M_BLACK[blackKingLoc];
			if (VERBOSE)
				print("\tPSQ: " + KING_POS_M_BLACK[blackKingLoc]);
			
			score -=
					BONUS_STRONG_SHIELD
							* Long.bitCount(STRONG_SAFE_BLACK[blackKingLoc]
									& b.black_pawns);
			if (VERBOSE)
				print("\tStrong shield: " + BONUS_STRONG_SHIELD
						* Long.bitCount(STRONG_SAFE_BLACK[blackKingLoc] & b.black_pawns));
			
			score -=
					BONUS_WEAK_SHIELD
							* Long.bitCount(WEAK_SAFE_BLACK[blackKingLoc] & b.black_pawns);
			if (VERBOSE)
				print("\tWeak shield: " + BONUS_WEAK_SHIELD
						* Long.bitCount(WEAK_SAFE_BLACK[blackKingLoc] & b.black_pawns));
		}
		
		if (b.white_to_move)
			return score;
		return -score;
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
}
