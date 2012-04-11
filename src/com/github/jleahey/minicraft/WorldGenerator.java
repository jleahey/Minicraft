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
import java.util.Random;


public class WorldGenerator 
{
	
	public static boolean[][] visibility;
	public static Int2 playerLocation;
	public static char[][] generate(int width, int height, Random random)
	{
		char[][] world = new char[width][height];
		visibility = new boolean[width][height];
		for(int i = 0; i < visibility.length; i++)
			for(int j = 0; j < visibility[0].length; j++)
				visibility[i][j] = true;
		
		playerLocation = new Int2(width/2,5);
		
		int seed = random.nextInt();
		System.out.println("Seed: "+ seed);
		random.setSeed(seed);
		int median = (int) (.5 * height);
		

		int minDirtDepth = 2;
		int maxDirtDepth = 5;
		int minSurface = (int) (.25 * height);
		int maxSurface = (int) (.75 * height);
		
		int surface = median;//maxSurface-5;
		int dirtDepth = 3;
		
		ArrayList<Int2> trees = new ArrayList<Int2>();
		
		int surfaceSum = 0;
		
		boolean playerLocFound = false;
		double chance;
		//loop left to right dirt and stone
		for(int i = 0; i < width; i++)
		{
			if(surface > median)
				surfaceSum++;
			else
				surfaceSum--;
			
			chance = random.nextDouble();
			if(chance > .75)
				dirtDepth = Math.min(maxDirtDepth, dirtDepth+1);
			else if(chance > .5)
				dirtDepth = Math.max(minDirtDepth, dirtDepth-1);
			
			chance = random.nextDouble();
			if(chance > .75)
				surface = Math.min(maxSurface, surface+1);
			else if(chance > .5)
				surface = Math.max(minSurface, surface-1);
			
			if(surfaceSum > width/16)
				surface = Math.min(maxSurface, surface-3);
			if(surfaceSum < -width/16)
				surface = Math.min(maxSurface, surface+3);
			
			
			if(random.nextDouble() > .8)
				trees.add(new Int2(i, surface-1));

			if(i>width/4 && surface<median && world[i-1][surface-1] == 0 && world[i-1][surface] == 'g' && !playerLocFound)
			{
				playerLocation.x = i;
				playerLocation.y = surface - 2;
				playerLocFound = true;
			}
			
			for(int j = 0; j <= surface; j++)
			{
				setVisible(i+1,j);
				setVisible(i,j+1);
				setVisible(i-1,j);
				setVisible(i,j-1);
			}
			
			world[i][surface] = 'g';
			for(int j = 1; j <= dirtDepth; j++)
			{
				world[i][surface+j] = 'd';
				visibility[i][surface+j] = false;
			}
			for(int j = dirtDepth; surface+j < height; j++)
			{
				world[i][surface+j] = 's';
				visibility[i][surface+j] = false;
			}
		}
		
		//water
		for(int i = 0; i < width; i++)
		{
			if(world[i][median] != 0)
				continue;
			
			//flood fill down
			for(int j = median; j < height; j++)
			{
				//setVisible(i+1,j);
				//setVisible(i,j+1);
				//setVisible(i-1,j);
				//setVisible(i,j-1);
				
				if(world[i][j] != 0)
				{
					carve(world, i, j-1, 1 + random.nextDouble()*2, 'n', new char[]{'t', 0}, false);
					break;
				}
				world[i][j] = 't';
			}
		}
		
		
		//coal
		int coalCount = (int) (width/2+ random.nextDouble()*3);
		for(int i = 0; i < coalCount; i++)
		{
			int posX = random.nextInt(width);
			int posY = random.nextInt(height);
			if(world[posX][posY] == 's')
			{
				double coalSize = 1+ random.nextDouble()*.6;
				carve(world, posX, posY, coalSize, 'c', new char[]{'d', 'n', 't', 0}, false);
			}
		}
		
		//iron
		int ironCount = (int) (width/4+ random.nextDouble()*3);
		for(int i = 0; i < ironCount; i++)
		{
			int posX = random.nextInt(width);
			int posY = random.nextInt(height/2)+height/2;
			if(world[posX][posY] == 's')
			{
				double ironSize = 1+ random.nextDouble()*.6;
				carve(world, posX, posY, ironSize, 'i', new char[]{'d', 'n', 'c', 't', 0}, false);
			}
		}
		
		char[] diamondIgnore = new char[]{'d', 'n', 'c', 't', 0};
		//diamond
		int diamondCount = (int) (width/16 + random.nextDouble()*3);
		for(int i = 0; i < diamondCount; i++)
		{
			int posX = random.nextInt(width);
			int posY = random.nextInt(height/8)+height*7/8;
			if(world[posX][posY] == 's')
			{
				double diamondSize = 1+ random.nextDouble()*.45;
				carve(world, posX, posY, diamondSize, 'm', diamondIgnore, false);
			}
		}
		
		
		char[] caveIgnore = new char[]{'d','c', 't', 'g', 'n', 0};
		//caves
		int caveCount = (int) (width/16 + random.nextDouble()*3);
		for(int i = 0; i < caveCount; i++)
		{
			int posX = random.nextInt(width);
			int posY = random.nextInt(height/8)+height*7/8;
			int caveLength = random.nextInt(width);
			int directionX = -1 + random.nextInt(3);
			int directionY = -1 + random.nextInt(3);
			for(int j = 0; j < caveLength; j++)
			{
				chance = random.nextDouble();
				//change direction
				if(chance > .9)
				{
					directionX = -1 + random.nextInt(3);
					directionY = -1 + random.nextInt(3);
				}
				posX += directionX + -1 + random.nextInt(3);
				posY += directionY + -1 + random.nextInt(3);
				if(posX < 0 || posX >= width || posY <= median || posY >= height)
					break;
				double caveSize = 1+ random.nextDouble()*.45;
				carve(world, posX, posY, caveSize, '\0', caveIgnore, false);
			}
		}
		
		
		for (Int2 pos : trees)
		{
			if(world[pos.x][pos.y+1] == 'g')
				addTemplate(world, TileTemplate.tree, pos);
		}
		
		return world;
	}
	
