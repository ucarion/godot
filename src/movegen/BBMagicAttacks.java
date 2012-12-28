package movegen;

import godot.Board;
import util.BBUtils;

/**
 * Generates moves with bitboards. Heavily based off of Alberto Ruibal's
 * Carballo program.<br>
 * 
 * Because I created this class after being done with MoveGenerator, there is
 * redundancy between BBMagicAttacks (this class) and MoveGenerator. I typically
 * favor MoveGenerator whenever possible.<br>
 * 
 * The original program by Alberto Ruibal can be found at:<br>
 * 
 * <pre>
 * https://github.com/albertoruibal/carballo/
 * </pre>
 * 
 * @author Alberto A. Ruibal
 * 
 */
public class BBMagicAttacks {
	private static boolean initialized = false;
	
	private final static byte[] rookShifts =
			{ 12, 11, 11, 11, 11, 11, 11, 12, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 12, 11, 11, 11, 11, 11, 11, 12 };
	
	private final static byte[] bishopShifts =
			{ 6, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 7, 7, 7, 5, 5, 5, 5, 7, 9, 9, 7, 5, 5, 5, 5, 7, 9, 9, 7, 5, 5, 5, 5, 7, 7, 7, 7, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 6 };
	
	private final static long[] magicRookNumbers =
			{ 0x1080108000400020L, 0x40200010004000L, 0x100082000441100L, 0x480041000080080L, 0x100080005000210L, 0x100020801000400L, 0x280010000800200L, 0x100008020420100L, 0x400800080400020L, 0x401000402000L, 0x100801000200080L, 0x801000800800L, 0x800400080080L, 0x800200800400L, 0x1000200040100L, 0x4840800041000080L, 0x20008080004000L, 0x404010002000L, 0x808010002000L, 0x828010000800L, 0x808004000800L, 0x14008002000480L, 0x40002100801L, 0x20001004084L, 0x802080004000L, 0x200080400080L, 0x810001080200080L, 0x10008080080010L, 0x4000080080040080L, 0x40080020080L, 0x1000100040200L, 0x80008200004124L, 0x804000800020L, 0x804000802000L, 0x801000802000L, 0x2000801000800804L, 0x80080800400L, 0x80040080800200L, 0x800100800200L, 0x8042000104L, 0x208040008008L, 0x10500020004000L, 0x100020008080L, 0x2000100008008080L, 0x200040008008080L, 0x8020004008080L, 0x1000200010004L, 0x100040080420001L, 0x80004000200040L, 0x200040100140L, 0x20004800100040L, 0x100080080280L, 0x8100800400080080L, 0x8004020080040080L, 0x9001000402000100L, 0x40080410200L, 0x208040110202L, 0x800810022004012L, 0x1000820004011L, 0x1002004100009L, 0x41001002480005L, 0x81000208040001L, 0x4000008201100804L, 0x2841008402L };
	private final static long[] magicBishopNumbers =
			{ 0x1020041000484080L, 0x20204010a0000L, 0x8020420240000L, 0x404040085006400L, 0x804242000000108L, 0x8901008800000L, 0x1010110400080L, 0x402401084004L, 0x1000200810208082L, 0x20802208200L, 0x4200100102082000L, 0x1024081040020L, 0x20210000000L, 0x8210400100L, 0x10110022000L, 0x80090088010820L, 0x8001002480800L, 0x8102082008200L, 0x41001000408100L, 0x88000082004000L, 0x204000200940000L, 0x410201100100L, 0x2000101012000L, 0x40201008200c200L, 0x10100004204200L, 0x2080020010440L, 0x480004002400L, 0x2008008008202L, 0x1010080104000L, 0x1020001004106L, 0x1040200520800L, 0x8410000840101L, 0x1201000200400L, 0x2029000021000L, 0x4002400080840L, 0x5000020080080080L, 0x1080200002200L, 0x4008202028800L, 0x2080210010080L, 0x800809200008200L, 0x1082004001000L, 0x1080202411080L, 0x840048010101L, 0x40004010400200L, 0x500811020800400L, 0x20200040800040L, 0x1008012800830a00L, 0x1041102001040L, 0x11010120200000L, 0x2020222020c00L, 0x400002402080800L, 0x20880000L, 0x1122020400L, 0x11100248084000L, 0x210111000908000L, 0x2048102020080L, 0x1000108208024000L, 0x1004100882000L, 0x41044100L, 0x840400L, 0x4208204L, 0x80000200282020cL, 0x8a001240100L, 0x2040104040080L };
	
