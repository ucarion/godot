package util;

import movegen.MoveGenerator;
import godot.Board;
import godot.Move;

/**
 * A bunch of useful methods and arrays for working with Boards and bitboards.
 * 
 * @author Ulysse
 */
public class BBUtils {
	public static final String START_FEN =
			"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	public static final int RANK_1 = 0;
	public static final int RANK_2 = 1;
	public static final int RANK_3 = 2;
	public static final int RANK_4 = 3;
	public static final int RANK_5 = 4;
	public static final int RANK_6 = 5;
	public static final int RANK_7 = 6;
	public static final int RANK_8 = 7;
	
	public static final int FILE_A = 0;
	public static final int FILE_B = 1;
	public static final int FILE_C = 2;
	public static final int FILE_D = 3;
	public static final int FILE_E = 4;
	public static final int FILE_F = 5;
	public static final int FILE_G = 6;
	public static final int FILE_H = 7;
	
	/**
	 * <code>maskRank[i]</code> has 1s at the <code>i</code>-th rank and 0s
	 * everywhere else.
	 */
	public static final long[] maskRank;
	
	/**
	 * <code>clearRank[i]</code> has 0s at the <code>i</code>-th rank and 1s
	 * everywhere else.
	 */
	public static final long[] clearRank;
	
	/**
	 * <code>maskFile[i]</code> has 1s at the <code>i</code>-th file and 0s
	 * everywhere else.
	 */
	public static final long[] maskFile;
	
	/**
	 * <code>clearRank[i]</code> has 0s at the <code>i</code>-th file and 1s
	 * everywhere else.
	 */
	public static final long[] clearFile;
	
	/**
	 * To get the square at A1, give 0. To get the square at A2, give 1. To get
	 * the square at H8, give 63.
	 */
	public static final long[] getSquare;
	
	// Single-bit border at the edges. Technically, these are equivalent to
	// their respective maskFile or maskRank counterparts.
	public static final long b_down = 0x00000000000000ffL;
	public static final long b_up = 0xff00000000000000L;
	public static final long b_right = 0x0101010101010101L;
	public static final long b_left = 0x8080808080808080L;
	
	// Thicker border for the knight generation in BBMagic
	public static final long b2_down = 0x000000000000ffffL;
	public static final long b2_up = 0xffff000000000000L;
	public static final long b2_right = 0x0303030303030303L;
	public static final long b2_left = 0xC0C0C0C0C0C0C0C0L;
	
	// Even thicker, only bottom
	public static final long b3_down = 0x0000000000ffffffL;
	public static final long b3_up = 0xffffff0000000000L;
	
	// Second-from top and second-from bottom ranks
	public static final long r2_down = 0x000000000000ff00L;
	public static final long r2_up = 0x00ff000000000000L;
	
