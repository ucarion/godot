package godot;

/**
 * A move is actually represented with an int; this class is more to make
 * negotiating with
 * these moves slightly less annoying. Moves are made with 19 bytes, which are:
 * <code><pre>
 * 
 *  101 | 0 | 101 | 010101 | 010101 |
 *  flg | c | typ |   to   |  from  |
 * 
 * </code></pre>the flag indicates special events in a move, such as promotion,
 * castling, or en passant.
 * The c is capture -- a boolean, really. Typ is for type, aka the piece that is
 * moving.
 * To is 6 bits representing the 0..63 locations we can move from, and from is
 * where we're
 * moving from.
 * 
 * @author Ulysse
 * 
 */
public class Move {
	public static final int MOVE_W_OO = 86404;
	public static final int MOVE_W_OOO = 151684;
	public static final int MOVE_B_OO = 90044;
	public static final int MOVE_B_OOO = 155324;
	
	public static final int PAWN = 0;
	public static final int KNIGHT = 1;
	public static final int BISHOP = 2;
	public static final int ROOK = 3;
	public static final int QUEEN = 4;
	public static final int KING = 5;
	
	public static final int NO_FLAG = 0;
	public static final int FLAG_CASTLE_KINGSIDE = 1;
	public static final int FLAG_CASTLE_QUEENSIDE = 2;
	public static final int FLAG_EN_PASSANT = 3;
	public static final int FLAG_PROMOTE_KNIGHT = 4;
	public static final int FLAG_PROMOTE_BISHOP = 5;
	public static final int FLAG_PROMOTE_ROOK = 6;
	public static final int FLAG_PROMOTE_QUEEN = 7;
	
	/**
	 * Generates a new move.
	 * 
	 * @param from
	 *            an integer in [0, 64) representing where the piece is moving
	 *            from.
	 * @param to
	 *            an integer in [0, 64) representing where the piece is moving
	 *            to.
	 * @param type
	 *            an integer representing the type of piece moving (these should
	 *            be acquired from <code>Move.PAWN</code>,
	 *            <code>Move.KNIGHT</code>, etc.)
	 * @param capture
	 *            true if the move is a capture, false otherwise.
	 * @param flag
	 *            an integer representing any special features about this move
	 *            (these should be acquired from <code>Move.NO_FLAG</code>,
	 *            <code>Move.FLAG_CASTLE_KINGSIDE</code>, etc.)
	 * @return the integer representing the passed information about a move
	 */
	public static int generateMove(int from, int to, int type, boolean capture, int flag) {
		return (from) | (to << 6) | (type << 12) | ( (capture ? 1 : 0) << 15)
				| (flag << 16);
	}
	
	/**
	 * Gets the integer in [0, 64) representing the "from" of this move.
	 * 
	 * @param move
	 *            the integer representing the move in question
	 * @return the location from which this move began.
	 */
	public static int getFrom(int move) {
		return move & 0x3f;
	}
	
	/**
	 * Gets the integer in [0, 64) representing the "to" of this move.
	 * 
	 * @param move
	 *            the integer representing the move in question
	 * @return the location to which this move went.
	 */
	public static int getTo(int move) {
		return (move >>> 6) & 0x3f;
	}
	
	/**
	 * Gets the piece that is moving in the passed move (ie.
	 * <code>Move.PAWN</code>, <code>Move.KNIGHT</code>, etc.).
	 * 
	 * @param move
	 *            the integer representing the move in question
	 * @return the piece which was moved in this move.
	 */
	public static int getPieceType(int move) {
		return (move >>> 12) & 0x7;
	}
	
	/**
	 * Determines if a move was a capture.
	 * 
	 * @param move
	 *            the integer representing the move in question
	 * @return true if the move was a capture, false otherwise.
	 */
	public static boolean isCapture(int move) {
		return ( (move >>> 15) & 0x1) == 1;
	}
	
	/**
	 * Gets any flag (ie <code>Move.NO_FLAG</code>,
	 * <code>Move.FLAG_CASLTE_KINGSIDE</code>, etc.).
	 * 
	 * @param move
	 *            the integer representing the move in question
	 * @return the flag associated with the passed move.
	 */
	public static int getFlag(int move) {
		return (move >>> 16) & 0x7;
	}
	
	/**
	 * <b>Note:</b> This method takes an argument the flag of a move, not the
	 * move itself.
	 * 
	 * @param flag
	 *            the flag of the move
	 * @return true if the move represents a promotion, false otherwise.
	 */
	public static boolean isPromotion(int flag) {
		return flag == FLAG_PROMOTE_KNIGHT || flag == FLAG_PROMOTE_BISHOP
				|| flag == FLAG_PROMOTE_ROOK || flag == FLAG_PROMOTE_QUEEN;
	}
}
