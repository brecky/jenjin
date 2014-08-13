package com.jenjinstudios.demo.client.ui;

import com.jenjinstudios.world.Location;
import com.jenjinstudios.world.WorldObject;
import com.jenjinstudios.world.client.ClientPlayer;
import com.jenjinstudios.world.client.WorldClient;
import com.jenjinstudios.world.math.Angle;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

/**
 * @author Caleb Brinkman
 */
public class WorldCanvas extends Canvas
{
	private static final double SCALE = 75;
	private final ClientPlayer clientPlayer;
	private final LocationTileManager locationTileManager;
	private final ObjectTileManager objectTileManager;

	public WorldCanvas(WorldClient worldClient, double width, double height) {
		super(width, height);
		this.clientPlayer = worldClient.getPlayer();
		PlayerControlKeyHandler playerControlKeyHandler = new PlayerControlKeyHandler(worldClient);
		setOnKeyPressed(playerControlKeyHandler);
		setOnKeyReleased(playerControlKeyHandler);
		Platform.runLater(this::requestFocus);

		locationTileManager = new LocationTileManager();
		objectTileManager = new ObjectTileManager();
	}

	public void drawWorld() {
		clearBackground();
		drawLocations();
		drawObjects();
		drawPlayer();
	}

	protected void clearBackground() {
		GraphicsContext graphicsContext2D = getGraphicsContext2D();
		graphicsContext2D.setFill(Color.BLACK);
		graphicsContext2D.fillRect(0, 0, getWidth(), getHeight());
	}

	protected void drawLocations() { clientPlayer.getVisibleLocations().forEach(this::drawLocation); }

	protected void drawLocation(Location location) {
		Location pLoc = clientPlayer.getLocation();
		if (location != null && pLoc != null)
		{
			int xDiff = location.X_COORDINATE - pLoc.X_COORDINATE;
			int yDiff = location.Y_COORDINATE - pLoc.Y_COORDINATE;//+ 1;
			double xBuff = clientPlayer.getVector2D().getXCoordinate() % Location.SIZE;
			double yBuff = clientPlayer.getVector2D().getYCoordinate() % Location.SIZE;

			double x = getWidth() / 2 + (xDiff * SCALE - xBuff * (SCALE / Location.SIZE));
			double y = getHeight() / 2 - (yDiff * SCALE - yBuff * (SCALE / Location.SIZE)) - SCALE;

			GraphicsContext graphicsContext2D = getGraphicsContext2D();
			locationTileManager.setGraphicsColor(location, graphicsContext2D);
			graphicsContext2D.fillRect(x, y, SCALE, SCALE);
		}
	}

	protected void drawObjects() { clientPlayer.getVisibleObjects().values().forEach(this::drawObject); }

	protected void drawPlayer() {
		double x = getWidth() / 2;
		double y = getHeight() / 2;

		GraphicsContext graphicsContext2D = getGraphicsContext2D();
		graphicsContext2D.save();
		Angle pAngle = clientPlayer.getAngle();
		double angle = pAngle.getAbsoluteAngle();
		angle = -Math.toDegrees(angle);
		Rotate r = new Rotate(angle, x, y);
		graphicsContext2D.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
		Image objectTile = objectTileManager.getObjectTile(clientPlayer);
		graphicsContext2D.drawImage(objectTile, x - objectTile.getWidth() / 2, y - objectTile.getHeight() / 2);
		graphicsContext2D.restore();
	}

	private void drawObject(WorldObject o) {
		double xDiff = o.getVector2D().getXCoordinate() - clientPlayer.getVector2D().getXCoordinate();
		double yDiff = o.getVector2D().getYCoordinate() - clientPlayer.getVector2D().getYCoordinate();

		double yOrig = getHeight() / 2;
		double xOrig = getWidth() / 2;

		double locScale = (SCALE / Location.SIZE);

		double x = xOrig + (xDiff * locScale);
		double y = yOrig - (yDiff * locScale);

		GraphicsContext graphicsContext2D = getGraphicsContext2D();
		graphicsContext2D.save();
		Rotate r = new Rotate(-Math.toDegrees(o.getAngle().getAbsoluteAngle()), x, y);
		graphicsContext2D.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
		Image objectTile = objectTileManager.getObjectTile(o);
		graphicsContext2D.drawImage(objectTile, x - objectTile.getWidth() / 2, y - objectTile.getHeight() / 2);
		graphicsContext2D.restore();
	}
}
