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

public class Int2 implements java.io.Serializable
{
    public int x, y;

    public Int2(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    public boolean Equals(Object obj)
    {
        Int2 compare = (Int2)obj;
        return x == compare.x && y == compare.y;
    }

    public String ToString()
    {
        return "{" + x + ", " + y + "}";
    }
}
