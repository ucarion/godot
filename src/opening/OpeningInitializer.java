package opening;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class OpeningInitializer {
	private static final String OPENING_DB = "IB1219.pgn";
	private static final String OUTPUT = "F:\\workspace\\Godot\\opening.txt";
	
	public static void init() {
		int count = 0;
		
		try {
			BufferedReader br =
					new BufferedReader(new InputStreamReader(new DataInputStream(
							new FileInputStream(OUTPUT))));
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
							new FileInputStream(OPENING_DB))));
			String line;
			ArrayList<String> games = new ArrayList<String>();
			String game = "";
			while ( (line = br.readLine()) != null) {
				count++;
				System.out.println(count);
				if (line.startsWith("[") || line.startsWith(" ")) {
					if ( !game.isEmpty())
						games.add(game);
					game = "";
				}
				else
					game += " " + line.trim();
				
				if (count % 100000 == 0) {
					StringBuilder sb = new StringBuilder();
					for (String s : games) {
						s = s.replaceAll("\\{.+?\\} ", "");
						s = s.replaceAll("\\d+\\.", "");
						s = s.replaceAll("\\s\\s+", " ");
						sb.append(s.trim() + "\n");
					}
					writeFile(OUTPUT, sb.toString());
					games.clear();
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
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
		init();
	}
}