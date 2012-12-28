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

import java.util.Random;

public class World implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	public Tile[][] tiles;
	public int width;
	public int height;
	public Int2 spawnLocation;
	
	private int chunkNeedsUpdate;
	private int chunkCount;
	private int chunkWidth = 16;
	private boolean chunkFillRight = true;
	private Random random;
	private long ticksAlive = 0;
	private final int dayLength = 20000;
	private LightingEngine lightingEngine;
	
	// private int[] columnHeights;
	
	public World(int width, int height, Random random) {
		
		char[][] generated = WorldGenerator.generate(width, height, random);
		WorldGenerator.visibility = null;
		this.spawnLocation = WorldGenerator.playerLocation;
		tiles = new Tile[width][height];
		// columnHeights = new int[width];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Tile tile = Constants.tileTypes.get(generated[i][j]);
				if (tile == null) {
					tiles[i][j] = Constants.tileTypes.get('a');
				} else {
					tiles[i][j] = Constants.tileTypes.get(generated[i][j]);
				}
				
			}
		}
		this.width = width;
		this.height = height;
		this.chunkCount = (int) Math.ceil((double) width / chunkWidth);
		this.chunkNeedsUpdate = 0;
		this.random = random;
		lightingEngine = new LightingEngine(width, height, tiles);
	}
	
	public void chunkUpdate() {
		ticksAlive++;
		for (int i = 0; i < chunkWidth; i++) {
			boolean isDirectLight = true;
			// int lightRayValue = Constants.LIGHT_VALUE_SUN;
			for (int j = 0; j < height; j++) {
				int x = i + chunkWidth * chunkNeedsUpdate;
				if (x >= width || x < 0) {
					continue;
				}
				int y = j;
				if (!chunkFillRight) {
					x = width - 1 - x;
					y = height - 1 - y;
				}
				if (isDirectLight && tiles[x][y].type.name == 'd') {
					if (random.nextDouble() < .005) {
						tiles[x][y] = Constants.tileTypes.get('g');
					}
				} else if (tiles[x][y].type.name == 'g' && tiles[x][y - 1].type.name != 'a'
						&& tiles[x][y - 1].type.name != 'l' && tiles[x][y - 1].type.name != 'w') {
					if (random.nextDouble() < .25) {
						tiles[x][y] = Constants.tileTypes.get('d');
					}
				} else if (tiles[x][y].type.name == 'n') {
					if (isAir(x, y + 1) || isLiquid(x, y + 1)) {
						changeTile(x, y + 1, tiles[x][y]);
						changeTile(x, y, Constants.tileTypes.get('a'));
					}
				} else if (tiles[x][y].type.name == 'S') {
					if (random.nextDouble() < .01) {
						addTemplate(TileTemplate.tree, x, y);
					}
				} else if (tiles[x][y].type.liquid) {
					if (isAir(x + 1, y)) {
						changeTile(x + 1, y, tiles[x][y]);
					}
					if (isAir(x - 1, y)) {
						changeTile(x - 1, y, tiles[x][y]);
					}
					if (isAir(x, y + 1)) {
						changeTile(x, y + 1, tiles[x][y]);
					}
				}
				// update lighting and visibility
				// lightRayValue -= tiles[x][y].type.lightBlocking;
				// if (lightRayValue < 0)
				// lightRayValue = 0;
				// if (lightValues[x][y] < lightRayValue)
				// lightValues[x][y] = lightRayValue;
				// if (tiles[x][y].type.lightBlocking < lightRayValue)
				// spreadLighting(x, y, lightValues[x][y]);
				
				if ((!tiles[x][y].type.passable || tiles[x][y].type.liquid)
						&& tiles[x][y].type.name != 'l') {
					isDirectLight = false;
				}
			}
		}
		chunkNeedsUpdate = (chunkNeedsUpdate + 1) % chunkCount;
		if (chunkNeedsUpdate == 0) {
			chunkFillRight = !chunkFillRight;
		}
		
	}
	
	private void addTemplate(TileTemplate tileTemplate, int x, int y) {
		for (int i = 0; i < tileTemplate.template.length; i++) {
			for (int j = 0; j < tileTemplate.template[0].length; j++) {
				if (tileTemplate.template[i][j] != 0 && x - tileTemplate.spawnY + i >= 0
						&& x - tileTemplate.spawnY + i < tiles.length
						&& y - tileTemplate.spawnX + j >= 0
						&& y - tileTemplate.spawnX + j < tiles[0].length) {
					addTile(x - tileTemplate.spawnY + i, y - tileTemplate.spawnX + j,
							tileTemplate.template[i][j]);
				}
			}
		}
	}
	
	// private void spreadLighting(int x, int y, int currentLighting) {
	// if (currentLighting > 0)
	// currentLighting -= 1;
	// setLighting(x + 1, y, currentLighting);
	// setLighting(x, y + 1, currentLighting);
	// setLighting(x - 1, y, currentLighting);
	// setLighting(x, y - 1, currentLighting);
	// }
	
	// private void setLighting(int x, int y, int lightValue) {
	// if (x < 0 || x >= width || y < 0 || y >= height || lightValue < lightValues[x][y]) {
	// return;
	// }
	// lightValues[x][y] = lightValue;
	// }
	
	public boolean addTile(int x, int y, char name) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		Tile tile = Constants.tileTypes.get(name);
		if (tile == null) {
			return false;
		}
		if (name == 'S' && y + 1 < height) {
			if (tiles[x][y + 1].type.name != 'd' && tiles[x][y + 1].type.name != 'g') {
				return false;
			}
		}
		tiles[x][y] = tile;
		lightingEngine.addedTile(x, y);
		return true;
	}
	
	public char removeTile(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return 0;
		}
		char name = tiles[x][y].type.name;
		tiles[x][y] = Constants.tileTypes.get('a');
		lightingEngine.removedTile(x, y);
		return name;
	}
	
	public void changeTile(int x, int y, Tile tile) {
		tiles[x][y] = tile;
	}
	
	private char[] breakWood = new char[] { 'w', 'p', 'f' };
	private char[] breakStone = new char[] { 's', 'b', 'c' };
	private char[] breakMetal = new char[] { 'i' };
	private char[] breakDiamond = new char[] { 'm' };
	
	public int breakTicks(int x, int y, Item item) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return Integer.MAX_VALUE;
		}
		char currentName = tiles[x][y].type.name;
		
		char[] breakType = null; // hand breakable by all
		for (char element : breakWood) {
			if (element == currentName) {
				breakType = breakWood;
			}
		}
		for (char element : breakStone) {
			if (element == currentName) {
				breakType = breakStone;
			}
		}
		for (char element : breakMetal) {
			if (element == currentName) {
				breakType = breakMetal;
			}
		}
		for (char element : breakDiamond) {
			if (element == currentName) {
				breakType = breakDiamond;
			}
		}
		if (item == null || item.getClass() != Tool.class) {
			return handResult(breakType);
		}
		Tool tool = (Tool) item;
		if (breakType == breakWood && tool.toolType == Tool.ToolType.Axe) {
			return (int) (getSpeed(tool) * 20);
		} else if (breakType != breakWood && breakType != null
				&& tool.toolType == Tool.ToolType.Pick) {
			return (int) (getSpeed(tool) * 25);
		} else if (breakType == null && tool.toolType == Tool.ToolType.Shovel) {
			return (int) (getSpeed(tool) * 15);
		} else {
			return handResult(breakType);
		}
		
	}
	
	private double getSpeed(Tool tool) {
		// if(tool == null)
		// return 5;
		if (tool.toolPower == Tool.ToolPower.Wood) {
			return 3;
		} else if (tool.toolPower == Tool.ToolPower.Stone) {
			return 2.5;
		} else if (tool.toolPower == Tool.ToolPower.Metal) {
			return 2;
		} else {
			return 1;
		}
	}
	
	private int handResult(char[] breakType) {
		if (breakType == null) {
			return 50;
		} else if (breakType == breakWood) {
			return 75;
		} else {
			return 500;
		}
	}
	
	public void draw(GraphicsHandler g, int x, int y, int screenWidth, int screenHeight,
			float cameraX, float cameraY, int tileSize) {
		Int2 pos;
		
		pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, screenWidth, screenHeight,
				tileSize, 0, height / 2);
		g.setColor(Color.darkGray);
		g.fillRect(pos.x, pos.y, width * tileSize, height * tileSize / 2);
		
		pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, screenWidth, screenHeight,
				tileSize, 0, 0);
		g.setColor(getSkyColor());
		g.fillRect(pos.x, pos.y, width * tileSize, height * tileSize / 2 - 1);
		for (int i = 0; i < width; i++) {
			int posX = (int) ((i - cameraX) * tileSize);
			int posY = (int) ((height - cameraY) * tileSize);
			if (posX < 0 - tileSize || posX > screenWidth || posY < 0 - tileSize
					|| posY > screenHeight) {
				continue;
			}
			Constants.tileTypes.get('x').type.sprite.draw(g, posX, posY, tileSize, tileSize);
		}
		
		for (int j = height / 2; j < height; j++) {
			int posX = (int) ((-1 - cameraX) * tileSize);
			int posY = (int) ((j - cameraY) * tileSize);
			if (!(posX < 0 - tileSize || posX > screenWidth || posY < 0 - tileSize || posY > screenHeight)) {
				Constants.tileTypes.get('x').type.sprite.draw(g, posX, posY, tileSize, tileSize);
			}
			
			posX = (int) ((width - cameraX) * tileSize);
			if (!(posX < 0 - tileSize || posX > screenWidth)) {
				Constants.tileTypes.get('x').type.sprite.draw(g, posX, posY, tileSize, tileSize);
			}
		}
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int posX = (int) ((i - cameraX) * tileSize);
				int posY = (int) ((j - cameraY) * tileSize);
				if (posX < 0 - tileSize || posX > screenWidth || posY < 0 - tileSize
						|| posY > screenHeight) {
					continue;
				}
				
				int lightIntensity = lightingEngine.getLightValue(i, j) * 255
						/ Constants.LIGHT_VALUE_SUN;
				Color tint = new Color(0, 0, 0, 255 - lightIntensity);
				
				if (tiles[i][j].type.name != 'a') {
					tiles[i][j].type.sprite.draw(g, posX, posY, tileSize, tileSize, tint);
				} else {
					g.setColor(tint);
					g.fillRect(posX, posY, tileSize, tileSize);
				}
			}
		}
	}
	
	public boolean passable(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		return tiles[x][y].type == null || tiles[x][y].type.passable;
	}
	
	public boolean isLiquid(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		return tiles[x][y].type != null && tiles[x][y].type.liquid;
	}
	
	public boolean isAir(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		return tiles[x][y].type != null && tiles[x][y].type.name == 'a';
	}
	
	public boolean isBreakable(int x, int y) {
		return !(isAir(x, y) || isLiquid(x, y));
	}
	
	public boolean isClimbable(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		return tiles[x][y].type != null
				&& (tiles[x][y].type.name == 'w' || tiles[x][y].type.name == 'p'
						|| tiles[x][y].type.name == 'L' || tiles[x][y].type.liquid);
	}
	
	public boolean isCraft(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		return tiles[x][y].type != null && (tiles[x][y].type.name == 'f');
	}
	
	// returns a float in the range [0,1)
	// 0 is dawn, 0.25 is noon, 0.5 is dusk, 0.75 is midnight
	public float getTimeOfDay() {
		return ((float) (ticksAlive % dayLength)) / dayLength;
	}
	
	public boolean isNight() {
		return getTimeOfDay() > 0.5f;
	}
	
	static final Color dawnSky = new Color(255, 217, 92);
	static final Color noonSky = new Color(132, 210, 230);
	static final Color duskSky = new Color(245, 92, 32);
	static final Color midnightSky = new Color(0, 0, 0);
	
	public Color getSkyColor() {
		float time = getTimeOfDay();
		if (time < 0.25f) {
			return dawnSky.interpolateTo(noonSky, 4 * time);
		} else if (time < 0.5f) {
			return noonSky.interpolateTo(duskSky, 4 * (time - 0.25f));
		} else if (time < 0.75f) {
			return duskSky.interpolateTo(midnightSky, 4 * (time - 0.5f));
		} else {
			return midnightSky.interpolateTo(dawnSky, 4 * (time - 0.75f));
		}
	}
	
}
