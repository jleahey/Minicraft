package com.github.jleahey.minicraft.awtgraphics;

import java.awt.Canvas;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.github.jleahey.minicraft.Game;

public class AwtEventsHandler {
	Game game;
	
	public AwtEventsHandler(Game game, Canvas canvas) {
		this.game = game;
		// add a key input system (defined below) to our canvas
		// so we can respond to key pressed
		canvas.addKeyListener(new KeyInputHandler());
		canvas.addMouseListener(new MouseInputHander());
		canvas.addMouseWheelListener(new MouseWheelInputHander());
		canvas.addMouseMotionListener(new MouseMoveInputHander());
		
		// TODO: A lot of this should be calling a nicer function in Game to handle
		// mouse+keyboard/touch input
	}
	
	private class MouseWheelInputHander implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int notches = e.getWheelRotation();
			game.inventory.hotbarIdx += notches;
			if (game.inventory.hotbarIdx < 0) {
				game.inventory.hotbarIdx = 0;
			} else if (game.inventory.hotbarIdx > 9) {
				game.inventory.hotbarIdx = 9;
			}
		}
	}
	
	private class MouseMoveInputHander implements MouseMotionListener {
		@Override
		public void mouseDragged(MouseEvent arg0) {
			game.screenMousePos.x = arg0.getX();
			game.screenMousePos.y = arg0.getY();
		}
		
		@Override
		public void mouseMoved(MouseEvent arg0) {
			game.screenMousePos.x = arg0.getX();
			game.screenMousePos.y = arg0.getY();
		}
	}
	
	private class MouseInputHander extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent arg0) {
			switch (arg0.getButton()) {
			case MouseEvent.BUTTON1:
				game.leftClick = true;
				break;
			case MouseEvent.BUTTON2: // fall through
			case MouseEvent.BUTTON3:
				game.rightClick = true;
				break;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent arg0) {
			switch (arg0.getButton()) {
			case MouseEvent.BUTTON1:
				game.leftClick = false;
				break;
			case MouseEvent.BUTTON2: // fall through
			case MouseEvent.BUTTON3:
				game.rightClick = false;
				break;
			}
		}
	}
	
	private class KeyInputHandler extends KeyAdapter {
		/**
		 * Notification from AWT that a key has been pressed. Note that
		 * a key being pressed is equal to being pushed down but *NOT*
		 * released. Thats where keyTyped() comes in.
		 * 
		 * @param e
		 *            The details of the key that was pressed
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
			case KeyEvent.VK_SPACE:
				game.player.startClimb();
				break;
			case KeyEvent.VK_A:
				game.player.startLeft(e.isShiftDown());
				break;
			case KeyEvent.VK_D:
				game.player.startRight(e.isShiftDown());
				break;
			}
		}
		
		/**
		 * Notification from AWT that a key has been released.
		 * 
		 * @param e
		 *            The details of the key that was released
		 */
		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
			case KeyEvent.VK_SPACE:
				game.player.endClimb();
				break;
			case KeyEvent.VK_A:
				game.player.stopLeft();
				break;
			case KeyEvent.VK_D:
				game.player.stopRight();
				break;
			case KeyEvent.VK_ESCAPE:
				if (game.inventory.isVisible()) {
					game.inventory.setVisible(false);
				} else {
					game.goToMainMenu();
				}
				break;
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			switch (e.getKeyChar()) {
			case '1':
			case '2': // these all fall through to 9
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				game.setInventorySelect(e.getKeyChar() - '1');
				break;
			case '0':
				game.setInventorySelect(9);
				break;
			case 'e':
				game.inventory.setVisible(!game.inventory.isVisible());
				break;
			case '=':
				game.zoom(1);
				break;
			case 'p':
				game.paused = !game.paused;
				break;
			case 'm':
				game.musicPlayer.toggleSound();
				break;
			case 'o':
				game.zoom(0);
				break;
			case '-':
				game.zoom(-1);
				break;
			case 'f':
				game.viewFPS = !game.viewFPS;
				break;
			case 'q':
				game.tossItem();
				break;
			}
		}
	}
}