	public static long[] rook;
	public static long[] rookMask;
	public static long[][] rookMagic;
	public static long[] bishop;
	public static long[] bishopMask;
	public static long[][] bishopMagic;
	public static long[] knight;
	public static long[] king;
	public static long[] blackPawn;
	public static long[] whitePawn;
	
	static {
		init();
	}
	
	private static long squareAttackedAux(long square, int shift, long border) {
		if ( (square & border) == 0) {
			if (shift > 0)
				square <<= shift;
			else
				square >>>= -shift;
			return square;
		}
		return 0;
	}
	
	private static long squareAttackedAuxSlider(long square, int shift, long border) {
		long ret = 0;
		while ( (square & border) == 0) {
			if (shift > 0)
				square <<= shift;
			else
				square >>>= -shift;
			ret |= square;
		}
		return ret;
	}
	
	private static long squareAttackedAuxSliderMask(long square, int shift, long border) {
		long ret = 0;
		while ( (square & border) == 0) {
			if (shift > 0)
				square <<= shift;
			else
				square >>>= -shift;
			if ( (square & border) == 0)
				ret |= square;
		}
		return ret;
	}
	
	private static void generateAttacks() {
		rook = new long[64];
		rookMask = new long[64];
		rookMagic = new long[64][];
		bishop = new long[64];
		bishopMask = new long[64];
		bishopMagic = new long[64][];
		knight = new long[64];
		king = new long[64];
		blackPawn = new long[64];
		whitePawn = new long[64];
		
		long square = 1;
		byte i = 0;
		while (square != 0) {
			rook[i] = squareAttackedAuxSlider(square, +8, BBUtils.b_up) //
					| squareAttackedAuxSlider(square, -8, BBUtils.b_down) //
					| squareAttackedAuxSlider(square, -1, BBUtils.b_right) //
					| squareAttackedAuxSlider(square, +1, BBUtils.b_left);
			
			rookMask[i] = squareAttackedAuxSliderMask(square, +8, BBUtils.b_up) //
					| squareAttackedAuxSliderMask(square, -8, BBUtils.b_down) //
					| squareAttackedAuxSliderMask(square, -1, BBUtils.b_right) //
					| squareAttackedAuxSliderMask(square, +1, BBUtils.b_left);
			
			bishop[i] =
					squareAttackedAuxSlider(square, +9, BBUtils.b_up | BBUtils.b_left) //
							| squareAttackedAuxSlider(square, +7, BBUtils.b_up
									| BBUtils.b_right) //
							| squareAttackedAuxSlider(square, -7, BBUtils.b_down
									| BBUtils.b_left) //
							| squareAttackedAuxSlider(square, -9, BBUtils.b_down
									| BBUtils.b_right);
			
			bishopMask[i] =
					squareAttackedAuxSliderMask(square, +9, BBUtils.b_up | BBUtils.b_left) //
							| squareAttackedAuxSliderMask(square, +7, BBUtils.b_up
									| BBUtils.b_right) //
							| squareAttackedAuxSliderMask(square, -7, BBUtils.b_down
									| BBUtils.b_left) //
							| squareAttackedAuxSliderMask(square, -9, BBUtils.b_down
									| BBUtils.b_right);
			
			knight[i] = squareAttackedAux(square, +17, BBUtils.b2_up | BBUtils.b_left) //
					| squareAttackedAux(square, +15, BBUtils.b2_up | BBUtils.b_right) //
					| squareAttackedAux(square, -15, BBUtils.b2_down | BBUtils.b_left) //
					| squareAttackedAux(square, -17, BBUtils.b2_down | BBUtils.b_right) //
					| squareAttackedAux(square, +10, BBUtils.b_up | BBUtils.b2_left) //
					| squareAttackedAux(square, +6, BBUtils.b_up | BBUtils.b2_right) //
					| squareAttackedAux(square, -6, BBUtils.b_down | BBUtils.b2_left) //
					| squareAttackedAux(square, -10, BBUtils.b_down | BBUtils.b2_right);
			
			whitePawn[i] = squareAttackedAux(square, 7, BBUtils.b_up | BBUtils.b_right) //
					| squareAttackedAux(square, 9, BBUtils.b_up | BBUtils.b_left);
			
			blackPawn[i] = squareAttackedAux(square, -7, BBUtils.b_down | BBUtils.b_left) //
					| squareAttackedAux(square, -9, BBUtils.b_down | BBUtils.b_right);
			
			king[i] = squareAttackedAux(square, +8, BBUtils.b_up) //
					| squareAttackedAux(square, -8, BBUtils.b_down) //
					| squareAttackedAux(square, -1, BBUtils.b_right) //
					| squareAttackedAux(square, +1, BBUtils.b_left) //
					| squareAttackedAux(square, +9, BBUtils.b_up | BBUtils.b_left) //
					| squareAttackedAux(square, +7, BBUtils.b_up | BBUtils.b_right) //
					| squareAttackedAux(square, -7, BBUtils.b_down | BBUtils.b_left) //
					| squareAttackedAux(square, -9, BBUtils.b_down | BBUtils.b_right);
			
			// And now generate magics
			int rookPositions = (1 << rookShifts[i]);
			rookMagic[i] = new long[rookPositions];
			for (int j = 0; j < rookPositions; j++) {
				long pieces =
						BBMagicAttacks.generatePieces(j, rookShifts[i], rookMask[i]);
				int magicIndex = transform(pieces, magicRookNumbers[i], rookShifts[i]);
				rookMagic[i][magicIndex] = getRookShiftAttacks(square, pieces);
			}
			
			int bishopPositions = (1 << bishopShifts[i]);
			bishopMagic[i] = new long[bishopPositions];
			for (int j = 0; j < bishopPositions; j++) {
				long pieces =
						BBMagicAttacks.generatePieces(j, bishopShifts[i], bishopMask[i]);
				int magicIndex =
						transform(pieces, magicBishopNumbers[i], bishopShifts[i]);
				bishopMagic[i][magicIndex] = getBishopShiftAttacks(square, pieces);
			}
			
			square <<= 1;
			i++;
		}
		initialized = true;
	}
	
