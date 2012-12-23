package com.github.jleahey.minicraft.awtgraphics;

import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.github.jleahey.minicraft.Color;
import com.github.jleahey.minicraft.GraphicsHandler;
import com.github.jleahey.minicraft.Sprite;
import com.github.jleahey.minicraft.SpriteStore;

/**
 * A sprite to be displayed on the screen. Note that a sprite
 * contains no state information, i.e. its just the image and
 * not the location. This allows us to use a single sprite in
 * lots of different places without having to store multiple
 * copies of the image.
 * 
 * @author Kevin Glass
 */
public class AwtSprite implements com.github.jleahey.minicraft.Sprite {
	private static final long serialVersionUID = 1L;
	
	/** The image to be drawn for this sprite */
	transient public Image image;
	public String ref;
	
	// for serialization loading
	public AwtSprite() {
		AwtSprite s = (AwtSprite) SpriteStore.get().getSprite(ref);
		this.image = s.image;
	}
	
	/**
	 * Create a new sprite based on an image
	 * 
	 * @param image
	 *            The image that is this sprite
	 */
	public AwtSprite(Image image, String ref) {
		this.image = image;
		this.ref = ref;
	}
	
	/**
	 * Get the width of the drawn sprite
	 * 
	 * @return The width in pixels of this sprite
	 */
	public int getWidth() {
		return image.getWidth(null);
	}
	
	/**
	 * Get the height of the drawn sprite
	 * 
	 * @return The height in pixels of this sprite
	 */
	public int getHeight() {
		return image.getHeight(null);
	}
	
	/**
	 * Draw the sprite onto the graphics context provided
	 * 
	 * @param g
	 *            The graphics context on which to draw the sprite
	 * @param x
	 *            The x location at which to draw the sprite
	 * @param y
	 *            The y location at which to draw the sprite
	 */
	public void draw(GraphicsHandler g, int x, int y) {
		g.drawImage(this, x, y);
	}
	
	public void draw(GraphicsHandler g, int x, int y, Color tint) {
		g.drawImage(this, x, y, tint);
	}
	
	public void draw(GraphicsHandler g, int x, int y, int width, int height) {
		g.drawImage(this, x, y, width, height);
	}
	
	public void draw(GraphicsHandler g, int x, int y, int width, int height, Color tint) {
		g.drawImage(this, x, y, width, height, tint);
	}
	
	/**
	 * Always treat de-serialization as a full-blown constructor, by
	 * validating the final state of the de-serialized object.
	 */
	@Override
	public void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException,
			IOException {
		// always perform the default de-serialization first
		// aInputStream.defaultReadObject();
		ref = (String) aInputStream.readObject();
		this.image = ((AwtSprite) AwtSpriteStore.get().getSprite(ref)).image;
	}
	
	/**
	 * This is the default implementation of writeObject.
	 * Customise if necessary.
	 */
	@Override
	public void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		// perform the default serialization for all non-transient, non-static fields
		aOutputStream.writeObject(ref);
		aOutputStream.defaultWriteObject();
	}
}