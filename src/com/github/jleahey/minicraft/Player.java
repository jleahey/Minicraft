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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;


public class Player extends Entity
{
	final float walkSpeed = .1f;
	final float swimSpeed = .04f;
	float armLength = 4.5f;
	Int2 handBreakPos = new Int2(0,0);
	Int2 handBuildPos = new Int2(0,0);
	private float moveDirection = 0;
	
	private Sprite leftWalkSprite;
	private Sprite rightWalkSprite;
	private long ticksAlive = 0;
	
	public float handStartX;
	public float handStartY;
	public float handEndX;
	public float handEndY;
	public boolean climbing = false;
	
	boolean jumping = false;
	boolean facingRight = true;
	
	public Player(boolean gravityApplies, float x, float y,
			int width, int height) {
		super(null, gravityApplies, x, y, width, height);
		
		leftWalkSprite = SpriteStore.get().getSprite("sprites/entities/left_man.png");
		rightWalkSprite = SpriteStore.get().getSprite("sprites/entities/right_man.png");
		sprite = SpriteStore.get().getSprite("sprites/entities/player.gif");
		
	}

	public void jump(World world, int tileSize)
	{
		if(jumping)
			return;

		if(!this.isInWater(world, tileSize))
		{
			dy = -.3f;
			jumping = true;
		}
		else
		{
			dy = -maxWaterDY-.000001f;//BIG HACK
		}
	}
	
	public void updatePosition(World world, int tileSize)
	{
		ticksAlive++;
		boolean isSwimClimb = this.isInWaterOrClimbable(world, tileSize);
		if(isSwimClimb)
			dx = moveDirection * swimSpeed;
		else
			dx = moveDirection * walkSpeed;
		if(climbing && isSwimClimb)
		{
			jumping = false;
			dy = -maxWaterDY-.000001f;//BIG HACK
		}
		super.updatePosition(world, tileSize);
		if(this.dy == 0)
			jumping = false;
		if(this.isInWater(world, tileSize))
			jumping = false;
	}
	public void startLeft(boolean slow)
	{
		facingRight = false;
		if(slow)
			moveDirection = -.2f;
		else
			moveDirection = -1;
	}
	public void stopLeft()
	{
		if(moveDirection < 0)
			moveDirection = 0;
	}	
	public void startRight(boolean slow)
	{
		facingRight = true;
		if(slow)
			moveDirection = .2f;
		else
			moveDirection = 1;
	}
	public void stopRight()
	{
		if(moveDirection > 0)
			moveDirection = 0;
	}
	
	public void startClimb()
	{
		climbing = true;
	}
	public void endClimb()
	{
		climbing = false;
	}
	
	public float findIntersection(float rayOx, float rayOy, float m, float p1x, float p1y, float p2x, float p2y) {
		
		float freeVar = -1;
		if(p1x == p2x)//segment is vertical
		{
			freeVar = -m*(rayOx - p1x) + rayOy;//y1
			if((freeVar < p1y && freeVar < p2y) || (freeVar > p1y && freeVar > p2y))
					return -1;
		}
		else if(p1y == p2y)//segment is horizontal
		{
			freeVar = -(rayOy - p1y)/m + rayOx;//x1
			if((freeVar < p1x && freeVar < p2x) || (freeVar > p1x && freeVar > p2x))
					return -1;
		}
		else
			System.out.println("Find intersection -- bad arguments");
		
		return freeVar;
	}
	
