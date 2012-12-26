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

public class MainMenu {
	
	/* menu sprites */
	private static final Sprite menu_bgTile = SpriteStore.get().getSprite("sprites/tiles/dirt.png");
	private static final Sprite menu_logo = SpriteStore.get().getSprite("sprites/menus/title.png");
	private static final Sprite menu_newUp = SpriteStore.get().getSprite("sprites/menus/new_up.png");
	private static final Sprite menu_newDown = SpriteStore.get().getSprite("sprites/menus/new_down.png");
	private static final Sprite menu_loadUp = SpriteStore.get().getSprite("sprites/menus/load_up.png");
	private static final Sprite menu_loadDown = SpriteStore.get().getSprite("sprites/menus/load_down.png");
	private static final Sprite menu_miniUp = SpriteStore.get().getSprite("sprites/menus/mini_up.png");
	private static final Sprite menu_mediumUp = SpriteStore.get().getSprite("sprites/menus/med_up.png");
	private static final Sprite menu_bigUp = SpriteStore.get().getSprite("sprites/menus/big_up.png");
	private static final Sprite menu_miniDown = SpriteStore.get().getSprite("sprites/menus/mini_down.png");
	private static final Sprite menu_mediumDown = SpriteStore.get().getSprite("sprites/menus/med_down.png");
	private static final Sprite menu_bigDown = SpriteStore.get().getSprite("sprites/menus/big_down.png");
	private static final Sprite menu_tag = SpriteStore.get().getSprite("sprites/menus/tag.png");
	private static final int menu_miniWidth = 256;
	private static final int menu_mediumWidth = 512;
	private static final int menu_bigWidth = 1024;
	
	private boolean newGame = false;
	private Game game;
	
	public MainMenu(Game g) {
		this.game = g;
	}

	public void draw(GraphicsHandler g) {
		game.drawTileBackground(g, menu_bgTile, 32);
		game.drawCenteredX(g, menu_logo, 70, 397, 50);
		float tagScale = ((float) Math.abs((game.ticksRunning % 100) - 50)) / 50 + 1;
		menu_tag.draw(g, 450, 70, (int) (60 * tagScale), (int) (37 * tagScale));

		if (newGame) {
			drawNewMenu(g);
		} else {
			drawStartMenu(g);
		}
	}
	
	private void drawStartMenu(GraphicsHandler g) {
		game.drawCenteredX(g, menu_newUp, 200, 160, 64);
		game.drawCenteredX(g, menu_loadUp, 300, 160, 64);
		if (!game.leftClick) {
			return;
		}
		game.leftClick = false;
		if (game.screenMouseY >= 300) {
			game.startGame(true, menu_mediumWidth);
		} else {
			newGame = true;
		}
	}
	
	private void drawNewMenu(GraphicsHandler g) {
		game.drawCenteredX(g, menu_miniUp, 150, 160, 64);
		game.drawCenteredX(g, menu_mediumUp, 250, 160, 64);
		game.drawCenteredX(g, menu_bigUp, 350, 160, 64);
		if (!game.leftClick) {
			return;
		}
		game.leftClick = false;
		newGame = false;
		if (game.screenMouseY >= 350) {
			game.startGame(false, menu_bigWidth);
		} else if (game.screenMouseY >= 250) {
			game.startGame(false, menu_mediumWidth);
		} else if (game.screenMouseY >= 150) {
			game.startGame(false, menu_miniWidth);
		}
	}
}
