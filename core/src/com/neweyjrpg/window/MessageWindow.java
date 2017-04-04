package com.neweyjrpg.window;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.neweyjrpg.enums.Enums;
import com.neweyjrpg.interaction.Interaction;
import com.neweyjrpg.sequences.MessageSequence;

public class MessageWindow extends GameWindow {

	private static int letterDelay = 1;
	private static int idleTime = 5; //TODO: make these constants
	private static int padding = 5;

	private BitmapFont font;
	private MessageSequence message;
	public MessageSequence getSequence() { return this.message; }
	
	public MessageWindow(Interaction i, int x, int y, int width, int height, String str) {
		super(i, x, y, width, height, Enums.Priority.Above);
		this.message = new MessageSequence(str, letterDelay, idleTime);
		this.font = new BitmapFont();
	}
	
	@Override
	public void draw(Batch batch, float deltaTime) {		
		super.draw(batch, deltaTime);
		String m = message.stepMessage();
		font.draw(batch, m, x + padding, y+height-padding);
	}
	
	public boolean isDone() {
		return message.isDone();
	}
	
	@Override
	public void dispose() {
		this.font.dispose();
		super.dispose();
	}

	@Override
	public void interact() {
		if (this.isDone())
			this.dispose();
		else {
			this.getSequence().skip();
		}
	}

}
