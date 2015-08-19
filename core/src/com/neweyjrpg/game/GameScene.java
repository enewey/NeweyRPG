package com.neweyjrpg.game;

import java.util.LinkedList;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.neweyjrpg.actor.CharacterActor;
import com.neweyjrpg.actor.GameActor;
import com.neweyjrpg.constants.Constants;
import com.neweyjrpg.controller.InputController;
import com.neweyjrpg.enums.Enums.PhysicalState;
import com.neweyjrpg.interaction.MessageInteraction;
import com.neweyjrpg.interfaces.IHandlesInteraction;
import com.neweyjrpg.interfaces.IProducesInputs;
import com.neweyjrpg.interfaces.Interaction;
import com.neweyjrpg.map.GameMap;
import com.neweyjrpg.models.ButtonInput;
import com.neweyjrpg.models.DirectionalInput;
import com.neweyjrpg.util.ClosestPosition;
import com.neweyjrpg.window.GameWindow;
import com.neweyjrpg.window.MessageWindow;

public class GameScene extends InputAdapter implements IProducesInputs, IHandlesInteraction {
	
	//CHANGE TO PRIVATE
	public float scrollX, scrollY; //Camera focus
	public float maxScrollX, maxScrollY;
	
	private Viewport viewport; //Used for drawing to the screen
	public Viewport getViewport() {	return viewport; }

	private Batch batch; //Used for drawing sprites
	public Batch getBatch() { return batch; }
	
	private GameMap map;
	public GameMap getMap() { return map; }
	public void setMap(GameMap map) { this.map = map; }

	private Array<GameActor> actors; //All actors in this scene
	public Array<GameActor> getActors() { return actors; }
	
	private LinkedList<GameWindow> windows;
	
	private CharacterActor player; //The player-controlled actor
	private InputController inputController; //Used to relay inputs to the actor
	
//	private BitmapFont font;
//	private String message = "";
////	private String buttonDebug = "";
//	private MessageSequence sequence;
//	private MessageWindow message;
	
	public GameScene(Viewport viewport, Batch batch, CharacterActor playerActor, GameMap map) {
		this.batch = batch;
		this.viewport = viewport;
		this.actors = new Array<GameActor>(false, 10, GameActor.class);
		
		this.player = playerActor;
		this.player.setController(this);
		this.actors.add(this.player);
		
		this.windows = new LinkedList<GameWindow>();
		
		this.inputController = new InputController();
		
		this.map = map;
		this.scrollX = 0f;
		this.scrollY = 0f;
		
//		this.font = new BitmapFont();
		
		//Max scroll dictates how far the camera can scroll, so the edges align to the edge of the GameMap 
		this.maxScrollX = -((map.getDimX() * Constants.TILE_WIDTH)-Constants.GAME_WIDTH);
		this.maxScrollY = -((map.getDimY() * Constants.TILE_HEIGHT)-Constants.GAME_HEIGHT);
	}
	
	public void addActor(GameActor actor) {
		actors.add(actor);
	}
	
	public void setPlayer(CharacterActor actor) {
		actor.setController(this);
		this.player = actor;
	}
	
	public void draw(float deltaTime) {		
		if (!batch.isDrawing()) //If the batch has not begun, go ahead and begin it
			batch.begin();
		
		//Draw each tile from the map -- TODO: draw only tiles within the viewport!
		for (int x=0; x<map.getDimX(); x++)
			for (int y=0; y<map.getDimY(); y++)
				batch.draw(map.getMapTile(x, y).getGraphic(), scrollX+(x*16), scrollY+(y*16));
		
		//Draw all actors in the scene at their given position, taking into account the camera scrolling
		for (GameActor actor : actors) {
			actor.draw(batch, deltaTime, actor.getX() + scrollX, actor.getY() + scrollY);
		}
		
		while (!windows.isEmpty()){
			if (windows.getFirst().isDisposed()) {
				windows.removeFirst();
			}
			else {
				windows.getFirst().draw(batch, deltaTime);
				break;
			}
		}
		
//		font.draw(this.batch, message, 0, 20);
//		font.draw(this.batch, buttonDebug, 0, 40);
	}
	
	/**
	 * 	Calls each actor's 'act' method, and then immediately checks for collision
	 * @param deltaTime
	 */
	public void act(float deltaTime) {
		
		
		//TODO: Consider the flow of the act method.
		//	Need to figure out a way to have different flows of control here.
		//	Should have one 'flow' be for when the game is 'alive', and all Actors are in play.
		//	Another flow might be for a menu, or for some sort of text display.
		//	Perhaps these should be in separate Scene classes all together...
		
		buttonPressing();
		
		if (windows.isEmpty()) 
		{
			player.act(deltaTime);
			this.detectCollision(player, true); //Detect collision after each individual action; this is key
			
			for (GameActor actor : actors){
				if (actor == this.player) continue;
				
				actor.act(deltaTime);
				if (actor.getPhysicsModel().getType() != PhysicalState.StaticBlock
						&& actor.getPhysicsModel().getType() != PhysicalState.StaticPushable)
					this.detectCollision(actor, false); //Detect collision after each individual action; this is key
			}
		}
		
		this.actors.sort(); //sorts by Y position, top to bottom; sort to maintain drawing overlap consistency
		this.adjustFocus();
	}
	
