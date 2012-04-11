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

public class Item extends Entity{

	public char name;
	public Template template;
	
	protected Item(){
		super();
	}
	
	public Item(String ref, boolean gravityApplies, float x, float y,
			int width, int height, char name, char[][] template, int templateCount) {
		super(ref, gravityApplies, x, y, width, height);
		this.template = new Template(template, templateCount);
		this.name = name;
	}
	
	public Object clone()
	{
		Item result = new Item();
		result.x = x;
		result.y = y;
		result.dx = dx;
		result.dy = dy;
		result.sprite = sprite;
		result.name = name;
		result.gravityApplies = gravityApplies;
		result.heightPX = heightPX;
		result.widthPX = widthPX;
		return result;
	}

}
