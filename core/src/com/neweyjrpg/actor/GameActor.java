package com.neweyjrpg.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.neweyjrpg.interfaces.ICanCollide;
import com.neweyjrpg.interfaces.IHandlesCollision;
import com.neweyjrpg.models.PhysicsModel;

public abstract class GameActor extends Actor implements Comparable<GameActor>, ICanCollide<GameActor> {
	
	//Fields
	protected PhysicsModel phys;
	public PhysicsModel getPhysicsModel() {	return this.phys; }
	protected float oldX, oldY;
	
	protected float physPaddingX, physPaddingY;
	
	protected IHandlesCollision<GameActor> collider;
	public IHandlesCollision<GameActor> getCollider() { return this.collider; }
	public void setCollider(IHandlesCollision<GameActor> collider) { this.collider = collider; }
	
	//Constructor
	public GameActor(float x, float y, PhysicsModel phys){
		this.phys = phys;
		this.setPosition(x, y);
		this.oldX = phys.getBounds().x; //Values for resetting movement on collisions
		this.oldY = phys.getBounds().y;
		
		physPaddingX = 0f;
		physPaddingY = 0f;
	}
	
	//Methods
	public abstract void draw(Batch batch, float deltaTime);
	public abstract void draw(Batch batch, float deltaTime, float x, float y);
	public abstract void move(float x, float y);
	
	@Override
	public void act(float deltaTime) {
		this.oldX = this.phys.getBounds().x;
		this.oldY = this.phys.getBounds().y;
		super.act(deltaTime);
		alignPhysicsModelToActor();
	}
	
	@Override
	public void setPosition(float x, float y) {
		this.setX(x);
		this.setY(y);
		alignPhysicsModelToActor();
	}
	
	private void alignPhysicsModelToActor() {
		this.getPhysicsModel().getBounds().setPosition(getX() + physPaddingX, getY() + physPaddingY);
	}
	
	public void setPhysicalPosition(float x, float y) {
		this.getPhysicsModel().getBounds().setPosition(x, y);
		alignActorToPhysicsModel();
	}
	
	public void movePhysicalPosition(float x, float y) {
		this.getPhysicsModel().getBounds().setPosition(
				this.getPhysicsModel().getBounds().x + x, 
				this.getPhysicsModel().getBounds().y + y);
		this.oldX = this.phys.getBounds().x;
		this.oldY = this.phys.getBounds().y;
		alignActorToPhysicsModel();
	}
	
	private void alignActorToPhysicsModel() {
		this.setX(getPhysicsModel().getBounds().x - physPaddingX);
		this.setY(getPhysicsModel().getBounds().y - physPaddingY);
	}
	
	public Vector2 getPosition() {
		return new Vector2(this.getX(), this.getY());
	}
	public Vector2 getOldPosition() {
		return new Vector2(this.oldX, this.oldY);
	}
	
	@Override
	public int compareTo(GameActor o) {
		return (int)((o.getY()*1000) - (this.getY()*1000));
	}
	
	@Override
	public void collideInto(GameActor obj) {
		this.getCollider().handleCollision(this, obj);
		
	}
	@Override
	public void collisionFrom(GameActor obj) {
		obj.getCollider().handleCollision(obj, this);
		
	}

}