	/**
	 * Checks if a given actor is colliding with any other actors in the scene.
	 * @param actor - the actor that is colliding into another actor.
	 * 			NOTE: Static Actors should not be specified as the parameter for this method.
	 */
	private void detectCollision(GameActor actor, boolean doInteract) {
		if (actor.getPhysicsModel().getBounds().x < 0)
			actor.setPhysicalPosition(0, actor.getPhysicsModel().getBounds().y);
		else if (actor.getPhysicsModel().getBounds().x > map.getDimX()*Constants.TILE_WIDTH - actor.getPhysicsModel().getBounds().width)
			actor.setPhysicalPosition(map.getDimX()*Constants.TILE_WIDTH - actor.getPhysicsModel().getBounds().width, actor.getPhysicsModel().getBounds().y);
		
		if (actor.getPhysicsModel().getBounds().y < 0)
			actor.setPhysicalPosition(actor.getPhysicsModel().getBounds().x, 0);
		else if (actor.getPhysicsModel().getBounds().y > map.getDimY()*Constants.TILE_HEIGHT - actor.getPhysicsModel().getBounds().height)
			actor.setPhysicalPosition(actor.getPhysicsModel().getBounds().x, map.getDimY()*Constants.TILE_HEIGHT - actor.getPhysicsModel().getBounds().height);
		
		Array<GameActor> sortedActors = new Array<GameActor>(actors);
		sortedActors.sort(new ClosestPosition(actor));
		
		for (int j = 0; j < sortedActors.size; j++) {
			GameActor subject = sortedActors.get(j);
			if (subject.equals(actor)) {
				continue;
			}
			if (actor.collideInto(subject) && doInteract) {
				this.handle(subject.onTouch());
			}
		}
	}
	
	private void buttonPressing() {
		while (!this.inputController.getQueue().isEmpty())
		{
			int pop = this.inputController.getQueue().pop(); 
			if (inputController.getButton(pop) == 0) {
				if (!windows.isEmpty()){
					windowInteract();
				}
				else {
					playerInteract();
				}
					
			} 
			else if (inputController.getButton(pop) == 1) {
				if (!windows.isEmpty()){
					windowCancel();
				}
				
			}
			else if (inputController.getButton(pop) == 2) {
				
			}
		}
	}
	
	private void windowInteract() {
		if (!windows.isEmpty()){
			windows.getFirst().interact();
		}
	}
	
	private void windowCancel() {
		for (GameWindow w : windows)
			w.dispose();
	}
	
	private void playerInteract() {
		Array<GameActor> sortedActors = new Array<GameActor>(actors);		
		switch (player.getDir()) {
		case UP:
			for (int i = 0; i<sortedActors.size; i++) {
				if (sortedActors.get(i).getY() < player.getY() || sortedActors.get(i) == player)
					sortedActors.removeIndex(i--);
			}
			break;
		case RIGHT:
			for (int i = 0; i<sortedActors.size; i++) {
				if (sortedActors.get(i).getX() < player.getX() || sortedActors.get(i) == player)
					sortedActors.removeIndex(i--);
			}
			break;
		case LEFT:
			for (int i = 0; i<sortedActors.size; i++) {
				if (sortedActors.get(i).getX() > player.getX() || sortedActors.get(i) == player)
					sortedActors.removeIndex(i--);
			}
			break;
		case DOWN:
			for (int i = 0; i<sortedActors.size; i++) {
				if (sortedActors.get(i).getY() > player.getY() || sortedActors.get(i) == player)
					sortedActors.removeIndex(i--);
			}
			break;
		}
		
		sortedActors.sort(new ClosestPosition(player));
		for (int i=0; /*i<5 &&*/ i<sortedActors.size; i++) {
			if (sortedActors.get(i).onAction() != null && sortedActors.get(i) != null) {
				if (sortedActors.get(i).getDistance(player) < Constants.CHARA_PHYS_SIZE + 2f) {
					this.handle(sortedActors.get(i).onAction());
				}
				break;
			}
		}
		
	}
	
	/**
	 * Sets the camera to be focused on the player.
	 */
	public void adjustFocus() {
		if (player.getX()+scrollX > Constants.UPPER_BOUND_X) {
			scrollX = Math.max(Math.round(Constants.UPPER_BOUND_X - player.getX()), maxScrollX);
		} else if (player.getX()+scrollX < Constants.LOWER_BOUND_X) {
			scrollX = Math.min(Math.round(Constants.LOWER_BOUND_X - player.getX()), 0);
		}
		
		if (player.getY()+scrollY > Constants.UPPER_BOUND_Y) {
			scrollY = Math.max(Math.round(Constants.UPPER_BOUND_Y - player.getY()), maxScrollY);
		} else if (player.getY()+scrollY < Constants.LOWER_BOUND_Y) {
			scrollY = Math.min(Math.round(Constants.LOWER_BOUND_Y - player.getY()), 0);
		}
	}
	
	@Override
	public boolean keyUp(int keycode) {
		if (inputController != null)
			return inputController.keyUp(keycode);
		return super.keyUp(keycode);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (inputController != null)
			return inputController.keyDown(keycode);
		return super.keyDown(keycode);
	}
	
	@Override
	public DirectionalInput getDirectionalInput() {
		if (inputController != null)
			return inputController.getDirectionalInput();
		return null;
	}
	
	@Override
	public ButtonInput getButtonInput() {
		if (inputController != null)
			return inputController.getButtonInput();
		return null;
	}
	
	@Override
	public boolean handle(Interaction interaction) {
		if (interaction == null) return false;
		
		if (interaction instanceof MessageInteraction) {
			for (String s : ((MessageInteraction) interaction).getData()) {
				windows.addLast(new MessageWindow(0,0,320,80,s));
			}
			return true;
		}
		else 
			return false;
	}
}
