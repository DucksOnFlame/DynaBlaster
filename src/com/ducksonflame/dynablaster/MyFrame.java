package com.ducksonflame.dynablaster;
import javax.swing.JFrame;

/**
 * Main JFrame - handles initial and end game screens (work in progress). Handles play again functionality.
 */

@SuppressWarnings("serial")
public class MyFrame extends JFrame {
	
	private Board myBoard;
	
	public MyFrame () {
		
		initUI();
	
	}
	
	/**
	 * Initiates new game. Sets basic properties.
	 */
	private void initUI() {
		
		myBoard = new Board(this);
		add(myBoard);
		setResizable(false);
		pack();
		
		setTitle("Dyna Blaster");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
	}
	
	/**
	 * Should handle new game after the option is chosen in TempPanel. (Work in progress)
	 */
	public void newGame () {
		getContentPane().removeAll();
		setContentPane(new Board(this));
		revalidate();
	}
	
	/**
	 * Clears the current game Board and initialized a TempPanel.
	 * @param winner Takes winner and passes it to TempPanel for display.
	 */
	public void playAgain(int winner) {
		getContentPane().removeAll();
		myBoard = null;
		setContentPane(new EndGamePanel(this, winner));
		revalidate();

	}
}
