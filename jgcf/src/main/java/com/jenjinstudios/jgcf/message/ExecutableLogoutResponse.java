package com.jenjinstudios.jgcf.message;

import com.jenjinstudios.jgcf.Client;
import com.jenjinstudios.message.Message;

/**
 * This class responds to a LogoutResponse message.
 *
 * @author Caleb Brinkman
 */
@SuppressWarnings("unused")
public class ExecutableLogoutResponse extends ClientExecutableMessage
{
	/**
	 * Construct an ExecutableMessage with the given Message.
	 *
	 * @param client  The client invoking this class.
	 * @param message The Message.
	 */
	public ExecutableLogoutResponse(Client client, Message message)
	{
		super(client, message);
	}

	@Override
	public void runSynced()
	{
		getClient().setReceivedLogoutResponse(true);
		getClient().setLoggedIn(!((boolean) getMessage().getArgument("success")));
	}

	@Override
	public void runASync()
	{
	}

}
