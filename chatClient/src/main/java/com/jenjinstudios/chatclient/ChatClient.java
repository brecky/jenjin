package com.jenjinstudios.chatclient;

import com.jenjinstudios.jgcf.Client;
import com.jenjinstudios.message.BaseMessage;

import java.io.IOException;
import java.util.LinkedList;

/**
 * The client for the Chat program tutorial.
 * @author Caleb Brinkman
 */
@SuppressWarnings("SameParameterValue")
public class ChatClient extends Client
{
	/** The list of received chat messages. */
	private final LinkedList<BaseMessage> chatMessages;
	/** The ID number for the ChatBroadcast message. */
	public static final short CHAT_BROADCAST_ID = 200;
	/** The ID number for the ChatMessage message. */
	public static final short CHAT_MESSAGE_ID = 201;
	/** The ID number for the ChatUsername message. */
	public static final short CHAT_USERNAME_ID = 202;

	/**
	 * Construct a new ChatClient.
	 * @param address The address of the server.
	 * @param port The port over which to communicate with the server.
	 * @param username The user's username.
	 */
	public ChatClient(String address, int port, String username)
	{
		super(address, port);
		chatMessages = new LinkedList<>();
		queueMessage(new BaseMessage(CHAT_USERNAME_ID, username));
	}

	/**
	 * Send a chat message to the server.
	 *
	 * @param message The message to send to the server.
	 */
	public final void sendChatMessage(String message)
	{
		sendChatMessage(message, 0);
	}

	/**
	 * Send a chat message to the server.
	 * @param message The message to send.
	 * @param group The group to send the message to.
	 */
	public final void sendChatMessage(String message, int group)
	{
		queueMessage(new BaseMessage(CHAT_MESSAGE_ID, message, group));
	}

	/**
	 * Get all the chat messages collected since the last check.
	 *
	 * @return A {@code LinkedList} of all the ChatMessages collected since the last time this method was
	 *         called.
	 */
	public final LinkedList<String> getChatMessages()
	{
		LinkedList<String> temp;
		synchronized (chatMessages)
		{
			temp = new LinkedList<>();
			while(!chatMessages.isEmpty())
			{
				BaseMessage current = chatMessages.pop();
				temp.add(current.getArgs()[0] + ": " + current.getArgs()[1]);
			}
		}
		return temp;
	}

	/**
	 * Add a chat message to the incoming chat message queue.
	 *
	 * @param message The chat message that hasbeen received.
	 */
	void processChatBroadcast(BaseMessage message)
	{
		synchronized (chatMessages)
		{
			chatMessages.add(message);
		}
	}

	/**
	 * Process the specified message.  This method should be overridden by any implementing classes, but it does
	 * contain functionality necessary to communicate with a DownloadServer or a ChatServer.
	 *
	 * @param message The message to be processed.
	 * @throws java.io.IOException If there is an IO error.
	 */
	protected void processMessage(BaseMessage message) throws IOException
	{
		if (message.getID() == CHAT_BROADCAST_ID)
			processChatBroadcast(message);
		else
			super.processMessage(message);
	}
}
