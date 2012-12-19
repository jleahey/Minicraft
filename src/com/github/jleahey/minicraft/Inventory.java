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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;

public class Inventory implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	public InventoryItem[][] inventoryItems;
	public int tableSizeAvailable = 2;
	public int selectedInventory = 0;
	
	private int maxCount = 64;
	private int playerRow;
	private boolean visible = false;
	private InventoryItem holding = new InventoryItem(null);
	private int holdingX;
	private int holdingY;
	private Int2 clickPos = new Int2(0, 0);;
	private int craftingHeight;
	private char[][] tableTwo = new char[2][2];
	private char[][] tableThree = new char[3][3];
	private InventoryItem craftable = new InventoryItem(null);
	private Map<Character, Item> itemTypes;
	
	public Inventory(int width, int height, int craftingHeight, Map<Character, Item> itemTypes) {
		inventoryItems = new InventoryItem[width][height + craftingHeight];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height + craftingHeight; j++) {
				inventoryItems[i][j] = new InventoryItem(null);
			}
		}
		selectedInventory = 0;
		playerRow = height + craftingHeight - 1;
		this.craftingHeight = craftingHeight;
		this.itemTypes = itemTypes;
	}
	
	public void addItem(Item item, int count) {
		// try active slots
		int itemsToGo = inventoryItems[0][playerRow].add(item, count);
		for (int i = 0; i < inventoryItems.length && itemsToGo > 0; i++) {
			itemsToGo = inventoryItems[i][playerRow].add(item, count);
		}
		
		// try the rest
		for (int i = 0; i < inventoryItems.length && itemsToGo > 0; i++) {
			for (int j = 0; j < inventoryItems[0].length - 1 && itemsToGo > 0; j++) {
				if ((j < craftingHeight && i < inventoryItems.length - tableSizeAvailable)
						|| (craftingHeight != tableSizeAvailable && j == tableSizeAvailable)) {
					continue;
				}
				itemsToGo = inventoryItems[i][playerRow].add(item, count);
			}
		}
	}
	
	public void decreaseSeleted(int count) {
		inventoryItems[selectedInventory][playerRow].remove(count);
	}
	
	public InventoryItem selectedItem() {
		return inventoryItems[selectedInventory][playerRow];
	}
	
	// returns true if the mouse hit in the inventory
	public boolean updateInventory(int screenWidth, int screenHeight, int mouseX, int mouseY,
			boolean leftClick, boolean rightClick) {
		if (!visible) {
			return false;
		}
		
		int tileSize = 16;
		int seperation = 15;
		
		int panelWidth = inventoryItems.length * (tileSize + seperation) + seperation;
		int panelHeight = inventoryItems[0].length * (tileSize + seperation) + seperation;
		int x = screenWidth / 2 - panelWidth / 2;
		int y = screenHeight / 2 - panelHeight / 2;
		
		if (mouseX < x || mouseX > x + panelWidth || mouseY < y || mouseY > y + panelHeight) {
			return false;
		}
		
		holdingX = mouseX;
		holdingY = mouseY - tileSize;
		if (!leftClick && !rightClick) {
			return true;
		}
		
		Int2 position = mouseToCoor(mouseX - x, mouseY - y, seperation, tileSize);
		if (position != null) {
			if (holding.isEmpty()) {
				if (rightClick && inventoryItems[position.x][position.y].count > 1) {
					holding.item = inventoryItems[position.x][position.y].item;
					holding.count = (int) Math
							.ceil((double) inventoryItems[position.x][position.y].count / 2);
					inventoryItems[position.x][position.y].count = (int) Math
							.floor((double) inventoryItems[position.x][position.y].count / 2);
				} else {
					holding.item = inventoryItems[position.x][position.y].item;
					holding.count = inventoryItems[position.x][position.y].count;
					inventoryItems[position.x][position.y].item = null;
					inventoryItems[position.x][position.y].count = 0;
				}
			} else if (inventoryItems[position.x][position.y].item == null) {
				if (rightClick) {
					inventoryItems[position.x][position.y].item = holding.item;
					inventoryItems[position.x][position.y].count = 1;
					holding.count--;
					if (holding.count <= 0) {
						holding.item = null;
					}
				} else {
					inventoryItems[position.x][position.y].item = holding.item;
					inventoryItems[position.x][position.y].count = holding.count;
					holding.item = null;
					holding.count = 0;
				}
			} else if (holding.item.item_id == inventoryItems[position.x][position.y].item.item_id
					&& inventoryItems[position.x][position.y].count < maxCount) {
				if ((holding.item.getClass() == Tool.class)
						|| (inventoryItems[position.x][position.y].item.getClass() == Tool.class)) {
				} else if (rightClick) {
					
					inventoryItems[position.x][position.y].count++;
					holding.count--;
					if (holding.count <= 0) {
						holding.item = null;
					}
					
				} else {
					inventoryItems[position.x][position.y].count += holding.count;
					if (inventoryItems[position.x][position.y].count > maxCount) {
						holding.count = maxCount - inventoryItems[position.x][position.y].count;
						inventoryItems[position.x][position.y].count = maxCount;
					} else {
						holding.item = null;
						holding.count = 0;
					}
					
				}
			} else {
				Item item = inventoryItems[position.x][position.y].item;
				int count = inventoryItems[position.x][position.y].count;
				inventoryItems[position.x][position.y].item = holding.item;
				inventoryItems[position.x][position.y].count = holding.count;
				holding.item = item;
				holding.count = count;
			}
		}
		
		x = screenWidth / 2 - panelWidth / 2;
		y = screenHeight / 2 - panelHeight / 2;
		x = x + (inventoryItems.length - tableSizeAvailable - 1) * (tileSize + seperation) - 5;
		y = y + seperation * 2 + tileSize - 5;
		
		boolean craftThisUpdate = false;
		if (mouseX >= x && mouseX <= x + tileSize + 10 && mouseY >= y
				&& mouseY <= y + tileSize * 2 + 10) {
			craftThisUpdate = true;
		}
		
		// check for a construction
		
		craftable.item = null;
		craftable.count = 0;
		
		boolean keepChecking = true;
		while (keepChecking) {
			keepChecking = false;
			// only craft one at a time for now
			char[][] currentTable = computeCraftTable();
			for (Item entry : itemTypes.values()) {
				craftable.item = null;
				craftable.count = 0;
				if (entry.template.compare(currentTable)) {
					craftable.item = entry;
					craftable.count = entry.template.outCount;
					if (craftThisUpdate) {
						if (entry.getClass() == Tool.class && !holding.isEmpty()) {
							break;
						}
						craftThisUpdate = false;
						keepChecking = true;
						craftable.item = null;
						craftable.count = 0;
						for (int i = 0; i < tableSizeAvailable; i++) {
							for (int j = 0; j < tableSizeAvailable; j++) {
								inventoryItems[i + inventoryItems.length - tableSizeAvailable][j].count -= 1;
								if (inventoryItems[i + inventoryItems.length - tableSizeAvailable][j].count <= 0) {
									inventoryItems[i + inventoryItems.length - tableSizeAvailable][j].item = null;
									inventoryItems[i + inventoryItems.length - tableSizeAvailable][j].count = 0;
								}
							}
						}
						int count = entry.template.outCount;
						holding.add((Item) entry.clone(), count);
					}
					break;
				}
			}
		}
		
		return true;
	}
	
	private char[][] computeCraftTable() {
		char[][] currentTable;
		if (tableSizeAvailable == 2) {
			currentTable = tableTwo;
		} else {
			currentTable = tableThree;
		}
		
		for (int i = 0; i < tableSizeAvailable; i++) {
			for (int j = 0; j < tableSizeAvailable; j++) {
				Item item = inventoryItems[i + inventoryItems.length - tableSizeAvailable][j].item;
				if (item != null) {
					currentTable[j][i] = (char) item.item_id;
				} else {
					currentTable[j][i] = (char) 0;
				}
			}
		}
		return currentTable;
	}
	
	// relative x/y in px
	private Int2 mouseToCoor(int x, int y, int seperation, int tileSize) {
		clickPos.x = x / (seperation + tileSize);
		clickPos.y = y / (seperation + tileSize) - 1;
		if (clickPos.x < 0
				|| clickPos.y < 0
				|| clickPos.x >= inventoryItems.length
				|| clickPos.y >= inventoryItems[0].length
				|| ((clickPos.y < craftingHeight && clickPos.x < inventoryItems.length
						- tableSizeAvailable) || (craftingHeight != tableSizeAvailable && clickPos.y == tableSizeAvailable))) {
			return null;
		}
		return clickPos;
	}
	
	public void draw(Graphics2D g, int screenWidth, int screenHeight) {
		int tileSize = 16;
		int seperation = 10;
		
		int panelWidth = inventoryItems.length * (tileSize + seperation) + seperation;
		int panelHeight = tileSize + seperation * 2;
		int x = screenWidth / 2 - panelWidth / 2;
		int y = screenHeight - panelHeight - seperation;
		
		g.setColor(Color.gray);
		g.fillRect(x, y, panelWidth, panelHeight);
		
		for (int j = 0; j < inventoryItems.length; j++) {
			InventoryItem current = inventoryItems[j][playerRow];
			if (selectedInventory == j) {
				g.setColor(Color.blue);
				g.fillRect(x + seperation - 2, y + seperation - 2, tileSize + 4, tileSize + 4);
			}
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(x + seperation, y + seperation, tileSize, tileSize);
			// if(selectedInventory == j)
			// {
			//
			// if(current.item == null)
			// }
			// else
			// {
			// g.setColor(Color.LIGHT_GRAY);
			// g.fillRect(x+seperation-2, y+seperation-2, tileSize+4, tileSize+4);
			// }
			
			current.draw(g, x + seperation, y + seperation, tileSize);
			x += tileSize + seperation;
		}
		if (!isVisible()) {
			return;
		}
		
		seperation = 15;
		
		panelWidth = inventoryItems.length * (tileSize + seperation) + seperation;
		panelHeight = inventoryItems[0].length * (tileSize + seperation) + seperation;
		x = screenWidth / 2 - panelWidth / 2;
		y = screenHeight / 2 - panelHeight / 2;
		
		g.setColor(Color.gray);
		g.fillRect(x, y, panelWidth, panelHeight);
		
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x + panelWidth - tableSizeAvailable * (tileSize + seperation) - seperation, y,
				tableSizeAvailable * (tileSize + seperation) + seperation, tableSizeAvailable
						* (tileSize + seperation) + seperation);
		
		for (int i = 0; i < inventoryItems[0].length; i++) {
			x = screenWidth / 2 - panelWidth / 2;
			for (int j = 0; j < inventoryItems.length; j++) {
				if ((i < craftingHeight && j < inventoryItems.length - tableSizeAvailable)
						|| (craftingHeight != tableSizeAvailable && i == tableSizeAvailable)) {
					x += tileSize + seperation;
					continue;
				}
				
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(x + seperation - 2, y + seperation - 2, tileSize + 4, tileSize + 4);
				InventoryItem current = inventoryItems[j][i];
				current.draw(g, x + seperation, y + seperation, tileSize);
				x += tileSize + seperation;
			}
			y += tileSize + seperation;
		}
		
		x = screenWidth / 2 - panelWidth / 2;
		y = screenHeight / 2 - panelHeight / 2;
		g.setColor(Color.orange);
		x = x + (inventoryItems.length - tableSizeAvailable - 1) * (tileSize + seperation);
		y = y + seperation * 2 + tileSize;
		
		g.fillRect(x - 5, y - 5, tileSize + 10, tileSize + 10);
		
		craftable.draw(g, x, y, tileSize);
		holding.draw(g, holdingX - tileSize / 2, holdingY - tileSize / 2, tileSize);
	}
	
	public void setVisible(boolean visible) {
		if (visible == false) {
			tableSizeAvailable = 2;
		}
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return visible;
	}
}
