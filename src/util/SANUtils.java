package util;

import movegen.MoveGenerator;
import godot.Board;
import godot.Move;

public class SANUtils {
	/**
	 * Gets the integer representing the move the passed SAN-described move
	 * describes.
	 * 
	 * @param b
	 *            the position the move is being made from (aka the board before
	 *            the move was made).
	 * @param san
	 *            the integer representing the move the SAN-described move
	 *            describes.
	 * @return the integer version of the SAN move.
	 */
	public static int getMove(Board b, String san) {
		int[] moves = new int[Board.MAX_MOVES];
		int num_moves = MoveGenerator.getAllLegalMoves(b, moves);
		
		for (int i = 0; i < num_moves; i++) {
			if (san.equals(getSAN(b, moves[i])))
				return moves[i];
		}
		return -1;
	}
	
	/**
	 * Gets the SAN version of a move.
	 * 
	 * @param b
	 *            the position the move is being made from (aka the board before
	 *            the move was made).
	 * @param move
	 *            the integer representing the move being made.
	 * @return the SAN version of the move.
	 */
	public static String getSAN(Board b, int move) {
		if (Move.getFlag(move) == Move.FLAG_CASTLE_KINGSIDE) {
			String san = "O-O";
			b.makeMove(move);
			if (b.isMate())
				san += "#";
			else if (b.isCheck())
				san += "+";
			b.undoMove();
			return san;
		}
		if (Move.getFlag(move) == Move.FLAG_CASTLE_QUEENSIDE) {
			String san = "O-O-O";
			b.makeMove(move);
			if (b.isMate())
				san += "#";
			else if (b.isCheck())
				san += "+";
			b.undoMove();
			return san;
		}
		
		int flag = Move.getFlag(move);
		int piece = Move.getPieceType(move);
		int from = Move.getFrom(move);
		int to = Move.getTo(move);
		int from_col = BBUtils.getLocCol(from);
		int from_row = BBUtils.getLocRow(from);
		
		boolean amb_file = false, amb_rank = false, amb_move = false;
		
		int[] moves = new int[Board.MAX_MOVES];
		int num_moves = MoveGenerator.getAllLegalMoves(b, moves);
		
		for (int i = 0; i < num_moves; i++) {
			if (moves[i] == move || Move.getTo(moves[i]) != to)
				continue;
			
			if (Move.isPromotion(Move.getFlag(move))) {
				if (Move.getFlag(moves[i]) != flag)
					continue;
			}
			
			int pieceX = Move.getPieceType(moves[i]);
			if (pieceX != piece)
				continue;
			
			int sq = Move.getFrom(moves[i]);
			
			int sq_col = BBUtils.getLocCol(sq);
			int sq_row = BBUtils.getLocRow(sq);
			
			if (sq_col == from_col)
				amb_file = true;
			if (sq_row == from_row)
				amb_rank = true;
			
			amb_move = true;
		}
		
		String san = BBUtils.moveToPieceString(move);
		
		// this is the technique Stockfish uses.
		if (amb_move) {
			if ( !amb_file)
				san += BBUtils.intColToString(from);
			else if ( !amb_rank)
				san += BBUtils.intRowToString(from);
			else
				san += BBUtils.intToAlgebraicLoc(from);
		}
		if (Move.isCapture(move)) {
			if (Move.getPieceType(move) == Move.PAWN && !amb_rank)
				san += BBUtils.intColToString(from);
			san += "x";
		}
		san += BBUtils.intToAlgebraicLoc(to);
		if (Move.isPromotion(Move.getFlag(move))) {
			san += "=";
			switch (Move.getFlag(move)) {
				case Move.FLAG_PROMOTE_BISHOP:
					san += "B";
					break;
				case Move.FLAG_PROMOTE_KNIGHT:
					san += "N";
					break;
				case Move.FLAG_PROMOTE_ROOK:
					san += "R";
					break;
				case Move.FLAG_PROMOTE_QUEEN:
					san += "Q";
					break;
			}
		}
		
		b.makeMove(move);
		if (b.isMate())
			san += "#";
		else if (b.isCheck())
			san += "+";
		b.undoMove();
		
		return san;
	}
}
