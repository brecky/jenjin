package com.jenjinstudios.client.message;

import com.jenjinstudios.client.net.ClientUser;
import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageRegistry;
import com.jenjinstudios.core.message.MessageFactory;

/**
 * Used to generate messages for the Jenjin core client.
 *
 * @author Caleb Brinkman
 */
public class ClientMessageFactory extends MessageFactory
{

    /**
     * Generate a LogoutRequest message.
     *
     * @return The LogoutRequestMessage.
     */
    public static Message generateLogoutRequest() { return MessageRegistry.getInstance().createMessage
          ("LogoutRequest");}

    /**
     * Generate a LoginRequest message.  This message will be encrypted if possible.
     *
     * @return The LoginRequest message.
     */
    public static Message generateLoginRequest(ClientUser user) {// Create the login request.
        Message loginRequest = MessageRegistry.getInstance().createMessage("LoginRequest");
        loginRequest.setArgument("username", user.getUsername());
        loginRequest.setArgument("password", user.getPassword());
        return loginRequest;
    }
}
