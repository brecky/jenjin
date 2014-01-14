package com.jenjinstudios.world;

import com.jenjinstudios.world.math.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Contains all the Zones, Locations and GameObjects.
 * @author Caleb Brinkman
 */
public class World
{
	/** The size of the world's location grid. */
	public final int SIZE = 50;
	/** The grid of locations in the game world. */
	private final Location[][] locationGrid;
	/** The GameObjects contained in the world. */
	private final ArrayList<WorldObject> worldObjects;
	/** The number of objects currently in the world. */
	private int objectCount;

	/** Construct a new World. */
	public World() {
		locationGrid = new Location[SIZE][SIZE];
		worldObjects = new ArrayList<>();
		for (int x = 0; x < SIZE; x++)
			for (int y = 0; y < SIZE; y++)
				locationGrid[x][y] = new Location(x, y);
	}

	/**
	 * Add an object to the world.
	 * @param object The object to add.
	 * @throws InvalidLocationException If an object is attempted to be added with an invalid location.
	 */
	public void addObject(WorldObject object) throws InvalidLocationException {
		if (object == null)
			throw new IllegalArgumentException("addObject(WorldObject obj) argument 0 not allowed to be null!");
		object.setWorld(this);
		object.setVector2D(object.getVector2D());
		synchronized (worldObjects)
		{
			object.setId(worldObjects.size());
			worldObjects.add(object);
		}
		objectCount++;
	}

	/**
	 * Remove an object from the world.  Specifically, sets the index of the given object in the world's array to null.
	 * @param object The object to remove.
	 */
	public void removeObject(WorldObject object) {
		synchronized (worldObjects)
		{
			worldObjects.set(object.getId(), null);
			object.getLocation().removeObject(object);
		}
		objectCount--;
	}

	/**
	 * Get the location from the zone grid that contains the specified vector2D.
	 * @param vector2D The vector2D
	 * @return The location that contains the specified vector2D.
	 */
	public Location getLocationForCoordinates(Vector2D vector2D) {
		double x = vector2D.getXCoordinate();
		double y = vector2D.getYCoordinate();
		if (!isValidLocation(new Vector2D(x, y)))
			return null;
		return locationGrid[(int) x / Location.SIZE][(int) y / Location.SIZE];
	}

	/**
	 * Determine whether the given vector lies within a valid location.
	 * @param vector2D The vector.
	 * @return Whether the vector lies within a valid location.
	 */
	public boolean isValidLocation(Vector2D vector2D) {
		double x = vector2D.getXCoordinate();
		double y = vector2D.getYCoordinate();
		return !(x < 0 || y < 0 || x / Location.SIZE >= SIZE || y / Location.SIZE >= SIZE);
	}

	/** Update all objects in the world. */
	public void update() {
		synchronized (worldObjects)
		{
			for (WorldObject o : worldObjects)
				if (o != null)
					o.update();
		}
	}

	/**
	 * Get an area of location objects.
	 * @param center The center of the area to return.
	 * @param radius The radius of the area.
	 * @return An ArrayList containing all valid locations in the specified area.
	 */
	public ArrayList<Location> getLocationArea(Location center, int radius) {
		ArrayList<Location> areaGrid = new ArrayList<>();
		int xStart = Math.max(center.X_COORDINATE - (radius - 1), 0);
		int yStart = Math.max(center.Y_COORDINATE - (radius - 1), 0);
		int xEnd = Math.min(center.X_COORDINATE + (radius - 1), locationGrid.length - 1);
		int yEnd = Math.min(center.Y_COORDINATE + (radius - 1), locationGrid.length - 1);

		for (int x = xStart; x <= xEnd; x++)
		{
			areaGrid.addAll(Arrays.asList(locationGrid[x]).subList(yStart, yEnd + 1));
		}

		return areaGrid;
	}

	/**
	 * Get the number of objects currently in the world.
	 * @return The number of objects currently in the world.
	 */
	public int getObjectCount() {
		return objectCount;
	}
}
