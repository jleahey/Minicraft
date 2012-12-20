package com.github.jleahey.minicraft.awtgraphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class AwtGraphicsHandler extends com.github.jleahey.minicraft.GraphicsHandler {
	Canvas canvas;
	private BufferStrategy strategy;
	private JFrame container;
	private Cursor myCursor = null;
	private JPanel panel;
	
	@Override
	public void init() {
		canvas = new Canvas();
		// TODO Auto-generated method stub
		// create a frame to contain our game
		container = new JFrame("Minicraft");
		
		try {
			ImageIcon ii = new ImageIcon(new URL("file:sprites/other/mouse.png"));
			Image im = ii.getImage();
			Toolkit tk = canvas.getToolkit();
			myCursor = tk.createCustomCursor(im, new Point(0, 0), "MyCursor");
		} catch (Exception e) {
			System.out.println("myCursor creation failed " + e);
		}
		
		// get hold the content of the frame and set up the resolution of the game
		panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(screenWidth, screenHeight));
		panel.setLayout(null);
		panel.setCursor(myCursor);
		panel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension d = e.getComponent().getSize();
				canvas.setBounds(0, 0, d.width, d.height);
				screenWidth = d.width;
				screenHeight = d.height;
			}
		});
		
		// setup our canvas size and put it into the content of the frame
		canvas.setBounds(0, 0, screenWidth + 10, screenHeight + 10);
		panel.add(canvas);
		
		// Tell AWT not to bother repainting our canvas since we're
		// going to do that our self in accelerated mode
		canvas.setIgnoreRepaint(true);
		
		// finally make the window visible
		container.pack();
		container.setResizable(true);
		container.setVisible(true);
		
		// add a listener to respond to the user closing the window. If they
		// do we'd like to exit the game
		//TODO: add this back in
//		container.addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosing(WindowEvent e) {
//				zoom(0);
//				SaveLoad.doSave(getGame());
//				musicPlayer.close();
//				System.exit(0);
//			}
//		});
		
		// add a key input system (defined below) to our canvas
		// so we can respond to key pressed
//		canvas.addKeyListener(new KeyInputHandler());
//		canvas.addMouseListener(new MouseInputHander());
//		canvas.addMouseWheelListener(new MouseWheelInputHander());
//		canvas.addMouseMotionListener(new MouseMoveInputHander());
		// request the focus so key events come to us
		canvas.requestFocus();
		
		// create the buffering strategy which will allow AWT
		// to manage our accelerated graphics
		canvas.createBufferStrategy(2);
		strategy = canvas.getBufferStrategy();
	}
	
	Graphics2D g;
	
	@Override
	public void startDrawing() {
		// Get hold of a graphics context for the accelerated
		// surface and blank it out
		g = (Graphics2D) strategy.getDrawGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, screenWidth, screenHeight);
					
	}
	
	@Override
	public void finishDrawing() {
		g.dispose();
		strategy.show();	
	}
	
	@Override
	public void setColor(com.github.jleahey.minicraft.Color color) {
		//TODO: Profile, this might be quite slow "new" every color change
		g.setColor(new Color(color.R, color.G, color.B));
	}
	
	@Override
	public void fillRect(int x, int y, int width, int height) {
		g.fillRect(x, y, width, height);
	}
	
	@Override
	public void drawString(String string, int x, int y) {
		g.drawString(string, x, y);
	}
	
	@Override
	public void fillOval(int x, int y, int width, int height) {
		g.fillOval(x, y, width, height);
	}
