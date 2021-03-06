package com.jenjinstudios.client.message;

import com.jenjinstudios.client.net.AuthClient;
import com.jenjinstudios.core.ExecutableMessage;
import com.jenjinstudios.core.io.Message;

/**
 * This class responds to a LogoutResponse message.
 *
 * @author Caleb Brinkman
 */
@SuppressWarnings("unused")
public class ExecutableLogoutResponse extends ExecutableMessage
{
    /**
     * Construct an ExecutableMessage with the given Message.
     *
     * @param client The client invoking this class.
     * @param message The Message.
     */
    public ExecutableLogoutResponse(AuthClient client, Message message) {
        super(client, message);
    }

    @Override
    public void runDelayed() {
        ((AuthClient) getConnection()).getLoginTracker().setLoggedIn(!((boolean) getMessage().getArgument
              ("success")));
    }

    @Override
    public void runImmediate() {
    }

}
