package movegen;

import godot.Board;
import godot.Move;
import util.BBUtils;

public class MoveGetter {
	private static long[] kingMoves =
			{ 0x303L, 0x707L, 0xe0eL, 0x1c1cL, 0x3838L, 0x7070L, 0xe0e0L, 0xc0c0L, 0x30303L, 0x70707L, 0xe0e0eL, 0x1c1c1cL, 0x383838L, 0x707070L, 0xe0e0e0L, 0xc0c0c0L, 0x3030300L, 0x7070700L, 0xe0e0e00L, 0x1c1c1c00L, 0x38383800L, 0x70707000L, 0xe0e0e000L, 0xc0c0c000L, 0x303030000L, 0x707070000L, 0xe0e0e0000L, 0x1c1c1c0000L, 0x3838380000L, 0x7070700000L, 0xe0e0e00000L, 0xc0c0c00000L, 0x30303000000L, 0x70707000000L, 0xe0e0e000000L, 0x1c1c1c000000L, 0x383838000000L, 0x707070000000L, 0xe0e0e0000000L, 0xc0c0c0000000L, 0x3030300000000L, 0x7070700000000L, 0xe0e0e00000000L, 0x1c1c1c00000000L, 0x38383800000000L, 0x70707000000000L, 0xe0e0e000000000L, 0xc0c0c000000000L, 0x303030000000000L, 0x707070000000000L, 0xe0e0e0000000000L, 0x1c1c1c0000000000L, 0x3838380000000000L, 0x7070700000000000L, 0xe0e0e00000000000L, 0xc0c0c00000000000L, 0x303000000000000L, 0x707000000000000L, 0xe0e000000000000L, 0x1c1c000000000000L, 0x3838000000000000L, 0x7070000000000000L, 0xe0e0000000000000L, 0xc0c0000000000000L };
	
	private static long[] knightMoves =
			{ 0x20400L, 0x50800L, 0xa1100L, 0x142200L, 0x284400L, 0x508800L, 0xa01000L, 0x402000L, 0x2040004L, 0x5080008L, 0xa110011L, 0x14220022L, 0x28440044L, 0x50880088L, 0xa0100010L, 0x40200020L, 0x204000402L, 0x508000805L, 0xa1100110aL, 0x1422002214L, 0x2844004428L, 0x5088008850L, 0xa0100010a0L, 0x4020002040L, 0x20400040200L, 0x50800080500L, 0xa1100110a00L, 0x142200221400L, 0x284400442800L, 0x508800885000L, 0xa0100010a000L, 0x402000204000L, 0x2040004020000L, 0x5080008050000L, 0xa1100110a0000L, 0x14220022140000L, 0x28440044280000L, 0x50880088500000L, 0xa0100010a00000L, 0x40200020400000L, 0x204000402000000L, 0x508000805000000L, 0xa1100110a000000L, 0x1422002214000000L, 0x2844004428000000L, 0x5088008850000000L, 0xa0100010a0000000L, 0x4020002040000000L, 0x400040200000000L, 0x800080500000000L, 0x1100110a00000000L, 0x2200221400000000L, 0x4400442800000000L, 0x8800885000000000L, 0x100010a000000000L, 0x2000204000000000L, 0x4020000000000L, 0x8050000000000L, 0x110a0000000000L, 0x22140000000000L, 0x44280000000000L, 0x88500000000000L, 0x10a00000000000L, 0x20400000000000L };
	
	private static long pseudoLegalKnightMoveDestinations(int loc, long targets) {
		return knightMoves[loc] & targets;
	}
	
	private static long pseudoLegalKingMoveDestinations(int loc, long targets) {
		return kingMoves[loc] & targets;
	}
	
