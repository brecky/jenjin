package com.jenjinstudios.world.server.message;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.world.Actor;
import com.jenjinstudios.world.math.Angle;
import com.jenjinstudios.world.math.MathUtil;
import com.jenjinstudios.world.math.Vector2D;
import com.jenjinstudios.world.server.Player;
import com.jenjinstudios.world.server.WorldClientHandler;
import com.jenjinstudios.world.state.MoveState;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process a StateChangeRequest.
 * @author Caleb Brinkman
 */
@SuppressWarnings("WeakerAccess")
public class ExecutableStateChangeRequest extends WorldExecutableMessage
{
	private static final double MAX_CORRECT = Actor.MOVE_SPEED;
	private static final Logger LOGGER = Logger.getLogger(ExecutableStateChangeRequest.class.getName());
	private Angle angle;
	/** The new position, corrected for lag. */
	private Vector2D position;
	/** The position before correction. */
	private Vector2D uncorrectedPosition;
	private long timePast;
	private long timeOfChange;

	/**
	 * Construct a new ExecutableMessage.  Must be implemented by subclasses.
	 * @param handler The handler using this ExecutableMessage.
	 * @param message The message.
	 */
	public ExecutableStateChangeRequest(WorldClientHandler handler, Message message) {
		super(handler, message);
	}

	@Override
	public void runDelayed() {
		Player player = getClientHandler().getPlayer();
		if (!isCorrectionSafe(player))
		{
			Angle pAngle = player.getAngle();
			Vector2D vector2D = player.getVectorBeforeUpdate();
			MoveState forcedState = new MoveState(pAngle, vector2D, player.getLastStepTime());
			player.setForcedState(forcedState);
		} else
		{
			player.setPendingAngle(angle);
			player.setAngle(angle);
			player.setVector2D(position);
			//player.setLastStepTime(System.currentTimeMillis());
		}
	}

	@Override
	public void runImmediate() {
		double relativeAngle = (double) getMessage().getArgument("relativeAngle");
		double absoluteAngle = (double) getMessage().getArgument("absoluteAngle");
		double x = (double) getMessage().getArgument("xCoordinate");
		double y = (double) getMessage().getArgument("yCoordinate");
		timeOfChange = (long) getMessage().getArgument("timeOfChange");
		uncorrectedPosition = new Vector2D(x, y);
		angle = new Angle(absoluteAngle, relativeAngle);
		timePast = (System.currentTimeMillis() - timeOfChange);
		double distance = MathUtil.round(Actor.MOVE_SPEED * ((double) timePast / 1000d), 2);
		position = uncorrectedPosition.getVectorInDirection(distance, angle.getStepAngle());
	}

	private boolean isCorrectionSafe(Player player) {
		boolean safe = false;
		if (!Boolean.parseBoolean(player.getWorld().getLocationForCoordinates(player.getZoneID(),
			  player.getVector2D()).getProperties().getProperty("walkable")))
		{
			double tolerance = Actor.MOVE_SPEED * 0.1;
			Vector2D proposedPlayerOrigin = getPlayerOrigin(player);
			double distance = uncorrectedPosition.getDistanceToVector(proposedPlayerOrigin);
			boolean distanceWithinTolerance = distance < tolerance;
			if (!distanceWithinTolerance)
			{
				LOGGER.log(Level.FINEST, "Distance to origin oustide of tolerance: {0},{1}",
					  new Object[]{distance, tolerance});
			}
			double clientDistance = uncorrectedPosition.getDistanceToVector(position);
			boolean withinMaxCorrect = clientDistance < MAX_CORRECT;
			if (!withinMaxCorrect)
			{
				LOGGER.log(Level.FINEST, "Distance to correct oustide of tolerance. " +
							"Position: {0}, Corrected: {1}, Step Angle: {2}, Time: {3}, TimePast: {4}",
					  new Object[]{uncorrectedPosition, position, angle, timeOfChange, timePast});
			}
			// Tolerance of a single update to account for timing discrepency.
			safe = withinMaxCorrect && distanceWithinTolerance;
		}
		return safe;
	}

	private Vector2D getPlayerOrigin(Player player) {
		double originDistance = player.getVector2D().getDistanceToVector(uncorrectedPosition);
		double playerReverseAngle = player.getPendingAngle().reverseStepAngle();
		return player.getVector2D().getVectorInDirection(originDistance, playerReverseAngle);
	}
}
