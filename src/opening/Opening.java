package opening;

import godot.Board;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;

import util.SANUtils;

public class Opening {
	private static TreeMap<Long, String> book;
	private static final String OPENING_BOOK = "basicbook.txt";
	public static final int MOVE_NOT_FOUND = -1;
	
	static {
		init();
	}
	
	private static void init() {
		try {
			BufferedReader br =
					new BufferedReader(new InputStreamReader(new DataInputStream(
							new FileInputStream(OPENING_BOOK))));
			
			book = new TreeMap<Long, String>();
			
			String line;
			while ( (line = br.readLine()) != null) {
				long key = Long.parseLong(line.split(" ")[0]);
				String move = line.split(" ")[1];
				book.put(key, move);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the "book move" for a position, if any.
	 * 
	 * @param b
	 *            the position to find the move for
	 * @return the book move for a position, or
	 *         <code>Opening.MOVE_NOT_FOUND</code> if no move was found.
	 */
	public static int getBookMove(Board b) {
		String s = book.get(b.key);
		if (s == null)
			return MOVE_NOT_FOUND;
		return SANUtils.getMove(b, s);
	}
}
