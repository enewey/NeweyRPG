package com.neweyjrpg.manager;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.neweyjrpg.enums.Enums;
import com.neweyjrpg.interaction.Interaction;
import com.neweyjrpg.interaction.windows.WindowInteraction;
import com.neweyjrpg.models.ButtonInput;
import com.neweyjrpg.models.DirectionalInput;
import com.neweyjrpg.window.GameWindow;

public class WindowManager extends Manager {

	private LinkedList<GameWindow> windows;
	public boolean isEmpty() { return this.windows.isEmpty(); }
	public void addWindow(GameWindow window) { this.windows.addLast(window); }
	
	private boolean halting;
	public void setHalting(boolean b) { this.halting = b; }
	
	public WindowManager(boolean halting) {
		this.windows = new LinkedList<GameWindow>();
		this.halting = halting;
	}
	
	private boolean determineHalting() {
		if (!this.windows.isEmpty())
			return halting;
		else
			return false;
	}

	@Override
	public void draw(float deltaTime, int yaxis, float offsetX, float offsetY, Batch batch, Enums.Priority priority) {
		if (yaxis != 0) //only wanna draw once, so draw when drawing hits bottom of screen. 
			return;
		
		if (priority != Enums.Priority.Above) { //TODO: Windows will draw above all else for now...
			return;
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
	}

	@Override
	public boolean act(float deltaTime) {
		if (!this.windows.isEmpty())
			return halting;
		else
			return false;
	}

	private boolean interact() {
		if (!windows.isEmpty()){
			windows.getFirst().interact();
			return true;
		}
		else
			return false;
	}
	
	private boolean cancel() {
		if (windows == null || windows.isEmpty())
			return false;
		
		for (GameWindow w : windows)
			w.dispose();
		
		return true;
	}
	
	@Override
	public boolean handle(Interaction interaction) {
		if (interaction instanceof WindowInteraction) {
			interaction.process(this);
			return true;
		}
		else 
			return false;
	}
	
	@Override
	public boolean handleButtonPress(int button) {
		switch(button) {
		case 0:
		case 3:
			return interact();
		case 1:
			return cancel();
		case 2:
		default:
			return false;
		}
	}

	@Override
	public boolean handleDirectionPress(int button) {
		return determineHalting();
	}

	@Override
	public boolean handleButtonState(ButtonInput button) {
		return determineHalting();
	}

	@Override
	public boolean handleDirectionState(DirectionalInput dir) {
		return determineHalting();
	}
	
	@Override
	public void dispose() {
		for (GameWindow window : windows) {
			window.dispose();
		}
	}
	
	private boolean isBlocked;
	public boolean isBlocked() { return this.isBlocked; }
	public void block() {
		this.isBlocked = true;
	}
	public void unblock() {
		this.isBlocked = false;
	}
	
	@Override
	public void onInteractionComplete(Interaction i) {
		// TODO Auto-generated method stub
		
	}
}
