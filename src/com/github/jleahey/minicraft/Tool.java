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

public class Tool extends Item
{
	public enum ToolType { Shovel, Pick, Axe};
	public enum ToolPower { Wood, Stone, Metal, Diamond};
	int totalUses;
	int uses;
	public ToolType toolType;
	public ToolPower toolPower;
	
	private Tool(){super();}
	
	public Tool(String ref, boolean gravityApplies, float x, float y,
			int width, int height, char name, char[][] template,
			int templateCount, ToolType toolType, ToolPower toolPower) {
		super(ref, gravityApplies, x, y, width, height, name, template, templateCount);
		if(toolPower == ToolPower.Wood)
			totalUses = 32;
		else if(toolPower == ToolPower.Stone)
			totalUses = 64;
		else if(toolPower == ToolPower.Metal)
			totalUses = 128;
		else
			totalUses = 256;
		this.toolPower = toolPower;
		this.toolType = toolType;
	}
	
	public Object clone()
	{
		Tool result = new Tool();
		result.x = x;
		result.y = y;
		result.dx = dx;
		result.dy = dy;
		result.sprite = sprite;
		result.name = name;
		result.gravityApplies = gravityApplies;
		result.heightPX = heightPX;
		result.widthPX = widthPX;
		result.totalUses = totalUses;
		result.uses = 0; //Reset the counter
		result.toolType = toolType;
		result.toolPower = toolPower;
		
		return result;
	}
}