//	
//	private class MouseWheelInputHander implements MouseWheelListener {
//		@Override
//		public void mouseWheelMoved(MouseWheelEvent e) {
//			int notches = e.getWheelRotation();
//			inventory.selectedInventory += notches;
//			if (inventory.selectedInventory < 0) {
//				inventory.selectedInventory = 0;
//			} else if (inventory.selectedInventory > 9) {
//				inventory.selectedInventory = 9;
//			}
//		}
//	}
//	
//	private class MouseMoveInputHander implements MouseMotionListener {
//		@Override
//		public void mouseDragged(MouseEvent arg0) {
//			screenMouseX = arg0.getX();
//			screenMouseY = arg0.getY();
//		}
//		
//		@Override
//		public void mouseMoved(MouseEvent arg0) {
//			screenMouseX = arg0.getX();
//			screenMouseY = arg0.getY();
//		}
//	}
//	
//	private class MouseInputHander extends MouseAdapter {
//		@Override
//		public void mousePressed(MouseEvent arg0) {
//			switch (arg0.getButton()) {
//			case MouseEvent.BUTTON1:
//				leftClick = true;
//				break;
//			case MouseEvent.BUTTON2: // fall through
//			case MouseEvent.BUTTON3:
//				rightClick = true;
//				break;
//			}
//		}
//		
//		@Override
//		public void mouseReleased(MouseEvent arg0) {
//			switch (arg0.getButton()) {
//			case MouseEvent.BUTTON1:
//				leftClick = false;
//				break;
//			case MouseEvent.BUTTON2: // fall through
//			case MouseEvent.BUTTON3:
//				rightClick = false;
//				break;
//			}
//		}
//	}
//	
//	private class KeyInputHandler extends KeyAdapter {
//		static final char ESCAPE = (char) 27;
//		
//		/**
//		 * Notification from AWT that a key has been pressed. Note that
//		 * a key being pressed is equal to being pushed down but *NOT*
//		 * released. Thats where keyTyped() comes in.
//		 * 
//		 * @param e
//		 *            The details of the key that was pressed
//		 */
//		@Override
//		public void keyPressed(KeyEvent e) {
//			switch (e.getKeyCode()) {
//			case KeyEvent.VK_W:
//				player.startClimb();
//				break;
//			case KeyEvent.VK_A:
//				player.startLeft(e.isShiftDown());
//				break;
//			case KeyEvent.VK_D:
//				player.startRight(e.isShiftDown());
//				break;
//			case KeyEvent.VK_SPACE:
//				spaceBar = true;
//				break;
//			}
//		}
//		
//		/**
//		 * Notification from AWT that a key has been released.
//		 * 
//		 * @param e
//		 *            The details of the key that was released
//		 */
//		@Override
//		public void keyReleased(KeyEvent e) {
//			switch (e.getKeyCode()) {
//			case KeyEvent.VK_W:
//				player.endClimb();
//				break;
//			case KeyEvent.VK_A:
//				player.stopLeft();
//				break;
//			case KeyEvent.VK_D:
//				player.stopRight();
//				break;
//			case KeyEvent.VK_SPACE:
//				spaceBar = false;
//				break;
//			}
//		}
//		
//		@Override
//		public void keyTyped(KeyEvent e) {
//			switch (e.getKeyChar()) {
//			case '1':
//			case '2': // these all fall through to 9
//			case '3':
//			case '4':
//			case '5':
//			case '6':
//			case '7':
//			case '8':
//			case '9':
//				setInventorySelect(e.getKeyChar() - '1');
//				break;
//			case '0':
//				setInventorySelect(9);
//				break;
//			case 'e':
//				inventory.setVisible(!inventory.isVisible());
//				break;
//			case '=':
//				zoom(1);
//				break;
//			case 'p':
//				paused = !paused;
//				break;
//			case 'm':
//				musicPlayer.toggleSound();
//				break;
//			case 'o':
//				zoom(0);
//				break;
//			case '-':
//				zoom(-1);
//				break;
//			case 'f':
//				viewFPS = !viewFPS;
//				break;
//			case 'q':
//				InventoryItem inventoryItem = inventory.selectedItem();
//				if (!inventoryItem.isEmpty()) {
//					Item newItem = inventoryItem.getItem();
//					if (!(newItem instanceof Tool)) {
//						newItem = (Item) newItem.clone();
//					}
//					inventoryItem.remove(1);
//					if (player.facingRight) {
//						newItem.x = player.x + 1 + random.nextFloat();
//					} else {
//						newItem.x = player.x - 1 - random.nextFloat();
//					}
//					;
//					newItem.y = player.y;
//					newItem.dy = -.1f;
//					entities.add(newItem);
//				}
//				break;
//			case ESCAPE:
//				zoom(0);
//				SaveLoad.doSave(getGame());
//				musicPlayer.close();
//				System.exit(0);
//			}
//		}
//	}
	
}
