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

public final class TileTemplate implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	public final static TileTemplate tree = new TileTemplate(
			new char[][]{
					{ 0 ,'l','l', 0 , 0 , 0},
					{'l','l','l','l', 0 , 0},
					{'l','l','l','w','w','w'},
					{'l','l','l','l', 0 , 0},
					{ 0 ,'l','l', 0 , 0 , 0}
			},
			5, 2);
	public char[][] template;
	public int spawnX;
	public int spawnY;
	private TileTemplate(char[][] template, int spawnX, int spawnY)
	{
		this.template = template;
		this.spawnX = spawnX;
		this.spawnY = spawnY;
	}
}