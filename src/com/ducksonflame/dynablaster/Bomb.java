package com.ducksonflame.dynablaster;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Bomb class used for creating Bomb instances.
 * Separates bombs of player 1 and 2 (to control max bomb limit).
 * Implements Comparable to store in Board's sets.
 * <br>Contains modifiable settings:
 * <br>CHANGE_DELAY - Controls animation speed (default is 500).
 * <br>BOOM_DELAY - Controls explosion delay after placement (default is 1500).
 * <br><br>
 * 
 * 
 */
@SuppressWarnings("serial")
public class Bomb extends JComponent implements Comparable<Bomb> {
	public final int BOMB_SIZE = Board.TILE_SIZE;
	public final Image img1;
	public final Image img2;
	public final int CHANGE_DELAY = 500;
	public final int BOOM_DELAY = 1500;
	
	public static int bombID1 = 1;
	public static int bombID2 = 1;
	
	private int x;
	private int y;
	private int id;
	private int counter = 0;
	private Timer timer;
	private Image currentImage;
	private Character p;
	
	private boolean exploding;
	
	/**
	 * Constructor decreases available bombs for player that placed the bomb. Assigns properties and starts the animation timer. Loads images.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param p Player reference
	 */
	public Bomb (int x, int y, Character p) {
		
		this.p = p;
		if (this.p.getPlayerNumber() == 1) {
			this.id = bombID1;
			bombID1++;
		}
		else if (this.p.getPlayerNumber() == 2) {
			this.id = bombID2;
			bombID2++;
		}
		this.x = x;
		this.y = y;
		img1 = new ImageIcon("images/bomb1_"+BOMB_SIZE+".png").getImage();
		img2 = new ImageIcon("images/bomb2_"+BOMB_SIZE+".png").getImage();
		currentImage = img1;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TaskScheduler(), CHANGE_DELAY, CHANGE_DELAY);

	}
		
	/**
	 * Contains simple logic for animation. Called repeatedly by a timer.
	 */
	public void changeImg () {
		
		if (counter == 6) {}
		
		else if (currentImage.equals(img1)) {
			currentImage = img2;
		}
		
		else {
			currentImage = img1;
		}
	}
	
	public int getX () {
		return x;
	}
	
	public int getY () {
		return y;
	}
	
	public Image getCurrentImage() {
		return currentImage;
	}
	
	/**
	 * Passes information about bomb explosion to the Player.
	 * Player regains access to one bomb and passes information to Board for explosion processing.
	 */
	public void boom () {
		exploding = true;
		timer = null;
		p.bombExploded(this);
	}
	
	/**
	 * Private class for timer's purposes. Contains some animation logic.
	 */
	private class TaskScheduler extends TimerTask {

        @Override
        public void run() {
        	changeImg();
			counter++;
			if (counter == 6) {
				if (exploding == false) {
				boom();
				}
			}
        }
    }

	@Override
	public int compareTo(Bomb other) {
		return id - other.id;
	}
	
}
