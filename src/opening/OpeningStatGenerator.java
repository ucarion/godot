package opening;

import godot.Board;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;

import util.BBUtils;
import util.SANUtils;

public class OpeningStatGenerator {
	private static final String OPENING_BOOK = "book.txt";
	private static final String GAME_LINES = "opening.txt";
	private static final int DEPTH = 14;
	
	public static void generateOpeningBook() {
		try {
			BufferedReader br =
					new BufferedReader(new InputStreamReader(new DataInputStream(
							new FileInputStream(OPENING_BOOK))));
			if (br.readLine() != null) {
				br.close();
				throw new Exception();
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Output is either not empty or nonexistent!");
			System.exit(0);
		}
		
		try {
			BufferedReader br =
					new BufferedReader(new InputStreamReader(new DataInputStream(
							new FileInputStream(GAME_LINES))));
			int count = 0;
			String line;
			TreeMap<MoveOption, Integer> map = new TreeMap<MoveOption, Integer>();
			while ( (line = br.readLine()) != null && count < 1500000) {
				count++;
				if (count % 100 == 0)
					System.out.println("Evaluated " + count + " positions ...");
				
				String[] moves = line.split(" ");
				if (moves.length <= DEPTH)
					continue;
				
				Board b = new Board();
				b.readFromFEN(BBUtils.START_FEN);
				for (int i = 0; i < DEPTH; i++) {
					MoveOption mo = new MoveOption(b.key, moves[i]);
					if ( !map.containsKey(mo))
						map.put(mo, 1);
					else
						map.put(mo, map.get(mo) + 1);
					b.makeMove(SANUtils.getMove(b, moves[i]));
				}
				MoveOption mo = new MoveOption(b.key, moves[DEPTH]);
				if ( !map.containsKey(mo))
					map.put(mo, 1);
				else
					map.put(mo, map.get(mo) + 1);
			}
			br.close();
			
			StringBuilder output = new StringBuilder();
			long key = map.firstKey().key;
			String s = map.firstKey().move;
			int max = map.firstEntry().getValue();
			for (MoveOption mo : map.keySet()) {
				if (mo.key != key) {
					output.append(key + " " + s + "\n");
					key = mo.key;
					s = mo.move;
					max = map.get(mo);
				}
				else if (map.get(mo) > max) {
					s = mo.move;
					max = map.get(mo);
				}
			}
			
			writeFile(OPENING_BOOK, output.toString());
			
			// StringBuilder s = new StringBuilder();
			// for (MoveOption mo : map.keySet())
			// s.append(mo + " " + map.get(mo) + "\n");
			// writeFile(OPENING_BOOK, s.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void writeFile(String path, String content) {
		try {
			FileWriter fw = new FileWriter(path, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		generateOpeningBook();
	}
	
	private static class MoveOption implements Comparable<MoveOption> {
		long key;
		String move;
		
		public MoveOption(long key, String move) {
			this.key = key;
			this.move = move;
		}
		
		public String toString() {
			return key + " " + move;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (key ^ (key >>> 32));
			result = prime * result + ( (move == null) ? 0 : move.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MoveOption other = (MoveOption) obj;
			if (key != other.key)
				return false;
			if (move == null) {
				if (other.move != null)
					return false;
			}
			else if ( !move.equals(other.move))
				return false;
			return true;
		}
		
		@Override
		public int compareTo(MoveOption mo) {
			if (key > mo.key)
				return 1;
			else if (key == mo.key)
				return 0;
			else
				return -1;
		}
	}
}
