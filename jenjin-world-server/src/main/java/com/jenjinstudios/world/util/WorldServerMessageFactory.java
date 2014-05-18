package com.jenjinstudios.world.util;

import com.jenjinstudios.io.Message;
import com.jenjinstudios.util.ServerMessageFactory;
import com.jenjinstudios.world.Actor;
import com.jenjinstudios.world.WorldClientHandler;
import com.jenjinstudios.world.WorldObject;
import com.jenjinstudios.world.WorldServer;

/**
 * Used to generate Message objects that are relevant to the World and WorldClientHandler classes.
 * @author Caleb Brinkman
 */
public class WorldServerMessageFactory extends ServerMessageFactory
{
	private final WorldClientHandler worldClientHandler;

	public WorldServerMessageFactory(WorldClientHandler conn) {
		super(conn);
		this.worldClientHandler = conn;
	}

	/**
	 * Generate an appropriate message for a newly visible object.
	 * @param object The object.
	 * @return The message.
	 */
	public Message generateNewlyVisibleMessage(WorldObject object) {
		Message newlyVisibleMessage;
		if (object instanceof Actor)
		{
			newlyVisibleMessage = generateActorVisibleMessage((Actor) object);
		} else
		{
			newlyVisibleMessage = generateObjectVisibleMessage(object);
		}
		return newlyVisibleMessage;
	}

	/**
	 * Generate an ActorVisibleMessage using the given actor.
	 * @param newlyVisible The Actor used to generate the message.
	 * @return A {@code Message} for the newly visible actor.
	 */
	public Message generateActorVisibleMessage(Actor newlyVisible) {
		Message newlyVisibleMessage;
		newlyVisibleMessage = new Message(worldClientHandler, "ActorVisibleMessage");
		newlyVisibleMessage.setArgument("name", newlyVisible.getName());
		newlyVisibleMessage.setArgument("id", newlyVisible.getId());
		newlyVisibleMessage.setArgument("resourceID", newlyVisible.getResourceID());
		newlyVisibleMessage.setArgument("xCoordinate", newlyVisible.getVector2D().getXCoordinate());
		newlyVisibleMessage.setArgument("yCoordinate", newlyVisible.getVector2D().getYCoordinate());
		newlyVisibleMessage.setArgument("relativeAngle", newlyVisible.getCurrentMoveState().relativeAngle);
		newlyVisibleMessage.setArgument("absoluteAngle", newlyVisible.getMoveAngle());
		newlyVisibleMessage.setArgument("stepsTaken", newlyVisible.getStepsTaken());
		newlyVisibleMessage.setArgument("stepsUntilChange", newlyVisible.getCurrentMoveState().stepsUntilChange);
		newlyVisibleMessage.setArgument("timeOfVisibility", 0l); // TODO Set this properly.
		return newlyVisibleMessage;
	}

	/**
	 * Generate an ObjectVisibleMessage using the given actor.
	 * @param object The Actor used to generate the message.
	 * @return A {@code Message} for the newly visible object.
	 */
	public Message generateObjectVisibleMessage(WorldObject object) {
		Message newlyVisibleMessage;
		newlyVisibleMessage = new Message(worldClientHandler, "ObjectVisibleMessage");
		newlyVisibleMessage.setArgument("name", object.getName());
		newlyVisibleMessage.setArgument("id", object.getId());
		newlyVisibleMessage.setArgument("resourceID", object.getResourceID());
		newlyVisibleMessage.setArgument("xCoordinate", object.getVector2D().getXCoordinate());
		newlyVisibleMessage.setArgument("yCoordinate", object.getVector2D().getYCoordinate());
		return newlyVisibleMessage;
	}

	/**
	 * Generate a state change message for the given actor.
	 * @param changedActor The actor with a new state.
	 * @return The state change message.
	 */
	public Message generateChangeStateMessage(Actor changedActor) {
		Message newState = new Message(worldClientHandler, "StateChangeMessage");
		newState.setArgument("id", changedActor.getId());
		newState.setArgument("relativeAngle", changedActor.getCurrentMoveState().relativeAngle);
		newState.setArgument("absoluteAngle", changedActor.getCurrentMoveState().absoluteAngle);
		newState.setArgument("stepsUntilChange", changedActor.getCurrentMoveState().stepsUntilChange);
		// TODO Set these properly
		newState.setArgument("timeOfChange", 0l);
		newState.setArgument("xCoord", 0.0d);
		newState.setArgument("yCoord", 0.0d);
		return newState;
	}

	/**
	 * Generate a forced state message.
	 * @param actor The actor who's state has been forced.
	 * @param server The server in which the world is running.
	 * @return A forced state message for the actor's state at the beginning of this server "tick".
	 */
	public Message generateForcedStateMessage(Actor actor, WorldServer server) {
		Message forcedStateMessage = new Message(worldClientHandler, "ForceStateMessage");
		forcedStateMessage.setArgument("relativeAngle", actor.getMoveDirection());
		forcedStateMessage.setArgument("absoluteAngle", actor.getMoveAngle());
		forcedStateMessage.setArgument("xCoordinate", actor.getVector2D().getXCoordinate());
		forcedStateMessage.setArgument("yCoordinate", actor.getVector2D().getYCoordinate());
		forcedStateMessage.setArgument("timeOfForce", server.getCycleStartTime());
		return forcedStateMessage;
	}

	/**
	 * Generate a step length message.
	 * @return The message.
	 */
	public Message generateActorStepLengthMessage() {
		Message stepLengthMessage = new Message(worldClientHandler, "ActorStepMessage");
		stepLengthMessage.setArgument("stepLength", Actor.STEP_LENGTH);
		return stepLengthMessage;
	}

	/**
	 * Generate a NewlyInvisibleObjectMessage for the given object.
	 * @param object The {@code WorldObject} that is newly invisible.
	 * @return A {@code Message} for the newly invisible object.
	 */
	public Message generateNewlyInvisibleMessage(WorldObject object) {
		Message newlyInvisibleMessage = new Message(worldClientHandler, "ObjectInvisibleMessage");
		newlyInvisibleMessage.setArgument("id", object.getId());
		return newlyInvisibleMessage;
	}

	public Message generateWorldLoginResponse() {
		return new Message(worldClientHandler, "WorldLoginResponse");
	}

	public Message generateWorldFileResponse(byte[] worldFileBytes) {
		Message response = new Message(worldClientHandler, "WorldFileResponse");
		response.setArgument("fileBytes", worldFileBytes);
		return response;
	}

	public Message generateWorldChecksumResponse(byte[] checkSum) {
		Message response = new Message(worldClientHandler, "WorldChecksumResponse");
		response.setArgument("checksum", checkSum);
		return response;
	}
}