package com.ducksonflame.dynablaster;

import java.awt.Dimension;
import java.awt.Image;

import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JComponent;


/**
 * Class containing properties and logic for players.
 */
@SuppressWarnings("serial")
public class Character extends JComponent {
	
	//Constants
	private final int CHAR_WIDTH = Board.TILE_SIZE;
	private final int CHAR_HEIGHT = Board.TILE_SIZE;
	private final int MOVEMENT_ERROR_MARGIN = Board.MOVEMENT_ERROR_MARGIN;
	private final int playerNumber; // 1 - white, 2 - black
	private final Board gameBoard;
	
	//Strings of images locations for animation
	private final String avatarUp;
	private final String avatarUp1;
	private final String avatarUp2;
	private final String avatarDown;
	private final String avatarDown1;
	private final String avatarDown2;
	private final String avatarLeft;
	private final String avatarLeft1;
	private final String avatarRight;
	private final String avatarRight1;
		
	//Variables
	private Image currentAvatar;
	private int x; // coordinate x
	private int y; // coordinate y
	private int bombsMax = Board.INITIAL_MAX_BOMBS;
	private int expRange = Board.INITIAL_RANGE;
	private int charSpeed = Board.TILE_SIZE/50;
	private int charTimerSpeed = Board.INITIAL_CHAR_TIMER_SPEED;
	private int FRAME_DELAY = 5*charTimerSpeed+100;
	private int lastX; // used for not stopping in center tiles
	private int lastY; // used for not stopping in center tiles
	private Dimension currentTile = new Dimension();
	
	//Animation utils
	private int animCounterUp = 0;
	private int animCounterDown = 0;
	private int animCounterRight = 0;
	private int animCounterLeft = 0;
	private Timer timerDown;
	private Timer timerUp;
	private Timer timerRight;
	private Timer timerLeft;
	private boolean timerUpRunning;
	private boolean timerDownRunning;
	private boolean timerRightRunning;
	private boolean timerLeftRunning;

	private TreeSet<Bomb> bombs;
	private Iterator<Bomb> iter;
	
	/**
	 * Constructor sets player number, positions the player accordingly and loads appropriate image Strings.
	 * @param playerNo Player number (1 is white and 2 is black)
	 * @param gameBoard Main game reference
	 */
	public Character (int playerNo, Board gameBoard) {
		
		this.gameBoard = gameBoard;
		this.playerNumber = playerNo;
		bombs = new TreeSet<Bomb>();
				
		if (playerNumber == 1) {
			
			x = Board.GAP_WIDTH + Board.TILE_SIZE;
			y = Board.TILE_SIZE;
			
			currentAvatar = loadImage("images/avatarDown_"+CHAR_WIDTH+".png");
			avatarUp = "images/avatarUp_"+CHAR_WIDTH+".png";
			avatarUp1 = "images/avatarUp1_"+CHAR_WIDTH+".png";
			avatarUp2 = "images/avatarUp2_"+CHAR_WIDTH+".png";			
			avatarDown = "images/avatarDown_"+CHAR_WIDTH+".png";
			avatarDown1 = "images/avatarDown1_"+CHAR_WIDTH+".png";
			avatarDown2 = "images/avatarDown2_"+CHAR_WIDTH+".png";
			avatarLeft = "images/avatarLeft_"+CHAR_WIDTH+".png";
			avatarLeft1 = "images/avatarLeft1_"+CHAR_WIDTH+".png";
			avatarRight = "images/avatarRight_"+CHAR_WIDTH+".png";
			avatarRight1 = "images/avatarRight1_"+CHAR_WIDTH+".png";
						
		} else {
			
			x = Board.GAP_WIDTH + (2*(Board.NUMBER_OF_FREE_COLUMNS)-1)*Board.TILE_SIZE;
			y = ((2*Board.NUMBER_OF_FREE_ROWS)-1)*Board.TILE_SIZE;
			
			currentAvatar = loadImage("images/avatarUp_black_"+CHAR_WIDTH+".png");
			avatarUp = "images/avatarUp_black_"+CHAR_WIDTH+".png";
			avatarUp1 = "images/avatarUp1_black_"+CHAR_WIDTH+".png";
			avatarUp2 = "images/avatarUp2_black_"+CHAR_WIDTH+".png";			
			avatarDown = "images/avatarDown_black_"+CHAR_WIDTH+".png";
			avatarDown1 = "images/avatarDown1_black_"+CHAR_WIDTH+".png";
			avatarDown2 = "images/avatarDown2_black_"+CHAR_WIDTH+".png";
			avatarLeft = "images/avatarLeft_black_"+CHAR_WIDTH+".png";
			avatarLeft1 = "images/avatarLeft1_black_"+CHAR_WIDTH+".png";
			avatarRight = "images/avatarRight_black_"+CHAR_WIDTH+".png";
			avatarRight1 = "images/avatarRight1_black_"+CHAR_WIDTH+".png";
			
		}
		
		lastX = x;
		lastY = y;
	}
		
