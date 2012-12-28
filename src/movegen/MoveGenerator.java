package movegen;

import godot.Board;
import godot.Move;

public class MoveGenerator {
	/**
	 * Gets all <i>pseudo-legal</i> moves available for the side to move. If the
	 * generated moves need to be legal (and not simply pseudo-legal), then
	 * <code>MoveGenerator.getAllLegalMoves</code> should be used instead.
	 * 
	 * @param b
	 *            the board to consider
	 * @param moves
	 *            the integer array to write onto
	 * @return the number of <i>pseudo-legal</i> moves generated, with the
	 *         actual moves written onto the passed array.
	 */
	public static int getAllMoves(Board b, int[] moves) {
		if (b.white_to_move)
			return getAllWhiteMoves(b, moves);
		return getAllBlackMoves(b, moves);
	}
	
	private static int getAllWhiteMoves(Board b, int[] moves) {
		int index = 0;
		
		index += MoveGetter.getWhitePawnMoves(b, moves, index);
		index += MoveGetter.getWhiteKnightMoves(b, moves, index);
		index += MoveGetter.getWhiteKingMoves(b, moves, index);
		index += MoveGetter.getWhiteRookMoves(b, moves, index);
		index += MoveGetter.getWhiteBishopMoves(b, moves, index);
		index += MoveGetter.getWhiteQueenMoves(b, moves, index);
		
		return index;
	}
	
	private static int getAllBlackMoves(Board b, int[] moves) {
		int index = 0;
		
		index += MoveGetter.getBlackPawnMoves(b, moves, index);
		index += MoveGetter.getBlackKnightMoves(b, moves, index);
		index += MoveGetter.getBlackKingMoves(b, moves, index);
		index += MoveGetter.getBlackRookMoves(b, moves, index);
		index += MoveGetter.getBlackBishopMoves(b, moves, index);
		index += MoveGetter.getBlackQueenMoves(b, moves, index);
		
		return index;
	}
	
	/**
	 * Gets all legal moves available for the side to move. If the actual
	 * legality of the moves generated is unimportant (the potential for moves
	 * staying or moving into check is unimportant),
	 * <code>MoveGenerator.getAllMoves</code> should be used instead.
	 * 
	 * @param b
	 *            the board to consider
	 * @param moves
	 *            the integer array to write onto
	 * @return the number of legal moves generated, with the
	 *         actual moves written onto the passed array.
	 */
	public static int getAllLegalMoves(Board b, int[] moves) {
		int lastIndex = getAllMoves(b, moves);
		int j = 0;
		for (int i = 0; i < lastIndex; i++) {
			if (b.makeMove(moves[i])) {
				moves[j++] = moves[i];
				b.undoMove();
			}
		}
		return j;
	}
	
	/**
	 * Gets all legal moves that happen to be a capture or promotion.
	 * 
	 * @param b
	 *            the board to consider
	 * @param moves
	 *            the integer array to write onto
	 * @return the number of moves generated, with the actual moves written onto
	 *         the passed array.
	 */
	public static int getAllCapturesAndPromotions(Board b, int[] moves) {
		int lastIndex = getAllLegalMoves(b, moves);
		int j = 0;
		
		for (int i = 0; i < lastIndex; i++) {
			if (Move.isPromotion(Move.getFlag(moves[i])) || Move.isCapture(moves[i])) {
				moves[j++] = moves[i];
			}
		}
		
		return j;
	}
}
