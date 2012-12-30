package com.github.jleahey.minicraft;

import java.util.HashMap;
import java.util.Map;

public class Constants {
	
	public enum TileID {
		DIRT(100), GRASS(0), LEAVES(0), PLANK(112), WOOD(119), STONE(115), AIR(0), WATER(0), SAND(
				110), IRON_ORE(105), COAL_ORE(99), DIAMOND_ORE(109), COBBLE(98), CRAFTING_BENCH(102), ADMINITE(
				0), SAPLING(83), LADDER(76), NONE(0);
		
		// This is for json to link to... It represents what the item will break into
		public final int breaksInto;
		
		TileID(int id) {
			this.breaksInto = id;
		}
	}
	
	public static Map<TileID, Tile> tileTypes = new HashMap<TileID, Tile>();
	public static Map<Integer, TileID> tileIDs = new HashMap<Integer, TileID>();
	
	static {
		tileTypes.put(TileID.DIRT, new Tile(new TileType("sprites/tiles/dirt.png", TileID.DIRT)));
		tileTypes.put(TileID.GRASS, new Tile(new TileType("sprites/tiles/dirtwithgrass.png",
				TileID.GRASS)));
		tileTypes.put(TileID.LEAVES, new Tile(new TileType("sprites/tiles/leaves.png",
				TileID.LEAVES, false, false, 1)));
		tileTypes
				.put(TileID.PLANK, new Tile(new TileType("sprites/tiles/plank.png", TileID.PLANK)));
		tileTypes.put(TileID.WOOD, new Tile(new TileType("sprites/tiles/wood.png", TileID.WOOD,
				true, false, 0)));
		tileTypes
				.put(TileID.STONE, new Tile(new TileType("sprites/tiles/stone.png", TileID.STONE)));
		tileTypes.put(TileID.AIR, new Tile(new TileType("sprites/tiles/air.png", TileID.AIR, true,
				false, 0)));
		tileTypes.put(TileID.WATER, new Tile(new TileType("sprites/tiles/water.png", TileID.WATER,
				true, true, 1)));
		tileTypes.put(TileID.SAND, new Tile(new TileType("sprites/tiles/sand.png", TileID.SAND)));
		tileTypes.put(TileID.IRON_ORE, new Tile(new TileType("sprites/tiles/ironore.png",
				TileID.IRON_ORE)));
		tileTypes.put(TileID.COAL_ORE, new Tile(new TileType("sprites/tiles/coalore.png",
				TileID.COAL_ORE)));
		tileTypes.put(TileID.DIAMOND_ORE, new Tile(new TileType("sprites/tiles/diamondore.png",
				TileID.DIAMOND_ORE)));
		tileTypes.put(TileID.COBBLE, new Tile(new TileType("sprites/tiles/cobble.png",
				TileID.COBBLE)));
		tileTypes.put(TileID.CRAFTING_BENCH, new Tile(new TileType("sprites/tiles/craft.png",
				TileID.CRAFTING_BENCH)));
		tileTypes.put(TileID.ADMINITE, new Tile(new TileType("sprites/tiles/adminite.png",
				TileID.ADMINITE)));
		tileTypes.put(TileID.SAPLING, new Tile(new TileType("sprites/tiles/sapling.png",
				TileID.SAPLING, true, false, 0)));
		tileTypes.put(TileID.LADDER, new Tile(new TileType("sprites/tiles/ladder.png",
				TileID.LADDER, true, false, 0)));
		
		for (TileID tileID : TileID.values()) {
			tileIDs.put(tileID.breaksInto, tileID);
		}
	}
	
	public static Map<Character, Item> itemTypes;
	static {
		itemTypes = ItemLoader.loadItems(16);
	}
	
	public static final int LIGHT_VALUE_TORCH = 13;
	public static final int LIGHT_VALUE_SUN = 15;
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_VISIBILITY_ON = false;
	public static final int LIGHT_VALUE_OPAQUE = 10000;
}
