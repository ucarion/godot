package godot;

import java.util.ArrayList;
import java.util.StringTokenizer;

import movegen.BBMagicAttacks;
import movegen.MoveGenerator;
import util.BBUtils;
import zobrist.Zobrist;

/**
 * The class that represents a board. Hooray. Instances of this class will take
 * care of the rules of chess.
 * 
 * @author Ulysse
 * 
 */
public class Board {
	/**
	 * The longest a game can last.
	 */
	public static final int MAX_GAME_LENGTH = 1024;
	
	/**
	 * The most possible moves there can be in a position.
	 */
	public static final int MAX_MOVES = 1024;
	
	private static final int[] SEE_PIECE_VALUES = { 100, 325, 325, 500, 975, 999999 };
	
	// I here violate encapsulation in exchange for simpler code.
	
	public long white_pawns;
	public long white_knights;
	public long white_bishops;
	public long white_rooks;
	public long white_queens;
	public long white_king;
	
	public long black_pawns;
	public long black_knights;
	public long black_bishops;
	public long black_rooks;
	public long black_queens;
	public long black_king;
	
	public long white_pieces;
	public long black_pieces;
	public long all_pieces;
	
	public boolean white_to_move;
	public int fiftyMoveRule;
	public int enPassantLoc;
	public int moveNumber;
	
	public boolean white_castle_k;
	public boolean white_castle_q;
	public boolean black_castle_k;
	public boolean black_castle_q;
	public boolean white_has_castled;
	public boolean black_has_castled;
	
	public long key; // zobrist key
	
	public int initMoveNumber; // only updated when initializing board.
	
	public long[] white_pawn_history;
	public long[] white_knight_history;
	public long[] white_bishop_history;
	public long[] white_rook_history;
	public long[] white_queen_history;
	public long[] white_king_history;
	public long[] black_pawn_history;
	public long[] black_knight_history;
	public long[] black_bishop_history;
	public long[] black_rook_history;
	public long[] black_queen_history;
	public long[] black_king_history;
	public long[] white_pieces_history;
	public long[] black_pieces_history;
	public long[] all_pieces_history;
	public boolean[] white_to_move_history;
	public int[] fiftyMoveRule_history;
	public int[] enPassantLoc_history;
	public int[] move_history;
	public char[][] pieceArray_history;
	public boolean[] white_castle_k_history;
	public boolean[] white_castle_q_history;
	public boolean[] black_castle_k_history;
	public boolean[] black_castle_q_history;
	public boolean[] white_has_castled_history;
	public boolean[] black_has_castled_history;
	public long[] key_history;
	
	/**
	 * Creates a new Board, but with no pieces on the board.
	 */
	public Board() {
		white_pawn_history = new long[MAX_GAME_LENGTH];
		white_knight_history = new long[MAX_GAME_LENGTH];
		white_bishop_history = new long[MAX_GAME_LENGTH];
		white_rook_history = new long[MAX_GAME_LENGTH];
		white_queen_history = new long[MAX_GAME_LENGTH];
		white_king_history = new long[MAX_GAME_LENGTH];
		black_pawn_history = new long[MAX_GAME_LENGTH];
		black_knight_history = new long[MAX_GAME_LENGTH];
		black_bishop_history = new long[MAX_GAME_LENGTH];
		black_rook_history = new long[MAX_GAME_LENGTH];
		black_queen_history = new long[MAX_GAME_LENGTH];
		black_king_history = new long[MAX_GAME_LENGTH];
		white_pieces_history = new long[MAX_GAME_LENGTH];
		black_pieces_history = new long[MAX_GAME_LENGTH];
		all_pieces_history = new long[MAX_GAME_LENGTH];
		white_to_move_history = new boolean[MAX_GAME_LENGTH];
		fiftyMoveRule_history = new int[MAX_GAME_LENGTH];
		enPassantLoc_history = new int[MAX_GAME_LENGTH];
		move_history = new int[MAX_GAME_LENGTH];
		pieceArray_history = new char[MAX_GAME_LENGTH][64];
		white_castle_k_history = new boolean[MAX_GAME_LENGTH];
		white_castle_q_history = new boolean[MAX_GAME_LENGTH];
		black_castle_k_history = new boolean[MAX_GAME_LENGTH];
		black_castle_q_history = new boolean[MAX_GAME_LENGTH];
		white_has_castled_history = new boolean[MAX_GAME_LENGTH];
		black_has_castled_history = new boolean[MAX_GAME_LENGTH];
		key_history = new long[MAX_GAME_LENGTH];
	}
	
