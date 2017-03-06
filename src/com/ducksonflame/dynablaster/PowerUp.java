package com.ducksonflame.dynablaster;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;


/**
 * Class containing logic for creating Power Ups.
 * Modifiable settings contain chances for different powerUps.
 * <br>Chance for bombsUp is calculated as 1 - (rangeChance + speedChance)
 * <br><br>
 * PowerUp knows its type and allows character to get it via a getter method. 
 * Player interaction implemented in the Player class.
 */
public class PowerUp {
		
	//Commented out Images for future implementation of burning animation
	private Image bombsUp = new ImageIcon("images/bombsUp_"+Board.TILE_SIZE+".png").getImage();
	private Image bombsUp1 = new ImageIcon("images/bombsUp_1_"+Board.TILE_SIZE+".png").getImage();
//	private Image bombsUpBurn = new ImageIcon("images/bombsUp_"+Board.TILE_SIZE+".png").getImage();
//	private Image bombsUpBurn1 = new ImageIcon("images/bombsUp_"+Board.TILE_SIZE+".png").getImage();
	private Image rangeUp = new ImageIcon("images/rangeUp_"+Board.TILE_SIZE+".png").getImage();
	private Image rangeUp1 = new ImageIcon("images/rangeUp_1_"+Board.TILE_SIZE+".png").getImage();
//	private Image rangeUpBurn = new ImageIcon("images/rangeUp_"+Board.TILE_SIZE+".png").getImage();
//	private Image rangeUpBurn1 = new ImageIcon("images/rangeUp_"+Board.TILE_SIZE+".png").getImage();
	private Image speedUp = new ImageIcon("images/speedUp_1_"+Board.TILE_SIZE+".png").getImage();
	private Image speedUp1 = new ImageIcon("images/speedUp_"+Board.TILE_SIZE+".png").getImage();
//	private Image speedUpBurn = new ImageIcon("images/speedUp_"+Board.TILE_SIZE+".png").getImage();
//	private Image speedUpBurn1 = new ImageIcon("images/speedUp_"+Board.TILE_SIZE+".png").getImage();
	
	private Board gameBoard;
	private Image currentImg;
	private Image img;
	private Image img1;
	private Image burnImg;
	private Image burnImg1;
	private boolean timersRunning = false;
	private int x;
	private int y;
	private int i;
	private int j;
	private int type; // 1 - range, 2 - speed, 3 - bombs
	
	//Modify these chances to get uneven distribution of powerUps. Chance for bombsUp is calculated as 1 - (rangeChance + speedChance)
	private double rangeChance = 0.33;
	private double speedChance = 0.33;
	
	/**
	 * Sets properties, including its type. Invokes timer for animation.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param i Index i
	 * @param j Index j
	 * @param gameBoard Main game reference
	 */
	public PowerUp (int x, int y, int i, int j, Board gameBoard) {
		
		this.x = x;
		this.y = y;
		this.i = i;
		this.j = j;
		this.gameBoard = gameBoard;
			
		double rand = Math.random();

		if (rand <= rangeChance) {
			img = rangeUp;
			img1 = rangeUp1;
			type = 1;
		}
		else if (rand <= speedChance + rangeChance) {
			img = speedUp;
			img1 = speedUp1;
			type = 2;
		}
		else {
			img = bombsUp;
			img1 = bombsUp1;
			type = 3;
		}
		
		Timer imgTimer = new Timer();
		imgTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run(){
				imgChange();
	    	}
		}, 0, 450);
		
	}
	
	/**
	 * Burns the PowerUp if hit by explosion. Contains logic for future burn animation, if implemented.
	 */
	public void burn () {
		
		if (timersRunning == false) {
			timersRunning = true;
			Timer burnImgTimer = new Timer();
			burnImgTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run(){
					burnImgChange();
				}
			}, 0, Explosion.expLife/2);
			
			Timer destrTimer = new Timer();
			destrTimer.schedule(new TimerTask() {
				@Override
				public void run(){
					gameBoard.powerUpPickUp(i, j);
				}
			},Explosion.expLife);			
		}
	}
	
	/**
	 * Contains logic for animation (flashing effect).
	 */
	private void imgChange () {
		if (this.currentImg == img) {
			this.currentImg = img1;
		}
		else {
			this.currentImg = img;
		}
	}

	/**
	 * Logic for burning animation.
	 */
	private void burnImgChange () {
		if (this.currentImg == img) { //Change to burnImg1 for implementation
			this.currentImg = burnImg;
		}
		else if (this.currentImg == burnImg) {
			this.currentImg = burnImg1;
		}
	}
	
	public Image getCurrentImg() {
		return currentImg;
	}
	
	public int getType() {
		return type;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
}