	public static int getWhiteKingMoves(Board b, int[] moves, int index) {
		long king = b.white_king;
		
		int num_moves_generated = 0;
		
		int from_loc = BBUtils.getLocFromBitboard(king);
		long movelocs = pseudoLegalKingMoveDestinations(from_loc, ~b.white_pieces);
		
		while (movelocs != 0L) {
			long to = Long.lowestOneBit(movelocs);
			int to_loc = BBUtils.getLocFromBitboard(to);
			boolean capt = (to & b.black_pieces) != 0L;
			int move = Move.generateMove(from_loc, to_loc, Move.KING, capt, Move.NO_FLAG);
			moves[index + num_moves_generated] = move;
			num_moves_generated++;
			movelocs &= ~to;
		}
		
		if (b.white_castle_k) {
			if ( (b.white_king << 1 & b.all_pieces) == 0L
					&& (b.white_king << 2 & b.all_pieces) == 0L) {
				if ( (b.white_king << 3 & b.white_rooks) != 0L) {
					if ( !BBMagicAttacks.isSquareAttacked(b, b.white_king, true)
							&& !BBMagicAttacks.isSquareAttacked(b, b.white_king << 1,
									true)
							&& !BBMagicAttacks.isSquareAttacked(b, b.white_king << 2,
									true)) {
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, from_loc + 2, Move.KING,
										false, Move.FLAG_CASTLE_KINGSIDE);
						num_moves_generated++;
					}
				}
			}
		}
		
