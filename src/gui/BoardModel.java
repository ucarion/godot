package gui;

import eval.CompleteEvaluator;
import godot.Board;
import godot.Move;

import java.util.Observable;

import search.IterativeDeepening;
import search.IterativeQSEE;
import search.NullIterQSEE;
import util.BBUtils;
import util.SANUtils;

import movegen.MoveGenerator;

public class BoardModel extends Observable {
	private Board b;
	private BoardView v;
	
	public BoardModel(Board b, BoardView v) {
		this.b = b;
		this.v = v;
	}
	
	public void makeMove(int move) {
		if (b.moveNumber % 2 == 0)
			System.out.println(" " + SANUtils.getSAN(b, move));
		else
			System.out.print( ((b.moveNumber + 1) / 2) + ". " + SANUtils.getSAN(b, move));
		
		int from = Move.getFrom(move);
		int to = Move.getTo(move);
		b.makeMove(move);
		v.setLastMove(from % 8, 7 - from / 8, to % 8, 7 - to / 8);
	//	System.out.println(b);
		setChanged();
		notifyObservers();
	}
	
	public void undoMove() {
		if (b.moveNumber > 0) {
			b.undoMove();
			b.undoMove();
			v.setLastMove(0, 0, 0, 0);
			setChanged();
			notifyObservers();
		}
	}
	
	public char get(int loc) {
		return b.getPieceAt(loc);
	}
	
	public void makeMove(int x1, int x2, int y1, int y2) {
//		System.out.println("(" + x1 + ", " + y1 + ") --> (" + x2 + ", " + y2 + ")");
		String s =
				(char) (x1 + 'a') + "" + (char) ('8' - y1) + "-" + (char) (x2 + 'a')
						+ (char) ('8' - y2);
		
		int[] moves = new int[Board.MAX_MOVES];
		int num_moves = MoveGenerator.getAllLegalMoves(b, moves);
	//	System.out.println(s);
		for (int i = 0; i < num_moves; i++) {
			if (s.equals(BBUtils.intToAlgebraicLoc(Move.getFrom(moves[i])) + "-"
					+ BBUtils.intToAlgebraicLoc(Move.getTo(moves[i])))) {
				makeMove(moves[i]);
//				BitboardUtils.printMove(moves[i]);
			}
		}
	}
	
	public void makeEngineMove() {
		NullIterQSEE.setDepth(5);
		NullIterQSEE.setEvaluator(new CompleteEvaluator());
		int move = NullIterQSEE.getBestMove(b);
		makeMove(move);
	}
	
	public boolean whiteWins() {
		return b.isMate() && !b.white_to_move;
	}
	
	public boolean blackWins() {
		return b.isMate() && b.white_to_move;
	}
	
	public boolean isDraw() {
		return b.isDraw();
	}
	
	public void displayConclusionInfo() {
		if (b.moveNumber % 2 == 0)
			System.out.print(b.moveNumber / 2 + ". ");
		else
			System.out.print(" ");
		
		if (whiteWins())
			System.out.println("1-0");
		if (blackWins())
			System.out.println("0-1");
		else
			System.out.println(".5-.5");
	}
}
