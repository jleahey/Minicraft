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
import java.io.IOException;

import org.newdawn.easyogg.OggClip;


public class MusicPlayer 
{
	//No one wants a music player crashing their game... ;)
	OggClip ogg;
    public MusicPlayer(String filename)
    {
		try {
			ogg = new OggClip(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    boolean flipMute = true;
    public void toggleSound()
    {
		try {
	    	if(flipMute)
	    		ogg.stop();
	    	else
	    		ogg.loop();
	    	flipMute = !flipMute;
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void play()
    {
    	try {
    		ogg.loop();
	    } catch (Exception e) {
			e.printStackTrace();
		}
    }
    public void close()
    {
    	try {
	    	ogg.stop();
	    	ogg.close();
	    } catch (Exception e) {
			e.printStackTrace();
		}
    }
}
