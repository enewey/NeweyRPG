package com.neweyjrpg.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.neweyjrpg.constants.Constants;
import com.neweyjrpg.game.NeweyJrpg;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
				
		config.title = "Game time y'all";
		config.height = Constants.GAME_HEIGHT*3;
		config.width = Constants.GAME_WIDTH*3;
		
		new LwjglApplication(new NeweyJrpg(), config);
	}
}
