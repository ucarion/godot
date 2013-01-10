package bot;

import java.util.ArrayList;
import java.util.Arrays;

import eval.CompleteEvaluator;
import godot.Board;
import godot.Move;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import search.NullIterQSEE;
import util.BBUtils;
import util.SANUtils;

/**
 * Automates online play by having Godot be connected to Selenium Webdriver.
 * 
 * This code is somewhat problematic-- it will occasionally crash due to
 * UnreachableBrowserExceptions and other unpredictable bugs. If you can find a
 * fix to these errors, drop me a message.
 * 
 * GodotBot assumes that the following settings are set for live chess:
 * - Board size: Default
 * - White on bottom: true
 * - Automatically promote to Queen: false
 * 
 * @author Ulysse
 * 
 */
public class GodotBot {
	private static final int WAITING_INTERVAL = 500;
	private static final int SQUARE_WIDTH = 55;
	private static final String USERNAME = "Samuel_Beckett"; // update this
	private static final String PASSWORD = "chess2600"; // update this
	private static final String TIME_CONTROL = "1 Min";
	private static final String GAME_TYPE = "Rated";
	private static final int DEPTH = 6;
	private static ChromeDriver driver;
	private static Board b;
	private static String url;
	
	/**
	 * Launches GodotBot.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		setup();
		
		while (true) {
			waitForGameBegin();
			while (gameIsGoing()) {
				if (engineTurnOnline())
					makeEngineMoveOnline();
				else
					listenForOpponentMoveAndUpdate();
				sleep(100);
			}
			
			startNewGame();
		}
	}
	
	private static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {}
	}
	
	private static void sleep() {
		sleep(WAITING_INTERVAL);
	}
	
	/**
	 * Sets up the game by initializing the driver and board, navigating to the
	 * game, and starting the first game.
	 */
	private static void setup() {
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		b = new Board(BBUtils.START_FEN);
		url = "";
		driver.get("http://www.chess.com");
		WebElement user_ele = driver.findElement(By.id("loginusername"));
		WebElement pass_ele = driver.findElement(By.id("loginpassword"));
		WebElement login_button = driver.findElement(By.id("c2"));
		user_ele.sendKeys(USERNAME);
		pass_ele.sendKeys(PASSWORD);
		login_button.click();
		driver.get("http://live.chess.com/simple");
		waitUntilGameLoaded();
		setGameSettings();
		driver.findElement(By.id("new_game_pane_create_button")).click();
	}
	
	private static void waitUntilGameLoaded() {
		WebElement go = null;
		while (true) {
			try {
				go = driver.findElement(By.id("new_game_pane_create_button"));
				driver.findElement(By.id("new_game_pane_rated_select"));
				driver.findElement(By.id("new_game_pane_options"));
				break;
			} catch (Exception e) {
				sleep();
			}
		}
		while ( !go.isDisplayed())
			sleep();
	}
	
	private static void setGameSettings() {
		Select select1 =
				new Select(driver.findElement(By.id("new_game_pane_rated_select")));
		for (WebElement option : select1.getOptions()) {
			if (option.getAttribute("text").equals(GAME_TYPE)) {
				option.click();
			}
		}
		
		Select select2 = new Select(driver.findElement(By.id("new_game_pane_options")));
		for (WebElement option : select2.getOptions()) {
			if (option.getAttribute("text").equals(TIME_CONTROL)) {
				option.click();
			}
		}
	}
	
	private static void makeMoveOnline(int move) {
		String from = BBUtils.intToAlgebraicLoc(Move.getFrom(move));
		String to = BBUtils.intToAlgebraicLoc(Move.getTo(move));
		WebElement ele = driver.findElement(By.id("img_chessboard_1_" + from));
		int dx = to.charAt(0) - from.charAt(0);
		int dy = from.charAt(1) - to.charAt(1);
		(new Actions(driver)).dragAndDropBy(ele, dx * SQUARE_WIDTH, dy * SQUARE_WIDTH)
				.perform();
		if (Move.isPromotion(Move.getFlag(move))) {
			sleep();
			switch (Move.getFlag(move)) {
				case Move.FLAG_PROMOTE_QUEEN:
					driver.findElement(By.id("chessboard_1_promotionq")).click();
					break;
				case Move.FLAG_PROMOTE_KNIGHT:
					driver.findElement(By.id("chessboard_1_promotionn")).click();
					break;
				case Move.FLAG_PROMOTE_ROOK:
					driver.findElement(By.id("chessboard_1_promotionr")).click();
					break;
				case Move.FLAG_PROMOTE_BISHOP:
					driver.findElement(By.id("chessboard_1_promotionb")).click();
					break;
			}
		}
		updateInternalBoard(move);
	}
	
	private static void updateInternalBoard(int move) {
		b.makeMove(move);
	}
	
	private static void waitForGameBegin() {
		while (true) {
			try {
				driver.findElement(By.id("white_player_name_1"));
				if ( !driver.getCurrentUrl().equals(url)
						&& (engineIsPlayingBlack() || engineIsPlayingWhite())) {
					url = driver.getCurrentUrl();
					break;
				}
			} catch (Exception e) {
				sleep();
			}
		}
	}
	
	private static void makeEngineMoveOnline() {
		NullIterQSEE.setDepth(DEPTH);
		NullIterQSEE.setEvaluator(new CompleteEvaluator());
		int move = NullIterQSEE.getBestMove(b);
		updateInternalBoard(move);
		makeMoveOnline(move);
	}
	
	private static void listenForOpponentMoveAndUpdate() {
		String movelist = getMovelistOnline();
		while (movelist.equals(getMovelistOnline())) {
			if ( !gameIsGoing())
				return;
			sleep();
		}
		
		String[] moves = getMovelistOnline().split("\\n");
		String lastmove = moves[moves.length - 1];
		
		updateInternalBoard(SANUtils.getMove(b, lastmove));
	}
	
	private static String getMovelistOnline() {
		return driver.findElement(By.id("notation_1")).getText().trim();
	}
	
	private static boolean whiteToPlayOnline() {
		ArrayList<String> moves =
				new ArrayList<String>(Arrays.asList(getMovelistOnline().split("\\n")));
		
		if (moves.size() == 0)
			return true;
		
		for (int i = moves.size() - 1; i >= 0; i--) {
			if (moves.get(i).isEmpty() || Character.isDigit(moves.get(i).charAt(0)))
				moves.remove(i);
		}
		
		return moves.size() % 2 == 0;
	}
	
	private static boolean engineIsPlayingWhite() {
		WebElement name = driver.findElement(By.id("white_player_name_1"));
		return name.getAttribute("text").equals(USERNAME);
	}
	
	private static boolean engineIsPlayingBlack() {
		WebElement name = driver.findElement(By.id("black_player_name_1"));
		return name.getAttribute("text").equals(USERNAME);
	}
	
	private static boolean gameIsGoing() {
		WebElement e = driver.findElement(By.id("b-quicknewgame_1"));
		return !e.isEnabled() || !e.isDisplayed();
	}
	
	private static boolean engineTurnOnline() {
		return engineIsPlayingWhite() == whiteToPlayOnline();
	}
	
	private static void startNewGame() {
		driver.findElement(By.id("b-quicknewgame_1")).click();
		b = new Board(BBUtils.START_FEN);
	}
}