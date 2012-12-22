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

public abstract class Entity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	protected static final float gravityAcceleration = .03f;
	protected static final float waterAcceleration = .015f;
	protected static final float maxWaterDY = .05f;
	protected static final float maxDY = .65f;
	
	public float x;
	public float y;
	public float dx;
	public float dy;
	
	protected Sprite sprite;
	protected boolean gravityApplies;
	protected int widthPX;
	protected int heightPX;
	
	private float lastYPeak;
	
	public Entity(String ref, boolean gravityApplies, float x, float y, int width, int height) {
		if (ref != null) {
			this.sprite = SpriteStore.get().getSprite(ref);
		}
		this.gravityApplies = gravityApplies;
		this.x = x;
		this.y = y;
		this.widthPX = width;
		this.heightPX = height;
		this.dx = this.dy = 0;
	}
	
	@Override
	protected Entity clone() throws CloneNotSupportedException {
		return (Entity) super.clone();
	}
	
	public void updatePosition(World world, int tileSize) {
		int pixels = (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) * tileSize);
		
		boolean favorVertical = (Math.abs(dy) > Math.abs(dx));
		boolean hitTop = false;
		boolean hitBottom = false;
		
		float left = this.getLeft(tileSize);
		float right = this.getRight(tileSize);
		float top = this.getTop(tileSize);
		float bottom = this.getBottom(tileSize);
		
		boolean topLeft = true;
		boolean topRight = true;
		boolean bottomLeft = true;
		boolean bottomRight = true;
		boolean middleLeft = true;
		boolean middleRight = true;
		
		if (dy < 0.01 && dy > -0.01) {
			lastYPeak = y;
		}
		
		if (favorVertical) {
			for (int i = 1; i <= pixels && topLeft && topRight && bottomLeft && bottomRight; i++) {
				float scale = 1.f / pixels;
				top = top + dy * scale;
				bottom = bottom + dy * scale;
				
				topLeft = world.passable((int) left, (int) top);
				topRight = world.passable((int) right, (int) top);
				bottomLeft = world.passable((int) left, (int) bottom);
				bottomRight = world.passable((int) right, (int) bottom);
				middleLeft = world.passable((int) left, (int) (top + (bottom - top) / 2));
				middleRight = world.passable((int) right, (int) (top + (bottom - top) / 2));
				
				if (!(topLeft && topRight && bottomLeft && bottomRight && middleLeft && middleRight)) {
					hitTop |= !topLeft || !topRight;
					hitBottom |= !bottomLeft || !bottomRight;
					top = top - dy * scale;
					bottom = bottom - dy * scale;
					// pixels = i;
				}
			}
			for (int i = 1; i <= pixels && topLeft && topRight && bottomLeft && bottomRight; i++) {
				float scale = 1.f / pixels;
				left = left + dx * scale;
				right = right + dx * scale;
				
				topLeft = world.passable((int) left, (int) top);
				topRight = world.passable((int) right, (int) top);
				bottomLeft = world.passable((int) left, (int) bottom);
				bottomRight = world.passable((int) right, (int) bottom);
				middleLeft = world.passable((int) left, (int) (top + (bottom - top) / 2));
				middleRight = world.passable((int) right, (int) (top + (bottom - top) / 2));
				
				if (!(topLeft && topRight && bottomLeft && bottomRight && middleLeft && middleRight)) {
					left = left - dx * scale;
					right = right - dx * scale;
				}
			}
		} else {
			for (int i = 1; i <= pixels && topLeft && topRight && bottomLeft && bottomRight; i++) {
				float scale = 1.f / pixels;
				left = left + dx * scale;
				right = right + dx * scale;
				
				topLeft = world.passable((int) left, (int) top);
				topRight = world.passable((int) right, (int) top);
				bottomLeft = world.passable((int) left, (int) bottom);
				bottomRight = world.passable((int) right, (int) bottom);
				middleLeft = world.passable((int) left, (int) (top + (bottom - top) / 2));
				middleRight = world.passable((int) right, (int) (top + (bottom - top) / 2));
				
				if (!(topLeft && topRight && bottomLeft && bottomRight && middleLeft && middleRight)) {
					left = left - dx * scale;
					right = right - dx * scale;
					// pixels = i;
				}
			}
			for (int i = 1; i <= pixels && topLeft && topRight && bottomLeft && bottomRight; i++) {
				float scale = 1.f / pixels;
				top = top + dy * scale;
				bottom = bottom + dy * scale;
				
				topLeft = world.passable((int) left, (int) top);
				topRight = world.passable((int) right, (int) top);
				bottomLeft = world.passable((int) left, (int) bottom);
				bottomRight = world.passable((int) right, (int) bottom);
				middleLeft = world.passable((int) left, (int) (top + (bottom - top) / 2));
				middleRight = world.passable((int) right, (int) (top + (bottom - top) / 2));
				
				if (!(topLeft && topRight && bottomLeft && bottomRight && middleLeft && middleRight)) {
					hitTop |= !topLeft || !topRight;
					hitBottom |= !bottomLeft || !bottomRight;
					top = top - dy * scale;
					bottom = bottom - dy * scale;
					// pixels = i;
				}
			}
		}
		
		// for(float i = 1; i <= pixels; i++)
		// {
		// float scale = 1.f/pixels;
		// left = left + dx*scale;
		// right = right + dx*scale;
		// top = top + dy*scale;
		// bottom = bottom + dy*scale;
		//
		// boolean topLeft = world.passable((int)left, (int)top);
		// boolean topRight = world.passable((int)right, (int)top);
		// boolean bottomLeft = world.passable((int)left, (int)bottom);
		// boolean bottomRight = world.passable((int)right, (int)bottom);
		// if(!(topLeft && topRight && bottomLeft && bottomRight))
		// {
		// hitTop = topLeft || topRight;
		// hitBottom = bottomLeft || bottomRight;
		//
		// left = left - dx*scale;
		// right = right - dx*scale;
		// top = top - dy*scale;
		// bottom = bottom - dy*scale;
		//
		// break;
		// }
		// }
		
		if (gravityApplies) {
			if (world.isClimbable((int) left, (int) top)
					|| world.isClimbable((int) right, (int) top)
					|| world.isClimbable((int) left, (int) bottom)
					|| world.isClimbable((int) right, (int) bottom)) {
				dy += waterAcceleration;
				if (dy > 0) {
					dy = Math.min(maxWaterDY + .000001f, dy);
				} else {
					dy = Math.max(-maxWaterDY + .000001f, dy);
				}
			} else {
				dy += gravityAcceleration;
				if (dy > 0) {
					dy = Math.min(maxDY, dy);
				} else {
					dy = Math.max(-maxDY, dy);
				}
			}
		}
		if (hitTop) {
			dy = 0.0000001f;
		} else if (hitBottom) {
			int dmg = (int) (y - lastYPeak - 2.75);
			if (dmg > 0)
				this.takeDamage(dmg * 5);
			dy = 0;
			lastYPeak = y;
		}
		
		x = left;
		y = top;
		
		// float onePixel = 1.f/tileSize;
		//
		//
		// int y = this.getBottom(tileSize);
		// int leftX = (int)(this.getLeft(tileSize) - onePixel);
		// int rightX = (int)(this.getRight(tileSize) + onePixel);
		// System.out.println("Left "+leftX + " Right "+rightX + " Y " + y);
		//
		//
		//
		//
		// float dx = this.dx;
		// float dy = this.dy;
		// this.x += dx;
		// this.dx = dx;
		//
		// if(gravityApplies)
		// {
		// dy += gravityAcceleration;
		// dy = Math.min(maxDY, dy);
		// }
		//
		// //left
		// if(dx < 0 && (!world.passable(leftX,y-1) || !world.passable(leftX,y-2)))
		// {
		// this.x -= dx;
		// }
		// //right
		// if(dx > 0 && (!world.passable(rightX,y-1) || !world.passable(rightX,y-2)))
		// {
		// this.x -= dx;
		// }
		//
		//
		//
		// leftX = this.getLeft(tileSize);
		// rightX = this.getRight(tileSize);
		//
		// //bottom
		// if(dy >= 0 && (!world.passable(leftX,y) || !world.passable(rightX,y)))
		// {
		// this.setYInd(y, tileSize);
		// //this.y -= this.dy;
		// dy = 0;
		// }
		//
		// //top
		// if(dy < 0 && (!world.passable(leftX,y-2) || !world.passable(rightX,y-2)))
		// {
		// this.setYInd(y, tileSize);
		// //this.y -= this.dy;
		// dy = 0;
		// }
		//
		//
		//
		//
		//
		// this.y += dy;
		// this.dy = dy;
		
	}
	
	public float getCenterY(int tileSize) {
		return y + (float) heightPX / (2 * tileSize);
	}
	
	public float getCenterX(int tileSize) {
		return x + (float) widthPX / (2 * tileSize);
	}
	
	public float getTop(int tileSize) {
		return y;
	}
	
	public float getBottom(int tileSize) {
		return (y + (float) (heightPX) / tileSize);
	}
	
	public float getLeft(int tileSize) {
		return x;
	}
	
	public float getRight(int tileSize) {
		return x + (float) (widthPX) / tileSize;
	}
	
	public void setXInd(int xInd) {
		x = xInd;
	}
	
	public void setYInd(int yInd, int tileSize) {
		y = yInd - (float) (heightPX + 1) / tileSize;
	}
	
	public boolean isInWater(World world, int tileSize) {
		int left = (int) this.getLeft(tileSize);
		int right = (int) this.getRight(tileSize);
		int top = (int) this.getTop(tileSize);
		int bottom = (int) this.getBottom(tileSize);
		return (world.isLiquid(left, top) || world.isLiquid(right, top)
				|| world.isLiquid(left, bottom) || world.isLiquid(right, bottom));
	}
	
	public boolean isHeadUnderWater(World world, int tileSize) {
		int top = (int) this.getTop(tileSize);
		int centerX = (int) this.getCenterX(tileSize);
		return world.isLiquid(centerX, top);
	}
	
	public boolean isInWaterOrClimbable(World world, int tileSize) {
		int left = (int) this.getLeft(tileSize);
		int right = (int) this.getRight(tileSize);
		int top = (int) this.getTop(tileSize);
		int bottom = (int) this.getBottom(tileSize);
		return (world.isLiquid(left, top) || world.isLiquid(right, top)
				|| world.isLiquid(left, bottom) || world.isLiquid(right, bottom)
				|| world.isClimbable(left, top) || world.isClimbable(right, top)
				|| world.isClimbable(left, bottom) || world.isClimbable(right, bottom));
	}
	
	public boolean collidesWith(Entity entity, int tileSize) {
		float left1, left2;
		float right1, right2;
		float top1, top2;
		float bottom1, bottom2;
		
		left1 = this.x;
		left2 = entity.x;
		right1 = this.getRight(tileSize);
		right2 = entity.getRight(tileSize);
		top1 = this.y;
		top2 = entity.y;
		bottom1 = this.getBottom(tileSize);
		bottom2 = entity.getBottom(tileSize);
		
		return !(bottom1 < top2 || top1 > bottom2 || right1 < left2 || left1 > right2);
	}
	
	public void draw(GraphicsHandler g, float cameraX, float cameraY, int screenWidth,
			int screenHeight, int tileSize) {
		Int2 pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, screenWidth,
				screenHeight, tileSize, x, y);
		if (StockMethods.onScreen) {
			sprite.draw(g, pos.x, pos.y, widthPX, heightPX);
		}
	}
	
	// Only living entities have hitpoints; they should override these methods.
	public void takeDamage(int amount) {
	}
	
	public void heal(int amount) {
	}
}
