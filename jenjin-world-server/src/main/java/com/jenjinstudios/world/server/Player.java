package com.jenjinstudios.world.server;

import com.jenjinstudios.server.authentication.User;
import com.jenjinstudios.world.object.Actor;

/**
 * Represents a Player in the game world.
 *
 * @author Caleb Brinkman
 */
public class Player extends Actor implements User
{
	private String password;
	private String salt;
	private boolean loggedIn;

	public Player(String name) {
		super(name);
	}

	@Override
	public String getUsername() { return getName(); }

	@Override
	public String getPassword() { return password; }

	@Override
	public void setPassword(String password) { this.password = password; }

	@Override
	public String getSalt() { return salt; }

	@Override
	public void setSalt(String salt) { this.salt = salt; }

	@Override
	public boolean isLoggedIn() { return loggedIn; }

	@Override
	public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
}
