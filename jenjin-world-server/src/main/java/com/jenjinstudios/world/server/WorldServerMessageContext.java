package com.jenjinstudios.world.server;

import com.jenjinstudios.server.net.ServerMessageContext;
import com.jenjinstudios.world.World;

import java.util.Arrays;

/**
 * Used to maintain context for messages executed on a WorldServer connection.
 *
 * @author Caleb Brinkman
 */
public class WorldServerMessageContext extends ServerMessageContext<Player>
{
	private World world;
	private byte[] worldChecksum;

	/**
	 * Get the world managed by this context.
	 *
	 * @return The world.
	 */
	public World getWorld() { return world; }

	/**
	 * Set the world managed by this context.
	 *
	 * @param world The world.
	 */
	public void setWorld(World world) { this.world = world; }

	/**
	 * Get the checksum of the file containing the world.
	 *
	 * @return The checksum.
	 */
	public byte[] getWorldChecksum() { return Arrays.copyOf(worldChecksum, worldChecksum.length); }

	/**
	 * Set the checksum of the file containing the world.
	 *
	 * @param worldChecksum The checksum.
	 */
	public void setWorldChecksum(byte... worldChecksum) { this.worldChecksum = worldChecksum; }
}
