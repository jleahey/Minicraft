package com.github.jleahey.minicraft;

import com.github.jleahey.minicraft.awtgraphics.AwtGraphicsHandler;

public abstract class GraphicsHandler {
	public static final boolean awtMode = true;
	
	protected static int screenWidth = 640;
	protected static int screenHeight = 480;
	
	private static GraphicsHandler single;
	
	public int getScreenWidth() {
		return screenWidth;
	}
	
	public int getScreenHeight() {
		return screenHeight;
	}
	
	public static GraphicsHandler get() {
		if (single == null) {
			if (awtMode) {
				single = new AwtGraphicsHandler();
			} else {
				// android!
			}
		}
		return single;
	}
	
	public abstract void init(Game game);
	
	public abstract void startDrawing();
	
	public abstract void finishDrawing();
	
	public abstract void setColor(Color color);
	
	public abstract void fillRect(int x, int y, int width, int height);
	
	public abstract void drawString(String string, int x, int y);
	
	public abstract void fillOval(int x, int y, int width, int height);
	
	public abstract void drawImage(Sprite sprite, int x, int y);
	
	public abstract void drawImage(Sprite sprite, int x, int y, int width, int height);
}
