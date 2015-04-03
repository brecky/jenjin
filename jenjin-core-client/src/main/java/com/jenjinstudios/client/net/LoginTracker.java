package com.jenjinstudios.client.net;

import com.jenjinstudios.core.io.Message;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to track the status of login requests and responses.
 *
 * @author Caleb Brinkman
 */
public class LoginTracker
{
	private static final Logger LOGGER = Logger.getLogger(LoginTracker.class.getName());
	private static final int MILLIS_IN_30_SECONDS = 30000;
	private volatile boolean loggedIn;
	private volatile boolean waitingForResponse;
	private long loggedInTime;
	private final AuthClient client;

	/**
	 * Construct a new LoginTracker working for the given client.
	 *
	 * @param client The client using this LoginTracker.
	 */
	public LoginTracker(AuthClient client) { this.client = client; }

	/**
	 * Returns whether this login tracker has received a successful login response.
	 *
	 * @return Whether this login tracker has received a successful login response.
	 */
	public boolean isLoggedIn() { return loggedIn; }

	/**
	 * Set when a successful login response has been received.
	 *
	 * @param loggedIn Whether the login was successful.
	 */
	public void setLoggedIn(boolean loggedIn) {
		waitingForResponse = false;
		this.loggedIn = loggedIn;
	}

	/**
	 * Get the time at which the login was performed.
	 *
	 * @return The time at which the login was performed.
	 */
	public long getLoggedInTime() { return loggedInTime; }

	/**
	 * Set the time at which the login was performed.
	 *
	 * @param loggedInTime The time at which the login was performed.
	 */
	public void setLoggedInTime(long loggedInTime) { this.loggedInTime = loggedInTime; }

	/**
	 * Send a login request and await the response.
	 *
	 * @return Whether the login was successful.
	 */
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	public boolean sendLoginRequestAndWaitForResponse() {
		AuthClient.sendLoginRequest(this, client);
		long startTime = System.currentTimeMillis();
		while (waitingForResponse && ((System.currentTimeMillis() - startTime) < MILLIS_IN_30_SECONDS))
		{
			waitTenMillis();
		}
		return loggedIn;
	}

	/**
	 * Send a logout request and block execution until the response is received.
	 */
	public void sendLogoutRequestAndWaitForResponse() {
		sendLogoutRequest();
		long startTime = System.currentTimeMillis();
		while (waitingForResponse && ((System.currentTimeMillis() - startTime) < MILLIS_IN_30_SECONDS))
		{
			waitTenMillis();
		}
	}

	/**
	 * Get whether this tracker is waiting for a response.
	 *
	 * @return Whether this tracker is waiting for a response.
	 */
	public boolean isWaitingForResponse() { return waitingForResponse; }

	/**
	 * Set whether this tracker is waiting for a response.
	 *
	 * @param waitingForResponse Whether this tracker is waiting for a response.
	 */
	public void setWaitingForResponse(boolean waitingForResponse) { this.waitingForResponse = waitingForResponse; }

	private void sendLogoutRequest() {
		waitingForResponse = true;
		Message message = AuthClient.generateLogoutRequest();
		client.enqueueMessage(message);
	}

	private static void waitTenMillis() {
		try
		{
			Thread.sleep(10);
		} catch (InterruptedException e)
		{
			LOGGER.log(Level.WARNING, "Interrupted while waiting for login response.", e);
		}
	}

}