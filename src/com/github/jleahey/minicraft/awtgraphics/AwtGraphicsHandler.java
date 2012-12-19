package com.github.jleahey.minicraft.awtgraphics;

import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.jleahey.minicraft.SaveLoad;

public class AwtGraphicsHandler extends com.github.jleahey.minicraft.GraphicsHandler {

	Canvas canvas;
	
	private BufferStrategy strategy;
	private JFrame container;
	private Cursor myCursor = null;
	private JPanel panel;
	@Override
	public void init(int screenWidth, int screenHeight) {
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
		container.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				zoom(0);
				SaveLoad.doSave(getGame());
				musicPlayer.close();
				System.exit(0);
			}
		});
		
		// add a key input system (defined below) to our canvas
		// so we can respond to key pressed
		addKeyListener(new KeyInputHandler());
		addMouseListener(new MouseInputHander());
		addMouseWheelListener(new MouseWheelInputHander());
		addMouseMotionListener(new MouseMoveInputHander());
		// request the focus so key events come to us
		canvas.requestFocus();
		
		// create the buffering strategy which will allow AWT
		// to manage our accelerated graphics
		canvas.createBufferStrategy(2);
		strategy = canvas.getBufferStrategy();
	}
}
