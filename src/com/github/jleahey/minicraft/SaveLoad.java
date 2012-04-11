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
import java.io.*;
import java.util.ArrayList;


public class SaveLoad {

    public static void doSave(Game game) {

        try {
            if(game.inventory == null)
            	return;
            
            FileOutputStream fileOut = new FileOutputStream("MiniCraft.sav");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);


            out.writeObject(game.inventory);
            out.writeObject(game.world);
            out.writeObject(game.entities);

            out.close();
            fileOut.close();
            
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void doLoad(Game game) {

        try {
        	File f = new File("MiniCraft.sav");
        	if(!f.exists())
        		return;
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            game.inventory = (Inventory)in.readObject();
            game.world = (World)in.readObject();
            game.entities = (ArrayList<Entity>)in.readObject();
            
            
            in.close();
            fileIn.close();
            
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
        	e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
