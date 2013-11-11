package com.jenjinstudios.message;

import com.jenjinstudios.world.WorldClient;

/**
 * Handles logout responses.
 * @author Caleb Brinkman
 */
public class ExecutableWorldLogoutResponse extends WorldClientExecutableMessage
{
	/**
	 * Construct an ExecutableMessage with the given Message.
	 * @param client The client invoking this message.
	 * @param message The Message.
	 */
	public ExecutableWorldLogoutResponse(WorldClient client, Message message) {
		super(client, message);
	}

	@Override
	public void runSynced() {
		getClient().setReceivedLogoutResponse(true);
		getClient().setLoggedIn(!((boolean) getMessage().getArgument("success")));
	}

	@Override
	public void runASync() {
	}
}
