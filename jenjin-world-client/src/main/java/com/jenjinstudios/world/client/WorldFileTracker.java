package com.jenjinstudios.world.client;

import com.jenjinstudios.world.World;
import com.jenjinstudios.world.io.WorldDocumentException;
import com.jenjinstudios.world.io.WorldDocumentReader;
import com.jenjinstudios.world.io.WorldDocumentWriter;

import java.io.*;
import java.util.Arrays;

/**
 * @author Caleb Brinkman
 */
public class WorldFileTracker
{
	private final File worldFile;
	private boolean waitingForChecksum;
	private byte[] checksum;
	private boolean waitingForFile;
	private byte[] bytes;
	private WorldDocumentReader worldDocumentReader;

	public WorldFileTracker(File worldFile) {
		this.worldFile = worldFile;
	}

	public void writeReceivedWorldToFile() throws WorldDocumentException {
		createNewFile(worldFile);
		World world = readWorldFromServer();
		writeWorldToFile(world);
	}

	public byte[] getChecksum() { return checksum; }

	public void setChecksum(byte[] checksum) { this.checksum = checksum; }

	public byte[] getBytes() { return bytes; }

	public void setBytes(byte[] bytes) { this.bytes = bytes; }

	public boolean isWaitingForChecksum() { return waitingForChecksum; }

	public void setWaitingForChecksum(boolean bool) { this.waitingForChecksum = bool; }

	public boolean isWaitingForFile() { return waitingForFile; }

	public void setWaitingForFile(boolean waiting) { this.waitingForFile = waiting; }

	protected World readWorldFromServer() throws WorldDocumentException {
		World world = null;
		if (bytes != null)
		{
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			worldDocumentReader = new WorldDocumentReader(byteArrayInputStream);
			world = worldDocumentReader.read();
		}
		return world;
	}

	protected World readWorldFromFile() throws WorldDocumentException {
		World world = null;
		if (worldFile != null && worldFile.exists())
		{
			try
			{
				FileInputStream inputStream = new FileInputStream(worldFile);
				worldDocumentReader = new WorldDocumentReader(inputStream);
				world = worldDocumentReader.read();
				bytes = worldDocumentReader.getWorldFileBytes();
			} catch (FileNotFoundException e)
			{
				throw new WorldDocumentException("Couldn't find world file.", e);
			}
		}
		return world;
	}

	public boolean needsWorldFile() {
		boolean checksumMismatch = true;
		boolean readerNull = worldDocumentReader == null;
		if (!readerNull)
		{
			checksumMismatch = !Arrays.equals(getChecksum(),
				  worldDocumentReader.getWorldFileChecksum());
		}
		return checksumMismatch;
	}

	private static void createNewFile(File worldFile) throws WorldDocumentException {
		if (!tryCreateWorldFileDirectory(worldFile) || !tryCreateWorldFile(worldFile))
		{
			throw new WorldDocumentException("Unable to create new world file!");
		}
	}

	private static boolean tryCreateWorldFile(File worldFile) throws WorldDocumentException {
		try
		{
			return worldFile.exists() || worldFile.createNewFile();
		} catch (IOException e)
		{
			throw new WorldDocumentException("Unable to create new file.", e);
		}
	}

	private static boolean tryCreateWorldFileDirectory(File worldFile) {
		return worldFile.getParentFile().exists() || worldFile.getParentFile().mkdirs();
	}

	public void waitForWorldFile() {
		while (isWaitingForFile())
		{
			WorldClient.waitTenMillis();
		}
	}

	private void writeWorldToFile(World world) throws WorldDocumentException {
		try (FileOutputStream worldOut = new FileOutputStream(worldFile))
		{
			WorldDocumentWriter worldDocumentWriter = new WorldDocumentWriter(world);
			worldDocumentWriter.write(worldOut);
		} catch (IOException ex)
		{
			throw new WorldDocumentException("Unable to write world file.", ex);
		} catch (NullPointerException ex)
		{
			throw new WorldDocumentException("Can't write world to file until world data has been received.");
		}
	}
}
