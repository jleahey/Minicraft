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

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class Game {
	
	private int worldWidth = 512;
	private int worldHeight = 256;
	private boolean gameRunning = true;
	public boolean leftClick = false;
	public boolean rightClick = false;
	public boolean paused = true;
	
	private Map<Character, Item> itemTypes;
	
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	
	private int tileSize = 32;
	public Inventory inventory;
	
	private int breakingTicks;
	private Int2 breakingPos;
	
	private Sprite builderIcon;
	private Sprite minerIcon;
	private Sprite[] breakingSprites;
	private Sprite fullHeart, halfHeart, emptyHeart, bubble, emptyBubble;
	
	public boolean viewFPS = false;
	private boolean inMenu = true;
	private MainMenu menu;
	public long ticksRunning;
	private Random random = new Random();
	
	public Player player;
	public World world;
	
	public MusicPlayer musicPlayer = new MusicPlayer("sounds/music.ogg");
	public Int2 screenMousePos = new Int2(0, 0);
	
	/**
	 * Construct our game and set it running.
	 */
	public Game() {
		menu = new MainMenu(this);
		GraphicsHandler.get().init(this);
		itemTypes = ItemLoader.loadItems(tileSize / 2);
		System.gc();
	}
	
	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
	public void startGame(boolean load, int width) {
		inMenu = false;
		if (load) {
			System.out.println("Loading world, width: " + worldWidth);
		} else {
			System.out.println("Creating world, width: " + width);
			worldWidth = width;
		}
		
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
		if (!load) {
			// make a new world and player
			world = new World(worldWidth, worldHeight, random);
			player = new Player(true, world.spawnLocation.x, world.spawnLocation.y,
					7 * (tileSize / 8), 14 * (tileSize / 8));
			entities.add(player);
			// make a new inventory
			inventory = new Inventory(10, 4, 3, itemTypes);
			if (Constants.DEBUG) {
				inventory.addItem(itemTypes.get((char) 175).clone(), 1);
				inventory.addItem(itemTypes.get((char) 88).clone(), 1);
			}
		}
		
		// load sprites
		final SpriteStore ss = SpriteStore.get();
		builderIcon = ss.getSprite("sprites/other/builder.png");
		minerIcon = ss.getSprite("sprites/other/miner.png");
		fullHeart = ss.getSprite("sprites/other/full_heart.png");
		halfHeart = ss.getSprite("sprites/other/half_heart.png");
		emptyHeart = ss.getSprite("sprites/other/empty_heart.png");
		bubble = ss.getSprite("sprites/other/bubble.png");
		// there's no empty bubble image, so we'll just use this for now
		emptyBubble = ss.getSprite("sprites/other/bubble_pop2.png");
		
		breakingSprites = new Sprite[8];
		for (int i = 0; i < 8; i++) {
			breakingSprites[i] = ss.getSprite("sprites/tiles/break" + i + ".png");
		}
		
		musicPlayer.play();
		System.gc();
	}
	
	public void drawCenteredX(GraphicsHandler g, Sprite s, int top, int width, int height) {
		s.draw(g, g.getScreenWidth() / 2 - width / 2, top, width, height);
	}
	
	public void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();
		
		if (Constants.DEBUG) {
			startGame(false, 512);
		}
		
		// keep looping round till the game ends
		while (gameRunning) {
			ticksRunning++;
			long delta = SystemTimer.getTime() - lastLoopTime;
			lastLoopTime = SystemTimer.getTime();
			

			GraphicsHandler g = GraphicsHandler.get();
			g.startDrawing();
			
			if (inMenu) {
				menu.draw(g);
				drawMouse(g, screenMousePos);
				g.finishDrawing();
				
				SystemTimer.sleep(lastLoopTime + 16 - SystemTimer.getTime());
				continue;
			}
			final int screenWidth = g.getScreenWidth();
			final int screenHeight = g.getScreenHeight();
			float cameraX = player.x - screenWidth / tileSize / 2;
			float cameraY = player.y - screenHeight / tileSize / 2;
			float worldMouseX = (cameraX * tileSize + screenMousePos.x) / tileSize;
			float worldMouseY = (cameraY * tileSize + screenMousePos.y) / tileSize - .5f;
			
			world.chunkUpdate();
			world.draw(g, 0, 0, screenWidth, screenHeight, cameraX, cameraY, tileSize);
			
			boolean inventoryFocus = inventory.updateInventory(
				screenWidth, screenHeight, screenMousePos, leftClick, rightClick);
			if (inventoryFocus) {
				leftClick = false;
				rightClick = false;
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
				entity.draw(g, cameraX, cameraY, screenWidth, screenHeight, tileSize);
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
			
			// draw the hotbar, and optionally the inventory screen
			inventory.draw(g, screenWidth, screenHeight);
			
			// draw the mouse
			Int2 mouseTest = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize,
					tileSize, tileSize, worldMouseX, worldMouseY);
			drawMouse(g, mouseTest);
			
			// HACK: draw hearts for health bar
			// TODO: move this elsewhere, don't use so many magic constants
			int heartX = (screenWidth - 250) / 2;
			int heartY = screenHeight - 50;
			for (int heartIdx = 1; heartIdx <= 10; ++heartIdx) {
				int hpDiff = player.hitPoints - heartIdx * 10;
				if (hpDiff >= 0) {
					fullHeart.draw(g, heartX, heartY, 10, 10);
				} else if (hpDiff >= -5) {
					halfHeart.draw(g, heartX, heartY, 10, 10);
				} else {
					emptyHeart.draw(g, heartX, heartY, 10, 10);
				}
				heartX += 15;
			}
			
			if (player.isHeadUnderWater(world, tileSize)) {
				// another HACK: draw air bubbles
				int bubbleX = (screenWidth + 50) / 2;
				int numBubbles = player.airRemaining();
				for (int bubbleIdx = 1; bubbleIdx <= 10; ++bubbleIdx) {
					if (bubbleIdx <= numBubbles) {
						bubble.draw(g, bubbleX, heartY, 10, 10);
					} else {
						emptyBubble.draw(g, bubbleX, heartY, 10, 10);
					}
					bubbleX += 15;
				}
			}
			
			g.finishDrawing();
			
			SystemTimer.sleep(lastLoopTime + 16 - SystemTimer.getTime());
		}
	}
	
	private void addToInventory(Item item) {
		inventory.addItem(item, 1);
	}
	
	public void setInventorySelect(int count) {
		inventory.selectedInventory = count;
	}
	
	private Game getGame() {
		return this;
	}

	public void drawMouse(GraphicsHandler g, Int2 pos) {
		g.setColor(Color.white);
		g.fillOval(pos.x - 4, pos.y - 4, 8, 8);
		g.setColor(Color.black);
		g.fillOval(pos.x - 3, pos.y - 3, 6, 6);
	}
	
	public void drawTileBackground(GraphicsHandler g, Sprite sprite, int tileSize) {
		for (int i = 0; i <= GraphicsHandler.get().getScreenWidth() / tileSize; i++) {
			for (int j = 0; j <= GraphicsHandler.get().getScreenHeight() / tileSize; j++) {
				sprite.draw(g, i * tileSize, j * tileSize, tileSize, tileSize);
			}
		}
	}
	
	public void zoom(int level) {
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
	
	public void tossItem() {
		InventoryItem inventoryItem = inventory.selectedItem();
		if (!inventoryItem.isEmpty()) {
			Item newItem = inventoryItem.getItem();
			if (!(newItem instanceof Tool)) {
				newItem = (Item) newItem.clone();
			}
			inventoryItem.remove(1);
			if (player.facingRight) {
				newItem.x = player.x + 1 + random.nextFloat();
			} else {
				newItem.x = player.x - 1 - random.nextFloat();
			}
			newItem.y = player.y;
			newItem.dy = -.1f;
			entities.add(newItem);
		}
	}
	
	public void quitNow() {
		zoom(0);
		SaveLoad.doSave(getGame());
		musicPlayer.close();
		System.exit(0);
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
