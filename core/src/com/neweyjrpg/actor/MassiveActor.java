package com.neweyjrpg.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.neweyjrpg.constants.Constants;
import com.neweyjrpg.enums.Enums;
import com.neweyjrpg.interfaces.IHasGraphics;
import com.neweyjrpg.interfaces.IProducesInputs;
import com.neweyjrpg.physics.BlockBody;

public class MassiveActor extends GameActor implements IHasGraphics {

	private TextureRegion[][] graphics;
	private float gWidth, gHeight; //Height of each individual texture; must all be same size!
	private float totalWidth, totalHeight;
	
	public MassiveActor(float x, float y, BlockBody phys, TextureRegion[][] graphics, float gWidth, float gHeight, Enums.Priority priority) {
		super(x, y, phys, priority);
		
		this.gWidth = gWidth;
		this.gHeight = gHeight;
		this.graphics = graphics;
		
		totalWidth = 0; 
		totalHeight= graphics.length * gHeight;
		for (int i=0; i < graphics.length; i++) {
			if (graphics[i].length * gWidth > totalWidth)
				totalWidth = graphics[i].length * gWidth;
		}
	}

	@Override
	public void draw(Batch batch, float deltaTime) {
		super.draw(batch, deltaTime);
		for(int i=0; i < graphics.length; i++) {
			for(int j=0; j < graphics[i].length; j++) {
				batch.draw(graphics[i][j], getX() + (j * gWidth), getY() + (i * gHeight));
			}
		}
	}

	@Override
	public void draw(Batch batch, float deltaTime, float offsetX, float offsetY) {
		float x = this.getX() + offsetX;
		float y = this.getY() + offsetY;
		for(int i=0; i < graphics.length; i++) {
			for(int j=0; j < graphics[i].length; j++) {
				float dx = j * gWidth;
				float dy = i * gHeight;
				if (x+dx+gWidth < 0 || x > Constants.GAME_WIDTH + dx + gWidth
				 || y+dy+gHeight < 0 || y > Constants.GAME_HEIGHT + dy + gHeight)
					continue;
				
				batch.draw(graphics[i][j], x + dx, y + dy);
			}
		}
	}

	@Override
	public void move(float x, float y) {
		this.setPosition(getX()+x, getY()+y);
	}
	
	@Override
	public void moveDistance(float x, float y, float speedScalar) {
		this.move(x*speedScalar, y*speedScalar);
	}
	
	@Override
	public Vector2 getGraphicSize() {
		return new Vector2(totalWidth, totalHeight);
	}
	
	public void dispose() {
		for (int i=0; i<graphics.length; i++) {
			for (int j=0; j<graphics[i].length; j++) {
				graphics[i][j].getTexture().dispose();
			}
		}
		super.dispose();
	}

	@Override
	public IProducesInputs getController() {
		return null;
	}
}