	/**
	 * Used for loading images.
	 * @param s File location/name
	 * @return Returns Image
	 */
	private Image loadImage (String s) {
		
		ImageIcon ii = new ImageIcon(s);
		return ii.getImage();
		
	}	
	
	/**
	 * Contains all logic for moving down (position updates), animation (image swapping) and collision detection.
	 * Handles powerUps if picked up.
	 */
	public void moveDown () {
		
		lastX = x;
		lastY = y;
		PowerUp[][] p = gameBoard.getPowerUps();
		
		if (p[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
			 [((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1] != null) {
			switch (p[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
					 [((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1].getType()) {
			case 1:
				rangeUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			case 2:
				speedUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			case 3:
				bombsUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			default:
				break;	
			}
		}
		
		if (playerNumber == 1 && gameBoard.getWRel() == false) {}
		
		else if (isNextDownBomb()) {
			animDown();
		}
		
		else if (playerNumber == 2 && gameBoard.getUpRel() == false) {}
		
		else if (this.y >= ((2*Board.NUMBER_OF_FREE_ROWS-1)*Board.TILE_SIZE)) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animDown();
				}
				else {
					stopTimerDown();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animDown();
				}
				else {
					stopTimerDown();
				}
			}
		} 
		
		else if ((((2*Board.NUMBER_OF_FREE_ROWS-1)*Board.TILE_SIZE) - this.y) <= charSpeed) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animDown();
					this.y = (2*Board.NUMBER_OF_FREE_ROWS-1)*Board.TILE_SIZE;
				}
				else {
					stopTimerDown();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animDown();
					this.y = (2*Board.NUMBER_OF_FREE_ROWS-1)*Board.TILE_SIZE;
				}
				else {
					stopTimerDown();
				}
			}
		}
		
		else if (((this.y + charSpeed) >= getCurrentTile().getHeight()) && 
				((gameBoard.dW.getPositions()
						[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
						[((int)getCurrentTile().getHeight()/Board.TILE_SIZE)]) != null)) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animDown();
					this.y = (int)getCurrentTile().getHeight();
				}
				else {
					stopTimerDown();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animDown();
					this.y = (int)getCurrentTile().getHeight();
				}
				else {
					stopTimerDown();
				}
			}
		}
		
		else if (((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) == Board.TILE_SIZE) {
			if (playerNumber == 1) {
				if ((gameBoard.getARel() == false || gameBoard.getDRel() == false) && y > lastY) {
					stopTimerDown();
				}
				else {
					animDown();
					this.y += charSpeed;					
				}
			}
			if (playerNumber == 2) {
				if ((gameBoard.getRightRel() == false || gameBoard.getLeftRel() == false) && y > lastY) {
					stopTimerDown();
				}
				else {
					animDown();
					this.y += charSpeed;					
				}
			}
		}
		
		else if (((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) > Board.TILE_SIZE && ((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) <= (Board.TILE_SIZE + MOVEMENT_ERROR_MARGIN)) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animDown();
					this.x -= charSpeed;
				}
				else {
					stopTimerDown();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animDown();
					this.x -= charSpeed;
				}
				else {
					stopTimerDown();
				}
			}
		}
		
		else if (((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) >= (Board.TILE_SIZE - MOVEMENT_ERROR_MARGIN) && ((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) < Board.TILE_SIZE) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animDown();
					this.x += charSpeed;
				}
				else {
					stopTimerDown();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animDown();
					this.x += charSpeed;
				}
				else {
					stopTimerDown();
				}
			}
		}
		
	}
	
	/**
	 * Contains all logic for moving up (position updates), animation (image swapping) and collision detection.
	 * Handles powerUps if picked up.
	 */
	public void moveUp () {
		
		lastX = x;
		lastY = y;
		PowerUp[][] p = gameBoard.getPowerUps();
		
		if (p[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
			 [((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1] != null) {
			switch (p[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
					 [((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1].getType()) {
			case 1:
				rangeUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			case 2:
				speedUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			case 3:
				bombsUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			default:
				break;	
			}
		}
		
		if (playerNumber == 1 && gameBoard.getSRel() == false) {}
		
		else if (isNextUpBomb()) {
			animUp();
		}
		
		else if (playerNumber == 2 && gameBoard.getDownRel() == false) {}
		
		else if (this.y <= Board.TILE_SIZE) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animUp();
				}
				else {
					stopTimerUp();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animUp();
				}
				else {
					stopTimerUp();
				}
			}
		}
		
		else if ((this.y - Board.TILE_SIZE) <= charSpeed) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animUp();
					this.y = Board.TILE_SIZE;
				}
				else {
					stopTimerUp();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animUp();
					this.y = Board.TILE_SIZE;
				}
				else {
					stopTimerUp();
				}
			}
		}
		
		else if (((this.y - charSpeed) <= getCurrentTile().getHeight()) && 
				((gameBoard.dW.getPositions()
						[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
						[((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-2]) != null)) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animUp();
					this.y = (int)getCurrentTile().getHeight();
				}
				else {
					stopTimerUp();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animUp();
					this.y = (int)getCurrentTile().getHeight();
				}
				else {
					stopTimerUp();
				}
			}
		}
		
		else if (((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) == Board.TILE_SIZE) {
			if (playerNumber == 1) {
				if ((gameBoard.getARel() == false || gameBoard.getDRel() == false) && y < lastY) {
					stopTimerUp();
				}
				else {
					animUp();
					this.y -= charSpeed;					
				}
			}
			if (playerNumber == 2) {
				if ((gameBoard.getRightRel() == false || gameBoard.getLeftRel() == false) && y < lastY) {
					stopTimerUp();
				}
				else {
					animUp();
					this.y -= charSpeed;				
				}
			}
		}
		
		else if (((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) > Board.TILE_SIZE && ((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) <= (Board.TILE_SIZE + MOVEMENT_ERROR_MARGIN)) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animUp();
					this.x -= charSpeed;
				}
				else {
					stopTimerUp();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animUp();
					this.x -= charSpeed;
				}
				else {
					stopTimerUp();
				}
			}			
		}
		
		else if (((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) >= (Board.TILE_SIZE - MOVEMENT_ERROR_MARGIN) && ((this.x - Board.GAP_WIDTH) % (2*Board.TILE_SIZE)) < Board.TILE_SIZE) {
			if (playerNumber == 1) {
				if (gameBoard.getDRel() == true && gameBoard.getARel() == true) {
					animUp();
					this.x += charSpeed;
				}
				else {
					stopTimerUp();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getRightRel() == true && gameBoard.getLeftRel() == true) {
					animUp();
					this.x += charSpeed;
				}
				else {
					stopTimerUp();
				}
			}
		}
		
	}
	
	/**
	 * Contains all logic for moving right (position updates), animation (image swapping) and collision detection.
	 * Handles powerUps if picked up.
	 */
	public void moveRight () {
		
		lastX = x;
		lastY = y;
		PowerUp[][] p = gameBoard.getPowerUps();
		
		if (p[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
			 [((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1] != null) {
			switch (p[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
					 [((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1].getType()) {
			case 1:
				rangeUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			case 2:
				speedUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			case 3:
				bombsUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			default:
				break;	
			}
		}
		
		if (this.x >= (((2*Board.NUMBER_OF_FREE_COLUMNS-1)*Board.TILE_SIZE) + Board.GAP_WIDTH)) {
			if (playerNumber == 1) {
				if (gameBoard.getWRel() == true && gameBoard.getSRel() == true) {
					animRight();
				}
				else {
					stopTimerRight();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getUpRel() == true && gameBoard.getDownRel() == true) {
					animRight();
				}
				else {
					stopTimerRight();
				}
			}
		} 
		
		else if (isNextRightBomb()) {
			animRight();
		}
		
		else if ((((2*Board.NUMBER_OF_FREE_COLUMNS-1)*Board.TILE_SIZE + Board.GAP_WIDTH) - this.x) <= charSpeed) {
			this.x = (2*Board.NUMBER_OF_FREE_COLUMNS-1)*Board.TILE_SIZE + Board.GAP_WIDTH;
		}
		
		else if (((this.x + charSpeed) >= getCurrentTile().getWidth()) && 
				((gameBoard.dW.getPositions()
						[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)]
						[((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1]) != null)) {
			if (playerNumber == 1) {
				if (gameBoard.getWRel() == true && gameBoard.getSRel() == true) {
					animRight();
					this.x = (int)getCurrentTile().getWidth();
				}
				else {
					stopTimerRight();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getUpRel() == true && gameBoard.getDownRel() == true) {
					animRight();
					this.x = (int)getCurrentTile().getWidth();
				}
				else {
					stopTimerRight();
				}
			}			
		}
		
		else if (((this.y) % (2*Board.TILE_SIZE)) == Board.TILE_SIZE) {
			if (playerNumber == 1) {
				if ((gameBoard.getWRel() == false || gameBoard.getSRel() == false) && x > lastX) {
					stopTimerRight();
				}
				else {
					animRight();
					this.x += charSpeed;				
				}
			}
			if (playerNumber == 2) {
				if ((gameBoard.getUpRel() == false || gameBoard.getDownRel() == false) && x > lastX) {
					stopTimerRight();
				}
				else {
					animRight();
					this.x += charSpeed;				
				}
			}
		}
		
		else if (((this.y) % (2*Board.TILE_SIZE)) > Board.TILE_SIZE && ((this.y) % (2*Board.TILE_SIZE)) <= (Board.TILE_SIZE + MOVEMENT_ERROR_MARGIN)) {
			if (playerNumber == 1) {
				if (gameBoard.getWRel() == true && gameBoard.getSRel() == true) {
					animRight();
					this.y -= charSpeed;
				}
				else {
					stopTimerRight();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getUpRel() == true && gameBoard.getDownRel() == true) {
					animRight();
					this.y -= charSpeed;
				}
				else {
					stopTimerRight();
				}
			}
		}
		
		else if (((this.y) % (2*Board.TILE_SIZE)) >= (Board.TILE_SIZE - MOVEMENT_ERROR_MARGIN) && ((this.y) % (2*Board.TILE_SIZE)) < Board.TILE_SIZE) {
			if (playerNumber == 1) {
				if (gameBoard.getWRel() == true && gameBoard.getSRel() == true) {
					animRight();
					this.y += charSpeed;
				}
				else {
					stopTimerRight();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getUpRel() == true && gameBoard.getDownRel() == true) {
					animRight();
					this.y += charSpeed;
				}
				else {
					stopTimerRight();
				}
			}
		} 
	}
	
	/**
	 * Contains all logic for moving left (position updates), animation (image swapping) and collision detection.
	 * Handles powerUps if picked up.
	 */
	public void moveLeft () {
		
		lastX = x;
		lastY = y;
		PowerUp[][] p = gameBoard.getPowerUps();
		
		if (p[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
			 [((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1] != null) {
			switch (p[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1]
					 [((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1].getType()) {
			case 1:
				rangeUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			case 2:
				speedUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			case 3:
				bombsUp();
				gameBoard.powerUpPickUp((((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-1, ((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1);
				break;
			default:
				break;	
			}
		}
		
		if (this.x <= (Board.GAP_WIDTH + Board.TILE_SIZE)) {
			if (playerNumber == 1) {
				if (gameBoard.getWRel() == true && gameBoard.getSRel() == true) {
					animLeft();
				}
				else {
					stopTimerLeft();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getUpRel() == true && gameBoard.getDownRel() == true) {
					animLeft();
				}
				else {
					stopTimerLeft();
				}
			}
		} 
		
		else if (isNextLeftBomb()) {
			animLeft();
		}
		
		else if ((this.x - (Board.GAP_WIDTH + Board.TILE_SIZE)) <= charSpeed) {			
			this.x = Board.GAP_WIDTH + Board.TILE_SIZE;
		}
		
		else if (((this.x - charSpeed) <= getCurrentTile().getWidth()) && 
				((gameBoard.dW.getPositions()
						[(((int)getCurrentTile().getWidth()-Board.GAP_WIDTH)/Board.TILE_SIZE)-2]
						[((int)getCurrentTile().getHeight()/Board.TILE_SIZE)-1]) != null)) {
			if (playerNumber == 1) {
				if (gameBoard.getWRel() == true && gameBoard.getSRel() == true) {
					animLeft();
					this.x = (int)getCurrentTile().getWidth();
				}
				else {
					stopTimerLeft();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getUpRel() == true && gameBoard.getDownRel() == true) {
					animLeft();
					this.x = (int)getCurrentTile().getWidth();
				}
				else {
					stopTimerLeft();
				}
			}
		}
		
		else if (((this.y) % (2*Board.TILE_SIZE)) == Board.TILE_SIZE) {
			if (playerNumber == 1) {
				if ((gameBoard.getWRel() == false || gameBoard.getSRel() == false) && x < lastX) {
					stopTimerLeft();
				}
				else {
					animLeft();
					this.x -= charSpeed;			
				}
			}
			if (playerNumber == 2) {
				if ((gameBoard.getUpRel() == false || gameBoard.getDownRel() == false) && x < lastX) {
					stopTimerLeft();
				}
				else {
					animLeft();
					this.x -= charSpeed;			
				}
			}
		}
		
		else if (((this.y) % (2*Board.TILE_SIZE)) > Board.TILE_SIZE && ((this.y) % (2*Board.TILE_SIZE)) <= (Board.TILE_SIZE + MOVEMENT_ERROR_MARGIN)) {
			if (playerNumber == 1) {
				if (gameBoard.getWRel() == true && gameBoard.getSRel() == true) {
					animLeft();
					this.y -= charSpeed;
				}
				else {
					stopTimerLeft();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getUpRel() == true && gameBoard.getDownRel() == true) {
					animLeft();
					this.y -= charSpeed;
				}
				else {
					stopTimerLeft();
				}
			}			
		}
		
		else if (((this.y) % (2*Board.TILE_SIZE)) >= (Board.TILE_SIZE - MOVEMENT_ERROR_MARGIN) && ((this.y) % (2*Board.TILE_SIZE)) < Board.TILE_SIZE) {
			if (playerNumber == 1) {
				if (gameBoard.getWRel() == true && gameBoard.getSRel() == true) {
					animLeft();
					this.y += charSpeed;
				}
				else {
					stopTimerLeft();
				}
			}
			else if (playerNumber == 2) {
				if (gameBoard.getUpRel() == true && gameBoard.getDownRel() == true) {
					animLeft();
					this.y += charSpeed;
				}
				else {
					stopTimerLeft();
				}
			}
		} 
	}
	
	/**
	 * Handles animation when running up. Scales with quicker movement (after powerUp pickup).
	 */
	public void animUp() {
		if (timerUpRunning == false) {
			
			timerUp = new Timer();
			timerUp.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run(){
					if (animCounterUp == 0) {
						currentAvatar = loadImage(avatarUp);
						animCounterUp++;
					}
					else if (animCounterUp == 1) {
						currentAvatar = loadImage(avatarUp1);
						animCounterUp++;
					}
					else if (animCounterUp == 2) {
						currentAvatar = loadImage(avatarUp);
						animCounterUp++;
					}
					else if (animCounterUp == 3) {
						currentAvatar = loadImage(avatarUp2);
						animCounterUp = 0;
					}
		    	}
			},0,FRAME_DELAY);
			
			timerUpRunning = true;
		}
	}
	
	/**
	 * Handles animation when running down. Scales with quicker movement (after powerUp pickup).
	 */
	public void animDown() {
		if (timerDownRunning == false) {
			
			timerDown = new Timer();
			timerDown.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run(){
					if (animCounterDown == 0) {
						currentAvatar = loadImage(avatarDown);
						animCounterDown++;
					}
					else if (animCounterDown == 1) {
						currentAvatar = loadImage(avatarDown1);
						animCounterDown++;
					}
					else if (animCounterDown == 2) {
						currentAvatar = loadImage(avatarDown);
						animCounterDown++;
					}
					else if (animCounterDown == 3) {
						currentAvatar = loadImage(avatarDown2);
						animCounterDown = 0;
					}
		    	}
			},0,FRAME_DELAY);
			
			timerDownRunning = true;
		}
	}
	
	/**
	 * Handles animation when running right. Scales with quicker movement (after powerUp pickup).
	 */
	public void animRight() {
		if (timerRightRunning == false) {
			
			timerRight = new Timer();
			timerRight.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run(){
					if (animCounterRight == 0) {
						currentAvatar = loadImage(avatarRight);
						animCounterRight++;
					}
					else if (animCounterRight == 1) {
						currentAvatar = loadImage(avatarRight1);
						animCounterRight = 0;
					}
		    	}
			},0,FRAME_DELAY);
			
			timerRightRunning = true;
		}
	}
	
	/**
	 * Handles animation when running left. Scales with quicker movement (after powerUp pickup).
	 */
	public void animLeft() {
		if (timerLeftRunning == false) {
			
			timerLeft = new Timer();
			timerLeft.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run(){
					if (animCounterLeft == 0) {
						currentAvatar = loadImage(avatarLeft);
						animCounterLeft++;
					}
					else if (animCounterLeft == 1) {
						currentAvatar = loadImage(avatarLeft1);
						animCounterLeft = 0;
					}
		    	}
			},0,FRAME_DELAY);
			
			timerLeftRunning = true;
		}
	}
	
	/**
	 * Calculates current tile.
	 * @return Returns current tile (Dimension).
	 */
	public Dimension getCurrentTile () {
		int tempX;
		int tempY;
		
		if (((this.y - Board.TILE_SIZE) % Board.TILE_SIZE) <= (CHAR_HEIGHT/2)) {
			tempY = this.y - ((this.y - Board.TILE_SIZE) % Board.TILE_SIZE);
		} 
		else {
			tempY = this.y - ((this.y - Board.TILE_SIZE) % Board.TILE_SIZE) + Board.TILE_SIZE;
		}
		
		if (((this.x - (Board.TILE_SIZE+Board.GAP_WIDTH)) % Board.TILE_SIZE) <= (CHAR_WIDTH/2)) {
			tempX = this.x - ((this.x - (Board.TILE_SIZE+Board.GAP_WIDTH)) % Board.TILE_SIZE);
		} 
		else {
			tempX = this.x - ((this.x - (Board.TILE_SIZE+Board.GAP_WIDTH)) % Board.TILE_SIZE) + Board.TILE_SIZE;
		}
		
		currentTile.setSize(tempX, tempY);
		return currentTile;
	}
		
	/**
	 * Places bomb on current tile. Checks whether the tile is valid.
	 */
	public void placeBomb () {
		if (bombs.size() < bombsMax) {
			if (bombs.size() == 0){
				bombs.add(new Bomb((int)getCurrentTile().getWidth(), (int)getCurrentTile().getHeight(), this));
			}
			else {
				boolean valid = true;
				
				for (Iterator<Bomb> i = bombs.iterator(); i.hasNext(); ) {
					Bomb b = i.next();
					if (b != null && getCurrentTile().getWidth() == b.getX() && getCurrentTile().getHeight() == b.getY()) {
						valid = false;
					}
				}
				if (valid == true) {						
					bombs.add(new Bomb((int)getCurrentTile().getWidth(), (int)getCurrentTile().getHeight(), this));
				}
			}
		}
	}
	
	/**
	 * Calls newExplosion from the gameBoard and removes the exploding bomb.
	 * @param b Reference to exploding bomb.
	 */
	public void bombExploded (Bomb b) {
		gameBoard.newExplosion(b.getX(), b.getY(), this.expRange);
		
		iter = bombs.iterator();
		if (iter.hasNext()) {
				iter.next();
				iter.remove();
		}
	}
	
	/**
	 * Stops animation (moving up) and sets the correct avatar if not moving in any other direction.
	 */
	public void stopTimerUp() {
		if (timerUpRunning) {
			timerUp.cancel();
			timerUpRunning = false;
		}
		if (!timerDownRunning && !timerRightRunning && !timerLeftRunning) {
			currentAvatar = loadImage(avatarUp);
		}
	}
	
	/**
	 * Stops animation (moving down) and sets the correct avatar if not moving in any other direction.
	 */
	public void stopTimerDown() {
		if (timerDownRunning) {
			timerDown.cancel();
			timerDownRunning = false;			
		}
		if (!timerUpRunning && !timerRightRunning && !timerLeftRunning) {
			currentAvatar = loadImage(avatarDown);			
		}	
	}
	
	/**
	 * Stops animation (moving right) and sets the correct avatar if not moving in any other direction.
	 */
	public void stopTimerRight () {
		if (timerRightRunning) {
			timerRight.cancel();
			timerRightRunning = false;
		}
		if (!timerDownRunning && !timerUpRunning && !timerLeftRunning) {
			currentAvatar = loadImage(avatarRight);			
		}	
	}
	
	/**
	 * Stops animation (moving left) and sets the correct avatar if not moving in any other direction.
	 */
	public void stopTimerLeft () {
		if (timerLeftRunning) {
			timerLeft.cancel();
			timerLeftRunning = false;
		}
		if (!timerDownRunning && !timerUpRunning && !timerRightRunning) {
			currentAvatar = loadImage(avatarLeft);			
		}
	}
	
	/**
	 * Kills the player and passes the player number to the main game board.
	 */
	public void kill() {
		gameBoard.stopTimer();
		gameBoard.endGame(playerNumber);
	}
	
	/**
	 * Checks whether the next tile down contains a bomb. Both bomb sets are evaluated.
	 * @return Returns true if there is a bomb in the next tile down.
	 */
	private boolean isNextDownBomb () {
		for (Bomb b: gameBoard.p1.getBombs()) {
			if (getX() == b.getX() && getY()+Board.TILE_SIZE+charSpeed >= b.getY() && getY()+Board.TILE_SIZE+charSpeed < b.getY()+Board.TILE_SIZE && getCurrentTile().getHeight() != b.getY()) {
				return true;
			}			
		}
		
		for (Bomb b: gameBoard.p2.getBombs()) {
			if (getX() == b.getX() && getY()+Board.TILE_SIZE+charSpeed >= b.getY() && getY()+Board.TILE_SIZE+charSpeed < b.getY()+Board.TILE_SIZE && getCurrentTile().getHeight() != b.getY()) {
				return true;
			}			
		}
		
		return false;
	}
	
	/**
	 * Checks whether the next tile up contains a bomb. Both bomb sets are evaluated.
	 * @return Returns true if there is a bomb in the next tile up.
	 */
	private boolean isNextUpBomb () {
		for (Bomb b: gameBoard.p1.getBombs()) {
			if (getX() == b.getX() && getY()-Board.TILE_SIZE-charSpeed <= b.getY() && getY()-Board.TILE_SIZE-charSpeed > b.getY()-Board.TILE_SIZE && getCurrentTile().getHeight() != b.getY()) {
				return true;
			}			
		}
		
		for (Bomb b: gameBoard.p2.getBombs()) {
			if (getX() == b.getX() && getY()-Board.TILE_SIZE-charSpeed <= b.getY() && getY()-Board.TILE_SIZE-charSpeed > b.getY()-Board.TILE_SIZE && getCurrentTile().getHeight() != b.getY()) {
				return true;
			}			
		}
		
		return false;
	}
	
	/**
	 * Checks whether the next tile right contains a bomb. Both bomb sets are evaluated.
	 * @return Returns true if there is a bomb in the next tile right.
	 */
	private boolean isNextRightBomb () {
		for (Bomb b: gameBoard.p1.getBombs()) {
			if (getX()+Board.TILE_SIZE+charSpeed >= b.getX() && getX()+Board.TILE_SIZE+charSpeed < b.getX()+Board.TILE_SIZE && getY() == b.getY() && getCurrentTile().getWidth() != b.getX()) {
				return true;
			}			
		}
		
		for (Bomb b: gameBoard.p2.getBombs()) {
			if (getX()+Board.TILE_SIZE+charSpeed >= b.getX() && getX()+Board.TILE_SIZE+charSpeed < b.getX()+Board.TILE_SIZE && getY() == b.getY() && getCurrentTile().getWidth() != b.getX()) {
				return true;
			}			
		}
		
		return false;
	}
	
	/**
	 * Checks whether the next tile left contains a bomb. Both bomb sets are evaluated.
	 * @return Returns true if there is a bomb in the next tile left.
	 */
	private boolean isNextLeftBomb () {
		for (Bomb b: gameBoard.p1.getBombs()) {
			if (getX()-Board.TILE_SIZE-charSpeed <= b.getX() && getX()-Board.TILE_SIZE-charSpeed > b.getX()-Board.TILE_SIZE && getY() == b.getY() && getCurrentTile().getWidth() != b.getX()) {
				return true;
			}			
		}
		
		for (Bomb b: gameBoard.p2.getBombs()) {
			if (getX()-Board.TILE_SIZE-charSpeed <= b.getX() && getX()-Board.TILE_SIZE-charSpeed > b.getX()-Board.TILE_SIZE && getY() == b.getY() && getCurrentTile().getWidth() != b.getX()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Increases maximum bomb limit (powerUp pickup).
	 */
	private void bombsUp() {
		this.bombsMax++;
	}
	
	/**
	 * Increases range on future bombs placed (powerUp pickup).
	 */
	private void rangeUp() {
		this.expRange++;
	}
	
	/**
	 * Increases speed of the player (actually uses higher refresh rate for movement). 
	 * Progression is non-linear, capped at 5 powerUp pickups (powerUp pickup).
	 */
	private void speedUp() {
		if (this.charTimerSpeed > 3) {
			this.charTimerSpeed--;			
		}
	}
	
	public int getCharTimerSpeed() {
		return charTimerSpeed;
	}
	
	public TreeSet<Bomb> getBombs() {
		return (TreeSet<Bomb>)bombs;
	}

	public Image getCurrentAvatar () {
		return currentAvatar;
	}
	
	public int getPlayerNumber () {
		return playerNumber;
	}
	
	public int getX () {
		return x;
	}
	
	public int getY () {
		return y;
	}
	
}