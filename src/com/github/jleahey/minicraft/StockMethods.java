/*
 * Copyright 2012 Jonathan Leahey
 * 
 * This file is part of Minicraft
 * 
 * Minicraft is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Minicraft is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Minicraft. If not, see http://www.gnu.org/licenses/.
 */

package com.github.jleahey.minicraft;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public final class StockMethods {
	public static Boolean onScreen = true;
	public static Int2 pos = new Int2(0, 0);
	
	public static Int2 computeDrawLocationInPlace(float cameraX, float cameraY, int width,
			int height, int tileSize, float positionX, float positionY) {
		StockMethods.pos.x = Math.round((positionX - cameraX) * tileSize);
		StockMethods.pos.y = Math.round((positionY - cameraY) * tileSize);
		onScreen = !(pos.x + tileSize < 0 || pos.x > width * tileSize || pos.y + tileSize < 0 || pos.y > height
				* tileSize);
		return StockMethods.pos;
	}
	
	public static String readFile(String pathname) throws IOException {
		// NOTE: drops newlines
		StringBuilder fileContents = new StringBuilder();
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(pathname);
		if (in == null) {
			throw new IOException("no resource found at " + pathname);
		}
		Scanner scanner = new Scanner(in);
		try {
			while (scanner.hasNextLine()) {
				fileContents.append(scanner.nextLine());
			}
			return fileContents.toString();
		} finally {
			scanner.close();
		}
	}
	
	/**
	 * Smoothly interpolates between edge0 and edge1 by x
	 * 
	 * This function plays like a sigmoid but is easier to compute
	 * @param edge0
	 * @param edge1
	 * @param x
	 */
	public static float smoothStep(float edge0, float edge1, float x) {
		float t = clamp((x - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}
	
	/**
	 * Clamps x to values [a,b]
	 * @param x
	 * @param a
	 * @param b
	 */
	public static float clamp(float x, float a, float b) {
		if (x < a) {
			return a;
		} else if (x > b) {
			return b;
		} else {
			return x;
		}
	}
}
