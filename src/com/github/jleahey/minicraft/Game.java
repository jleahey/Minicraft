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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.net.URL;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Game extends Canvas
{
	private int screenWidth = 640;
	private int screenHeight = 480;
	private int worldWidth = 512;
	private int worldHeight = 256;
	private BufferStrategy strategy;
	private boolean gameRunning = true;
	private boolean spaceBar = false;
	private boolean leftClick = false;
	private boolean rightClick = false;
	private boolean paused = true;
	
	private Map<Character, Item> itemTypes = new HashMap<Character, Item>();
	
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	
	private int tileSize = 32;
	public Inventory inventory;
	
	
	private int breakingTicks;
	private int breakingX;
	private int breakingY;
	
	
	/* menu sprites */
	private Sprite menu_bgTile = SpriteStore.get().getSprite("sprites/tiles/dirt.png");
	private Sprite menu_logo = SpriteStore.get().getSprite("sprites/menus/title.png");
	private Sprite menu_newUp = SpriteStore.get().getSprite("sprites/menus/new_up.png");
	private Sprite menu_newDown = SpriteStore.get().getSprite("sprites/menus/new_down.png");
	private Sprite menu_loadUp = SpriteStore.get().getSprite("sprites/menus/load_up.png");
	private Sprite menu_loadDown = SpriteStore.get().getSprite("sprites/menus/load_down.png");
	private Sprite menu_miniUp = SpriteStore.get().getSprite("sprites/menus/mini_up.png");
	private Sprite menu_mediumUp = SpriteStore.get().getSprite("sprites/menus/med_up.png");
	private Sprite menu_bigUp = SpriteStore.get().getSprite("sprites/menus/big_up.png");
	private Sprite menu_miniDown = SpriteStore.get().getSprite("sprites/menus/mini_down.png");
	private Sprite menu_mediumDown = SpriteStore.get().getSprite("sprites/menus/mini_down.png");
	private Sprite menu_bigDown = SpriteStore.get().getSprite("sprites/menus/big_down.png");
	private Sprite menu_tag = SpriteStore.get().getSprite("sprites/menus/tag.png");
	private int menu_miniWidth = 256;
	private int menu_mediumWidth = 512;
	private int menu_bigWidth = 1024;
	
	private Sprite builderIcon;
	private Sprite minerIcon;
	private Sprite[] breakingSprites;
	
	private boolean viewFPS = false;
	private boolean startMenu = true;
	private boolean newMenu = false;
	private long ticksRunning;
	Random random = new Random();
	 
	private Player player;
	public World world;
	
	MusicPlayer musicPlayer = new MusicPlayer("sounds/music.ogg");
	int screenMouseX;
	int screenMouseY;

	JFrame container;
	Cursor myCursor = null;
	JPanel panel;
	/**
	 * Construct our game and set it running.
	 */
	public Game() {

		// create a frame to contain our game
		container = new JFrame("Minicraft");
		
		try
		{
			ImageIcon ii = new ImageIcon(new URL("file:sprites/other/mouse.png"));
			Image im = ii.getImage();
			Toolkit tk = getToolkit();
			myCursor = tk.createCustomCursor(im, new Point(0,0), "MyCursor");
		}
		catch (Exception e)
		{
			System.out.println("myCursor creation failed " + e);
		}
		
		// get hold the content of the frame and set up the resolution of the game
		panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(screenWidth, screenHeight));
		panel.setLayout(null);
		panel.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e) {
				Dimension d = e.getComponent().getSize();
				setBounds(0,0, d.width, d.height);
				screenWidth = d.width;
				screenHeight = d.height;
			}
		});
		
		// setup our canvas size and put it into the content of the frame
		setBounds(0, 0, screenWidth+10, screenHeight+10);
		panel.add(this);
		
		// Tell AWT not to bother repainting our canvas since we're
		// going to do that our self in accelerated mode
		setIgnoreRepaint(true);
		
		// finally make the window visible 
		container.pack();
		container.setResizable(true);
		container.setVisible(true);
		
		// add a listener to respond to the user closing the window. If they
		// do we'd like to exit the game
		container.addWindowListener(new WindowAdapter() {
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
		requestFocus();

		// create the buffering strategy which will allow AWT
		// to manage our accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();

		System.gc();
	}
	
	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
	private void startGame(boolean load, int width)
	{
		System.out.println("Created world width: " + width);
		panel.setCursor(myCursor); 
		worldWidth = width;
		
		entities.clear();
		if(load)
		{
			SaveLoad.doLoad(this);
			for(Entity entity : entities)
				if(entity.getClass() == Player.class)
				{
					player = (Player) entity;
					player.widthPX = 7*(tileSize/8);
					player.heightPX = 14*(tileSize/8);
				}
		}
		if(player == null)
		{
			world = new World(worldWidth,worldHeight, random);
			player = new Player(true,world.spawnLocation.x,world.spawnLocation.y,7*(tileSize/8),14*(tileSize/8));
			entities.add(player);
		}

		builderIcon = SpriteStore.get().getSprite("sprites/other/builder.png");
		minerIcon = SpriteStore.get().getSprite("sprites/other/miner.png");
		 
		itemTypes.put((char)('k'+'w'), new Tool("sprites/tools/wPic.png", true, 0, 0, tileSize/2, tileSize/2, (char)('k'+'w'), new char[][]{{'p','p','p'},{ 0, 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Pick, Tool.ToolPower.Wood));
		itemTypes.put((char)('k'+'s'), new Tool("sprites/tools/sPic.png", true, 0, 0, tileSize/2, tileSize/2, (char)('k'+'s'), new char[][]{{'b','b','b'},{ 0, 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Pick, Tool.ToolPower.Stone));
		itemTypes.put((char)('k'+'i'), new Tool("sprites/tools/mPic.png", true, 0, 0, tileSize/2, tileSize/2, (char)('k'+'i'), new char[][]{{'i','i','i'},{ 0, 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Pick, Tool.ToolPower.Metal));
		itemTypes.put((char)('k'+'m'), new Tool("sprites/tools/dPic.png", true, 0, 0, tileSize/2, tileSize/2, (char)('k'+'m'), new char[][]{{'m','m','m'},{ 0, 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Pick, Tool.ToolPower.Diamond));
		 

		itemTypes.put((char)('x'+'w'), new Tool("sprites/tools/wAxe.png", true, 0, 0, tileSize/2, tileSize/2, (char)('x'+'w'), new char[][]{{'p','p',0},{ 'p', 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Axe, Tool.ToolPower.Wood));
		itemTypes.put((char)('x'+'s'), new Tool("sprites/tools/sAxe.png", true, 0, 0, tileSize/2, tileSize/2, (char)('x'+'s'), new char[][]{{'b','b',0},{ 'b', 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Axe, Tool.ToolPower.Stone));
		itemTypes.put((char)('x'+'i'), new Tool("sprites/tools/mAxe.png", true, 0, 0, tileSize/2, tileSize/2, (char)('x'+'i'), new char[][]{{'i','i',0},{ 'i', 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Axe, Tool.ToolPower.Metal));
		itemTypes.put((char)('x'+'m'), new Tool("sprites/tools/dAxe.png", true, 0, 0, tileSize/2, tileSize/2, (char)('x'+'m'), new char[][]{{'m','m',0},{ 'm', 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Axe, Tool.ToolPower.Diamond));
		
		itemTypes.put((char)('s'+'w'), new Tool("sprites/tools/wShovel.png", true, 0, 0, tileSize/2, tileSize/2, (char)('s'+'w'), new char[][]{{0,'p',0},{ 0, 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Shovel, Tool.ToolPower.Wood));
		itemTypes.put((char)('s'+'s'), new Tool("sprites/tools/sShovel.png", true, 0, 0, tileSize/2, tileSize/2, (char)('s'+'s'), new char[][]{{0,'b',0},{ 0, 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Shovel, Tool.ToolPower.Stone));
		itemTypes.put((char)('s'+'i'), new Tool("sprites/tools/mShovel.png", true, 0, 0, tileSize/2, tileSize/2, (char)('s'+'i'), new char[][]{{0,'i',0},{ 0, 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Shovel, Tool.ToolPower.Metal));
		itemTypes.put((char)('s'+'m'), new Tool("sprites/tools/dShovel.png", true, 0, 0, tileSize/2, tileSize/2, (char)('s'+'m'), new char[][]{{0,'m',0},{ 0, 'k', 0},{ 0, 'k', 0}}, 1, Tool.ToolType.Shovel, Tool.ToolPower.Diamond));
		
		 
		itemTypes.put('L', new Item("sprites/tiles/ladder.png", true, 0, 0, tileSize/2, tileSize/2, 'L', new char[][]{{'k', 0,'k'},{ 'k', 'k', 'k'},{ 'k', 0, 'k'}}, 8));
		itemTypes.put('d', new Item("sprites/tiles/dirt.png", true, 0, 0, tileSize/2, tileSize/2, 'd', null, 0));
		itemTypes.put('p', new Item("sprites/tiles/plank.png", true, 0, 0, tileSize/2, tileSize/2, 'p', new char[][]{{'w'}}, 4));
		itemTypes.put('s', new Item("sprites/tiles/stone.png", true, 0, 0, tileSize/2, tileSize/2, 's', null, 0));
		itemTypes.put('n', new Item("sprites/tiles/sand.png", true, 0, 0, tileSize/2, tileSize/2, 'n', null, 0));
		itemTypes.put('i', new Item("sprites/entities/iron.png", true, 0, 0, tileSize/2, tileSize/2, 'i', null, 0));
		itemTypes.put('c', new Item("sprites/entities/coal.png", true, 0, 0, tileSize/2, tileSize/2, 'c', null, 0));
		itemTypes.put('m', new Item("sprites/entities/diamond.png", true, 0, 0, tileSize/2, tileSize/2, 'm', null, 0));
		itemTypes.put('b', new Item("sprites/tiles/cobble.png", true, 0, 0, tileSize/2, tileSize/2, 'b', null, 0));
		itemTypes.put('w', new Item("sprites/tiles/wood.png", true, 0, 0, tileSize/2, tileSize/2, 'w', null, 0 ));
		itemTypes.put('f', new Item("sprites/tiles/craft.png", true, 0, 0, tileSize/2, tileSize/2, 'f', new char[][]{{'p','p'},{'p','p'}}, 1));
		itemTypes.put('k', new Item("sprites/entities/stick.png", true, 0, 0, tileSize/2, tileSize/2, 'k', new char[][]{{'p'},{'p'}}, 4));
		itemTypes.put('S', new Item("sprites/tiles/sappling.png", true, 0, 0, tileSize/2, tileSize/2, 'S', null, 0 ));
		 
		if(inventory == null)
			inventory = new Inventory(10,4,3,itemTypes);
		 
		 musicPlayer.play();
		 
		 breakingSprites = new Sprite[8];
		 for(int i = 0; i < 8; i++)
			breakingSprites[i] = SpriteStore.get().getSprite("sprites/tiles/break"+i+".png");
		 
		System.gc();
	}

	public void drawStartMenu(Graphics2D g)
	{
		drawTileBackground(g, menu_bgTile, 32);
		drawCenteredX(g, menu_logo, 70, 397, 50);
		drawCenteredX(g, menu_newUp, 200, 160, 64);
		drawCenteredX(g, menu_loadUp, 300, 160, 64);
		float pixels = (Math.abs((float)((ticksRunning%100)-50)/ 50)+1);
		menu_tag.draw(g, 450, 70, (int)(60*pixels), (int)(37*pixels));
		if(leftClick && screenMouseY >= 300)
		{
			leftClick = false;
			startMenu = false;
			startGame(true, menu_mediumWidth);
		}
		else if(leftClick && screenMouseY < 300)
		{
			leftClick = false;
			startMenu = false;
			newMenu = true;
		}
	}

	public void drawNewMenu(Graphics2D g)
	{
		drawTileBackground(g, menu_bgTile, 32);
		drawCenteredX(g, menu_logo, 70, 397, 50);
		drawCenteredX(g, menu_miniUp, 150, 160, 64);
		drawCenteredX(g, menu_mediumUp, 250, 160, 64);
		drawCenteredX(g, menu_bigUp, 350, 160, 64);
		float pixels = (Math.abs((float)((ticksRunning%100)-50)/ 50)+1);
		menu_tag.draw(g, 450, 70, (int)(60*pixels), (int)(37*pixels));
		if(leftClick && screenMouseY >= 350)
		{
			leftClick = false;
			startMenu = false;
			newMenu = false;
			startGame(false, menu_bigWidth);
		}
		else if(leftClick && screenMouseY >= 250)
		{
			leftClick = false;
			startMenu = false;
			newMenu = false;
			startGame(false, menu_mediumWidth);
		}
		else if(leftClick && screenMouseY >= 150)
		{
			leftClick = false;
			startMenu = false;
			newMenu = false;
			startGame(false, menu_miniWidth);
		}
	}
		
	public void drawCenteredX(Graphics2D g, Sprite s, int top, int width, int height)
	{
		s.draw(g, screenWidth/2 - width/2, top, width, height);
	}
	
	public void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();

		// keep looping round till the game ends
		while (gameRunning) {
			ticksRunning++;
			long delta = SystemTimer.getTime() - lastLoopTime;
			lastLoopTime = SystemTimer.getTime();

			// Get hold of a graphics context for the accelerated 
			// surface and blank it out
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, screenWidth, screenHeight);
			
			if (startMenu || newMenu)
			{
				if (startMenu) drawStartMenu(g);
				else           drawNewMenu(g);
				g.dispose();
				strategy.show();
				SystemTimer.sleep(lastLoopTime+16-SystemTimer.getTime());
				continue;
			}

			float cameraX = player.x-screenWidth/tileSize/2;
			float cameraY = player.y-screenHeight/tileSize/2;
			
			world.chunkUpdate();
			world.draw(g, 0, 0, screenWidth, screenHeight, cameraX, cameraY, tileSize);
			
			boolean inventoryFocus = inventory.updateInventory(screenWidth, screenHeight, screenMouseX, screenMouseY, leftClick, rightClick);
			if(inventoryFocus)
			{
				leftClick = false;
				rightClick = false;
			}

			if(spaceBar) player.jump(world, tileSize);

			if(leftClick && player.handBreakPos.x != -1)
			{
				if(player.handBreakPos.x == breakingX && player.handBreakPos.y == breakingY)
					breakingTicks++;
				else
					breakingTicks = 0;
				breakingX = player.handBreakPos.x;
				breakingY = player.handBreakPos.y;
				
				InventoryItem inventoryItem = inventory.selectedItem();
				Item item = inventoryItem.getItem();
				int ticksNeeded = world.breakTicks(breakingX, breakingY, item);
				
				Int2 pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize, tileSize, tileSize, breakingX, breakingY);
				int sprite_index = (int) Math.min(1, (double)breakingTicks/ticksNeeded)*(breakingSprites.length-1);
				breakingSprites[sprite_index].draw(g,pos.x, pos.y,tileSize,tileSize);
				if(breakingTicks >= ticksNeeded)
				{
					if(item != null && item.getClass() == Tool.class)
					{
						Tool tool = (Tool)item;
						tool.uses++;
						if(tool.uses >= tool.totalUses)
						{
							inventoryItem.setEmpty();
						}
					}
					
					breakingTicks = 0;
					char name = world.removeTile(player.handBreakPos.x, player.handBreakPos.y);
					if(name == 'g')
						name = 'd';
					if(name == 's')
						name = 'b';
					if(name == 'l' && random.nextDouble() < .1)
						name = 'S';
					Item newItem = itemTypes.get(name);
					if(newItem != null)  //couldn't find that item
					{
						newItem = (Item) newItem.clone();
						newItem.x = player.handBreakPos.x + random.nextFloat()*(1-(float)newItem.widthPX/tileSize);
						newItem.y = player.handBreakPos.y + random.nextFloat()*(1-(float)newItem.widthPX/tileSize);
						newItem.dy = -.07f;
						entities.add(newItem);
					}
				}
			}
			else
				breakingTicks = 0;

			if(rightClick)
			{
				if(world.isCraft(player.handBreakPos.x,player.handBreakPos.y))
				{
					inventory.tableSizeAvailable = 3;
					inventory.setVisible(true);
				}
				else
				{
					rightClick = false;
					InventoryItem current = inventory.selectedItem();
					if(!current.isEmpty())
					{
						int left = (int) player.getLeft(tileSize);
						int right = (int) player.getRight(tileSize);
						int top = (int) player.getTop(tileSize);
						int bottom = (int) player.getBottom(tileSize);
						
						if(!(player.handBuildPos.x >= left && player.handBuildPos.x <= right && player.handBuildPos.y >= top && player.handBuildPos.y <= bottom))
						{
							boolean placed = world.addTile(player.handBuildPos.x, player.handBuildPos.y, current.getItem().name);
							if(placed)
								inventory.decreaseSeleted(1);
						}
					}
				}
			}
			
			float worldMouseX = (float) ((cameraX*tileSize + screenMouseX)/tileSize);
			float worldMouseY = (float) ((cameraY*tileSize + screenMouseY)/tileSize)-.5f;
			player.updateHand(cameraX, cameraY, g, worldMouseX, worldMouseY, world, tileSize);

			java.util.Iterator<Entity> it = entities.iterator();
			while(it.hasNext())
			{
				Entity entity = it.next();
				if(entity != player && player.collidesWith(entity, tileSize))
				{
					if(entity instanceof Item || entity instanceof Tool)
						addToInventory(((Item)entity));
					it.remove();
					continue;
				}
				entity.updatePosition(world, tileSize);
				entity.draw(g, cameraX, cameraY, screenWidth, screenHeight, tileSize);
			}
			
			if(viewFPS)
			{
				String fps = "Fps: " + 1/((float)delta/1000) + "(" + Runtime.getRuntime().freeMemory()/1024/1024 + " / " + Runtime.getRuntime().totalMemory()/1024/1024 + ") Free MB";
				g.setColor(Color.white);
				g.drawString(fps, 10, 10);
			}

			//Draw the UI
			if(player.handBreakPos.x != -1)
			{
				Int2 pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize, tileSize, tileSize, player.handBuildPos.x, player.handBuildPos.y);
				builderIcon.draw(g, pos.x, pos.y, tileSize, tileSize);
				
				pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize, tileSize, tileSize, player.handBreakPos.x, player.handBreakPos.y);
				minerIcon.draw(g, pos.x, pos.y, tileSize, tileSize);
			}
			
			inventory.draw(g, screenWidth, screenHeight);
			
			Int2 mouseTest = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize, tileSize, tileSize, worldMouseX, worldMouseY);
			g.setColor(Color.white);
			g.fillOval(mouseTest.x-4, mouseTest.y-4, 8, 8);
			g.setColor(Color.black);
			g.fillOval(mouseTest.x-3, mouseTest.y-3, 6, 6);

			// finally, we've completed drawing so clear up the graphics
			// and flip the buffer over
			g.dispose();
			strategy.show();

			SystemTimer.sleep(lastLoopTime+16-SystemTimer.getTime());
		}
	}
	
	private void addToInventory(Item item)
	{
		inventory.addItem(item,1);
	}
	
	private void setInventorySelect(int count)
	{
		inventory.selectedInventory = count;
	}
	
	private Game getGame()
	{
		return this;
	}
	
	private void drawTileBackground(Graphics2D g, Sprite sprite, int tileSize)
	{
		for(int i = 0; i <= screenWidth/tileSize; i++)
			for(int j = 0; j <= screenHeight/tileSize; j++)
			{
				sprite.draw(g, i*tileSize, j*tileSize, tileSize, tileSize);
			}
	}
	
	private void zoom(int level)
	{
		if(level == 0)
		{
			if(tileSize < 32)
			{
				zoom(1);
				zoom(0);
			}
			if(tileSize > 32)
			{
				zoom(-1);
				zoom(0);
			}
		}
		else if (level == 1)
		{
			if(tileSize < 128)
			{
				tileSize = tileSize*2;
				for(Entity entity :entities)
				{
					entity.widthPX *= 2;
					entity.heightPX *= 2;
				}
				for(Item item: itemTypes.values())
				{
					item.widthPX *= 2;
					item.heightPX *= 2;
				}
			}
		}
		else if(level == -1)
		{
			if(tileSize > 8)
			{
				tileSize = tileSize/2;
				for(Entity entity :entities)
				{
					entity.widthPX /= 2;
					entity.heightPX /= 2;
				}
				for(Item item: itemTypes.values())
				{
					item.widthPX /= 2;
					item.heightPX /= 2;
				}
			}
		}
	}

	private class MouseWheelInputHander implements MouseWheelListener
	{
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int notches = e.getWheelRotation();
			inventory.selectedInventory += notches;
			if(inventory.selectedInventory < 0)//hack should be get/set
				inventory.selectedInventory = 0;
			if(inventory.selectedInventory > 9)
				inventory.selectedInventory = 9;
		}
	}
	
	private class MouseMoveInputHander implements MouseMotionListener
	{
		@Override
		public void mouseDragged(MouseEvent arg0) {
			screenMouseX = arg0.getX();
			screenMouseY = arg0.getY();
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			screenMouseX = arg0.getX();
			screenMouseY = arg0.getY();
		}
	}
	
	private class MouseInputHander implements MouseListener
	{
		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mousePressed(MouseEvent arg0) {
			if(arg0.getButton() == MouseEvent.BUTTON1)
				leftClick = true;
			if (arg0.getButton() == MouseEvent.BUTTON2 || arg0.getButton() == MouseEvent.BUTTON3)
				rightClick = true;
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if(arg0.getButton() == MouseEvent.BUTTON1)
				leftClick = false;
			if (arg0.getButton() == MouseEvent.BUTTON2|| arg0.getButton() == MouseEvent.BUTTON3)
				rightClick = false;
		}

		@Override
		public void mouseClicked(MouseEvent e) {}
	}
	
	private class KeyInputHandler extends KeyAdapter {
		
		/**
		 * Notification from AWT that a key has been pressed. Note that
		 * a key being pressed is equal to being pushed down but *NOT*
		 * released. Thats where keyTyped() comes in.
		 *
		 * @param e The details of the key that was pressed 
		 */
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_W) {
				player.startClimb();
			}
			else if (e.getKeyCode() == KeyEvent.VK_A) {
				player.startLeft(e.isShiftDown());
			}
			else if (e.getKeyCode() == KeyEvent.VK_D) {
				player.startRight(e.isShiftDown());
			}
			else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				spaceBar = true;
			}
		} 
		
		/**
		 * Notification from AWT that a key has been released.
		 *
		 * @param e The details of the key that was released 
		 */
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
				player.endClimb(); break;
			case KeyEvent.VK_A:
				player.stopLeft(); break;
			case KeyEvent.VK_D:
				player.stopRight(); break;
			case KeyEvent.VK_SPACE:
				spaceBar = false; break;
			}
		}

		public void keyTyped(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_1: setInventorySelect(1); break;
				case KeyEvent.VK_2: setInventorySelect(2); break;
				case KeyEvent.VK_3: setInventorySelect(3); break;
				case KeyEvent.VK_4: setInventorySelect(4); break;
				case KeyEvent.VK_5: setInventorySelect(5); break;
				case KeyEvent.VK_6: setInventorySelect(6); break;
				case KeyEvent.VK_7: setInventorySelect(7); break;
				case KeyEvent.VK_8: setInventorySelect(8); break;
				case KeyEvent.VK_9: setInventorySelect(9); break;
				case KeyEvent.VK_0: setInventorySelect(9); break;
				case KeyEvent.VK_I: inventory.setVisible(!inventory.isVisible()); break;
				case KeyEvent.VK_EQUALS: zoom(1); break;
				case KeyEvent.VK_P: paused = !paused; break;
				case KeyEvent.VK_M: musicPlayer.toggleSound(); break;
				case KeyEvent.VK_O: zoom(0); break;
				case KeyEvent.VK_MINUS: zoom(-1); break;
				case KeyEvent.VK_F: viewFPS = !viewFPS; break;
				case KeyEvent.VK_Q:
					InventoryItem inventoryItem = inventory.selectedItem();
					if(!inventoryItem.isEmpty())
					{
						Item newItem = inventoryItem.getItem();
						if (!(newItem instanceof Tool))
							newItem = (Item) newItem.clone();
						inventoryItem.remove(1);
						if(player.facingRight)
							newItem.x = player.x + 1 + random.nextFloat();
						else
							newItem.x = player.x - 1 - random.nextFloat();;
						newItem.y = player.y;
						newItem.dy = -.1f;
						entities.add(newItem);					
					}
					break;
				case KeyEvent.VK_ESCAPE:
					zoom(0);
					SaveLoad.doSave(getGame());
					musicPlayer.close();
					System.exit(0);
			}
		}
	}
	
	/**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 * 
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		Game g = new Game();

		// Start the main game loop, note: this method will not
		// return until the game has finished running. Hence we are
		// using the actual main thread to run the game.
		g.gameLoop();
	}
}