	/**
	 * Creates a new board based on the FEN passed.
	 * 
	 * @param fen
	 *            the FEN to read from.
	 */
	public Board(String fen) {
		this();
		readFromFEN(fen);
	}
	
	private void updateSpecialBitboards() {
		white_pieces =
				white_pawns | white_knights | white_bishops | white_rooks | white_queens
						| white_king;
		black_pieces =
				black_pawns | black_knights | black_bishops | black_rooks | black_queens
						| black_king;
		
		all_pieces = white_pieces | black_pieces;
	}
	
	/**
	 * Sets up a position based on a FEN (Forsyth–Edwards Notation) string.
	 * 
	 * @param fen
	 *            the FEN to read from
	 */
	public void readFromFEN(String fen) {
		StringTokenizer st = new StringTokenizer(fen, "/ ");
		ArrayList<String> arr = new ArrayList<String>();
		
		while (st.hasMoreTokens()) {
			arr.add(st.nextToken());
		}
		
		// traversing the square-description part of the FEN
		int up = 7;
		int out = 0;
		for (int i = 0; i < 8; i++) {
			out = 0;
			for (char c : arr.get(i).toCharArray()) {
				if (Character.isDigit(c)) {
					for (int j = 0; j < Character.digit(c, 10); j++) {
						out++;
					}
				}
				else {
					long square = BBUtils.getSquare[up * 8 + out];
					
					switch (c) {
						case 'p':
							black_pawns |= square;
							break;
						case 'P':
							white_pawns |= square;
							break;
						case 'n':
							black_knights |= square;
							break;
						case 'N':
							white_knights |= square;
							break;
						case 'b':
							black_bishops |= square;
							break;
						case 'B':
							white_bishops |= square;
							break;
						case 'r':
							black_rooks |= square;
							break;
						case 'R':
							white_rooks |= square;
							break;
						case 'q':
							black_queens |= square;
							break;
						case 'Q':
							white_queens |= square;
							break;
						case 'k':
							black_king |= square;
							break;
						case 'K':
							white_king |= square;
							break;
					}
					out++;
				}
			}
			up--;
		}
		
		updateSpecialBitboards();
		
		// and now we deal with turn, ep, 50mr, and move number
		
		// turn
		white_to_move = arr.get(8).equals("w");
		
		// castling
		white_castle_k = arr.get(9).contains("K");
		white_castle_q = arr.get(9).contains("Q");
		black_castle_k = arr.get(9).contains("k");
		black_castle_q = arr.get(9).contains("q");
		
		white_has_castled = false;
		black_has_castled = false;
		
		// en passant
		enPassantLoc = BBUtils.algebraicLocToInt(arr.get(10));
		
		// 50mr
		fiftyMoveRule = Integer.parseInt(arr.get(11));
		
		// move number
		moveNumber = Integer.parseInt(arr.get(12));
		initMoveNumber = moveNumber;
		
		key = Zobrist.getKeyForBoard(this);
	}
	
