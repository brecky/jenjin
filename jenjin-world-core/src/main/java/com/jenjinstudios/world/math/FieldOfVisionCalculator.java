package com.jenjinstudios.world.math;

import com.jenjinstudios.world.Location;
import com.jenjinstudios.world.Zone;
import com.jenjinstudios.world.util.ZoneUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Caleb Brinkman
 */
public class FieldOfVisionCalculator
{
	private final Zone zone;
	private final int radius;
	private final int centerX;
	private final int centerY;
	private final Location center;

	@SuppressWarnings("SameParameterValue")
	public FieldOfVisionCalculator(Zone zone, Location center, int radius) {
		this.center = center;
		this.radius = radius;
		this.zone = zone;
		this.centerX = center.getX();
		this.centerY = center.getY();
	}

	public List<Location> scan() {
		List<Location> visibles = new LinkedList<>();
		visibles.add(center);
		for (int x = centerX - radius; x < centerX + radius; x++)
		{
			addColumn(visibles, x);
		}
		return visibles;
	}

	private void addColumn(List<Location> visibles, int x) {
		for (int y = centerY - radius; y < centerY + radius; y++)
		{
			Location current = ZoneUtils.getLocationOnGrid(zone, x, y);
			if (current != null && inRange(current))
			{
				visibles.add(current);
			}
		}
	}

	protected boolean inRange(Location location) {
		double deltaX = location.getX() - centerX;
		double deltaY = location.getY() - centerY;
		double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
		return distance < radius;
	}
}
