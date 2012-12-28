package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class BoardView extends JPanel implements Observer {
	private static final int WIDTH = 50;
	
	private BoardModel b;
	private int prevXfrom;
	private int prevYfrom;
	private int prevXto;
	private int prevYto;
	
	private static ImageIcon black_pawn = new ImageIcon("icons\\black_pawn.gif");
	private static ImageIcon black_knight = new ImageIcon("icons\\black_knight.gif");
	private static ImageIcon black_bishop = new ImageIcon("icons\\black_bishop.gif");
	private static ImageIcon black_rook = new ImageIcon("icons\\black_rook.gif");
	private static ImageIcon black_queen = new ImageIcon("icons\\black_queen.gif");
	private static ImageIcon black_king = new ImageIcon("icons\\black_king.gif");
	private static ImageIcon white_pawn = new ImageIcon("icons\\white_pawn.gif");
	private static ImageIcon white_knight = new ImageIcon("icons\\white_knight.gif");
	private static ImageIcon white_bishop = new ImageIcon("icons\\white_bishop.gif");
	private static ImageIcon white_rook = new ImageIcon("icons\\white_rook.gif");
	private static ImageIcon white_queen = new ImageIcon("icons\\white_queen.gif");
	private static ImageIcon white_king = new ImageIcon("icons\\white_king.gif");
	
	@Override
	public void update(Observable o, Object a) {
		b = (BoardModel) o;
		repaint();
	}
	
	public void setLastMove(int x1, int y1, int x2, int y2) {
		prevXfrom = x1;
		prevYfrom = y1;
		prevXto = x2;
		prevYto = y2;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// g.setColor(Color.BLACK);
		// g.fillRect(100, 200, 50, 50);
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if ( ! ( (i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0)))
					g.setColor(new Color(77, 77, 255));
				else
					g.setColor(Color.WHITE);
				g.fillRect(i * WIDTH, j * WIDTH, WIDTH, WIDTH);
			}
		}
		
		g.setColor(Color.YELLOW);
		if (prevXfrom != 0 || prevYfrom != 0 || prevXto != 0 || prevYto != 0) {
			g.fillRect(prevXfrom * WIDTH, prevYfrom * WIDTH, WIDTH, WIDTH);
			g.fillRect(prevXto * WIDTH, prevYto * WIDTH, WIDTH, WIDTH);
		}
		
		for (int up = 7; up >= 0; up--) {
			for (int out = 0; out < 8; out++) {
				int x = (7 - up) * WIDTH;
				int y = out * WIDTH;
				
				switch (b.get( (7 - out) * 8 + 7 - up)) {
					case 'p':
						black_pawn.paintIcon(this, g, x, y);
						break;
					case 'P':
						white_pawn.paintIcon(this, g, x, y);
						break;
					case 'n':
						black_knight.paintIcon(this, g, x, y);
						break;
					case 'N':
						white_knight.paintIcon(this, g, x, y);
						break;
					case 'b':
						black_bishop.paintIcon(this, g, x, y);
						break;
					case 'B':
						white_bishop.paintIcon(this, g, x, y);
						break;
					case 'r':
						black_rook.paintIcon(this, g, x, y);
						break;
					case 'R':
						white_rook.paintIcon(this, g, x, y);
						break;
					case 'q':
						black_queen.paintIcon(this, g, x, y);
						break;
					case 'Q':
						white_queen.paintIcon(this, g, x, y);
						break;
					case 'k':
						black_king.paintIcon(this, g, x, y);
						break;
					case 'K':
						white_king.paintIcon(this, g, x, y);
						break;
				}
			}
		}
	}
}
