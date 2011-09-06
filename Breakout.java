/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 501;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 3;
	
/** Other constants */
	private static final int PAUSE_TIME = 10;
	private static final double MAX_VELOCITY = 3.0;
	private static final double MIN_VELOCITY = 1.0;
	private static final int BALLS_OFFSET = 5;

/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
		setup();
		play();
	}
	
	private void setup() {
		addMouseListeners();
		drawBricks();
		drawPaddle();
		drawLivesLeft();
	}
	
	private void drawBricks() {
		for (int i = 0; i < NBRICK_ROWS; i++) {
			Color color;
			switch(i / 2) {
			case 0: color = Color.red; break;
			case 1: color = Color.orange; break;
			case 2: color = Color.yellow; break;
			case 3: color = Color.green; break;
			case 4: color = Color.cyan; break;
			default: color = Color.black; break;
			}
			int y = BRICK_Y_OFFSET + i * (BRICK_HEIGHT + BRICK_SEP);
			drawRow(color, y);
		}
	}
	
	private void drawRow(Color color, int y) {
		for (int i = 0; i < NBRICKS_PER_ROW; i++) {
			int widthOfAllBricks = BRICK_WIDTH * NBRICKS_PER_ROW + BRICK_SEP * (NBRICKS_PER_ROW - 1);
			int firstRowX = (WIDTH - widthOfAllBricks) / 2;
			int x = firstRowX + i * (BRICK_WIDTH + BRICK_SEP);
			drawBrick(x, y, color);
		}
	}
	
	private void drawBrick(int x, int y, Color color) {
		add(new GBrick(BRICK_WIDTH, BRICK_HEIGHT, color), x, y);
	}
	
	private void drawPaddle() {
		add(paddle, (WIDTH - PADDLE_WIDTH) / 2, HEIGHT - PADDLE_Y_OFFSET);
	}
	
	private void drawLivesLeft() {
		for (int i = 0; i < livesLeft; i++) {
			int x = BALLS_OFFSET + i * (BALL_RADIUS + 3);
			int y = HEIGHT - BALL_RADIUS - BALLS_OFFSET;
			GBall miniBall = new GBall(BALL_RADIUS, BALL_RADIUS, Color.black);
			add(miniBall, x, y);
		}		
	}
	
	private void updateLivesLeft() {
		int x = BALLS_OFFSET + livesLeft * (BALL_RADIUS + 3);
		int y = HEIGHT - BALL_RADIUS - BALLS_OFFSET;
		remove(getElementAt(x, y));
	}
	
	public void mouseMoved(MouseEvent e) {
		int x = Math.min(WIDTH - PADDLE_WIDTH, Math.max(0, e.getX() - PADDLE_WIDTH / 2));
		paddle.setLocation(x, HEIGHT - PADDLE_Y_OFFSET);
	}
	
	private void drawBall() {
		add(ball, WIDTH / 2 - BALL_RADIUS, HEIGHT / 2 - BALL_RADIUS);
		ball.sendToBack();
	}
	
	private void play() {
		while (result == 0 && livesLeft > 0) {
			animateBall();
		}
		endGame();
	}
	
	private void animateBall() {
		
		/* Setup the ball and wait for the click */
		
		drawBall();
		vx = rgen.nextDouble(MIN_VELOCITY, MAX_VELOCITY);
		if (rgen.nextBoolean()) vx = -vx;
		vy = MAX_VELOCITY;
		waitForClick();
		
		/* Run the game */
		
		while (true) {
			ball.move(vx, vy);
			pause(PAUSE_TIME);
			
			/* Check for wall collisions */
			
			if (ball.getX() < 0) vx = Math.abs(vx);
			if (ball.getX() > WIDTH - 2 * BALL_RADIUS) vx = -Math.abs(vx);
			if (ball.getY() < 0) vy = Math.abs(vy);
			
			/* Check for collisions with paddle or bricks */
			
			getCollidingObject();
			if (collidee == paddle) {
				if (surface == 6) {
					ball.setLocation(ball.getX(), HEIGHT - PADDLE_Y_OFFSET - 2 * BALL_RADIUS);
					vy = -vy;
				} else if (surface == 3) {
					vx = -Math.abs(vx);
				} else if (surface == 9) {
					vx = Math.abs(vx);
				}
			} else if (collidee != null) {
				if (surface == 6 || surface == 12) vy = -vy;
				if (surface == 3 || surface == 9) vx = -vx;
				remove(collidee);
				bricksLeft--;
			}	
			
			/* End turn if ball hits bottom wall */
			
			if (ball.getY() > HEIGHT - 2 * BALL_RADIUS) {
				remove(ball);
				result = 0;
				livesLeft--;
				updateLivesLeft();
				break;
			}
			
			/* End game if ball hits final brick */
			
			if (bricksLeft == 0) {
				remove(ball);
				result = 1;
				break;
			}
		}
	}
	
	private void getCollidingObject() {
		
		/* Define 3, 6, 9, and 12 o'clock locations of the ball */
		
		GPoint left = new GPoint(ball.getX() - 1, ball.getY() + BALL_RADIUS);
		GPoint right = new GPoint(ball.getX() + 2 * BALL_RADIUS + 1, ball.getY() + BALL_RADIUS);
		GPoint top = new GPoint(ball.getX() + BALL_RADIUS, ball.getY() - 1);
		GPoint bottom = new GPoint(ball.getX() + BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS + 1);
		
		/* Find GObject that the ball collided with, if it exists */
		
		if (getElementAt(bottom) != null) {
			collidee = getElementAt(bottom);
			surface = 6;
		} else if (getElementAt(top) != null) {
			collidee = getElementAt(top);
			surface = 12;
		} else if (getElementAt(left) != null) {
			collidee = getElementAt(left);
			surface = 9;
		} else if (getElementAt(right) != null) {
			collidee = getElementAt(right);
			surface = 3;
		} else {
			collidee = null;
			surface = 0;
		}
	}
	
	private void endGame() {
		
	}
	
	
	private GBrick paddle = new GBrick(PADDLE_WIDTH, PADDLE_HEIGHT, Color.black);
	private GBall ball = new GBall(2 * BALL_RADIUS, 2 * BALL_RADIUS, Color.black);
	private GObject collidee = null;
	private int surface = 0;
	private double vx, vy;
	private int livesLeft = NTURNS;
	private int result = 0;
	private int bricksLeft = NBRICK_ROWS * NBRICKS_PER_ROW;
	private RandomGenerator rgen = RandomGenerator.getInstance();
	
}
