package com.jenjinstudios.server.net;

import com.jenjinstudios.core.io.MessageRegistry;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base Server class for implementation of the JGSA.  It contains extensible execution functionality designed to be
 * used by Executable Messages from ClientHandlers.
 * @author Caleb Brinkman
 */
@SuppressWarnings("SameParameterValue")
public class Server<T extends ClientHandler> extends Thread
{
	/** The default number of max clients. */
	private static final int DEFAULT_MAX_CLIENTS = 100;
	/** The logger used by this class. */
	public static final Logger LOGGER = Logger.getLogger(Server.class.getName());
	/** The number of milliseconds before a blocking method should time out. */
	private static final long TIMEOUT_MILLIS = 30000;
	/** The updates per second. */
	public final int UPS;
	/** The period of the update in milliseconds. */
	public final int PERIOD;
	/** The list of {@code ClientListener}s working for this server. */
	private final ClientListener<T> clientListener;
	/** The list of {@code ClientHandler}s working for this server. */
	private final List<T> clientHandlers;
	/** The map of clients stored by username. */
	private final Map<String, T> clientsByUsername;
	/** Indicates whether this server is initialized. */
	private volatile boolean initialized;
	/** The current number of connected clients. */
	private int numClients;
	/** The MessageRegistry used by this server. */
	private final MessageRegistry messageRegistry;

	/**
	 * Construct a new Server without a SQLHandler.
	 * @throws java.io.IOException If there is an IO Error initializing the server.
	 * @throws NoSuchMethodException If there is no appropriate constructor for the specified ClientHandler
	 * constructor.
	 */
	@SuppressWarnings("unchecked")
	public Server(ServerInit<T> initInfo) throws IOException, NoSuchMethodException {
		super("Server");
		ClientListenerInit<T> listenerInit = initInfo.getClientListenerInit();
		messageRegistry = initInfo.getMessageRegistry();
		LOGGER.log(Level.FINE, "Initializing Server.");
		UPS = initInfo.getUps();
		PERIOD = 1000 / UPS;
		clientsByUsername = new TreeMap<>();
		clientListener = new ClientListener<>(getClass(), listenerInit);
		clientHandlers = new ArrayList<>();
		for (int i = 0; i < DEFAULT_MAX_CLIENTS; i++)
			clientHandlers.add(null);
		numClients = 0;
	}

	/**
	 * Add new clients that have connected to the client listeners.
	 * @return true if new clients were added.
	 */
	public boolean getNewClients() {
		boolean clientsAdded;
		LinkedList<T> nc = clientListener.getNewClients();
		clientsAdded = !nc.isEmpty();
		for (T h : nc)
		{
			int nullIndex = clientHandlers.indexOf(null);
			clientHandlers.set(nullIndex, h);
			h.setHandlerId(nullIndex);
			h.start();
			numClients++;
		}

		return clientsAdded;
	}

	public void runClientHandlerQueuedMessages() {
		synchronized (clientHandlers)
		{
			for (ClientHandler current : clientHandlers)
			{
				if (current != null)
				{
					current.runQueuedExecutableMessages();
				}
			}
		}
	}

	/** Broadcast all outgoing messages to clients. */
	public void broadcast() {
		synchronized (clientHandlers)
		{
			for (ClientHandler current : clientHandlers)
			{
				if (current != null) { current.writeAllMessages(); }
			}
		}
	}

	/** Update all clients before they sendAllMessages. */
	public void update() {
		synchronized (clientHandlers)
		{
			for (ClientHandler current : clientHandlers)
			{
				if (current != null) { current.update(); }
			}
		}
	}

	/** Refresh all clients after they sendAllMessages. */
	public void refresh() {
		synchronized (clientHandlers)
		{
			for (ClientHandler current : clientHandlers)
			{
				if (current != null) { current.refresh(); }
			}
		}
	}

	/** Run the server. */
	@Override
	public void run() {
		clientListener.startListening(this);
		initialized = true;
	}

	/**
	 * Start the server, and do not return until it is fully initialized.
	 * @return If the blocking start was successful.
	 */
	public final boolean blockingStart() {
		long startTime = System.currentTimeMillis();
		long timePast = System.currentTimeMillis() - startTime;
		start();
		while (!initialized && (timePast < TIMEOUT_MILLIS))
		{
			try
			{
				Thread.sleep(10);
				timePast = System.currentTimeMillis() - startTime;
			} catch (InterruptedException e)
			{
				LOGGER.log(Level.WARNING, "Server blocking start was interrupted.", e);
			}
		}
		return initialized;
	}

	/**
	 * Shutdown the server, forcing all client links to close.
	 * @throws IOException if there is an error shutting down a client.
	 */
	public void shutdown() throws IOException {
		synchronized (clientHandlers)
		{
			for (ClientHandler h : clientHandlers)
			{
				if (h != null) { h.shutdown(); }
			}
		}
		clientListener.stopListening();
	}

	/**
	 * Return whether this server is initialized.
	 * @return true if the server has been initialized.
	 */
	public boolean isInitialized() { return initialized; }

	/**
	 * Get the ClientHandler with the given username.
	 * @param username The username of the client to look up.
	 * @return The client with the username specified; null if there is no client with this username.
	 */
	public T getClientHandlerByUsername(String username) {
		synchronized (clientsByUsername)
		{
			return clientsByUsername.get(username);
		}
	}

	/**
	 * Get the current number of connected clients.
	 * @return The current number of connected clients.
	 */
	public int getNumClients() {
		return numClients;
	}

	/**
	 * Get the MessageRegistry used by this server.
	 * @return The MessageRegistry used by this server,
	 */
	public MessageRegistry getMessageRegistry() { return messageRegistry; }

	/**
	 * Schedule a client to be removed during the next update.
	 * @param handler The client handler to be removed.
	 */
	void removeClient(ClientHandler handler) {
		User user = handler.getUser();

		if (user != null && user.getUsername() != null)
		{
			clientsByUsername.remove(user.getUsername());
		}
		synchronized (clientHandlers)
		{
			clientHandlers.set(handler.getHandlerId(), null);
		}
		numClients--;
	}

	/**
	 * Called by ClientHandler when the client sets a username.
	 * @param username The username assigned to the ClientHandler.
	 * @param handler The ClientHandler that has had a username set.
	 */
	@SuppressWarnings("unchecked")
	public void associateUsernameWithClientHandler(String username, ClientHandler handler) {
		synchronized (clientsByUsername)
		{
			clientsByUsername.put(username, (T) handler);
		}
	}
}
