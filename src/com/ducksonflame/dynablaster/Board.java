package com.ducksonflame.dynablaster;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.util.*;

/**
 * Main game JPanel - core of the game.
 * Available TILE_SIZE's: 50, 100 - in order to add support for other sizes, add appropriate image sizes (only even TILE.SIZE's).
 * The Board contains several finals that can be modified (currently only via source code modifications) in order to mod the game settings.
 * <br><br>Settings that can be modified:
 * <br><br>MOVEMENT_ERROR_MARGIN - Tolerance for player movement collision. Allows for non-pixelperfect movement. (default is 50).
 * <br>TILE_SIZE - Size of a Tile in pixels. (for 1080p screens and below suggested is 50, for higher res screens - 100).
 * <br>NUMBER_OF_FREE_COLUMNS - Number of columns (default is 8). 
 * <br>NUMBER_OF_FREE_ROWS - Number of rows (default is 6). 
 * <br>GAP_WIDTH - Size of the gap between left/right edges and the outside walls. Only cosmetics. (Default and recommended is TILE_SIZE)
 * <br>INITIAL_MAX_BOMBS - Initial maximum number of bombs that can be placed by a player (default is 1).
 * <br>INITIAL_RANGE - Initial range of explosions for players (default is 2).
 * <br>INITIAL_CHAR_TIMER_SPEED - Initial interval for updating player's position. The lower the higher speed. Intended, non-linear progression.
 * <br>SPAWN_RATE - Dictates how many Destructible Walls are created (default and suggested is 0.9). 1 means all eligible tiles are filled (except for 2 adjecent to both players' position).
 * <br>POWER_UP_RATE - Sets rate at which powerUps are created after destroying walls (default is 0.2).
 */

@SuppressWarnings("serial")
public class Board extends JPanel implements KeyListener {
	
	//Modifiable settings
	public static final int MOVEMENT_ERROR_MARGIN = 50;
	public static final int TILE_SIZE = 100; 
	public static final int NUMBER_OF_FREE_COLUMNS = 8;
	public static final int NUMBER_OF_FREE_ROWS = 6;
	public static final int GAP_WIDTH = TILE_SIZE;
	public static final int INITIAL_MAX_BOMBS = 4;
	public static final int INITIAL_RANGE = 2;
	public static final int INITIAL_CHAR_TIMER_SPEED = 8;
	public static final double SPAWN_RATE = 0;
	public static final double POWER_UP_RATE = 0.2;
	
	//Characters
	public Character p1 = new Character(1, this);
	public Character p2 = new Character(2, this);
	
	//JFrame reference
	private MyFrame myFrame;
	
	//Used for drawing pillars
	private Dimension[][] positionsTable = new Dimension[NUMBER_OF_FREE_COLUMNS-1][NUMBER_OF_FREE_ROWS-1];
	
	private Timer timer;

	//Explosion set, Destructible walls and PowerUps
	private TreeSet<Explosion> explosions = new TreeSet<Explosion>(); //Probably should change to a list instead...
	private Iterator<Explosion> iterator;
	public DestrWalls dW = new DestrWalls(this);
	private PowerUp[][] powerUps = new PowerUp[(2*Board.NUMBER_OF_FREE_COLUMNS)-1][(2*Board.NUMBER_OF_FREE_ROWS)-1];
	
	//Keybinds timers and booleans
	private Timer wTimer;
	private Timer sTimer;
	private Timer aTimer;
	private Timer dTimer;
	private Timer gTimer;
	private Timer upTimer;
	private Timer downTimer;
	private Timer rightTimer;
	private Timer leftTimer;
	private Timer enterTimer;
	
	private boolean wRel = true;
	private boolean sRel = true;
	private boolean aRel = true;
	private boolean dRel = true;
	private boolean gRel = true;
	private boolean upRel = true;
	private boolean downRel = true;
	private boolean rightRel = true;
	private boolean leftRel = true;
	private boolean enterRel = true;
	
	
		public Board(MyFrame jF){
			initUI(jF);
		}
		
