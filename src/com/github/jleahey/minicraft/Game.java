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

//import java.awt.Canvas;
//import java.awt.Color;
//import java.awt.Cursor;
//import java.awt.Dimension;
//import java.awt.Graphics2D;
//import java.awt.Image;
//import java.awt.Point;
//import java.awt.Toolkit;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseMotionListener;
//import java.awt.event.MouseWheelEvent;
//import java.awt.event.MouseWheelListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

//import javax.swing.ImageIcon;
//import javax.swing.JFrame;
//import javax.swing.JPanel;

//import com.github.jleahey.minicraft.awtgraphics.Sprite;
//import com.github.jleahey.minicraft.awtgraphics.SpriteStore;

public class Game {
	
	private int worldWidth = 512;
	private int worldHeight = 256;
	private boolean gameRunning = true;
	private boolean spaceBar = false;
	private boolean leftClick = false;
	private boolean rightClick = false;
	private boolean paused = true;
	
	private Map<Character, Item> itemTypes;
	
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	
	private int tileSize = 32;
	public Inventory inventory;
	
	private int breakingTicks;
	private Int2 breakingPos;
	
	/* menu sprites */
	private final Sprite menu_bgTile = SpriteStore.get().getSprite("sprites/tiles/dirt.png");
	private final Sprite menu_logo = SpriteStore.get().getSprite("sprites/menus/title.png");
	private final Sprite menu_newUp = SpriteStore.get().getSprite("sprites/menus/new_up.png");
	private final Sprite menu_newDown = SpriteStore.get().getSprite("sprites/menus/new_down.png");
	private final Sprite menu_loadUp = SpriteStore.get().getSprite("sprites/menus/load_up.png");
	private final Sprite menu_loadDown = SpriteStore.get().getSprite("sprites/menus/load_down.png");
	private final Sprite menu_miniUp = SpriteStore.get().getSprite("sprites/menus/mini_up.png");
	private final Sprite menu_mediumUp = SpriteStore.get().getSprite("sprites/menus/med_up.png");
	private final Sprite menu_bigUp = SpriteStore.get().getSprite("sprites/menus/big_up.png");
	private final Sprite menu_miniDown = SpriteStore.get().getSprite("sprites/menus/mini_down.png");
	private final Sprite menu_mediumDown = SpriteStore.get().getSprite(
			"sprites/menus/mini_down.png");
	private final Sprite menu_bigDown = SpriteStore.get().getSprite("sprites/menus/big_down.png");
	private final Sprite menu_tag = SpriteStore.get().getSprite("sprites/menus/tag.png");
	private final int menu_miniWidth = 256;
	private final int menu_mediumWidth = 512;
	private final int menu_bigWidth = 1024;
	
	private Sprite builderIcon;
	private Sprite minerIcon;
	private Sprite[] breakingSprites;
	
	private boolean viewFPS = false;
	private boolean startMenu = true;
	private boolean newMenu = false;
	private long ticksRunning;
	private Random random = new Random();
	
	private Player player;
	public World world;
	
	private MusicPlayer musicPlayer = new MusicPlayer("sounds/music.ogg");
	private int screenMouseX;
	private int screenMouseY;
	
	/**
	 * Construct our game and set it running.
	 */
	public Game() {
		GraphicsHandler.get().init();
		itemTypes = ItemLoader.loadItems(tileSize / 2);
		System.gc();
	}
	
	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
	private void startGame(boolean load, int width) {
		System.out.println("Creating world width: " + width);
		worldWidth = width;
		
		entities.clear();
		if (load && SaveLoad.doLoad(this)) {
			for (Entity entity : entities) {
				if (entity instanceof Player) {
					player = (Player) entity;
					player.widthPX = 7 * (tileSize / 8);
					player.heightPX = 14 * (tileSize / 8);
				}
			}
		}
		if (player == null) // didn't load a saved game
		{
			world = new World(worldWidth, worldHeight, random);
			player = new Player(true, world.spawnLocation.x, world.spawnLocation.y,
					7 * (tileSize / 8), 14 * (tileSize / 8));
			entities.add(player);
		}
		
		builderIcon = SpriteStore.get().getSprite("sprites/other/builder.png");
		minerIcon = SpriteStore.get().getSprite("sprites/other/miner.png");
		
		if (inventory == null) {
			inventory = new Inventory(10, 4, 3, itemTypes);
		}
		
		musicPlayer.play();
		
		breakingSprites = new Sprite[8];
		for (int i = 0; i < 8; i++) {
			breakingSprites[i] = SpriteStore.get().getSprite("sprites/tiles/break" + i + ".png");
		}
		
		System.gc();
	}
	
	public void drawStartMenu(GraphicsHandler g) {
		drawTileBackground(g, menu_bgTile, 32);
		drawCenteredX(g, menu_logo, 70, 397, 50);
		drawCenteredX(g, menu_newUp, 200, 160, 64);
		drawCenteredX(g, menu_loadUp, 300, 160, 64);
		float pixels = (Math.abs((float) ((ticksRunning % 100) - 50) / 50) + 1);
		menu_tag.draw(g, 450, 70, (int) (60 * pixels), (int) (37 * pixels));
		if (!leftClick) {
			return;
		}
		
		if (screenMouseY >= 300) {
			leftClick = false;
			startMenu = false;
			startGame(true, menu_mediumWidth);
		} else if (screenMouseY < 300) {
			leftClick = false;
			startMenu = false;
			newMenu = true;
		}
	}
	