		if (b.white_castle_q) {
			if ( (b.white_king >>> 1 & b.all_pieces) == 0L
					&& (b.white_king >>> 2 & b.all_pieces) == 0L
					&& (b.white_king >>> 3 & b.all_pieces) == 0L) {
				if ( (b.white_king >>> 4 & b.white_rooks) != 0L) {
					if ( !BBMagicAttacks.isSquareAttacked(b, b.white_king, true)
							&& !BBMagicAttacks.isSquareAttacked(b, b.white_king >>> 1,
									true)
							&& !BBMagicAttacks.isSquareAttacked(b, b.white_king >>> 2,
									true)) {
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, from_loc - 2, Move.KING,
										false, Move.FLAG_CASTLE_QUEENSIDE);
						num_moves_generated++;
					}
				}
			}
		}
		
		return num_moves_generated;
	}
	
	public static int getBlackKingMoves(Board b, int[] moves, int index) {
		long king = b.black_king;
		
		int num_moves_generated = 0;
		
		int from_loc = BBUtils.getLocFromBitboard(king);
		long movelocs = pseudoLegalKingMoveDestinations(from_loc, ~b.black_pieces);
		
		while (movelocs != 0L) {
			long to = Long.lowestOneBit(movelocs);
			int to_loc = BBUtils.getLocFromBitboard(to);
			boolean capt = (to & b.white_pieces) != 0L;
			int move = Move.generateMove(from_loc, to_loc, Move.KING, capt, Move.NO_FLAG);
			moves[index + num_moves_generated] = move;
			num_moves_generated++;
			movelocs &= ~to;
		}
		
		if (b.black_castle_k) {
			if ( (b.black_king << 1 & b.all_pieces) == 0L
					&& (b.black_king << 2 & b.all_pieces) == 0L) {
				if ( (b.black_king << 3 & b.black_rooks) != 0L) {
					if ( !BBMagicAttacks.isSquareAttacked(b, b.black_king, false)
							&& !BBMagicAttacks.isSquareAttacked(b, b.black_king << 1,
									false)
							&& !BBMagicAttacks.isSquareAttacked(b, b.black_king << 2,
									false)) {
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, from_loc + 2, Move.KING,
										false, Move.FLAG_CASTLE_KINGSIDE);
						num_moves_generated++;
					}
				}
			}
		}
		
		if (b.black_castle_q) {
			if ( (b.black_king >>> 1 & b.all_pieces) == 0L
					&& (b.black_king >>> 2 & b.all_pieces) == 0L
					&& (b.black_king >>> 3 & b.all_pieces) == 0L) {
				if ( (b.black_king >>> 4 & b.black_rooks) != 0L) {
					if ( !BBMagicAttacks.isSquareAttacked(b, b.black_king, false)
							&& !BBMagicAttacks.isSquareAttacked(b, b.black_king >>> 1,
									false)
							&& !BBMagicAttacks.isSquareAttacked(b, b.black_king >>> 2,
									false)) {
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, from_loc - 2, Move.KING,
										false, Move.FLAG_CASTLE_QUEENSIDE);
						num_moves_generated++;
					}
				}
			}
		}
		
		return num_moves_generated;
	}
	
	public static int getWhiteKnightMoves(Board b, int[] moves, int index) {
		long knights = b.white_knights;
		int num_moves_generated = 0;
		
		while (knights != 0L) {
			long from = Long.lowestOneBit(knights);
			int from_loc = BBUtils.getLocFromBitboard(from);
			long movelocs = pseudoLegalKnightMoveDestinations(from_loc, ~b.white_pieces);
			
			while (movelocs != 0L) {
				long to = Long.lowestOneBit(movelocs);
				int to_loc = BBUtils.getLocFromBitboard(to);
				boolean capt = (to & b.black_pieces) != 0L;
				int move =
						Move.generateMove(from_loc, to_loc, Move.KNIGHT, capt,
								Move.NO_FLAG);
				moves[index + num_moves_generated] = move;
				num_moves_generated++;
				movelocs &= ~to;
			}
			knights &= ~from;
		}
		return num_moves_generated;
	}
	
	public static int getBlackKnightMoves(Board b, int[] moves, int index) {
		long knights = b.black_knights;
		int num_moves_generated = 0;
		
		while (knights != 0L) {
			long from = Long.lowestOneBit(knights);
			int from_loc = BBUtils.getLocFromBitboard(from);
			long movelocs = pseudoLegalKnightMoveDestinations(from_loc, ~b.black_pieces);
			
			while (movelocs != 0L) {
				long to = Long.lowestOneBit(movelocs);
				int to_loc = BBUtils.getLocFromBitboard(to);
				boolean capt = (to & b.white_pieces) != 0L;
				int move =
						Move.generateMove(from_loc, to_loc, Move.KNIGHT, capt,
								Move.NO_FLAG);
				moves[index + num_moves_generated] = move;
				num_moves_generated++;
				movelocs &= ~to;
			}
			knights &= ~from;
		}
		return num_moves_generated;
	}
	
	/*
	 * OK, this can be done the following way:
	 * (1) Check what rank the pawn is on.
	 * (2a) If the pawn is on the 7th rank (2nd for black), we can forget about
	 * double jump and we must consider promotions.
	 * (2b) If the pawn isn't on the 7th, then we proceed normally.
	 * 
	 * Special note: If the pawn is on the a-file, it cannot capture left; the
	 * same goes for an h-file pawn and capturing right.
	 */
	public static int getWhitePawnMoves(Board b, int[] moves, int index) {
		long pawns = b.white_pawns;
		int num_moves_generated = 0;
		
		while (pawns != 0L) {
			long from = Long.lowestOneBit(pawns);
			int from_loc = BBUtils.getLocFromBitboard(from);
			
			if ( (from & BBUtils.maskRank[BBUtils.RANK_7]) != 0L) {
				// promos are possible, no en passant
				if ( (from & BBUtils.b_right) == 0L && (from << 7 & b.black_pieces) != 0L) {
					int to_loc = BBUtils.getLocFromBitboard(from << 7);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_QUEEN);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_KNIGHT);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_ROOK);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_BISHOP);
					num_moves_generated++;
				}
				
				if ( (from & BBUtils.b_left) == 0L && (from << 9 & b.black_pieces) != 0L) {
					int to_loc = BBUtils.getLocFromBitboard(from << 9);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_QUEEN);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_KNIGHT);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_ROOK);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_BISHOP);
					num_moves_generated++;
				}
				
				if ( (from << 8 & b.all_pieces) == 0L) {
					int to_loc = BBUtils.getLocFromBitboard(from << 8);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.FLAG_PROMOTE_QUEEN);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.FLAG_PROMOTE_KNIGHT);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.FLAG_PROMOTE_ROOK);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.FLAG_PROMOTE_BISHOP);
					num_moves_generated++;
				}
			}
			else {
				// no promos to worry about, but there is en passant
				if ( ( (from & BBUtils.b_right) == 0L)
						&& ( ( (from << 7 & b.black_pieces) != 0L) || BBUtils
								.getLocFromBitboard(from << 7) == b.enPassantLoc)) {
					int to_loc = BBUtils.getLocFromBitboard(from << 7);
					if (BBUtils.getLocFromBitboard(from << 7) == b.enPassantLoc)
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, to_loc, Move.PAWN, true,
										Move.FLAG_EN_PASSANT);
					else
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, to_loc, Move.PAWN, true,
										Move.NO_FLAG);
					num_moves_generated++;
				}
				
				if ( ( (from & BBUtils.b_left) == 0L)
						&& ( ( (from << 9 & b.black_pieces) != 0L) || BBUtils
								.getLocFromBitboard(from << 9) == b.enPassantLoc)) {
					int to_loc = BBUtils.getLocFromBitboard(from << 9);
					if (BBUtils.getLocFromBitboard(from << 9) == b.enPassantLoc)
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, to_loc, Move.PAWN, true,
										Move.FLAG_EN_PASSANT);
					else
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, to_loc, Move.PAWN, true,
										Move.NO_FLAG);
					num_moves_generated++;
				}
				
				boolean one_square_ahead_clear = false;
				
				if ( (from << 8 & b.all_pieces) == 0L) {
					one_square_ahead_clear = true;
					int to_loc = BBUtils.getLocFromBitboard(from << 8);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.NO_FLAG);
					num_moves_generated++;
				}
				
				if ( (from & BBUtils.maskRank[BBUtils.RANK_2]) != 0L
						&& one_square_ahead_clear && (from << 16 & b.all_pieces) == 0L) {
					int to_loc = BBUtils.getLocFromBitboard(from << 16);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.NO_FLAG);
					num_moves_generated++;
				}
			}
			
			pawns &= ~from;
		}
		return num_moves_generated;
	}
	
	/*
	 * See description of getWhitePawnMoves for an explanation of logic.
	 */
	public static int getBlackPawnMoves(Board b, int[] moves, int index) {
		long pawns = b.black_pawns;
		int num_moves_generated = 0;
		
		while (pawns != 0L) {
			long from = Long.lowestOneBit(pawns);
			int from_loc = BBUtils.getLocFromBitboard(from);
			
			if ( (from & BBUtils.maskRank[BBUtils.RANK_2]) != 0L) {
				// promos are possible, no en passant
				if ( (from & BBUtils.b_left) == 0L && (from >>> 7 & b.white_pieces) != 0L) {
					int to_loc = BBUtils.getLocFromBitboard(from >>> 7);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_QUEEN);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_KNIGHT);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_ROOK);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_BISHOP);
					num_moves_generated++;
				}
				
				if ( (from & BBUtils.b_right) == 0L
						&& (from >>> 9 & b.white_pieces) != 0L) {
					int to_loc = BBUtils.getLocFromBitboard(from >>> 9);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_QUEEN);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_KNIGHT);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_ROOK);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, true,
									Move.FLAG_PROMOTE_BISHOP);
					num_moves_generated++;
				}
				
				if ( (from >>> 8 & b.all_pieces) == 0L) {
					int to_loc = BBUtils.getLocFromBitboard(from >>> 8);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.FLAG_PROMOTE_QUEEN);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.FLAG_PROMOTE_KNIGHT);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.FLAG_PROMOTE_ROOK);
					num_moves_generated++;
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.FLAG_PROMOTE_BISHOP);
					num_moves_generated++;
				}
			}
			else {
				// no promos to worry about, but there is en passant
				if ( ( (from & BBUtils.b_left) == 0L)
						&& ( ( (from >>> 7 & b.white_pieces) != 0L) || BBUtils
								.getLocFromBitboard(from >>> 7) == b.enPassantLoc)) {
					int to_loc = BBUtils.getLocFromBitboard(from >>> 7);
					if (BBUtils.getLocFromBitboard(from >>> 7) == b.enPassantLoc)
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, to_loc, Move.PAWN, true,
										Move.FLAG_EN_PASSANT);
					else
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, to_loc, Move.PAWN, true,
										Move.NO_FLAG);
					num_moves_generated++;
				}
				
				if ( ( (from & BBUtils.b_right) == 0L)
						&& ( ( (from >>> 9 & b.white_pieces) != 0L) || BBUtils
								.getLocFromBitboard(from >>> 9) == b.enPassantLoc)) {
					int to_loc = BBUtils.getLocFromBitboard(from >>> 9);
					if (BBUtils.getLocFromBitboard(from >>> 9) == b.enPassantLoc)
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, to_loc, Move.PAWN, true,
										Move.FLAG_EN_PASSANT);
					else
						moves[index + num_moves_generated] =
								Move.generateMove(from_loc, to_loc, Move.PAWN, true,
										Move.NO_FLAG);
					num_moves_generated++;
				}
				
				boolean one_square_ahead_clear = false;
				
				if ( (from >>> 8 & b.all_pieces) == 0L) {
					one_square_ahead_clear = true;
					int to_loc = BBUtils.getLocFromBitboard(from >>> 8);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.NO_FLAG);
					num_moves_generated++;
				}
				
				if ( (from & BBUtils.maskRank[BBUtils.RANK_7]) != 0L
						&& one_square_ahead_clear && (from >>> 16 & b.all_pieces) == 0L) {
					int to_loc = BBUtils.getLocFromBitboard(from >>> 16);
					moves[index + num_moves_generated] =
							Move.generateMove(from_loc, to_loc, Move.PAWN, false,
									Move.NO_FLAG);
					num_moves_generated++;
				}
			}
			
			pawns &= ~from;
		}
		return num_moves_generated;
	}
	
	public static int getWhiteBishopMoves(Board b, int[] moves, int index) {
		long bishops = b.white_bishops;
		int num_moves_generated = 0;
		
		while (bishops != 0L) {
			long from = Long.lowestOneBit(bishops);
			int from_loc = BBUtils.getLocFromBitboard(from);
			long movelocs =
					BBMagicAttacks.getBishopAttacks(from_loc, b.all_pieces & ~from);
			movelocs &= ~b.white_pieces;
			
			while (movelocs != 0L) {
				long to = Long.lowestOneBit(movelocs);
				int to_loc = BBUtils.getLocFromBitboard(to);
				boolean capt = (to & b.black_pieces) != 0L;
				int move =
						Move.generateMove(from_loc, to_loc, Move.BISHOP, capt,
								Move.NO_FLAG);
				moves[index + num_moves_generated] = move;
				num_moves_generated++;
				movelocs &= ~to;
			}
			
			bishops &= ~from;
		}
		return num_moves_generated;
	}
	
	public static int getBlackBishopMoves(Board b, int[] moves, int index) {
		long bishops = b.black_bishops;
		int num_moves_generated = 0;
		
		while (bishops != 0L) {
			long from = Long.lowestOneBit(bishops);
			int from_loc = BBUtils.getLocFromBitboard(from);
			long movelocs =
					BBMagicAttacks.getBishopAttacks(from_loc, b.all_pieces & ~from);
			movelocs &= ~b.black_pieces;
			
			while (movelocs != 0L) {
				long to = Long.lowestOneBit(movelocs);
				int to_loc = BBUtils.getLocFromBitboard(to);
				boolean capt = (to & b.white_pieces) != 0L;
				int move =
						Move.generateMove(from_loc, to_loc, Move.BISHOP, capt,
								Move.NO_FLAG);
				moves[index + num_moves_generated] = move;
				num_moves_generated++;
				movelocs &= ~to;
			}
			
			bishops &= ~from;
		}
		return num_moves_generated;
	}
	
	public static int getWhiteRookMoves(Board b, int[] moves, int index) {
		long rooks = b.white_rooks;
		int num_moves_generated = 0;
		
		while (rooks != 0L) {
			long from = Long.lowestOneBit(rooks);
			int from_loc = BBUtils.getLocFromBitboard(from);
			long movelocs = BBMagicAttacks.getRookAttacks(from_loc, b.all_pieces & ~from);
			movelocs &= ~b.white_pieces;
			
			while (movelocs != 0L) {
				long to = Long.lowestOneBit(movelocs);
				int to_loc = BBUtils.getLocFromBitboard(to);
				boolean capt = (to & b.black_pieces) != 0L;
				int move =
						Move.generateMove(from_loc, to_loc, Move.ROOK, capt, Move.NO_FLAG);
				moves[index + num_moves_generated] = move;
				num_moves_generated++;
				movelocs &= ~to;
			}
			
			rooks &= ~from;
		}
		return num_moves_generated;
	}
	
	public static int getBlackRookMoves(Board b, int[] moves, int index) {
		long rooks = b.black_rooks;
		int num_moves_generated = 0;
		
		while (rooks != 0L) {
			long from = Long.lowestOneBit(rooks);
			int from_loc = BBUtils.getLocFromBitboard(from);
			long movelocs = BBMagicAttacks.getRookAttacks(from_loc, b.all_pieces & ~from);
			movelocs &= ~b.black_pieces;
			
			while (movelocs != 0L) {
				long to = Long.lowestOneBit(movelocs);
				int to_loc = BBUtils.getLocFromBitboard(to);
				boolean capt = (to & b.white_pieces) != 0L;
				int move =
						Move.generateMove(from_loc, to_loc, Move.ROOK, capt, Move.NO_FLAG);
				moves[index + num_moves_generated] = move;
				num_moves_generated++;
				movelocs &= ~to;
			}
			
			rooks &= ~from;
		}
		return num_moves_generated;
	}
	
	public static int getWhiteQueenMoves(Board b, int[] moves, int index) {
		long queens = b.white_queens;
		int num_moves_generated = 0;
		
		while (queens != 0L) {
			long from = Long.lowestOneBit(queens);
			int from_loc = BBUtils.getLocFromBitboard(from);
			long movelocs =
					BBMagicAttacks.getQueenAttacks(from_loc, b.all_pieces & ~from);
			
			movelocs &= ~b.white_pieces;
			
			while (movelocs != 0L) {
				long to = Long.lowestOneBit(movelocs);
				int to_loc = BBUtils.getLocFromBitboard(to);
				boolean capt = (to & b.black_pieces) != 0L;
				int move =
						Move.generateMove(from_loc, to_loc, Move.QUEEN, capt,
								Move.NO_FLAG);
				moves[index + num_moves_generated] = move;
				num_moves_generated++;
				movelocs &= ~to;
			}
			
			queens &= ~from;
		}
		return num_moves_generated;
	}
	
	public static int getBlackQueenMoves(Board b, int[] moves, int index) {
		long queens = b.black_queens;
		int num_moves_generated = 0;
		
		while (queens != 0L) {
			long from = Long.lowestOneBit(queens);
			int from_loc = BBUtils.getLocFromBitboard(from);
			long movelocs =
					BBMagicAttacks.getQueenAttacks(from_loc, b.all_pieces & ~from);
			movelocs &= ~b.black_pieces;
			
			while (movelocs != 0L) {
				long to = Long.lowestOneBit(movelocs);
				int to_loc = BBUtils.getLocFromBitboard(to);
				boolean capt = (to & b.white_pieces) != 0L;
				int move =
						Move.generateMove(from_loc, to_loc, Move.QUEEN, capt,
								Move.NO_FLAG);
				moves[index + num_moves_generated] = move;
				num_moves_generated++;
				movelocs &= ~to;
			}
			
			queens &= ~from;
		}
		return num_moves_generated;
	}
}