	/**
	 * Converts this board into a human-readable form.
	 */
	public String toString() {
		String s = "     a   b   c   d   e   f   g   h\n";
		s += "   +---+---+---+---+---+---+---+---+\n 8 | ";
		
		for (int up = 7; up >= 0; up--) {
			for (int out = 0; out < 8; out++) {
				s += getPieceAt(up * 8 + out) + " | ";
			}
			s += (up + 1) + "\n   +---+---+---+---+---+---+---+---+";
			if (up != 0)
				s += "\n " + up + " | ";
		}
		
		s += "\n     a   b   c   d   e   f   g   h\n\n";
		
		s += "White to move: " + white_to_move + "\n";
		s += "White: O-O: " + white_castle_k + " -- O-O-O: " + white_castle_q + "\n";
		s += "Black: O-O: " + black_castle_k + " -- O-O-O: " + black_castle_q + "\n";
		s +=
				"En Passant: " + enPassantLoc + " ("
						+ BBUtils.intToAlgebraicLoc(enPassantLoc) + ")\n";
		s += "50 move rule: " + fiftyMoveRule + "\n";
		s += "Move number: " + moveNumber + "\n";
		return s;
	}
	
	/**
	 * Gets what is found at a particular location.
	 * 
	 * @param loc
	 *            an integer in [0, 64) representing a position.
	 * @return a character representing the piece at the passed location.
	 */
	public char getPieceAt(int loc) {
		long sq = BBUtils.getSquare[loc];
		if ( (white_pawns & sq) != 0L)
			return 'P';
		if ( (white_knights & sq) != 0L)
			return 'N';
		if ( (white_bishops & sq) != 0L)
			return 'B';
		if ( (white_rooks & sq) != 0L)
			return 'R';
		if ( (white_queens & sq) != 0L)
			return 'Q';
		if ( (white_king & sq) != 0L)
			return 'K';
		
		if ( (black_pawns & sq) != 0L)
			return 'p';
		if ( (black_knights & sq) != 0L)
			return 'n';
		if ( (black_bishops & sq) != 0L)
			return 'b';
		if ( (black_rooks & sq) != 0L)
			return 'r';
		if ( (black_queens & sq) != 0L)
			return 'q';
		if ( (black_king & sq) != 0L)
			return 'k';
		
		return ' ';
	}
	