	public void updateHand(float cameraX, float cameraY, Graphics2D g, float mouseX, float mouseY, World world, int tileSize)
	{
		/*
		float startX = this.getCenterX(tileSize);
		float startY = this.getCenterY(tileSize);
		
		float stepX = startX;
		float stepY = startY;
		
		float prevX = -1;
		float prevY = -1;
		
		float m;
		if(startX - mouseX == 0)
			m = Float.MAX_VALUE;
		else
			m = (startY - mouseY)/(startX - mouseX);
		
		float pixel = 1f/tileSize;
		
		float nudgeX = pixel*m;//(pixel - startY)/m + startX;
		float nudgeY = pixel*(1/m);//m*(startX-pixel) + startY;
		
		int steps = 0;
		while(!world.isBreakable((int)stepX, (int)stepY))
		{
			prevX = stepX;
			prevY = stepY;
			
			stepX += nudgeX;//startX + steps*pixel;
			stepY += nudgeY; //-m*(startX - stepX) + startY;
			steps++;
			if(steps > armLength*tileSize)
			{
				handBreakPos.x = -1;
				handBreakPos.y = -1;
				 
				handBuildPos.x = -1;
				handBuildPos.y = -1;
				return;
			}
		}
		
		
		handBreakPos.x = (int)stepX;
		handBreakPos.y = (int)stepY;
		 
		handBuildPos.x = (int)prevX;
		handBuildPos.y = (int)prevY;
		
		handEndX = prevX+1;
		handEndY = prevY;
		*/
		
		float x = .5f+ (int)this.getCenterX(tileSize);
		float y = .5f+ (int)this.getCenterY(tileSize);

		handStartX = x;
		handStartY = y;
		
		float tMax = (float) Math.ceil(armLength);
		int hitX = -1;
		int hitY = -1;
		handEndX = -1;
		handEndY = -1;
		
		handBuildPos.x = -1;
		handBuildPos.y = -1;

		float m;
		if(x - mouseX == 0)
			m = Float.MAX_VALUE;
		else
			m = (y - mouseY)/(x - mouseX);
		
		for(float i = 0; i <= Math.ceil(armLength)*2; i++)
		{
			for(float j = 0; j <= Math.ceil(armLength)*2; j++)
			{
				float px = (float) (x - Math.ceil(armLength) + i) - .5f;
				float py = (float) (y - Math.ceil(armLength) + j) - .5f;
				if(!world.isBreakable((int)px, (int)py))
					continue;
				
				

				
				
				float down = -1;
				float left = -1;
				float up = -1;
				float right = -1;

				float downY = py+1;
				float rightX = px+1;
				float upY = py-1;
				float leftX = px;

				
				
				
				if((x >= px && x >= mouseX) &&//left 
					(y >= py && y >= mouseY)) //up
				{
					right = findIntersection(x,y,m,px+1,py,  px+1,py+1);
					down =  findIntersection(x,y,m,px  ,py+1,px+1,py+1);
				}
				else if((x- .5f <= px && x <= mouseX) &&//right
					(y- .5f >= py && y >= mouseY)) //up
				{
					left =  findIntersection(x,y,m,px  ,py,  px,  py+1);
					down =  findIntersection(x,y,m,px  ,py+1,px+1,py+1);
				}
				else if((x >= px && x >= mouseX) &&//left
					(y-1 < py && y <= mouseY)) //down
				{
					right = findIntersection(x,y,m,px+1,py,  px+1,py+1);
					up =    findIntersection(x,y,m,px  ,py,  px+1,py);
				}
				else if((x- .5f <= px && x <= mouseX) &&//right
					(y- .5f <= py && y <= mouseY)) //down
				{
					left =  findIntersection(x,y,m,px  ,py,  px,  py+1);
					up =    findIntersection(x,y,m,px  ,py,  px+1,py);
				}
				else
					continue;
				
				
				

				
				

				if(down != -1 || left!= -1  || up!= -1  || right!= -1 )
				{
					Int2 pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize, tileSize, tileSize, x, y);
					/*
					int posX = pos.x;
					int posY = pos.y;
					pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, tileSize, tileSize, tileSize, px, py);
					g.setColor(Color.red);
					g.drawLine(pos.x, pos.y, posX, posY);
					SpriteStore.get().getSprite("sprites/tiles/diamondore.png").draw(g, pos.x, pos.y, tileSize, tileSize);
					*/
					float newTMax = (float) Math.sqrt(Math.pow(Math.abs(x)-Math.abs(px), 2) + Math.pow(Math.abs(y)-Math.abs(py),2));
					if(newTMax >= tMax)
						continue;

					if(up != -1)
					{
						handEndX = up;
						handEndY = upY;
						handBuildPos.x = (int) px;
						handBuildPos.y = (int) py-1;
					}
					if(down != -1 )
					{
						handEndX = down;
						handEndY = downY;
						handBuildPos.x = (int) px;
						handBuildPos.y = (int) py+1;
					}
					if(left != -1 )
					{
						handEndX = leftX;
						handEndY = left;
						handBuildPos.x = (int) px-1;
						handBuildPos.y = (int) py;
					}
					if(right != -1 )
					{
						handEndX = rightX;
						handEndY = right;
						handBuildPos.x = (int) px+1;
						handBuildPos.y = (int) py;
					}
					
					hitX = (int) px;
					hitY = (int) py;
					
					tMax = newTMax;
				}
			}
		}
		
		handBreakPos.x = hitX;
		handBreakPos.y = hitY;
		
	}
	
	
	
	
	public void draw(Graphics g, float cameraX, float cameraY, int screenWidth, int screenHeight, int tileSize)
	{
		Int2 pos = StockMethods.computeDrawLocationInPlace(cameraX, cameraY, screenWidth, screenHeight, tileSize, x, y);
		if(StockMethods.onScreen)
		{
			int frame = (int)x%4;//(int) ((ticksAlive/20)%4);
			if(facingRight)
			{
				if(frame == 0 || frame == 2 || dx <= 0)
				{
					sprite.draw(g, pos.x, pos.y, widthPX, heightPX);
				}
				else if(frame == 1)
					rightWalkSprite.draw(g, pos.x, pos.y, widthPX, heightPX);
				else
					leftWalkSprite.draw(g, pos.x, pos.y, widthPX, heightPX);
					
				
			}
			else
			{
				if(frame == 0 || frame == 2 || dx >= 0)
				{
					sprite.draw(g, pos.x+widthPX, pos.y, -widthPX, heightPX);
				}
				else if(frame == 1)
					rightWalkSprite.draw(g, pos.x+widthPX, pos.y, -widthPX, heightPX);
				else
					leftWalkSprite.draw(g, pos.x+widthPX, pos.y, -widthPX, heightPX);
				
			}
				
		}
	}
	


}
