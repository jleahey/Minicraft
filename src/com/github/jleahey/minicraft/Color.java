package com.github.jleahey.minicraft;

public class Color {
	// From AWT Color constant values
	public static final Color white = new Color(255, 255, 255);
	public static final Color darkGray = new Color(64, 64, 64);
	public static final Color black = new Color(0, 0, 0);
	public static final Color green = new Color(0, 255, 0);
	public static final Color gray = new Color(128, 128, 128);
	public static final Color blue = new Color(0, 0, 255);
	public static final Color LIGHT_GRAY = new Color(192, 192, 192);
	public static final Color DARK_GRAY = darkGray;
	public static final Color orange = new Color(255, 200, 0);
	
	public byte R, G, B;
	
	public Color(int R, int G, int B) {
		this.R = (byte) R;
		this.G = (byte) G;
		this.B = (byte) B;
	}
}
