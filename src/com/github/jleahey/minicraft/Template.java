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

public class Template implements java.io.Serializable
{
	public int outCount;
	public Int2 position = new Int2(0,0);
	private char[][] matrix;
	
	public Template(char[][] matrix, int outCount)
	{
		this.matrix = matrix;
		this.outCount = outCount;
	}
	
	public boolean compare(char[][] input)
	{
		if(matrix == null)
			return false;
		//boolean foundSomething = false;
		for(int x = 0; x <= (input.length - matrix.length); x++)
			for(int y = 0; y <= (input[0].length - matrix[0].length); y++)
			{
				boolean isGood = false;
				boolean isBad = false;
				//now try the template here -- some bruteforcing
				for(int i = 0; i < matrix.length; i++)
					for(int j = 0; j < matrix[0].length; j++)
					{
						if(matrix[i][j] != input[x+i][y+j])
						{
							if(input[x+i][y+j] != 0)
								return false;
							if(matrix[i][j] != 0 && input[x+i][y+j] == 0)
								isBad = true;
						}
						else if(input[x+i][y+j] != 0 && matrix[i][j] != 0)
							isGood = true;
					}
				
				if(isGood && !isBad)
				{
					position.x = x;
					position.y = y;
					return true;
				}

			}

		return false;
	}
}