	private static void init() {
		if (initialized)
			return;
		generateAttacks();
	}
	
	/**
	 * Determines if a square is being attacked by a given side.
	 * 
	 * @param b
	 *            the board to consider.
	 * @param square
	 *            the target square.
	 * @param white
	 *            true if white is supposedly attacking the square, false
	 *            otherwise.
	 * @return true if the square is being attacked, false otherwise.
	 */
	public static boolean isSquareAttacked(Board b, long square, boolean white) {
		return isIndexAttacked(b, BBUtils.square2Index(square), white);
	}
	
	private static boolean isIndexAttacked(Board b, byte i, boolean white) {
		if (i < 0 || i > 63)
			return false;
		long others = (white ? b.black_pieces : b.white_pieces);
		long all = b.all_pieces;
		
		if ( ( (white ? BBMagicAttacks.whitePawn[i] : BBMagicAttacks.blackPawn[i])
				& (b.white_pawns | b.black_pawns) & others) != 0)
			return true;
		if ( (BBMagicAttacks.king[i] & (b.white_king | b.black_king) & others) != 0)
			return true;
		if ( (BBMagicAttacks.knight[i] & (b.white_knights | b.black_knights) & others) != 0)
			return true;
		if ( (getRookAttacks(i, all)
				& ( (b.white_rooks | b.black_rooks) | (b.white_queens | b.black_queens)) & others) != 0)
			return true;
		if ( (getBishopAttacks(i, all)
				& ( (b.white_bishops | b.black_bishops) | (b.white_queens | b.black_queens)) & others) != 0)
			return true;
		return false;
	}
	
	/**
	 * Returns a long containing all attackers at a square.
	 * 
	 * @param b
	 *            the board to consider
	 * @param i
	 *            the integer in [0, 64) representing the targeted position
	 * @return a bitboard containing the locations of all attackers of a square
	 */
	public static long getIndexAttacks(Board b, int i) {
		if (i < 0 || i > 63)
			return 0;
		long all = b.all_pieces;
		
		return ( (b.black_pieces & BBMagicAttacks.whitePawn[i] | b.white_pieces
				& BBMagicAttacks.blackPawn[i]) & (b.white_pawns | b.black_pawns))
				| (BBMagicAttacks.king[i] & (b.white_king | b.black_king))
				| (BBMagicAttacks.knight[i] & (b.white_knights | b.black_knights))
				| (getRookAttacks(i, all) & ( (b.white_rooks | b.black_rooks) | (b.white_queens | b.black_queens)))
				| (getBishopAttacks(i, all) & ( (b.white_bishops | b.black_bishops) | (b.white_queens | b.black_queens)));
	}
	
