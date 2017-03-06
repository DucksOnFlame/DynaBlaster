package com.ducksonflame.dynablaster;
import java.awt.Dimension;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Class for destructible walls (destructible by bomb explosions).
 */

@SuppressWarnings("serial")
public class DestrWalls extends JComponent {
		
	private final Image img = new ImageIcon("images/destrWalls_"+Board.TILE_SIZE+".png").getImage();
	private final Image imgDestr = new ImageIcon("images/destrWalls_1_"+Board.TILE_SIZE+".png").getImage();
	private final Image imgDestr1 = new ImageIcon("images/destrWalls_2_"+Board.TILE_SIZE+".png").getImage();
	
	//Walls positions
	private Dimension[][] positions = new Dimension[(2*Board.NUMBER_OF_FREE_COLUMNS)][(2*Board.NUMBER_OF_FREE_ROWS)];
	
	//Separated img positions in order to facilitate animations per each wall
	private Image[][] imgPositions = new Image[(2*Board.NUMBER_OF_FREE_COLUMNS)][(2*Board.NUMBER_OF_FREE_ROWS)];
	
	//Booleans for animations and for preventing repeated calls to setPositionNull method corrupting logic
	private boolean[][] positionsTimerRunning = new boolean[(2*Board.NUMBER_OF_FREE_COLUMNS)][(2*Board.NUMBER_OF_FREE_ROWS)];
	private boolean[][] imgPositionsTimerRunning = new boolean[(2*Board.NUMBER_OF_FREE_COLUMNS)][(2*Board.NUMBER_OF_FREE_ROWS)];
	
	private final double POWER_UP_RATE = Board.POWER_UP_RATE;
	private Board gameBoard;
	
	/**
	 * Assigns eligible locations. Excludes tiles adjacent to players' starting positions to allow for proper game start.
	 * @param b Reference to main game
	 */
	public DestrWalls (Board b) {
		
		gameBoard = b;
		
		for (int i = 0; i < positions.length-1; i++) {
			for (int j = 0; j < positions[i].length-1; j++) {
				
				double rand = Math.random();
				
				if ((rand > Board.SPAWN_RATE) || (((i % 2) == 1) && ((j % 2) == 1))) { 
					positions[i][j] = null;
					imgPositions[i][j] = null;
				}
				
				else if ((i == 0 || i == 1) && (j == 0 || j == 1)) {
					positions[i][j] = null;
					imgPositions[i][j] = null;
				}
				
				else if ((i == (positions.length-2) || i == (positions.length-3)) && (j == (positions[i].length-2) || j == (positions[i].length-3))) {
					positions[i][j] = null;
					imgPositions[i][j] = null;
				}
				
				else {
					positions[i][j] = new Dimension((i*Board.TILE_SIZE)+Board.TILE_SIZE+Board.GAP_WIDTH, (j*Board.TILE_SIZE)+Board.TILE_SIZE);
					imgPositions[i][j] = img;
					positionsTimerRunning[i][j] = false;
					imgPositionsTimerRunning[i][j] = false;
				}
			}
		}
	}
	
	public Image getImg(int i, int j) {
		return imgPositions[i][j];
	}
	
	public Dimension[][] getPositions() {
		return positions;
	}
	
	/**
	 * Handles animation and destruction of the wall after getting hit by an explosion. Speed scales with explosion's speed.
	 * @param i Index i
	 * @param j Index j
	 */
	public void setPositionNull (int i, int j) {
		
		double pwrUpRandom = Math.random();
		
		if (imgPositionsTimerRunning[i][j] == false) {
			imgPositionsTimerRunning[i][j] = true;
			Timer imgTimer = new Timer();
			imgTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run(){
					changeImg(i, j);
				}
			}, 0, Explosion.expLife/2);			
		}
		
		if (!positionsTimerRunning[i][j]) {
			positionsTimerRunning[i][j] = true;
			Timer destrTimer = new Timer();
			destrTimer.schedule(new TimerTask() {
				@Override
				public void run(){
					positions[i][j] = null;
					positionsTimerRunning[i][j] = false;
					imgPositionsTimerRunning[i][j] = false;
					if (pwrUpRandom <= POWER_UP_RATE) {
						gameBoard.newPowerUp(i, j, (i*Board.TILE_SIZE)+Board.TILE_SIZE+Board.GAP_WIDTH, (j*Board.TILE_SIZE)+Board.TILE_SIZE);
					}
		    	}
			},Explosion.expLife);
		}
	}
	
	/**
	 * Animation logic.
	 * @param i Index i
	 * @param j Index j
	 */
	private void changeImg (int i, int j) {
		if (imgPositions[i][j] == img) {
			imgPositions[i][j] = imgDestr;
		}
		else if (imgPositions[i][j] == imgDestr) {
			imgPositions[i][j] = imgDestr1;
		}
	}

}