	static {
		getSquare = new long[64];
		maskRank = new long[8];
		maskFile = new long[8];
		clearRank = new long[8];
		clearFile = new long[8];
		
		for (int i = 0; i < 64; i++) {
			getSquare[i] = 1L << i;
			
			maskRank[getLocRow(i)] |= getSquare[i];
			maskFile[getLocCol(i)] |= getSquare[i];
		}
		
		for (int i = 0; i < 8; i++) {
			clearRank[i] = ~maskRank[i];
			clearFile[i] = ~maskFile[i];
		}
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 8; i++) {
			printBitboard(clearRank[i]);
		}
	}
	
	/**
	 * Gets the index location (for use in <code>Move</code> class or other
	 * lists in
	 * this class) corresponding to the single set bit of a bitboard.
	 * 
	 * @param bitboard
	 *            the bitboard with a set bit.
	 * @return the index location if it exists. The number is meaningless if
	 *         there are multiple set bits.
	 */
	public static int getLocFromBitboard(long bitboard) {
		return Long.numberOfTrailingZeros(bitboard);
	}
	
	/**
	 * Gets the column, or file, of the location provided.
	 * 
	 * @param loc
	 *            the location, a number in [0, 64)
	 * @return the column (aka file) of the location.
	 */
	public static int getLocCol(int loc) {
		return loc % 8;
	}
	
	/**
	 * Gets the row, or rank, of the location provided.
	 * 
	 * @param loc
	 *            the location, a number in [0, 64)
	 * @return the row (aka rank) of the location.
	 */
	public static int getLocRow(int loc) {
		return loc / 8;
	}
	
	/**
	 * Gets the bitboard highlighting a single square. Calling this method is
	 * exactly the same as calling
	 * 
	 * <pre>
	 * getSquare[rank * 8 + file]
	 * </pre>
	 * 
	 * Which is prehaps less legible.
	 * 
	 * @param rank
	 *            the rank of the location
	 * @param file
	 *            the file of the location
	 * @return a bitboard with a bit set at the rank and file provided.
	 */
	public static long getSquare(int rank, int file) {
		return getSquare[rank * 8 + file];
	}
	
	/**
	 * Gets the lsb (least significant bit) of the board. Though I could use
	 * Long.lowestOneBit all the time, pragmatically it's a long method name for
	 * a simple process, so I'm using this method instead.
	 * 
	 * @param board
	 *            a bitboard
	 * @return a long with only one bit set, at the location of the rightmost
	 *         bit set in the passed bitboard. If the passed bitboard has no
	 *         bits set, this returns 0.
	 */
	public static long lsb(long board) {
		return Long.lowestOneBit(board);
	}
	
	// This array and the subsequent method are for BitboardAttacks ...
	
	public static final byte[] bitTable =
			{ 63, 30, 3, 32, 25, 41, 22, 33, 15, 50, 42, 13, 11, 53, 19, 34, 61, 29, 2, 51, 21, 43, 45, 10, 18, 47, 1, 54, 9, 57, 0, 35, 62, 31, 40, 4, 49, 5, 52, 26, 60, 6, 23, 44, 46, 27, 56, 16, 7, 39, 48, 24, 59, 14, 12, 55, 38, 28, 58, 20, 37, 17, 36, 8 };
	
	public static byte square2Index(long square) {
		long b = square ^ (square - 1);
		int fold = (int) (b ^ (b >>> 32));
		return bitTable[ (fold * 0x783a9b23) >>> 26];
	}
	
	// ... and we're back.
	
	/**
	 * Converts a <i>bitboard</i>, not a board, to a string. The string will
	 * look identical to the result of Board.toString, except that pieces are
	 * instead shown as 'X'.
	 * 
	 * @param bitboard
	 *            the bitboard to represent as a string.
	 * @return a string representation of the bitboard.
	 */
	public static String bitboardToString(long bitboard) {
		String asBinary = Long.toBinaryString(bitboard);
		
		while (asBinary.length() != 64)
			asBinary = "0" + asBinary;
		
		String s = "     a   b   c   d   e   f   g   h\n";
		s += "   +---+---+---+---+---+---+---+---+\n 8 | ";
		
		for (int up = 7; up >= 0; up--) {
			for (int out = 0; out < 8; out++) {
				if (asBinary.charAt(63 - (up * 8 + out)) == '1')
					s += "X | ";
				else
					s += "  | ";
			}
			s += (up + 1) + "\n   +---+---+---+---+---+---+---+---+";
			if (up != 0)
				s += "\n " + up + " | ";
		}
		
		s += "\n     a   b   c   d   e   f   g   h\n";
		return s;
	}
	
	/**
	 * Prints a bitboard by sending the result of <code>bitboardToString</code>
	 * to <code>System.out</code>.
	 * 
	 * @param bitboard
	 *            the bitboard to print.
	 */
	public static void printBitboard(long bitboard) {
		System.out.println(bitboardToString(bitboard));
	}
	
	/**
	 * Outputs all bitboards of a board.
	 * 
	 * @param b
	 *            the board to print out
	 */
	public static void printAllBitboards(Board b) {
		System.out.println("p:\n" + bitboardToString(b.black_pawns));
		System.out.println("P:\n" + bitboardToString(b.white_pawns));
		System.out.println("n:\n" + bitboardToString(b.black_knights));
		System.out.println("N:\n" + bitboardToString(b.white_knights));
		System.out.println("b:\n" + bitboardToString(b.black_bishops));
		System.out.println("B:\n" + bitboardToString(b.white_bishops));
		System.out.println("r:\n" + bitboardToString(b.black_rooks));
		System.out.println("R:\n" + bitboardToString(b.white_rooks));
		System.out.println("q:\n" + bitboardToString(b.black_queens));
		System.out.println("Q:\n" + bitboardToString(b.white_queens));
		System.out.println("k:\n" + bitboardToString(b.black_king));
		System.out.println("K:\n" + bitboardToString(b.white_king));
		System.out.println("Black:\n" + bitboardToString(b.black_pieces));
		System.out.println("White:\n" + bitboardToString(b.white_pieces));
		System.out.println("All:\n" + bitboardToString(b.all_pieces));
	}
	
	/**
	 * Converts a location in "algebraic notation" (ie. of the form 'a7', 'c5',
	 * etc.) into its integer representation.
	 * 
	 * <b>Note:</b> this method may throw a <code>NumberFormatException</code>
	 * if the passed string is malformed-- no error checking occurs in this
	 * method.
	 * 
	 * @param loc
	 *            a string representing a location
	 * @return the integer representation of the string
	 */
	public static int algebraicLocToInt(String loc) {
		if (loc.equals("-"))
			return -1;
		int out = loc.charAt(0) - 'a';
		int up = Integer.parseInt(loc.charAt(1) + "") - 1;
		return up * 8 + out;
	}
	
	/**
	 * Converts an integer location in [0, 64) to a string in
	 * "algebraic notation" (ie. of the form 'a7', 'c5).
	 * 
	 * @param loc
	 *            an int in [0, 64) representing a location
	 * @return the "algebraic notation" of the location
	 */
	public static String intToAlgebraicLoc(int loc) {
		if (loc == -1)
			return "-";
		int out = loc % 8;
		int up = loc / 8;
		char outc = (char) (out + 'a');
		char upc = (char) (up + '1');
		return outc + "" + upc;
	}
	
	/**
	 * Converts an integer location in [0, 64) to a string in ['a', 'h']
	 * representing its column (aka file).
	 * 
	 * @param loc
	 *            an int in [0, 64) representing a location.
	 * @return the string representing the file of the location.
	 */
	public static String intColToString(int loc) {
		return (char) ( ( (loc % 8) + 'a')) + "";
	}
	
	/**
	 * Converts an integer location in [0, 64) to a string in ['a', 'h']
	 * representing its rank (aka row).
	 * 
	 * @param loc
	 *            an int in [0, 64) representing a location.
	 * @return the string representing the rank of the location.
	 */
	public static String intRowToString(int loc) {
		return (char) ( ( (loc / 8) + '1')) + "";
	}
	
	/**
	 * Prints a move by sending the result of <code>moveToString</code> to
	 * <code>System.out</code>.
	 * 
	 * @param move
	 *            the move to print
	 */
	public static void printMove(int move) {
		System.out.println(moveToString(move));
	}
	
	/**
	 * Returns the string associated with the piece that is moving in the passed
	 * integer representing a move.
	 * 
	 * The piece moving is found using <code>Move.getPieceType</code>, and the
	 * strings being returned are:
	 * 
	 * <pre>
	 * ""  if the piece is a pawn.
	 * "N" if the piece is a knight.
	 * "B" if the piece is a bishop.
	 * "R" if the piece is a rook.
	 * "Q" if the piece is a queen.
	 * "K" if the piece is a king.
	 * </pre>
	 * 
	 * @param move
	 * @return
	 */
	public static String moveToPieceString(int move) {
		switch (Move.getPieceType(move)) {
			case Move.KNIGHT:
				return "N";
			case Move.BISHOP:
				return "B";
			case Move.ROOK:
				return "R";
			case Move.QUEEN:
				return "Q";
			case Move.KING:
				return "K";
		}
		return "";
	}
	
	/**
	 * Converts a move into a string of the form:
	 * 
	 * <pre>
	 * PIECE FROM (x / -) TO
	 * </pre>
	 * 
	 * If the move is a capture, then this move will have an 'x' between the
	 * "from" and "to", but it will have a "-" otherwise.
	 * 
	 * 
	 * If the move represents kingside or queenside castling, then this method
	 * will instead return "0-0" or "0-0-0", respectively.
	 * 
	 * @param move
	 * @return
	 */
	public static String moveToString(int move) {
		String s = "";
		
		if (Move.getFlag(move) == Move.FLAG_CASTLE_KINGSIDE)
			return "0-0";
		if (Move.getFlag(move) == Move.FLAG_CASTLE_QUEENSIDE)
			return "0-0-0";
		
		switch (Move.getPieceType(move)) {
			case Move.KNIGHT:
				s += "N";
				break;
			case Move.BISHOP:
				s += "B";
				break;
			case Move.ROOK:
				s += "R";
				break;
			case Move.QUEEN:
				s += "Q";
				break;
			case Move.KING:
				s += "K";
				break;
		}
		
		s += intToAlgebraicLoc(Move.getFrom(move));
		if (Move.isCapture(move))
			s += "x";
		else
			s += "-";
		s += intToAlgebraicLoc(Move.getTo(move));
		switch (Move.getFlag(move)) {
			case Move.FLAG_EN_PASSANT:
				s += " e.p.";
				break;
			case Move.FLAG_PROMOTE_BISHOP:
				s += "=B";
				break;
			case Move.FLAG_PROMOTE_KNIGHT:
				s += "=N";
				break;
			case Move.FLAG_PROMOTE_ROOK:
				s += "=R";
				break;
			case Move.FLAG_PROMOTE_QUEEN:
				s += "=Q";
				break;
		}
		
		return s;
	}
	
	/**
	 * Prints all legal moves in a position. This method works based on
	 * <code>MoveGenerator.getAllLegalMove</code> and
	 * <code>BBUtils.moveToString</code>.
	 * 
	 * @param b
	 *            a board
	 */
	public static void printLegalMoves(Board b) {
		int[] moves = new int[Board.MAX_MOVES];
		int num_moves = MoveGenerator.getAllLegalMoves(b, moves);
		for (int i = 0; i < num_moves; i++) {
			System.out.print(moves[i] + " ==> ");
			printMove(moves[i]);
		}
	}
}