	public void drawNewMenu(GraphicsHandler g) {
		drawTileBackground(g, menu_bgTile, 32);
		drawCenteredX(g, menu_logo, 70, 397, 50);
		drawCenteredX(g, menu_miniUp, 150, 160, 64);
		drawCenteredX(g, menu_mediumUp, 250, 160, 64);
		drawCenteredX(g, menu_bigUp, 350, 160, 64);
		float pixels = (Math.abs((float) ((ticksRunning % 100) - 50) / 50) + 1);
		menu_tag.draw(g, 450, 70, (int) (60 * pixels), (int) (37 * pixels));
		if (!leftClick) {
			return;
		}
		
		if (screenMouseY >= 350) {
			leftClick = false;
			startMenu = false;
			newMenu = false;
			startGame(false, menu_bigWidth);
		} else if (screenMouseY >= 250) {
			leftClick = false;
			startMenu = false;
			newMenu = false;
			startGame(false, menu_mediumWidth);
		} else if (screenMouseY >= 150) {
			leftClick = false;
			startMenu = false;
			newMenu = false;
			startGame(false, menu_miniWidth);
		}
	}
	
	public void drawCenteredX(GraphicsHandler g, Sprite s, int top, int width, int height) {
		s.draw(g, GraphicsHandler.get().getScreenWidth() / 2 - width / 2, top, width, height);
	}
	
	public void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();
		
		// keep looping round till the game ends
		while (gameRunning) {
			ticksRunning++;
			long delta = SystemTimer.getTime() - lastLoopTime;
			lastLoopTime = SystemTimer.getTime();
			
			GraphicsHandler g = GraphicsHandler.get();
			g.startDrawing();
			
			if (startMenu || newMenu) {
				if (startMenu) {
					drawStartMenu(g);
				} else {
					drawNewMenu(g);
				}
				
				GraphicsHandler.get().finishDrawing();
				
				SystemTimer.sleep(lastLoopTime + 16 - SystemTimer.getTime());
				continue;
			}
			
			float cameraX = player.x - GraphicsHandler.get().getScreenWidth() / tileSize / 2;
			float cameraY = player.y - GraphicsHandler.get().getScreenHeight() / tileSize / 2;
			
			world.chunkUpdate();
			world.draw(GraphicsHandler.get(), 0, 0, GraphicsHandler.get().getScreenWidth(),
					GraphicsHandler.get().getScreenHeight(), cameraX, cameraY, tileSize);
			
			boolean inventoryFocus = inventory.updateInventory(GraphicsHandler.get()
					.getScreenWidth(), GraphicsHandler.get().getScreenHeight(), screenMouseX,
					screenMouseY, leftClick, rightClick);
			if (inventoryFocus) {
				leftClick = false;
				rightClick = false;
			}
			
			if (spaceBar) {
				player.jump(world, tileSize);
			}
			
			if (leftClick && player.handBreakPos.x != -1) {
				if (player.handBreakPos.equals(breakingPos)) {
					breakingTicks++;
				} else {
					breakingTicks = 0;
				}
				breakingPos = player.handBreakPos;
				
				InventoryItem inventoryItem = inventory.selectedItem();
				Item item = inventoryItem.getItem();
				int ticksNeeded = world.breakTicks(breakingPos.x, breakingPos.y, item);
				
				Int2 pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize,
						tileSize, tileSize, breakingPos.x, breakingPos.y);
				int sprite_index = (int) (Math.min(1, (double) breakingTicks / ticksNeeded) * (breakingSprites.length - 1));
				breakingSprites[sprite_index].draw(g, pos.x, pos.y, tileSize, tileSize);
				
				if (breakingTicks >= ticksNeeded) {
					if (item != null && item.getClass() == Tool.class) {
						Tool tool = (Tool) item;
						tool.uses++;
						if (tool.uses >= tool.totalUses) {
							inventoryItem.setEmpty();
						}
					}
					
					breakingTicks = 0;
					char name = world.removeTile(player.handBreakPos.x, player.handBreakPos.y);
					if (name == 'g') {
						name = 'd';
					}
					if (name == 's') {
						name = 'b';
					}
					if (name == 'l' && random.nextDouble() < .1) {
						name = 'S';
					}
					Item newItem = itemTypes.get(name);
					if (newItem != null) // couldn't find that item
					{
						newItem = (Item) newItem.clone();
						newItem.x = player.handBreakPos.x + random.nextFloat()
								* (1 - (float) newItem.widthPX / tileSize);
						newItem.y = player.handBreakPos.y + random.nextFloat()
								* (1 - (float) newItem.widthPX / tileSize);
						newItem.dy = -.07f;
						entities.add(newItem);
					}
				}
			} else {
				breakingTicks = 0;
			}
			