	private static void setVisible(int x, int y)
	{
		if(x < 0 || x >= visibility.length || y < 0 || y >= visibility[0].length)
			return;
		visibility[x][y] = true;
	}
	
	private static void carve(char[][] world, int x, int y, double distance, char type, char[] ignoreTypes, boolean left)
	{
		for(int i = -(int)distance; (!left && i <= (int)distance) || (left && i <= 0); i++)
		{
			int currentX = x + i;
			if(currentX < 0 || currentX >= world.length)
				continue;
			for(int j = -(int)distance; j <= (int)distance; j++)
			{
				int currentY = y + j;
				if(currentY < 0 || currentY >= world[0].length)
					continue;
				boolean ignoreThis = false;
				for(char ignore : ignoreTypes)
					if(world[currentX][currentY] == ignore)
						ignoreThis = true;
				if(ignoreThis)
					continue;
				if(Math.sqrt(i*i+j*j) <= distance)
					world[currentX][currentY] = type;
			}
		}
	}
	
	private static void addTemplate(char[][] world, TileTemplate tileTemplate, Int2 position)
	{
		for(int i = 0; i < tileTemplate.template.length; i++)
			for(int j = 0; j < tileTemplate.template[0].length; j++)
				if(tileTemplate.template[i][j] != 0 
						&& position.x  - tileTemplate.spawnY + i >= 0
						&& position.x  - tileTemplate.spawnY + i < world.length
						&& position.y  - tileTemplate.spawnX + j >= 0
						&& position.y  - tileTemplate.spawnX + j < world[0].length)
					world[position.x  - tileTemplate.spawnY + i][position.y  - tileTemplate.spawnX + j] = tileTemplate.template[i][j];
	}
}
