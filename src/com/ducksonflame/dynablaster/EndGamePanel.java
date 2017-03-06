package com.ducksonflame.dynablaster;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Class that should handle the End Game tasks - displaying the winner and offering replay.
 * As of now no reply functionality.
 */
@SuppressWarnings("serial")
public class EndGamePanel extends JPanel implements KeyListener {
	
	private MyFrame myFrame;
	private int winner;
	
	/**
	 * Constructor sets JFrame reference and takes information about the winner.
	 * @param f JFrame reference
	 * @param winner Winning player number
	 */
	public EndGamePanel(MyFrame f, int winner) {
		
		myFrame = f;
		this.winner = winner;
		initUI();
		
	}
	
	/**
	 * Assumes the correct size, adds keyListener, adds a label for displaying text.
	 */
	public void initUI() {
		
		setPreferredSize(new Dimension((int)((2*Board.NUMBER_OF_FREE_COLUMNS)+1)*Board.TILE_SIZE + 2*Board.GAP_WIDTH, (int)((2*Board.NUMBER_OF_FREE_ROWS)+1)*Board.TILE_SIZE));
		setLayout(new GridLayout(10,1));
		addKeyListener(this);
		setBackground(Color.WHITE);
		JLabel label1 = new JLabel();
		
		label1.setFont(new Font("Verdana", Font.BOLD, 55));
		
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		
		label1.setText("Player "+winner+" has won!");
	    
	    add(label1);
	}
	
	/**
	 * Keybindings for potential future replay implementation.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		
		switch(e.getKeyCode()) {
		
		case KeyEvent.VK_Y:
			myFrame.newGame();
        	break;
		case KeyEvent.VK_N:
			System.exit(0);
        	break;
        default:
        	break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
}
