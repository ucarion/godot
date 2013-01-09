package gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import util.BBUtils;
import godot.Board;

public class BoardMain extends JFrame implements ActionListener {
	private static final long serialVersionUID = 6796881569710597920L;
	
	private static final int WIDTH = 50;
	private static final int LOWER_BUFFER = 51;
	private static final int SIDE_BUFFER = 5;
	
	private static final boolean PLAYER_IS_WHITE = true;
	private static final int NUM_MINUTES = 1;
	private static final boolean CAN_UNDO = true;
	
	private BoardModel model;
	private BoardView view;
	
	private Timer timer;
	
	private int fromx;
	private int fromy;
	private int tox;
	private int toy;
	
	private boolean player_turn;
	
	private int player_time;
	private int engine_time;
	
	public BoardMain() {
		super("Godot chess GUI v.0.1");
		setResizable(false);
		setSize(WIDTH * 8 + SIDE_BUFFER, WIDTH * 8 + LOWER_BUFFER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Board b = new Board();
		b.readFromFEN(BBUtils.START_FEN);
		view = new BoardView();
		model = new BoardModel(b, view);
		model.addObserver(view);
		view.update(model, null);
		view.repaint();
		Container c = getContentPane();
		c.add(view);
		
		timer = new Timer(100, this);
		
		JMenuBar mBar = new JMenuBar();
		JMenu m;
		JMenuItem i;
		
		m = new JMenu("Game");
		i = new JMenuItem("Undo move");
		i.addActionListener(this);
		m.add(i);
		mBar.add(m);
		
		m = new JMenu("Engine");
		i = new JMenuItem("Godot");
		i.addActionListener(this);
		m.add(i);
		mBar.add(m);
		
		setJMenuBar(mBar);
		
		c.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseClicked(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {
				fromx = e.getX();
				fromy = e.getY();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				tox = e.getX();
				toy = e.getY();
				
				model.makeMove(fromx / WIDTH, tox / WIDTH, fromy / WIDTH, toy / WIDTH);
				
				if (fromx / WIDTH != tox / WIDTH || fromy / WIDTH != toy / WIDTH)
					player_turn = false;
				
				//view.setLastMove(fromx / WIDTH, fromy / WIDTH, tox / WIDTH, toy / WIDTH);
			}
		});
		
		player_time = 60 * NUM_MINUTES * 1000;
		engine_time = 60 * NUM_MINUTES * 1000;
		
		player_turn = PLAYER_IS_WHITE;
		
		showTimesOnTitlebar();
		
		timer.start();
	}
	
	public static void main(String[] args) {
		BoardMain bm = new BoardMain();
		bm.setVisible(true);
	}
	
	private void showTimesOnTitlebar() {
		if (PLAYER_IS_WHITE)
			setTitle("[Godot GUI] White: " + (player_time / 1000.0) + " -- Black: "
					+ (engine_time / 1000.0));
		else
			setTitle("[Godot GUI] White: " + (engine_time / 1000.0) + " -- Black: "
					+ (player_time / 1000.0));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == timer) {
			if (player_time < 0) {
				JOptionPane.showMessageDialog(null, "Player lost on time.");
				System.exit(0);
			}
			else if (engine_time < 0) {
				JOptionPane.showMessageDialog(null, "Engine lost on time.");
				System.exit(0);
			}
			
			showTimesOnTitlebar();
			
			if (player_turn) {
				player_time -= 100;
			}
			else {
				timer.stop();
				long start = System.currentTimeMillis();
				model.makeEngineMove();
				long stop = System.currentTimeMillis();
				engine_time -= (stop - start);
				player_turn = true;
				timer.start();
			}
			
			if (model.whiteWins()) {
				if (PLAYER_IS_WHITE) {
					JOptionPane.showMessageDialog(null, "Player wins by checkmate.");
					System.exit(0);
				}
				else {
					JOptionPane.showMessageDialog(null, "Engine wins by checkmate.");
					System.exit(0);
				}
			}
			else if (model.blackWins()) {
				if (PLAYER_IS_WHITE) {
					JOptionPane.showMessageDialog(null, "Engine wins by checkmate.");
					System.exit(0);
				}
				else {
					JOptionPane.showMessageDialog(null, "Player wins by checkmate.");
					System.exit(0);
				}
			}
			else if (model.isDraw()) {
				JOptionPane.showMessageDialog(null, "Draw");
				System.exit(0);
			}
		}
		else if (e.getActionCommand().equals("Undo move") && CAN_UNDO)
			model.undoMove();
	}
}
