package com.neweyjrpg.interaction.actors;

import com.badlogic.gdx.math.Vector2;
import com.neweyjrpg.actor.characters.CharacterActor;
import com.neweyjrpg.enums.Enums;
import com.neweyjrpg.interfaces.IHandlesInteraction;
import com.neweyjrpg.manager.ActorManager;
import com.neweyjrpg.manager.Manager;
import com.neweyjrpg.util.Conversion;

public class MovementInteraction extends ActorInteraction {
		
	private Enums.Move type;
	public Enums.Move getType() {
		return this.type;
	}
	private Object[] extra; // extra data for processing move type... could be a Vector2, Dir, a secondary target, etc..

	public MovementInteraction(IHandlesInteraction scene, String target, Enums.Move type, Object... extra ) {
		super(scene, target);
		this.extra = extra;
		this.type = type;
	}
	
	private Vector2 data;
	@Override
	public Vector2 getData() {
		return this.data;
	}

	@Override
	public MovementInteraction process(Manager m) {
		super.process(m);
		ActorManager mana = (ActorManager)m;
		this.target = mana.getActorByName(targetName);
		
		switch (this.type) { //TODO: This type shit pattern is bad, make abstract classes!!
		case StepDir: //extra[0] = Direction, extra[1] = scalar (optional)
			Vector2 v = Conversion.dirToVec((Enums.Dir)extra[0]);
			float s = extra.length > 1 ? (Float)extra[1] : 1f;
			this.target.moveDistance(v.x, v.y, s, this);
			this.data = null;
			break;
		case StepToVec: //extra[0] = Vector2 to step towards
			this.data = null;
			break;
		case Face: //extra[0] = applicable Dir
			if (target instanceof CharacterActor) {
				CharacterActor chara = (CharacterActor)target;
				chara.setDir((Enums.Dir)extra[0]);
			}
			this.data = null;
			break;
		case Pause: //extra[0] = float (time to wait)
			target.wait((Float)extra[0], this);
			this.data = null;
			break;
		default:
			this.data = null;
			break;
		}
		
		return this;
	}
}