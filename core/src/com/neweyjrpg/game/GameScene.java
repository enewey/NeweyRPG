package com.neweyjrpg.game;

import java.util.LinkedList;
import java.util.ListIterator;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.neweyjrpg.actor.CharacterActor;
import com.neweyjrpg.actor.GameActor;
import com.neweyjrpg.constants.Constants;
import com.neweyjrpg.controller.InputState;
import com.neweyjrpg.enums.Enums;
import com.neweyjrpg.interfaces.IHandlesInteraction;
import com.neweyjrpg.interfaces.IProducesInputs;
import com.neweyjrpg.interfaces.Interaction;
import com.neweyjrpg.manager.ActorManager;
import com.neweyjrpg.manager.Manager;
import com.neweyjrpg.manager.WindowManager;
import com.neweyjrpg.map.GameMap;
import com.neweyjrpg.models.ButtonInput;
import com.neweyjrpg.models.DirectionalInput;

public class GameScene extends InputAdapter implements IProducesInputs, IHandlesInteraction {
	
	private float stateTime;
	public void incrementStateTime(float deltaTime) {
		stateTime += deltaTime;
	}
	
	private float scrollX, scrollY; //Represents how far the camera has scrolled, justified at the bottom-left corner.
	private float maxScrollX, maxScrollY; //How far the camera is allowed to scroll = (mapWidth-screenWidth, mapHeight-screenHeight)
	
	private Viewport viewport; //Used for drawing to the screen
	public Viewport getViewport() {	return viewport; }

	private Batch batch; //Used for drawing sprites
	public Batch getBatch() { return batch; }
	
	private GameMap map;
	public GameMap getMap() { return map; }
	public void setMap(GameMap map) { this.map = map; }
	
	private ActorManager actorManager;
	private WindowManager windowManager;
	private LinkedList<Manager> managers;

	private InputState input; //Used to relay inputs to the actor
	
	public GameScene(Viewport viewport, Batch batch, CharacterActor playerActor, GameMap map) {
		this.stateTime = 0f;
		
		this.batch = batch;
		this.viewport = viewport;
		
		this.map = map;
		this.scrollX = 0f;
		this.scrollY = 0f;
		//Max scroll dictates how far the camera can scroll, so the edges align to the edge of the GameMap 
		this.maxScrollX = -((map.getDimX() * Constants.TILE_WIDTH)-Constants.GAME_WIDTH);
		this.maxScrollY = -((map.getDimY() * Constants.TILE_HEIGHT)-Constants.GAME_HEIGHT);
		
		this.actorManager = new ActorManager(playerActor, map.getDimX()*Constants.TILE_WIDTH, map.getDimY()*Constants.TILE_HEIGHT, this, this);
		this.windowManager = new WindowManager(true);
		
		this.managers = new LinkedList<Manager>();
		managers.addLast(actorManager);
		managers.addLast(windowManager);
		
		this.input = new InputState();
	}
	
	public void addActor(GameActor actor) {
		actorManager.addActor(actor);
	}
	
	public void setPlayer(CharacterActor actor) {
//		actor.setController(this);
		this.actorManager.setPlayer(actor);
	}
	
	/**
	 * Draws everything within this scene.
	 * NOTE: MAKE SURE TO UPDATE STATETIME BEFORE CALLING
	 */
	public void draw() {
		if (!batch.isDrawing()) //If the batch has not begun, go ahead and begin it
			batch.begin();
		
		//enum.values() guaranteed to iterate over the enum in the order of declaration.
		for (Enums.Priority p : Enums.Priority.values()) {
			//draw graphics row by row, in order to maintain depth illusion
			for (int yaxis = map.getDimY()-1; yaxis >= 0; yaxis--) {
				batch.setColor(Color.WHITE); //reset batch setcolor to allow each manager handle their own coloring
				
				for (Manager m : managers) {
					batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
					m.draw(stateTime, yaxis, scrollX, scrollY, batch, p);
				}
				
				map.draw(batch, yaxis, stateTime, scrollX, scrollY, p);
			}
		}
	}
	
	/**
	 *  First handles any button presses within the InputController queue.
	 * 	Then calls each actor's 'act' method, and immediately checks for collision afterwards.
	 * NOTE: UPDATE THE STATETIME BEFORE CALLING
	 */
	public void act() {
		buttonPressing();
		
		ListIterator<Manager> li = managers.listIterator(managers.size());
		while (li.hasPrevious()) {
			if (li.previous().act(stateTime)) {
				break;
			}
		}
		
		this.adjustFocus(actorManager.getPlayerPos());
	}
	
	/**
	 * Cycles through the Managers, and handles the state of all inputs.
	 */
	private void buttonPressing() {
		//Handle direction state
		if (!this.input.getDirectionalState().isEmpty()) {
			ListIterator<Manager> li = managers.listIterator(managers.size());
			while (li.hasPrevious()) {
				if (li.previous().handleDirectionState(this.input.getDirectionalState())) {
					break;
				}
			}
		}
		
		//Handle button state
		if (!this.input.getButtonState().isEmpty()) {
			ListIterator<Manager> li = managers.listIterator(managers.size());
			while (li.hasPrevious()) {
				if (li.previous().handleButtonState(this.input.getButtonState())) {
					break;
				}
			}
		}
			
		//Handle one-off keypresses
		while (!this.input.getQueue().isEmpty())
		{
			int keycode = this.input.getQueue().pop();
			if (InputState.isButton(keycode)) { //handle button presses
				int pop = InputState.getButton(keycode);
				ListIterator<Manager> li = managers.listIterator(managers.size());
				while (li.hasPrevious()) {
					if (li.previous().handleButtonPress(pop)) {
						break;
					}
				}
			} else if (InputState.isDirection(keycode)) { //handle arrow presses
				int pop = InputState.getDirection(keycode);
				ListIterator<Manager> li = managers.listIterator(managers.size());
				while (li.hasPrevious()) {
					if (li.previous().handleDirectionPress(pop)) {
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Sets the camera to be centered on a specific spot.
	 */
	public void adjustFocus(Vector2 pos) {		
		if (pos.x+scrollX > Constants.UPPER_BOUND_X) {
			scrollX = Math.max(Math.round(Constants.UPPER_BOUND_X - pos.x), maxScrollX);
		} else if (pos.x+scrollX < Constants.LOWER_BOUND_X) {
			scrollX = Math.min(Math.round(Constants.LOWER_BOUND_X - pos.x), 0);
		}
		
		if (pos.y+scrollY > Constants.UPPER_BOUND_Y) {
			scrollY = Math.max(Math.round(Constants.UPPER_BOUND_Y - pos.y), maxScrollY);
		} else if (pos.y+scrollY < Constants.LOWER_BOUND_Y) {
			scrollY = Math.min(Math.round(Constants.LOWER_BOUND_Y - pos.y), 0);
		}
	}
	
	@Override
	public boolean keyUp(int keycode) {
		if (input != null) {
			return input.lift(keycode);
		}
		return super.keyUp(keycode);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (input != null) {
			return input.push(keycode);
		}
		return super.keyDown(keycode);
	}
	
	@Override
	public DirectionalInput getDirectionalState() {
		if (input != null)
			return input.getDirectionalState();
		return null;
	}
	
	@Override
	public ButtonInput getButtonState() {
		if (input != null)
			return input.getButtonState();
		return null;
	}
	
	@Override
	public boolean handle(Interaction interaction) {
		if (interaction == null) return false;
		
		for (Manager m : managers){
			if (m.handle(interaction)){
				return true;
			}
		}
		return false;
	}
	
	public void dispose() {
		
	}
}
