package com.github.jleahey.minicraft;

import java.util.HashMap;
import java.util.Map;

public class Constants {
	public static Map<Character, Tile> tileTypes = new HashMap<Character, Tile>();
	static {
		tileTypes.put('d', new Tile(new TileType("sprites/tiles/dirt.png", 'd')));
		tileTypes.put('g', new Tile(new TileType("sprites/tiles/dirtwithgrass.png", 'g')));
		tileTypes.put('l', new Tile(new TileType("sprites/tiles/leaves.png", 'l')));
		tileTypes.put('p', new Tile(new TileType("sprites/tiles/plank.png", 'p')));
		tileTypes.put('w', new Tile(new TileType("sprites/tiles/wood.png", 'w', true, false)));
		tileTypes.put('s', new Tile(new TileType("sprites/tiles/stone.png", 's')));
		tileTypes.put('a', new Tile(new TileType("sprites/tiles/air.png", 'a', true, false)));
		tileTypes.put('t', new Tile(new TileType("sprites/tiles/water.png", 't', true, true)));
		tileTypes.put('n', new Tile(new TileType("sprites/tiles/sand.png", 'n')));
		tileTypes.put('i', new Tile(new TileType("sprites/tiles/ironore.png", 'i')));
		tileTypes.put('c', new Tile(new TileType("sprites/tiles/coalore.png", 'c')));
		tileTypes.put('m', new Tile(new TileType("sprites/tiles/diamondore.png", 'm')));
		tileTypes.put('b', new Tile(new TileType("sprites/tiles/cobble.png", 'b')));
		tileTypes.put('f', new Tile(new TileType("sprites/tiles/craft.png", 'f')));
		tileTypes.put('x', new Tile(new TileType("sprites/tiles/adminite.png", 'x')));
		tileTypes.put('S', new Tile(new TileType("sprites/tiles/sapling.png", 'S', true, false)));
		tileTypes.put('L', new Tile(new TileType("sprites/tiles/ladder.png", 'L', true, false)));
	}
}
