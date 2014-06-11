package com.jenjinstudios.message;

import com.jenjinstudios.io.Message;
import com.jenjinstudios.io.MessageRegistry;
import com.jenjinstudios.util.MessageFactory;

import java.security.PublicKey;

/**
 * Used to generate messages for the Jenjin core client.
 * @author Caleb Brinkman
 */
public class ClientMessageFactory extends MessageFactory
{

	/**
	 * Construct a new ClientMessageFactory.
	 */
	public ClientMessageFactory(MessageRegistry messageRegistry) {
		super(messageRegistry);
	}

	/**
	 * Generate a PublicKeyMessage with the given public key.
	 * @param publicKey The public key.
	 * @return The PublicKeyMessage.
	 */
	public Message generatePublicKeyMessage(PublicKey publicKey) {
		Message publicKeyMessage = new Message("PublicKeyMessage", getMessageRegistry());
		publicKeyMessage.setArgument("key", publicKey.getEncoded());
		return publicKeyMessage;
	}

	/**
	 * Generate a LogoutRequest message.
	 * @return The LogoutRequestMessage.
	 */
	public Message generateLogoutRequest() {return new Message("LogoutRequest", getMessageRegistry());}

	/**
	 * Generate a LoginRequest message.  This message will be encrypted if possible.
	 * @param username The user's plaintext username.
	 * @param password The user's plaintext password.
	 * @return The LoginRequest message.
	 */
	public Message generateLoginRequest(String username, String password) {// Create the login request.
		Message loginRequest = new Message("LoginRequest", getMessageRegistry());
		loginRequest.setArgument("username", username);
		loginRequest.setArgument("password", password);
		return loginRequest;
	}
}
