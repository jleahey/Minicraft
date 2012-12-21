package com.github.jleahey.minicraft;

import java.util.HashMap;

import com.github.jleahey.minicraft.awtgraphics.AwtSpriteStore;

public abstract class SpriteStore {
	/** The single instance of this class */
	protected static SpriteStore single;
	
	/**
	 * Get the single instance of this class
	 * 
	 * @return The single instance of this class
	 */
	public static SpriteStore get() {
		if (single == null) {
			if (GraphicsHandler.awtMode) {
				single = new AwtSpriteStore();
			} else {
				// android!
			}
		}
		return single;
	}
	
	/** The cached sprite map, from reference to sprite instance */
	private HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
	
	/**
	 * Retrieve a sprite from the store
	 * 
	 * @param ref
	 *            The reference to the image to use for the sprite
	 * @return A sprite instance containing an accelerate image of the request reference
	 */
	public Sprite getSprite(String ref) {
		// if we've already got the sprite in the cache
		// then just return the existing version
		if (sprites.get(ref) != null) {
			return sprites.get(ref);
		}
		
		// create a sprite, add it the cache then return it
		Sprite sprite = loadSprite(ref);
		sprites.put(ref, sprite);
		
		return sprite;
	}
	
	public abstract Sprite loadSprite(String ref);
}
