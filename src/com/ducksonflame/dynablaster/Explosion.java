package com.ducksonflame.dynablaster;
import java.awt.Image;
import java.util.TimerTask;
import java.util.Timer;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Class handling explosions and their lifetime.
 * Implements Comparable for proper handling in Board class. 
 * expLife can be modified to change the time that the explosion is active.
 * Range is remembered from the moment of bomb placement not explosion time.
 */

@SuppressWarnings("serial")
public class Explosion extends JComponent implements Comparable<Explosion> {
	//Images for the middle tile and vertical/horizontal "arm" tiles
	public final Image mid = new ImageIcon("images/explosionMid_"+Board.TILE_SIZE+".png").getImage();
	public final Image vert = new ImageIcon("images/explosionVertical_"+Board.TILE_SIZE+".png").getImage();
	public final Image hori = new ImageIcon("images/explosionHorizontal_"+Board.TILE_SIZE+".png").getImage();
	
	//Images for last tiles at each end.
	public final Image endUp = new ImageIcon("images/explosionEndUp_"+Board.TILE_SIZE+".png").getImage();
	public final Image endDown = new ImageIcon("images/explosionEndDown_"+Board.TILE_SIZE+".png").getImage();
	public final Image endRight = new ImageIcon("images/explosionEndRight_"+Board.TILE_SIZE+".png").getImage();
	public final Image endLeft = new ImageIcon("images/explosionEndLeft_"+Board.TILE_SIZE+".png").getImage();
	
	//Modifiable to extend/reduce explosion's life.
	public final static int expLife = 350;

	private int x;
	private int y;
	private int range;
	private int id;
	private Timer expTimer;
	public static int expID = 1;
	private Board gameBoard;
	
	/**
	 * Assigns ID and other properties. Starts timer for explosion's end.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param range Range from bomb placement time
	 * @param game Main game reference
	 */
	public Explosion(int x, int y, int range, Board game) {
		gameBoard = game;
		this.x = x;
		this.y = y;
		this.range = range;
		this.id = expID;
		expID++;
		
		expTimer = new Timer();
		expTimer.schedule(new TimerTask() {
			@Override
			public void run(){
				gameBoard.endExplosion();				
	    	}
		},expLife);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getRange() {
		return range;
	}
	
	@Override
	public int compareTo(Explosion other) {
		return id - other.id;
	}
	
}
