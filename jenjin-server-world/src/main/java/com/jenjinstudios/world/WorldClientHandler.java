package com.jenjinstudios.world;

import com.jenjinstudios.io.Message;
import com.jenjinstudios.net.ClientHandler;
import com.jenjinstudios.world.util.WorldServerMessageGenerator;

import java.io.IOException;
import java.net.Socket;

/**
 * Handles clients for a world server.
 * @author Caleb Brinkman
 */
public class WorldClientHandler extends ClientHandler
{
	/** The WorldServer owning this handler. */
	private final WorldServer server;
	/** The ID of the player controlled by this clienthandler. */
	private long playerID = -1;
	/** The Actor managed by this handler. */
	private Actor actor;

	/**
	 * Construct a new Client Handler using the given socket.  When constructing a new ClientHandler, it is necessary to
	 * send the client a FirstConnectResponse message with the server's UPS
	 * @param s The server for which this handler works.
	 * @param sk The socket used to communicate with the client.
	 * @throws java.io.IOException If the socket is unable to connect.
	 */
	public WorldClientHandler(WorldServer s, Socket sk) throws IOException {
		super(s, sk);
		server = s;
		queueMessage(WorldServerMessageGenerator.generateActorStepLengthMessage());
	}

	/**
	 * Get the actor of this client handler.
	 * @return The actor controlled by this client handler.
	 */
	public Actor getActor() {
		return actor;
	}

	/**
	 * Set the Actor managed by this handler.
	 * @param actor The actor to be managed by this handler.
	 */
	public void setActor(Actor actor) {
		this.actor = actor;
		setUsername(actor.getName());
		setPlayerID(actor.getId());
	}

	/**
	 * Set the player ID, id it is not already set.
	 * @param id The new ID.
	 */
	public void setPlayerID(long id) {
		if (playerID == -1)
			playerID = id;
	}

	@Override
	public void update() {
		super.update();

		if (actor == null)
			return;

		queueForcesStateMessage();
		queueNewlyVisibleMessages();
		queueNewlyInvisibleMessages();
		queueStateChangeMessages();
	}

	@Override
	public WorldServer getServer() { return server; }

	/**
	 * Get the player associated with this client handler.
	 * @return The player associated with this client handler.
	 */
	public Actor getPlayer() { return actor; }

	/** Generate and queue messages for newly visible objects. */
	private void queueNewlyVisibleMessages() {
		for (WorldObject object : actor.getNewlyVisibleObjects())
		{
			Message newlyVisibleMessage;
			newlyVisibleMessage = WorldServerMessageGenerator.generateNewlyVisibleMessage(object);
			queueMessage(newlyVisibleMessage);
		}
	}

	/** Generate and queue messages for newly invisible objects. */
	private void queueNewlyInvisibleMessages() {
		for (WorldObject object : actor.getNewlyInvisibleObjects())
		{
			Message newlyInvisibleMessage = WorldServerMessageGenerator.generateNewlyInvisibleMessage(object);
			queueMessage(newlyInvisibleMessage);
		}
	}

	/** Generate and queue messages for actors with changed states. */
	private void queueStateChangeMessages() {
		for (WorldObject object : actor.getVisibleObjects().values())
		{
			Actor changedActor;
			if (object instanceof Actor && (changedActor = (Actor) object).isNewState())
			{
				Message newState = WorldServerMessageGenerator.generateChangeStateMessage(changedActor);
				queueMessage(newState);
			}
		}
	}

	/** Generate and queue a ForcedStateMessage if necessary. */
	private void queueForcesStateMessage() {
		if (actor.isForcedState())
			queueMessage(WorldServerMessageGenerator.generateForcedStateMessage(actor, server));
	}
}