	/**
	 * Finds all attackers that attack a square through another square. Calling
	 * this method is exactly the same as calling
	 * 
	 * <code>getXrayAttacks(b, i, b.all_pieces)</code>.
	 * 
	 * @param b
	 *            the board representing the position to consider
	 * @param i
	 *            the target location.
	 * @return
	 */
	public static long getXrayAttacks(Board b, int i) {
		return getXrayAttacks(b, i, b.all_pieces);
	}
	
	/**
	 * Finds all attackers that attack a square through another square.
	 * 
	 * @param b
	 *            the board to consider
	 * @param i
	 *            the target location
	 * @param all
	 *            the bitboard representing the places through which we can
	 *            move.
	 * @return
	 */
	public static long getXrayAttacks(Board b, int i, long all) {
		if (i < 0 || i > 63)
			return 0;
		
		return ( (getRookAttacks(i, all) & ( (b.white_rooks | b.black_rooks) | (b.white_queens | b.black_queens))) | (getBishopAttacks(
				i, all) & ( (b.white_bishops | b.black_bishops) | (b.white_queens | b.black_queens))))
				& all;
	}
	
	/**
	 * Gets a bitboard containing all squares a rook at a location can move to.
	 * 
	 * @param index
	 *            the location of the rook.
	 * @param all
	 *            the locations of all pieces on the board.
	 * @return the locations a rook can move to.
	 */
	public static long getRookAttacks(int index, long all) {
		int i =
				transform(all & rookMask[index], magicRookNumbers[index],
						rookShifts[index]);
		return rookMagic[index][i];
	}
	
	/**
	 * Gets a bitboard containing all squares a bishop at a location can move
	 * to.
	 * 
	 * @param index
	 *            the location of the bishop.
	 * @param all
	 *            the locations of all pieces on the board.
	 * @return the locations a bishop can move to.
	 */
	public static long getBishopAttacks(int index, long all) {
		int i =
				transform(all & bishopMask[index], magicBishopNumbers[index],
						bishopShifts[index]);
		return bishopMagic[index][i];
	}
	
	/**
	 * Gets a bitboard containing all squares a queen at a location can move to.
	 * 
	 * @param index
	 *            the location of the queen.
	 * @param all
	 *            the locations of all pieces on the board.
	 * @return the locations a queen can move to.
	 */
	public static long getQueenAttacks(int index, long all) {
		return getRookAttacks(index, all) | getBishopAttacks(index, all);
	}
	
	private static int transform(long b, long magic, byte bits) {
		return (int) ( (b * magic) >>> (64 - bits));
	}
	
	private static long generatePieces(int index, int bits, long mask) {
		int i;
		long lsb;
		long result = 0L;
		for (i = 0; i < bits; i++) {
			lsb = Long.lowestOneBit(mask);
			mask ^= lsb;
			if ( (index & (1 << i)) != 0)
				result |= lsb;
		}
		return result;
	}
	
	private static long getRookShiftAttacks(long square, long all) {
		return squareAttackedAux(square, all, +8, BBUtils.b_up)
				| squareAttackedAux(square, all, -8, BBUtils.b_down)
				| squareAttackedAux(square, all, -1, BBUtils.b_right)
				| squareAttackedAux(square, all, +1, BBUtils.b_left);
	}
	
	private static long getBishopShiftAttacks(long square, long all) {
		return squareAttackedAux(square, all, +9, BBUtils.b_up | BBUtils.b_left)
				| squareAttackedAux(square, all, +7, BBUtils.b_up | BBUtils.b_right)
				| squareAttackedAux(square, all, -7, BBUtils.b_down | BBUtils.b_left)
				| squareAttackedAux(square, all, -9, BBUtils.b_down | BBUtils.b_right);
	}
	
	private static long squareAttackedAux(long square, long all, int shift, long border) {
		long ret = 0;
		while ( (square & border) == 0) {
			if (shift > 0)
				square <<= shift;
			else
				square >>>= -shift;
			ret |= square;
			
			if ( (square & all) != 0)
				break;
		}
		return ret;
	}
}