	/**
	 * Makes a move, assuming it is a pseudo-legal move-- it is a completely
	 * legal move aside from the possibility of moving into or staying in check.
	 * 
	 * If the passed pseudo-legal move is illegal, the board will automatically
	 * revert itself to its previous state-- asking the board to make illegal
	 * moves will not do anything.
	 * 
	 * @param move
	 *            the move we're making
	 * @return true if the move was completed, false if it wasn't
	 */
	public boolean makeMove(int move) {
		/*
		 * General plan of attack is as follows:
		 * 
		 * (0) First of all, check if you're moving your own piece.
		 * (1) Account for captures by clearing that square from all arrays,
		 * setting 50MR to 0.
		 * (2) Make the move, but do so depending on the piece moving:
		 * (2a) Pawns -- account for ep, promos, and 50MR
		 * (2b) Kings -- account for castling
		 * (2c) Rooks -- account for lost castling rights
		 * (2d) Other -- business as usual
		 * (3) Check if king is in check, if he is, undo move
		 */
		
		saveHistory(moveNumber);
		
		int from_loc = Move.getFrom(move);
		int to_loc = Move.getTo(move);
		long from = BBUtils.getSquare[from_loc];
		long to = BBUtils.getSquare[to_loc];
		int move_flag = Move.getFlag(move);
		int piece_moving = Move.getPieceType(move);
		boolean capture = Move.isCapture(move);
		
		fiftyMoveRule++;
		moveNumber++;
		
		/*
		 * The great thing about this bitboard is that if, say, you're moving a
		 * white rook and XOR this to white_rooks, the following will happen
		 * (we're moving along a rank; "f" is from, "t" is to):
		 * 
		 * . . . . . . . . f . . . . t
		 * white_rooks = 0 1 0 0 0 0 0;
		 * from | to . = 0 1 0 0 0 0 1;
		 * w_r XOR f|t = 0 0 0 0 0 0 1;
		 * 
		 * Tadaa! You just moved the rook from "f" to "t".
		 */
		long moveMask = to | from;
		
		if (white_to_move) {
			if ( (from & white_pieces) == 0L)
				return false;
		}
		else {
			if ( (from & black_pieces) == 0L)
				return false;
		}
		
		if (capture) {
			fiftyMoveRule = 0;
			
			long pieceToRemove = to;
			int pieceToRemove_loc = to_loc;
			
			if (move_flag == Move.FLAG_EN_PASSANT) {
				pieceToRemove = (white_to_move) ? (to >>> 8) : (to << 8);
				pieceToRemove_loc = (white_to_move) ? (to_loc - 8) : (to_loc + 8);
			}
			
			char pieceRemoved = getPieceAt(pieceToRemove_loc);
			
			if (white_to_move) { // captured a black
				black_pawns &= ~pieceToRemove;
				black_knights &= ~pieceToRemove;
				black_bishops &= ~pieceToRemove;
				black_rooks &= ~pieceToRemove;
				black_queens &= ~pieceToRemove;
				black_king &= ~pieceToRemove;
			}
			else { // captured a white
				white_pawns &= ~pieceToRemove;
				white_knights &= ~pieceToRemove;
				white_bishops &= ~pieceToRemove;
				white_rooks &= ~pieceToRemove;
				white_queens &= ~pieceToRemove;
				white_king &= ~pieceToRemove;
			}
			
			key ^= Zobrist.getKeyForSquare(pieceToRemove_loc, pieceRemoved);
		}
		
		// remove ep from Zobrist, but only if's there already (otherwise the
		// XOR would change things -- if it's there it's simply removed, though)
		if (enPassantLoc != -1)
			key ^= Zobrist.passantColumn[BBUtils.getLocCol(enPassantLoc)];
		
		// It may be re-initialized later, but from here on out the last e.p.
		// doesn't matter.
		enPassantLoc = -1;
		
		switch (piece_moving) {
			case Move.PAWN:
				fiftyMoveRule = 0;
				// check to see if we need to update ep square
				if (white_to_move && (from << 16 & to) != 0L)
					enPassantLoc = BBUtils.getLocFromBitboard(from << 8);
				if ( !white_to_move && (from >>> 16 & to) != 0L)
					enPassantLoc = BBUtils.getLocFromBitboard(from >>> 8);
				
				if (enPassantLoc != -1)
					key ^= Zobrist.passantColumn[BBUtils.getLocCol(enPassantLoc)];
				
				// handle promotions
				if (Move.isPromotion(move_flag)) {
					if (white_to_move) {
						white_pawns &= ~from;
						key ^= Zobrist.getKeyForSquare(from_loc, 'P');
					}
					else {
						black_pawns &= ~from;
						key ^= Zobrist.getKeyForSquare(from_loc, 'p');
					}
					
					switch (move_flag) {
						case Move.FLAG_PROMOTE_QUEEN:
							if (white_to_move) {
								white_queens |= to;
								key ^= Zobrist.getKeyForSquare(to_loc, 'Q');
							}
							else {
								black_queens |= to;
								key ^= Zobrist.getKeyForSquare(to_loc, 'q');
							}
							break;
						case Move.FLAG_PROMOTE_KNIGHT:
							if (white_to_move) {
								white_knights |= to;
								key ^= Zobrist.getKeyForSquare(to_loc, 'N');
							}
							else {
								black_knights |= to;
								key ^= Zobrist.getKeyForSquare(to_loc, 'n');
							}
							break;
						case Move.FLAG_PROMOTE_ROOK:
							if (white_to_move) {
								white_rooks |= to;
								key ^= Zobrist.getKeyForSquare(to_loc, 'R');
							}
							else {
								black_rooks |= to;
								key ^= Zobrist.getKeyForSquare(to_loc, 'r');
							}
							break;
						case Move.FLAG_PROMOTE_BISHOP:
							if (white_to_move) {
								white_bishops |= to;
								key ^= Zobrist.getKeyForSquare(to_loc, 'B');
							}
							else {
								black_bishops |= to;
								key ^= Zobrist.getKeyForSquare(to_loc, 'b');
							}
							break;
					}
				}
				else { // not a promotion
					if (white_to_move) {
						white_pawns ^= moveMask;
						key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'P');
					}
					else {
						black_pawns ^= moveMask;
						key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'p');
					}
				}
				break;
			case Move.KNIGHT:
				if (white_to_move) {
					white_knights ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'N');
				}
				else {
					black_knights ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'n');
				}
				break;
			case Move.BISHOP:
				if (white_to_move) {
					white_bishops ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'B');
				}
				else {
					black_bishops ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'b');
				}
				break;
			case Move.ROOK:
				if (white_to_move) {
					white_rooks ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'R');
				}
				else {
					black_rooks ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'r');
				}
				break;
			case Move.QUEEN:
				if (white_to_move) {
					white_queens ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'Q');
				}
				else {
					black_queens ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'q');
				}
				break;
			case Move.KING: // we must handle potential castling
				// rookmask will represent how the rook will move, if at all
				long rookmask = 0L;
				int rook_to_loc = 0;
				int rook_from_loc = 0;
				if (move_flag == Move.FLAG_CASTLE_KINGSIDE) {
					if (white_to_move) {
						white_has_castled = true;
						rookmask = 0xa0L;
						rook_to_loc = 5;
						rook_from_loc = 7;
					}
					else {
						black_has_castled = true;
						rookmask = 0xa000000000000000L;
						rook_to_loc = 5 + 56;
						rook_from_loc = 7 + 56;
					}
				}
				if (move_flag == Move.FLAG_CASTLE_QUEENSIDE) {
					if (white_to_move) {
						white_has_castled = true;
						rookmask = 0x9L;
						rook_to_loc = 3;
						rook_from_loc = 0;
					}
					else {
						black_has_castled = true;
						rookmask = 0x900000000000000L;
						rook_to_loc = 3 + 56;
						rook_from_loc = 0 + 56;
					}
				}
				if (rookmask != 0L) {
					if (white_to_move) {
						white_rooks ^= rookmask;
						key ^= Zobrist.getKeyForMove(rook_from_loc, rook_to_loc, 'R');
					}
					else {
						black_rooks ^= rookmask;
						key ^= Zobrist.getKeyForMove(rook_from_loc, rook_to_loc, 'r');
					}
				}
				
				if (white_to_move) {
					white_king ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'K');
				}
				else {
					black_king ^= moveMask;
					key ^= Zobrist.getKeyForMove(from_loc, to_loc, 'k');
				}
		}
		
		updateSpecialBitboards();
		
		// and now we must update castling rights
		if (white_to_move) {
			if ( (moveMask & 0x90L) != 0) { // 0x90 is e1 | h1 -- the king or
											// king's rook has moved
				white_castle_k = false;
				key ^= Zobrist.whiteKingSideCastling;
			}
			if ( (moveMask & 0x11L) != 0) { // 0x11 is e1 | a1 -- the king or
											// queen's rook has moved
				white_castle_q = false;
				key ^= Zobrist.whiteQueenSideCastling;
			}
		}
		else {
			if ( (moveMask & 0x9000000000000000L) != 0) { // 0x90... is 0x90
															// <<'d to
															// black's side
				black_castle_k = false;
				key ^= Zobrist.blackKingSideCastling;
			}
			if ( (moveMask & 0x1100000000000000L) != 0) { // 0x11... is 0x11
															// <<'d to
															// black's side
				black_castle_q = false;
				key ^= Zobrist.blackQueenSideCastling;
			}
		}
		
		if (ownKingIsInCheck()) {
			undoMove();
			return false;
		}
		white_to_move = !white_to_move;
		key ^= Zobrist.whiteMove;
		return true;
	}
	
	/**
	 * Makes a "null move", which essentially amounts to passing one's turn.
	 * However, en passant goes away too.
	 */
	public void doNullMove() {
		saveHistory(moveNumber);
		moveNumber++;
		if (enPassantLoc != -1)
			key ^= Zobrist.passantColumn[BBUtils.getLocCol(enPassantLoc)];
		enPassantLoc = -1;
		white_to_move = !white_to_move;
		key ^= Zobrist.whiteMove;
	}
	
	/**
	 * Determines if a position has entered the endgame state. <b>Do not confuse
	 * with <code>isEndOfGame()</code>!</b> Endgames are boards where there both
	 * sides have (a) no queen, or (b) a queen and a bishop/knight.
	 * 
	 * @return true if it's the endgame, false otherwise
	 */
	public boolean isEndGame() {
		// q == 0 ||
		// .... ((q == 1 && n == 1 && b == 0 && r == 0)
		// .... || (q == 1 && n == 0 && b == 1 && r == 0))
		int q = Long.bitCount(white_queens);
		int n = Long.bitCount(white_knights);
		int b = Long.bitCount(white_bishops);
		int r = Long.bitCount(white_rooks);
		boolean white_endgame =
				(q == 0 && r <= 1)
						|| ( (q == 1 && n == 1 && b == 0 && r == 0) || (q == 1 && n == 0
								&& b == 1 && r == 0));
		
		q = Long.bitCount(black_queens);
		n = Long.bitCount(black_knights);
		b = Long.bitCount(black_bishops);
		r = Long.bitCount(black_rooks);
		
		boolean black_endgame =
				(q == 0 && r <= 1)
						|| ( (q == 1 && n == 1 && b == 0 && r == 0) || (q == 1 && n == 0
								&& b == 1 && r == 0));
		
		return white_endgame && black_endgame;
	}
	
	/**
	 * Is the game over?
	 * 
	 * @return true if the board is currently in a mated or drawn position,
	 *         false otherwise.
	 */
	public boolean isEndOfGame() {
		return isMate() || isDraw();
	}
	
	/**
	 * Is either side in check?
	 * 
	 * @return true if either side's king is being attacked, false otherwise.
	 */
	public boolean isCheck() {
		return BBMagicAttacks.isSquareAttacked(this, white_king, true)
				|| BBMagicAttacks.isSquareAttacked(this, black_king, false);
	}
	
	/**
	 * Has either side been mated?
	 * 
	 * @return true if the side to move is in checkmate, false otherwise.
	 */
	public boolean isMate() {
		int[] moves = new int[MAX_MOVES];
		return isCheck() && MoveGenerator.getAllLegalMoves(this, moves) == 0;
	}
	
	/**
	 * Is the position a draw? This may be due to stalemate, the fifty-move
	 * rule, threefold reptition, or if there are only kings remaining on the
	 * board.
	 * 
	 * @return true if the position is a draw, false otherwise.
	 */
	public boolean isDraw() {
		// stale
		int[] moves = new int[MAX_MOVES];
		if (MoveGenerator.getAllLegalMoves(this, moves) == 0 && !isCheck())
			return true;
		
		// 50mr
		if (fiftyMoveRule >= 50)
			return true;
		
		// threefold rep
		/*
		 * This can be optimized -- start only from movenumber - 50mr, and go by
		 * twos.
		 * This _must_ be done in zobrist though-- this layout will not work.
		 */
		int reps = 0;
		for (int i = moveNumber - fiftyMoveRule; i < moveNumber - 2; i += 2) {
			if (key_history[i] == key)
				reps++;
			if (reps == 2)
				return true;
		}
		
		// insufficient material -- so far only Kk, but need to add:
		// KBk, KNk, KBkb where bishops of same color
		if ( (white_pieces & ~white_king) == 0 && (black_pieces & ~black_king) == 0)
			return true;
		
		return false;
	}
	
	/**
	 * Determines how much white's pieces are worth, assuming Knights and Bishop
	 * are worth 325, Rooks are worth 500, and Queens are worth 975. This method
	 * does <b>not</b> count pawns.
	 * 
	 * @return the value of white's pieces
	 */
	public int whitePieceMaterial() {
		return 325 * Long.bitCount(white_knights) + 325 * Long.bitCount(white_bishops)
				+ 500 * Long.bitCount(white_rooks) + 975 * Long.bitCount(white_queens);
	}
	
	/**
	 * Determines how much black's pieces are worth, assuming Knights and Bishop
	 * are worth 325, Rooks are worth 500, and Queens are worth 975. This method
	 * does <b>not</b> count pawns.
	 * 
	 * @return the value of black's pieces
	 */
	public int blackPieceMaterial() {
		return 325 * Long.bitCount(black_knights) + 325 * Long.bitCount(black_bishops)
				+ 500 * Long.bitCount(black_rooks) + 975 * Long.bitCount(black_queens);
	}
	
	/**
	 * Determines how much the side to move's material is worth, ssuming Knights
	 * and Bishop
	 * are worth 325, Rooks are worth 500, and Queens are worth 975. This method
	 * does <b>not</b> count pawns.
	 * 
	 * This method relies on <code>whitePieceMaterial()</code> and
	 * <code>blackPieceMaterial()</code>.
	 * 
	 * @return the value of the side to move's pieces.
	 */
	public int movingSideMaterial() {
		return (white_to_move) ? whitePieceMaterial() : blackPieceMaterial();
	}
	
	/**
	 * Reverts the board to its previous state. <i>Note</i>: if this method is
	 * called from the board's initial position (the position it read an FEN
	 * from), this method will throw an
	 * <code>ArrayIndexOutOfBoundsException</code>.
	 */
	public void undoMove() {
		undoMove(moveNumber - 1);
	}
	
	private void undoMove(int moveNumber) {
		// System.out.println("reverting to " + moveNumber);
		if (moveNumber < 0 || moveNumber < initMoveNumber) {
			return;
		}
		
		white_pawns = white_pawn_history[moveNumber];
		white_knights = white_knight_history[moveNumber];
		white_bishops = white_bishop_history[moveNumber];
		white_rooks = white_rook_history[moveNumber];
		white_queens = white_queen_history[moveNumber];
		white_king = white_king_history[moveNumber];
		black_pawns = black_pawn_history[moveNumber];
		black_knights = black_knight_history[moveNumber];
		black_bishops = black_bishop_history[moveNumber];
		black_rooks = black_rook_history[moveNumber];
		black_queens = black_queen_history[moveNumber];
		black_king = black_king_history[moveNumber];
		white_pieces = white_pieces_history[moveNumber];
		black_pieces = black_pieces_history[moveNumber];
		all_pieces = all_pieces_history[moveNumber];
		white_to_move = white_to_move_history[moveNumber];
		fiftyMoveRule = fiftyMoveRule_history[moveNumber];
		enPassantLoc = enPassantLoc_history[moveNumber];
		white_castle_k = white_castle_k_history[moveNumber];
		white_castle_q = white_castle_q_history[moveNumber];
		black_castle_k = black_castle_k_history[moveNumber];
		black_castle_q = black_castle_q_history[moveNumber];
		white_has_castled = white_has_castled_history[moveNumber];
		black_has_castled = black_has_castled_history[moveNumber];
		key = key_history[moveNumber];
		this.moveNumber = moveNumber;
	}
	
	private boolean ownKingIsInCheck() {
		if (white_to_move)
			return BBMagicAttacks.isSquareAttacked(this, white_king, true);
		return BBMagicAttacks.isSquareAttacked(this, black_king, false);
	}
	
	private void saveHistory(int move) {
		white_pawn_history[moveNumber] = white_pawns;
		white_knight_history[moveNumber] = white_knights;
		white_bishop_history[moveNumber] = white_bishops;
		white_rook_history[moveNumber] = white_rooks;
		white_queen_history[moveNumber] = white_queens;
		white_king_history[moveNumber] = white_king;
		black_pawn_history[moveNumber] = black_pawns;
		black_knight_history[moveNumber] = black_knights;
		black_bishop_history[moveNumber] = black_bishops;
		black_rook_history[moveNumber] = black_rooks;
		black_queen_history[moveNumber] = black_queens;
		black_king_history[moveNumber] = black_king;
		white_pieces_history[moveNumber] = white_pieces;
		black_pieces_history[moveNumber] = black_pieces;
		all_pieces_history[moveNumber] = all_pieces;
		white_to_move_history[moveNumber] = white_to_move;
		fiftyMoveRule_history[moveNumber] = fiftyMoveRule;
		enPassantLoc_history[moveNumber] = enPassantLoc;
		move_history[moveNumber] = move;
		white_castle_k_history[moveNumber] = white_castle_k;
		white_castle_q_history[moveNumber] = white_castle_q;
		black_castle_k_history[moveNumber] = black_castle_k;
		black_castle_q_history[moveNumber] = black_castle_q;
		key_history[moveNumber] = key;
	}
	
	/**
	 * SEE, as implemented by Alberto Ruibal, who converted it to Java code from
	 * the chess programming wiki.<br>
	 * <br>
	 * 
	 * <i>Code from:
	 * </i>http://chessprogramming.wikispaces.com/SEE+-+The+Swap+Algorithm<br>
	 * <br>
	 * 
	 * All I've done is converted the code to something meaningful in my
	 * program.
	 * 
	 * @param move the capturing move to evaluate
	 * @return the value of the resulting tradeoff
	 */
	public int see(int move) {
		int pieceCaptured = 0;
		long to = BBUtils.getSquare[Move.getTo(move)];
		
		if ( (to & (white_knights | black_knights)) != 0) {
			pieceCaptured = Move.KNIGHT;
		}
		else if ( (to & (white_bishops | black_bishops)) != 0) {
			pieceCaptured = Move.BISHOP;
		}
		else if ( (to & (white_rooks | black_rooks)) != 0) {
			pieceCaptured = Move.ROOK;
		}
		else if ( (to & (white_queens | black_queens)) != 0) {
			pieceCaptured = Move.QUEEN;
		}
		else if (Move.isCapture(move)) {
			pieceCaptured = Move.PAWN;
		}
		
		return see(Move.getFrom(move), Move.getTo(move), Move.getPieceType(move),
				pieceCaptured);
	}
	
	private int see(int fromIndex, int toIndex, int pieceMoved, int targetPiece) {
		int d = 0;
		int[] seeGain = new int[32];
		long mayXray =
				(white_pawns | black_pawns) | (white_bishops | black_bishops)
						| (white_rooks | black_rooks) | (white_queens | black_queens);
		long fromSquare = BBUtils.getSquare[fromIndex];
		long all = all_pieces;
		long attacks = BBMagicAttacks.getIndexAttacks(this, toIndex);
		long fromCandidates = 0;
		seeGain[d] = SEE_PIECE_VALUES[targetPiece];
		do {
			long side;
			
			if ( (d % 2 != 0 && white_to_move) || (d % 2 == 0 && !white_to_move))
				side = white_pieces;
			else
				side = black_pieces;
			
			d++;
			seeGain[d] = SEE_PIECE_VALUES[pieceMoved] - seeGain[d - 1];
			attacks ^= fromSquare;
			all ^= fromSquare;
			if ( (fromSquare & mayXray) != 0)
				attacks |= BBMagicAttacks.getXrayAttacks(this, toIndex, all);
			
			fromSquare = 0;
			fromCandidates = 0;
			if ( (fromCandidates = attacks & (white_pawns | black_pawns) & side) != 0) {
				pieceMoved = Move.PAWN;
			}
			else if ( (fromCandidates = attacks & (white_knights | black_knights) & side) != 0) {
				pieceMoved = Move.KNIGHT;
			}
			else if ( (fromCandidates = attacks & (white_bishops | black_bishops) & side) != 0) {
				pieceMoved = Move.BISHOP;
			}
			else if ( (fromCandidates = attacks & (white_rooks | black_rooks) & side) != 0) {
				pieceMoved = Move.ROOK;
			}
			else if ( (fromCandidates = attacks & (white_queens | black_queens) & side) != 0) {
				pieceMoved = Move.QUEEN;
			}
			else if ( (fromCandidates = attacks & (white_king | black_king) & side) != 0) {
				pieceMoved = Move.KING;
			}
			fromSquare = BBUtils.lsb(fromCandidates);
			
		} while (fromSquare != 0);
		for (int i = 0; i < 5; i++)
			while (d > 1) {
				d--;
				seeGain[d - 1] = -Math.max( -seeGain[d - 1], seeGain[d]);
			}
		return seeGain[0];
	}
}