			if (rightClick) {
				if (world.isCraft(player.handBreakPos.x, player.handBreakPos.y)) {
					inventory.tableSizeAvailable = 3;
					inventory.setVisible(true);
				} else {
					rightClick = false;
					InventoryItem current = inventory.selectedItem();
					if (!current.isEmpty()) {
						int left = (int) player.getLeft(tileSize);
						int right = (int) player.getRight(tileSize);
						int top = (int) player.getTop(tileSize);
						int bottom = (int) player.getBottom(tileSize);
						
						if (!(player.handBuildPos.x >= left && player.handBuildPos.x <= right
								&& player.handBuildPos.y >= top && player.handBuildPos.y <= bottom)) {
							boolean placed = world.addTile(player.handBuildPos.x,
									player.handBuildPos.y, (char) current.getItem().item_id);
							if (placed) {
								inventory.decreaseSeleted(1);
							}
						}
					}
				}
			}
			
			float worldMouseX = (cameraX * tileSize + screenMouseX) / tileSize;
			float worldMouseY = (cameraY * tileSize + screenMouseY) / tileSize - .5f;
			player.updateHand(g, cameraX, cameraY, worldMouseX, worldMouseY, world, tileSize);
			
			java.util.Iterator<Entity> it = entities.iterator();
			while (it.hasNext()) {
				Entity entity = it.next();
				if (entity != player && player.collidesWith(entity, tileSize)) {
					if (entity instanceof Item || entity instanceof Tool) {
						addToInventory(((Item) entity));
					}
					it.remove();
					continue;
				}
				entity.updatePosition(world, tileSize);
				entity.draw(g, cameraX, cameraY, GraphicsHandler.get().getScreenWidth(),
						GraphicsHandler.get().getScreenHeight(), tileSize);
			}
			
			if (viewFPS) {
				String fps = "Fps: " + 1 / ((float) delta / 1000) + "("
						+ Runtime.getRuntime().freeMemory() / 1024 / 1024 + " / "
						+ Runtime.getRuntime().totalMemory() / 1024 / 1024 + ") Free MB";
				g.setColor(Color.white);
				g.drawString(fps, 10, 10);
			}
			
			// Draw the UI
			if (player.handBreakPos.x != -1) {
				Int2 pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize,
						tileSize, tileSize, player.handBuildPos.x, player.handBuildPos.y);
				builderIcon.draw(g, pos.x, pos.y, tileSize, tileSize);
				
				pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize, tileSize,
						tileSize, player.handBreakPos.x, player.handBreakPos.y);
				minerIcon.draw(g, pos.x, pos.y, tileSize, tileSize);
			}
			
			inventory.draw(g, GraphicsHandler.get().getScreenWidth(), GraphicsHandler.get()
					.getScreenHeight());
			
			Int2 mouseTest = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize,
					tileSize, tileSize, worldMouseX, worldMouseY);
			g.setColor(Color.white);
			g.fillOval(mouseTest.x - 4, mouseTest.y - 4, 8, 8);
			g.setColor(Color.black);
			g.fillOval(mouseTest.x - 3, mouseTest.y - 3, 6, 6);
			
			g.finishDrawing();
			
			SystemTimer.sleep(lastLoopTime + 16 - SystemTimer.getTime());
		}
	}
	
	private void addToInventory(Item item) {
		inventory.addItem(item, 1);
	}
	
	private void setInventorySelect(int count) {
		inventory.selectedInventory = count;
	}
	
	private Game getGame() {
		return this;
	}
	
	private void drawTileBackground(GraphicsHandler g, Sprite sprite, int tileSize) {
		for (int i = 0; i <= GraphicsHandler.get().getScreenWidth() / tileSize; i++) {
			for (int j = 0; j <= GraphicsHandler.get().getScreenHeight() / tileSize; j++) {
				sprite.draw(g, i * tileSize, j * tileSize, tileSize, tileSize);
			}
		}
	}
	
	private void zoom(int level) {
		if (level == 0) {
			if (tileSize < 32) {
				zoom(1);
				zoom(0);
			}
			if (tileSize > 32) {
				zoom(-1);
				zoom(0);
			}
		} else if (level == 1) {
			if (tileSize < 128) {
				tileSize = tileSize * 2;
				for (Entity entity : entities) {
					entity.widthPX *= 2;
					entity.heightPX *= 2;
				}
				for (Item item : itemTypes.values()) {
					item.widthPX *= 2;
					item.heightPX *= 2;
				}
			}
		} else if (level == -1) {
			if (tileSize > 8) {
				tileSize = tileSize / 2;
				for (Entity entity : entities) {
					entity.widthPX /= 2;
					entity.heightPX /= 2;
				}
				for (Item item : itemTypes.values()) {
					item.widthPX /= 2;
					item.heightPX /= 2;
				}
			}
		}
	}
	
	/**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 * 
	 * @param argv
	 *            The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		Game g = new Game();
		
		// Start the main game loop, note: this method will not
		// return until the game has finished running. Hence we are
		// using the actual main thread to run the game.
		g.gameLoop();
	}
}