		/**
		 * @param jF takes parent JFrame reference
		 * Initializing the UI
		 * Adding the KeyListener
		 * Adding Dimensions (it probably should not be the Dimension class I know...) for DestrWalls to use
		 * Setting the refresh rate to 60fps and 60Hz check rate for explosions (e.g. if player walks into one)
		 * Initializing the Explosions Set
		 * (The size of this JPanel automatically scales to the chosen TILE_SIZE and columns/rows number)
		 */
		public void initUI(MyFrame jF){
						
			myFrame = jF;
			
			setPreferredSize(new Dimension((int)((2*NUMBER_OF_FREE_COLUMNS)+1)*TILE_SIZE + 2*GAP_WIDTH, (int)((2*NUMBER_OF_FREE_ROWS)+1)*TILE_SIZE));
			setDoubleBuffered(true);
			setFocusable(true);
			addKeyListener(this);
			
			for (int i = 0; i < positionsTable.length; i++) {
				for (int j = 0; j < positionsTable[i].length; j++) {
					
					positionsTable[i][j] = new Dimension((i*2*TILE_SIZE)+2*TILE_SIZE+GAP_WIDTH, (j*2*TILE_SIZE)+2*TILE_SIZE);
				}
			}
			
			
			timer = new Timer(1000/60, new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					repaint();
					explosionCheck();
				}
			});
			timer.start();
						
		}
		
		/**
		 * 
		 * @param x Takes the x position of the exploding bomb
		 * @param y Takes the y position of the exploding bomb
		 * @param range Takes the player's range
		 * 
		 * Method should only be invoked by exploding bombs. Note that the player's range is taken from the bomb not directly from the player.
		 * This means the range of the explosion is taken from the moment of bomb placement and is not affected by later rangeUp pickups.
		 */
		public void newExplosion (int x, int y, int range) {
			explosions.add(new Explosion(x, y, range, this));
		}
		
		/**
		 * Pauses the refresh timer and gets rid of the explosion.
		 * In case of Exceptions the method is set to call itself again.
		 * There is currently a bug where if 3+ explosions chain together, one may remain on the map.
		 * Looking for a proper fix.
		 */
		public synchronized void endExplosion () {			
			timer.stop();
			try {
				iterator = explosions.iterator();
				if (iterator.hasNext()) {
					iterator.next();
					iterator.remove();	
				}
			}
			catch (RuntimeException e) {
				e.printStackTrace();
				endExplosion();
			}
			finally {
				timer.start();				
			}
		}
		
		/**
		 * Method invoked at refresh rate.
		 * Checks whether the explosion touches a Player, DestrWall, Bomb or PowerUp.
		 * If yes, invokes the kill/explode/setNull method from target.
		 */
		public void explosionCheck() {
			try {
				for (Explosion e : explosions) {
					
					/** MID CHECK **/
					if (e.getX() == p1.getCurrentTile().getWidth() && e.getY() == p1.getCurrentTile().getHeight()) {
						p1.kill();
					}
					
					if (e.getX() == p2.getCurrentTile().getWidth() && e.getY() == p2.getCurrentTile().getHeight()) {
						p2.kill();
					}
					
					leftCheck();
					upCheck();
					downCheck();
					rightCheck();
				}
			}
			catch (ConcurrentModificationException cME) {
				explosionCheck();
			}
		}
			
		/**
		 * Main paint method. Invokes methods for paintable objects.
		 * First draws the Floor.
		 * Last draws are Characters and Explosions.
		 * @param g Graphics context
		 */
		@Override
		public void paintComponent (Graphics g) {
			
			super.paintComponent(g);
			drawFloor(g);
			drawBombs(g);
			drawWalls(g);
			drawPillars(g);
			drawDestrWalls(g);
			drawPowerUps(g);
			drawCharacters(g);
			drawExplosions(g);
			
		}
		
		/**
		 * Draws bombs. Animations are handled by the Bomb class.
		 * @param g Graphics context
		 */
		public void drawBombs (Graphics g) {
						
			for (Bomb b : p1.getBombs()) {
				g.drawImage(b.getCurrentImage(), b.getX(), b.getY(), null);
			}
			
			for (Bomb b : p2.getBombs()) {
				g.drawImage(b.getCurrentImage(), b.getX(), b.getY(), null);
			}
		}
		
		/**
		 * Draws Explosions.
		 * Contains advanced logic for drawing explosions in range of other explosions.
		 * 4 for loops for each Explosion are invoked to check all directions.
		 * Checks for explosions being null.
		 * @param g Graphics context
		 */
		public void drawExplosions (Graphics g) {
			
			if (explosions != null) {
				try {
					for (Explosion e : explosions) {
						
						for (int i = 1; i < e.getRange()+1; i++) {
							
							if (e.getY()-i*TILE_SIZE < TILE_SIZE) {}
							else if (((e.getX()-GAP_WIDTH) % (2*TILE_SIZE)) == 0) {}
							else if (dW.getPositions()[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()-i*TILE_SIZE)/TILE_SIZE)-1] != null) {
								i = e.getRange();
							}
							else if (powerUps[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()-i*TILE_SIZE)/TILE_SIZE)-1] != null) {
								i = e.getRange();
							}
							else {
								
								boolean canIPaint = true;
								
								for (Explosion e1 : explosions) {
									if (e1.getY() == e.getY()-(i)*TILE_SIZE && e1.getX() == e.getX()) {
										canIPaint = false;
										i = e.getRange();
									}
									
									else if (canIPaint) {
										if (i < e.getRange()) {
											g.drawImage(e.vert, e.getX(), e.getY()-i*TILE_SIZE, null);							
										}
										else {
											g.drawImage(e.endUp, e.getX(), e.getY()-e.getRange()*TILE_SIZE, null);
										}
									}
								}
							}
						}
						
						for (int i = 1; i < e.getRange()+1; i++) {
		
							if (e.getX()+i*TILE_SIZE > Board.GAP_WIDTH + (2*(Board.NUMBER_OF_FREE_COLUMNS)-1)*Board.TILE_SIZE) {}
							else if (((e.getY()) % (2*TILE_SIZE)) == 0) {}
							else if (dW.getPositions()[((e.getX()-GAP_WIDTH+i*TILE_SIZE)/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1] != null) {
								i = e.getRange();
							}
							else if (powerUps[((e.getX()-GAP_WIDTH+i*TILE_SIZE)/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1] != null) {
								i = e.getRange();
							}
							else {
								
								boolean canIPaint = true;
								
								for (Explosion e1 : explosions) {
									if (e1.getX() == e.getX()+(i)*TILE_SIZE && e1.getY() == e.getY()) {
										canIPaint = false;
										i = e.getRange();
									}
									
									else if (canIPaint) {
										if (i < e.getRange()) {
											g.drawImage(e.hori, e.getX()+i*TILE_SIZE, e.getY(), null);						
										}
										else {
											g.drawImage(e.endRight, e.getX()+e.getRange()*TILE_SIZE, e.getY(), null);	
										}
									}
								}
							}
						}
						
						for (int i = 1; i < e.getRange()+1; i++) {
		
							if (e.getY()+i*TILE_SIZE > ((2*Board.NUMBER_OF_FREE_ROWS)-1)*Board.TILE_SIZE) {}
							else if (((e.getX()-GAP_WIDTH) % (2*TILE_SIZE)) == 0) {}
							else if (dW.getPositions()[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()+i*TILE_SIZE)/TILE_SIZE)-1] != null) {
								i = e.getRange();
							}
							else if (powerUps[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()+i*TILE_SIZE)/TILE_SIZE)-1] != null) {
								i = e.getRange();
							}
							else {
								
								boolean canIPaint = true;
								
								for (Explosion e1 : explosions) {
									if (e1.getY() == e.getY()+(i)*TILE_SIZE && e1.getX() == e.getX()) {
										canIPaint = false;
										i = e.getRange();
									}
									
									else if (canIPaint) {
										if (i < e.getRange()) {
											g.drawImage(e.vert, e.getX(), e.getY()+i*TILE_SIZE, null);					
										}
										else {
											g.drawImage(e.endDown, e.getX(), e.getY()+e.getRange()*TILE_SIZE, null);
										}
									}
								}
							}
						}
						
						for (int i = 1; i < e.getRange()+1; i++) {
		
							if (e.getX()-i*TILE_SIZE < Board.GAP_WIDTH + Board.TILE_SIZE) {}
							else if (((e.getY()) % (2*TILE_SIZE)) == 0) {}
							else if (dW.getPositions()[(((e.getX()-GAP_WIDTH)-i*TILE_SIZE)/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1] != null) {
								i = e.getRange();
							}
							else if (powerUps[(((e.getX()-GAP_WIDTH)-i*TILE_SIZE)/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1] != null) {
								i = e.getRange();
							}
							else {
								
								boolean canIPaint = true;
								
								for (Explosion e1 : explosions) {
									if (e1.getX() == e.getX()-(i)*TILE_SIZE && e1.getY() == e.getY()) {
										canIPaint = false;
										i = e.getRange();
									}
									
									else if (canIPaint) {
										if (i < e.getRange()) {
											g.drawImage(e.hori, e.getX()-i*TILE_SIZE, e.getY(), null);			
										}
										else {
											g.drawImage(e.endLeft, e.getX()-e.getRange()*TILE_SIZE, e.getY(), null);
										}
									}
								}
							}
						}
					}
				}
				catch (ConcurrentModificationException cME) {
					repaint();
				}
				catch (NoSuchElementException nSEE) {
					repaint();
				}
			}
			
			if (explosions != null) {
				try {
					for (Explosion e : explosions) {						
						g.drawImage(e.mid, e.getX(), e.getY(), null);
					}
				}
				catch (ConcurrentModificationException cME) {
					repaint();
				}
				catch (NoSuchElementException nSEE) {
					repaint();
				}
			}
		}
				
		/**
		 * Draws characters.
		 * @param g Graphics context
		 */
		public void drawCharacters (Graphics g) {
			g.drawImage(p1.getCurrentAvatar(), p1.getX(), p1.getY(), null);
			g.drawImage(p2.getCurrentAvatar(), p2.getX(), p2.getY(), null);
		}
		
		/**
		 * Draws walls surrounding the arena.
		 * @param g Graphics context
		 */
		public void drawWalls (Graphics g) {
			g.drawImage(new ImageIcon("images/wallsUpLeft_"+TILE_SIZE+".png").getImage(), GAP_WIDTH, 0, null);
			g.drawImage(new ImageIcon("images/wallsUpRight_"+TILE_SIZE+".png").getImage(), ((2*NUMBER_OF_FREE_COLUMNS)*TILE_SIZE)+GAP_WIDTH, 0, null);
			g.drawImage(new ImageIcon("images/wallsDownLeft_"+TILE_SIZE+".png").getImage(), GAP_WIDTH, (2*NUMBER_OF_FREE_ROWS)*TILE_SIZE, null);
			g.drawImage(new ImageIcon("images/wallsDownRight_"+TILE_SIZE+".png").getImage(), ((2*NUMBER_OF_FREE_COLUMNS)*TILE_SIZE)+GAP_WIDTH, (2*NUMBER_OF_FREE_ROWS)*TILE_SIZE, null);
			
			for (int i = 1; i < (2*NUMBER_OF_FREE_COLUMNS); i++) {
				g.drawImage(new ImageIcon("images/wallsHorizontalUp_"+TILE_SIZE+".png").getImage(), (i*TILE_SIZE)+GAP_WIDTH, 0, null);
			}
			
			for (int i = 1; i < (2*NUMBER_OF_FREE_COLUMNS); i++) {
				g.drawImage(new ImageIcon("images/wallsHorizontalDown_"+TILE_SIZE+".png").getImage(), (i*TILE_SIZE)+GAP_WIDTH, (2*NUMBER_OF_FREE_ROWS)*TILE_SIZE, null);
			}
			
			for (int i = 1; i < (2*NUMBER_OF_FREE_ROWS); i++) {
				g.drawImage(new ImageIcon("images/wallsVerticalLeft_"+TILE_SIZE+".png").getImage(), GAP_WIDTH, i*TILE_SIZE, null);
			}
			
			for (int i = 1; i < (2*NUMBER_OF_FREE_ROWS); i++) {
				g.drawImage(new ImageIcon("images/wallsVerticalRight_"+TILE_SIZE+".png").getImage(), ((2*NUMBER_OF_FREE_COLUMNS)*TILE_SIZE)+GAP_WIDTH, i*TILE_SIZE, null);
			}
		}
		
		/**
		 * Draws floor on the whole arena.
		 * @param g Graphics context
		 */
		public void drawFloor (Graphics g) {
			for (int i = 0; i*TILE_SIZE <= 2*Board.GAP_WIDTH + ((2*Board.NUMBER_OF_FREE_COLUMNS)+1)*Board.TILE_SIZE ; i++) {
				for (int j = 0; j*TILE_SIZE <= ((2*Board.NUMBER_OF_FREE_ROWS)+1)*Board.TILE_SIZE; j++) {
					g.drawImage(new ImageIcon("images/floor_"+TILE_SIZE+".png").getImage(), i*TILE_SIZE, j*TILE_SIZE, null);
					
				}
			}
		}
		
		/**
		 * Draws static, non-destructible pillars.
		 * @param g Graphics context
		 */
		public void drawPillars (Graphics g) {
			
			for (int i = 0; i < positionsTable.length; i++) {
				for (int j = 0; j < positionsTable[i].length; j++) {
					g.drawImage(new ImageIcon("images/pillars_"+TILE_SIZE+".png").getImage(), (int)positionsTable[i][j].getWidth(), (int)positionsTable[i][j].getHeight(), null);
					
				}
			}
			
		}
		
		/**
		 * Draws destructible walls.
		 * @param g Graphics context
		 */
		public void drawDestrWalls (Graphics g) {
			
			for (int i = 0; i < dW.getPositions().length-1; i++) {
				for (int j = 0; j < dW.getPositions()[i].length-1; j++) {
					if (dW.getPositions()[i][j] != null) {
						g.drawImage(dW.getImg(i, j), (int)dW.getPositions()[i][j].getWidth(), (int)dW.getPositions()[i][j].getHeight(), null);
					}
				}
			}			
		}
		
		/**
		 * Draws powerUps. 
		 * @param g Graphics context
		 */
		public void drawPowerUps (Graphics g) {
			
			for (int i = 0; i < powerUps.length; i++) {
				for (int j = 0; j < powerUps[i].length; j++) {
					if (powerUps[i][j] != null) {
						g.drawImage(powerUps[i][j].getCurrentImg(), powerUps[i][j].getX(), powerUps[i][j].getY(), null);
					}
				}
			}			
		}
		
		/**
		 * Creates new PowerUp instance if called by a destroyed wall.
		 * @param i Index i used for further reference (e.g. when picked up)
		 * @param j Index j used for further reference (e.g. when picked up)
		 * @param x Position x
		 * @param y Position y
		 */
		public void newPowerUp (int i, int j, int x, int y) {
			if (powerUps[i][j] == null) {
				powerUps[i][j] = new PowerUp(x, y, i, j, this);
			}
		}
		
		/**
		 * Called by Character instance. Collision checked in the Character class. Sets the PowerUp as null after pickup.
		 * @param i Index i for identification of proper instance.
		 * @param j Index j for identification of proper instance.
		 */
		public void powerUpPickUp (int i, int j) {
			powerUps[i][j] = null;
		}
				
		/**
		 * Keybindings. WSAD/Directional arrows for Player 1/2 movement. G/Enter for Player 1/2 bomb placement.
		 * For movement - booleans control movement (set true on press, set false on release). 
		 * Coded with booleans for smooth movement.
		 * <br>Special key - SPACE - clears map of explosions. Used for debugging. Should be removed for final release.
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			
			switch(e.getKeyCode()) {
			
			case KeyEvent.VK_W:
				if (wRel == true) {
					wRel = false;
					ActionListener go = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p1.moveUp();
						}
					};
				wTimer = new Timer (p1.getCharTimerSpeed(), go);
				wTimer.start();
				}
	        	break;
			case KeyEvent.VK_S:
				if (sRel == true) {
					sRel = false;
					ActionListener go = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p1.moveDown();
						}
					};
				sTimer = new Timer (p1.getCharTimerSpeed(), go);
				sTimer.start();
				}
				break;
			case KeyEvent.VK_A:
				if (aRel == true) {
					aRel = false;
					ActionListener go = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p1.moveLeft();
						}
					};
				aTimer = new Timer (p1.getCharTimerSpeed(), go);
				aTimer.start();
				}	        	
				break;
			case KeyEvent.VK_D:
				if (dRel == true) {
					dRel = false;
					ActionListener go = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p1.moveRight();
						}
					};
				dTimer = new Timer (p1.getCharTimerSpeed(), go);
				dTimer.start();
				}
	        	break;
	        case KeyEvent.VK_DOWN:
	        	if (downRel == true) {
					downRel = false;
					ActionListener go = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p2.moveDown();
						}
					};
				downTimer = new Timer (p2.getCharTimerSpeed(), go);
				downTimer.start();
				}
	        	break;
	        case KeyEvent.VK_UP: 
	        	if (upRel == true) {
					upRel = false;
					ActionListener go = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p2.moveUp();
						}
					};
				upTimer = new Timer (p2.getCharTimerSpeed(), go);
				upTimer.start();
				}	        	
	        	break;
	        case KeyEvent.VK_RIGHT:
	        	if (rightRel == true) {
					rightRel = false;
					ActionListener go = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p2.moveRight();
						}
					};
				rightTimer = new Timer (p2.getCharTimerSpeed(), go);
				rightTimer.start();
				}
	        	break;
	        case KeyEvent.VK_LEFT:
	        	if (leftRel == true) {
					leftRel = false;
					ActionListener go = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p2.moveLeft();
						}
					};
				leftTimer = new Timer (p2.getCharTimerSpeed(), go);
				leftTimer.start();
				}
	        	break;
	        case KeyEvent.VK_G:
	        	if (gRel == true) {
					gRel = false;
					ActionListener bomb = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p1.placeBomb();
						}
					};
				gTimer = new Timer (1000/60, bomb);
				gTimer.start();
				}
	        	break;
	        case KeyEvent.VK_ENTER:
	        	if (enterRel == true) {
					enterRel = false;
					ActionListener bomb = new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							p2.placeBomb();
						}
					};
				enterTimer = new Timer (1000/60, bomb);
				enterTimer.start();
				}
	        	break;
	        	
	        case KeyEvent.VK_SPACE:
				try {
					endExplosion();
				}
				catch (ConcurrentModificationException cME) {
					endExplosion();
				}
	        	break;
	        default:
	        	break;
			}
		}

		/**
		 * Sets booleans false on movement key release. See keyPressed documentation for further information.
		 */
		@Override
		public void keyReleased(KeyEvent e) {
			
			switch(e.getKeyCode()) {
			
			case KeyEvent.VK_W:
				wTimer.stop();
				wRel = true;
				p1.stopTimerUp();
				break;
			case KeyEvent.VK_S:
				sTimer.stop();
				sRel = true;
				p1.stopTimerDown();
				break;
			case KeyEvent.VK_A:
				aTimer.stop();
				aRel = true;
				p1.stopTimerLeft();
				break;
			case KeyEvent.VK_D:
				dTimer.stop();
				dRel = true;
				p1.stopTimerRight();
	        	break;
	        case KeyEvent.VK_DOWN:
	        	downTimer.stop();
				downRel = true;
				p2.stopTimerDown();
	        	break;
	        case KeyEvent.VK_UP: 
	        	upTimer.stop();
				upRel = true;
				p2.stopTimerUp();
	        	break;
	        case KeyEvent.VK_RIGHT:
	        	rightTimer.stop();
				rightRel = true;
				p2.stopTimerRight();
	        	break;
	        case KeyEvent.VK_LEFT:
	        	leftTimer.stop();
				leftRel = true;
				p2.stopTimerLeft();
	        	break;
	        case KeyEvent.VK_G:
	        	gTimer.stop();
				gRel = true;
	        	break;
	        case KeyEvent.VK_ENTER:
	        	enterTimer.stop();
				enterRel = true;
	        	break;
	        default:
	        	break;
			}
			
		}

		/**
		 * Not used.
		 */
		@Override
		public void keyTyped(KeyEvent arg0) {}
		
		
		public boolean getWRel () {
			return wRel;
		}
		
		public boolean getSRel () {
			return sRel;
		}
		
		public boolean getDRel () {
			return dRel;
		}
		
		public boolean getARel () {
			return aRel;
		}

		public boolean getUpRel () {
			return upRel;
		}

		public boolean getDownRel () {
			return downRel;
		}

		public boolean getRightRel () {
			return rightRel;
		}

		public boolean getLeftRel () {
			return leftRel;
		}
		
		/**
		 * Ends the game on player kill. Currently no logic for draws (win for player 2).
		 * Invokes the playAgain method of the JFrame and passs the winner information.
		 * @param playerNo Takes killed player number to determine who won.
		 */
		public void endGame (int playerNo) {
			int winner;
			if (playerNo == 1) {
				winner = 2;
			}
			else {
				winner = 1;
			}
			myFrame.playAgain(winner);
		}
		
		/**
		 * Stops repaint and explosionCheck mehtods. Used by other classes to get access to the explosion set.
		 */
		public void stopTimer () {
			timer.stop();
		}
		
		public PowerUp[][] getPowerUps () {
			return powerUps;
		}
		
		
		/**
		 * Checks whether the explosion has collided with anything on the left. Invokes respective kill methods.
		 * Used by the explosionCheck method.
		 */
		public void leftCheck () {
			try {
				for (Explosion e : explosions) {
					/** LEFT CHECK **/
					for (int i = 1; i < e.getRange()+1; i++) {
						
						for (Bomb b: p1.getBombs()) {
							if ((e.getX()-(i*TILE_SIZE)) == b.getX() && e.getY() == b.getY() && ((e.getY()) % (2*TILE_SIZE)) != 0) {
								b.boom();
								break;
							}
						}
						
						for (Bomb b: p2.getBombs()) {
							if ((e.getX()-(i*TILE_SIZE)) == b.getX() && e.getY() == b.getY() && ((e.getY()) % (2*TILE_SIZE)) != 0) {
								b.boom();
								break;
							}
						}
						
						
						if (e.getX()-i*TILE_SIZE < Board.GAP_WIDTH + Board.TILE_SIZE) {}
						else if (((e.getY()) % (2*TILE_SIZE)) == 0) {}
						else if (dW.getPositions()[(((e.getX()-GAP_WIDTH)-i*TILE_SIZE)/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1] != null) {
							dW.setPositionNull((((e.getX()-GAP_WIDTH)-i*TILE_SIZE)/TILE_SIZE)-1, (e.getY()/TILE_SIZE)-1);
							i = e.getRange();
						}
						else if (powerUps[(((e.getX()-GAP_WIDTH)-i*TILE_SIZE)/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1] != null) {
							powerUps[(((e.getX()-GAP_WIDTH)-i*TILE_SIZE)/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1].burn();
							i = e.getRange();
						}
						else if ((e.getX()-(i*TILE_SIZE)) == p1.getCurrentTile().getWidth() && e.getY() == p1.getCurrentTile().getHeight() && (e.getX()-(i*TILE_SIZE)) == p2.getCurrentTile().getWidth() && e.getY() == p2.getCurrentTile().getHeight()) {
							p1.kill();
							// draw?
						}
						
						else if ((e.getX()-(i*TILE_SIZE)) == p1.getCurrentTile().getWidth() && e.getY() == p1.getCurrentTile().getHeight()) {
							p1.kill();
						}
						
						else if ((e.getX()-(i*TILE_SIZE)) == p2.getCurrentTile().getWidth() && e.getY() == p2.getCurrentTile().getHeight()) {
							p2.kill();
						}
					}
				}
			}
			catch (ConcurrentModificationException cME) {
				cME.printStackTrace();
				leftCheck();
			}
		}
		
		/**
		 * Checks whether the explosion has collided with anything down. Invokes respective kill methods.
		 * Used by the explosionCheck method.
		 */
		public void downCheck () {
			try {
				for (Explosion e : explosions) {
					/** DOWN CHECK **/
					for (int i = 1; i < e.getRange()+1; i++) {
						
						for (Bomb b: p1.getBombs()) {
							if (e.getX() == b.getX() && (e.getY()+(i*TILE_SIZE)) == b.getY() && ((e.getX()-GAP_WIDTH) % (2*TILE_SIZE)) != 0) {
								b.boom();
								break;
							}
						}
						
						for (Bomb b: p2.getBombs()) {
							if (e.getX() == b.getX() && (e.getY()+(i*TILE_SIZE)) == b.getY() && ((e.getX()-GAP_WIDTH) % (2*TILE_SIZE)) != 0) {
								b.boom();
								break;
							}
						}
						
						
						if (e.getY()+i*TILE_SIZE > (((2*Board.NUMBER_OF_FREE_ROWS)-1)*Board.TILE_SIZE)) {}
						else if (((e.getX()-GAP_WIDTH) % (2*TILE_SIZE)) == 0) {}
						else if (dW.getPositions()[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()+(i*TILE_SIZE))/TILE_SIZE)-1] != null) {
							dW.setPositionNull(((e.getX()-GAP_WIDTH)/TILE_SIZE)-1, ((e.getY()+(i*TILE_SIZE))/TILE_SIZE)-1);
							i = e.getRange();
						}
						else if (powerUps[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()+(i*TILE_SIZE))/TILE_SIZE)-1] != null) {
							powerUps[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()+(i*TILE_SIZE))/TILE_SIZE)-1].burn();
							i = e.getRange();
						}
						else if (e.getX() == p1.getCurrentTile().getWidth() && (e.getY()+(i*TILE_SIZE)) == p1.getCurrentTile().getHeight() && e.getX() == p2.getCurrentTile().getWidth() && (e.getY()+(i*TILE_SIZE)) == p2.getCurrentTile().getHeight()) {
							p1.kill();
							//draw?
						}
						else if (e.getX() == p1.getCurrentTile().getWidth() && (e.getY()+(i*TILE_SIZE)) == p1.getCurrentTile().getHeight()) {
							p1.kill();
						}
						else if (e.getX() == p2.getCurrentTile().getWidth() && (e.getY()+(i*TILE_SIZE)) == p2.getCurrentTile().getHeight()) {
							p2.kill();
						}
					}
				}
			}
			catch (ConcurrentModificationException cME) {
				cME.printStackTrace();
				downCheck();
			}
		}
		
		/**
		 * Checks whether the explosion has collided with anything up. Invokes respective kill methods.
		 * Used by the explosionCheck method.
		 */
		public void upCheck () {
			try {
				for (Explosion e : explosions) {
					/** UP CHECK **/
					for (int i = 1; i < e.getRange()+1; i++) {
						
						for (Bomb b: p1.getBombs()) {
							if (e.getX() == b.getX() && (e.getY()-(i*TILE_SIZE)) == b.getY() && ((e.getX()-GAP_WIDTH) % (2*TILE_SIZE)) != 0) {
								b.boom();
								break;
							}
						}
						
						for (Bomb b: p2.getBombs()) {
							if (e.getX() == b.getX() && (e.getY()-(i*TILE_SIZE)) == b.getY() && ((e.getX()-GAP_WIDTH) % (2*TILE_SIZE)) != 0) {
								b.boom();
								break;
							}
						}
						
						
						if (e.getY()-i*TILE_SIZE < TILE_SIZE) {}
						else if (((e.getX()-GAP_WIDTH) % (2*TILE_SIZE)) == 0) {}
						else if (dW.getPositions()[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()-i*TILE_SIZE)/TILE_SIZE)-1] != null) {
							dW.setPositionNull(((e.getX()-GAP_WIDTH)/TILE_SIZE)-1, ((e.getY()-i*TILE_SIZE)/TILE_SIZE)-1);
							i = e.getRange();
						}
						else if (powerUps[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()-i*TILE_SIZE)/TILE_SIZE)-1] != null) {
							powerUps[((e.getX()-GAP_WIDTH)/TILE_SIZE)-1][((e.getY()-i*TILE_SIZE)/TILE_SIZE)-1].burn();
							i = e.getRange();
						}
						else if (e.getX() == p1.getCurrentTile().getWidth() && (e.getY()-(i*TILE_SIZE)) == p1.getCurrentTile().getHeight() && e.getX() == p2.getCurrentTile().getWidth() && (e.getY()-(i*TILE_SIZE)) == p2.getCurrentTile().getHeight()) {
							p1.kill();
							//draw?
						}
						else if (e.getX() == p1.getCurrentTile().getWidth() && (e.getY()-(i*TILE_SIZE)) == p1.getCurrentTile().getHeight()) {
							p1.kill();
						}
						else if (e.getX() == p2.getCurrentTile().getWidth() && (e.getY()-(i*TILE_SIZE)) == p2.getCurrentTile().getHeight()) {
							p2.kill();
						}
					}
				}
			}
			catch (ConcurrentModificationException cME) {
				cME.printStackTrace();
				upCheck();
			}
		}
		
		/**
		 * Checks whether the explosion has collided with anything on the right. Invokes respective kill methods.
		 * Used by the explosionCheck method.
		 */
		public void rightCheck () {
			try {
				for (Explosion e : explosions) {
					/** RIGHT CHECK **/
					for (int i = 1; i < e.getRange()+1; i++) {
						
						for (Bomb b: p1.getBombs()) {
							if ((e.getX()+(i*TILE_SIZE)) == b.getX() && e.getY() == b.getY() && ((e.getY()) % (2*TILE_SIZE)) != 0) {
								b.boom();
								break;
							}
						}
						
						for (Bomb b: p2.getBombs()) {
							if ((e.getX()+(i*TILE_SIZE)) == b.getX() && e.getY() == b.getY() && ((e.getY()) % (2*TILE_SIZE)) != 0) {
								b.boom();
								break;
							}
						}
						
						
						if (e.getX()+(i*TILE_SIZE) > (Board.GAP_WIDTH + (2*(Board.NUMBER_OF_FREE_COLUMNS)-1)*Board.TILE_SIZE)) {}
						else if (((e.getY()) % (2*TILE_SIZE)) == 0) {}
						else if (dW.getPositions()[(((e.getX()-GAP_WIDTH)+(i*TILE_SIZE))/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1] != null) {
							dW.setPositionNull((((e.getX()-GAP_WIDTH)+(i*TILE_SIZE))/TILE_SIZE)-1, (e.getY()/TILE_SIZE)-1);
							i = e.getRange();
						}
						else if (powerUps[(((e.getX()-GAP_WIDTH)+(i*TILE_SIZE))/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1] != null) {
							powerUps[(((e.getX()-GAP_WIDTH)+(i*TILE_SIZE))/TILE_SIZE)-1][(e.getY()/TILE_SIZE)-1].burn();
							i = e.getRange();
						}
						else if ((e.getX()+(i*TILE_SIZE)) == p1.getCurrentTile().getWidth() && e.getY() == p1.getCurrentTile().getHeight() && (e.getX()+(i*TILE_SIZE)) == p2.getCurrentTile().getWidth() && e.getY() == p2.getCurrentTile().getHeight()) {
							p1.kill();
							// draw?
						}
						else if ((e.getX()+(i*TILE_SIZE)) == p1.getCurrentTile().getWidth() && e.getY() == p1.getCurrentTile().getHeight()) {
							p1.kill();
						}
						else if ((e.getX()+(i*TILE_SIZE)) == p2.getCurrentTile().getWidth() && e.getY() == p2.getCurrentTile().getHeight()) {
							p2.kill();
						}
					}
				}
			}
			catch (ConcurrentModificationException cME) {
				cME.printStackTrace();
				rightCheck();
			}
		}
}