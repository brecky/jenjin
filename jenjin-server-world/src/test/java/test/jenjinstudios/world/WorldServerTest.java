package test.jenjinstudios.world;

import com.jenjinstudios.io.MessageRegistry;
import com.jenjinstudios.util.FileUtil;
import com.jenjinstudios.world.*;
import com.jenjinstudios.world.io.WorldFileReader;
import com.jenjinstudios.world.math.MathUtil;
import com.jenjinstudios.world.math.Vector2D;
import com.jenjinstudios.world.sql.WorldSQLHandler;
import org.junit.*;

import java.io.File;

/**
 * Test the world server.
 * @author Caleb Brinkman
 */
public class WorldServerTest
{
	// Server fields
	/** The world server used to test. */
	private WorldServer worldServer;
	/** The world used for testing. */
	private World world;
	/** The server-side actor representing the player. */
	private Actor serverPlayer;

	// Client fields
	/** The world client used to test. */
	private WorldClient worldClient;
	/** The client-side player used for testing. */
	private ClientPlayer clientPlayer;

	/**
	 * Construct the test.
	 * @throws Exception If there's an Exception.
	 */
	@BeforeClass
	public static void construct() throws Exception { MessageRegistry.registerXmlMessages(true); }

	/**
	 * Set up the client and server.
	 * @throws Exception If there's an exception.
	 */
	@Before
	public void setUp() throws Exception {
		initWorldServer();
		initWorldClient();
	}

	/**
	 * Tear down the client and server.
	 * @throws Exception If there's an exception.
	 */
	@After
	public void tearDown() throws Exception {
		serverPlayer.setVector2D(new Vector2D(0, 0));
		worldClient.sendBlockingLogoutRequest();
		worldClient.shutdown();

		worldServer.shutdown();

		File resourcesDir = new File("resources/");
		FileUtil.deleteRecursively(resourcesDir);
	}

	/**
	 * Test the actor visibility after player and actor movement.
	 * @throws Exception If there's an exception.
	 */
	@Test(timeout = 60000)
	public void testActorVisibility() throws Exception {
		double visibilityEdge = Location.SIZE * (SightedObject.VIEW_RADIUS + 1);
		Vector2D serverActorStartPosition = new Vector2D(0, visibilityEdge + 1);
		Vector2D serverActorTargetPosition = new Vector2D(0, visibilityEdge - 1);
		Actor serverActor = new Actor("TestActor");
		serverActor.setVector2D(serverActorStartPosition);
		world.addObject(serverActor);

		WorldTestUtils.moveServerActorToVector(serverActor, serverActorTargetPosition);

		WorldObject clientActor = worldClient.getPlayer().getVisibleObjects().get(serverActor.getId());
		Assert.assertEquals(1, worldClient.getPlayer().getVisibleObjects().size());
		Assert.assertNotNull(clientActor);
		Thread.sleep(50);
		Assert.assertEquals(serverActor.getVector2D(), clientActor.getVector2D());

		WorldTestUtils.moveServerActorToVector(serverActor, serverActorStartPosition);
		Assert.assertEquals(0, worldClient.getPlayer().getVisibleObjects().size());

		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(0, Location.SIZE + 1), clientPlayer, serverPlayer);
		Assert.assertEquals(1, worldClient.getPlayer().getVisibleObjects().size());
		clientActor = worldClient.getPlayer().getVisibleObjects().get(serverActor.getId());
		Assert.assertEquals(serverActor.getVector2D(), clientActor.getVector2D());

		WorldTestUtils.moveClientPlayerTowardVector(Vector2D.ORIGIN, clientPlayer, serverPlayer);
		Assert.assertEquals(0, worldClient.getPlayer().getVisibleObjects().size());
	}

	/**
	 * Test the state-forcing functionality.
	 * @throws Exception If there's an exception.
	 */
	@Test(timeout = 60000)
	public void testForcedStateFromEdge() throws Exception {
		WorldTestUtils.idleClientPlayer(1, clientPlayer);
		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(-1.0, 0), clientPlayer, serverPlayer);
		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(1, 0), clientPlayer, serverPlayer);
		Assert.assertFalse(clientPlayer.isForcedState());
		Assert.assertEquals(serverPlayer.getVector2D(), clientPlayer.getVector2D());
	}

	/**
	 * Test the state forcing functionality.
	 * @throws Exception If there's an exception.
	 */
	@Test(timeout = 60000)
	public void testForcedState() throws Exception {
		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(0.5, 0.5), clientPlayer, serverPlayer);
		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(-0.5, -0.5), clientPlayer, serverPlayer);
		WorldTestUtils.idleClientPlayer(5, clientPlayer);
		Assert.assertEquals(clientPlayer.getVector2D(), serverPlayer.getVector2D());
	}

	/**
	 * Test basic movement.
	 * @throws Exception If there's an exception.
	 */
	@Test(timeout = 60000)
	public void testMovement() throws Exception {
		Vector2D targetVector = new Vector2D(3.956, 3.7468);
		WorldTestUtils.moveClientPlayerTowardVector(targetVector, clientPlayer, serverPlayer);
	}

	/**
	 * Test repeatedly forcing client.
	 * @throws Exception If there's an exception.
	 */
	@Test(timeout = 60000)
	public void testRepeatedForcedState() throws Exception {
		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(.5, .5), clientPlayer, serverPlayer);
		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(-1, -1), clientPlayer, serverPlayer);
		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(.5, .5), clientPlayer, serverPlayer);
		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(-1, -1), clientPlayer, serverPlayer);
		WorldTestUtils.moveClientPlayerTowardVector(new Vector2D(.5, .5), clientPlayer, serverPlayer);
	}

	/**
	 * Test movement to various random vectors.
	 * @throws Exception If there's an exception.
	 */
	@Test(timeout = 60000)
	public void testRandomMovement() throws Exception {
		WorldTestUtils.idleClientPlayer(1, clientPlayer);
		int maxCoordinate = 5;
		for (int i = 0; i < 5; i++)
		{
			double randomX = MathUtil.round(java.lang.Math.random() * maxCoordinate, 4);
			double randomY = MathUtil.round(java.lang.Math.random() * maxCoordinate, 4);
			Vector2D random = new Vector2D(randomX, randomY);
			WorldTestUtils.moveClientPlayerTowardVector(random, clientPlayer, serverPlayer);
			double distance = clientPlayer.getVector2D().getDistanceToVector(serverPlayer.getVector2D());
			Assert.assertEquals("Movement number " + i + " to " + random, 0, distance, .001);
		}
	}

	/**
	 * Test attempting to walk into a "blocked" location.
	 * @throws Exception If there's an Exception.
	 */
	@Test(timeout = 60000)
	public void testAttemptBlockedLocation() throws Exception {
		Vector2D vector1 = new Vector2D(35, 0);
		Vector2D attemptedVector2 = new Vector2D(35, 35);
		Vector2D actualVector2 = new Vector2D(35, 29.8);
		Vector2D vector3 = new Vector2D(35, 25);
		Vector2D vector4 = new Vector2D(25, 25);
		Vector2D vector5 = new Vector2D(25, 35);
		Vector2D attemptedVector6 = new Vector2D(35, 35);
		Vector2D actualVector6 = new Vector2D(29.8, 35);
		Vector2D attemptedVector7 = new Vector2D(35, 35);
		Vector2D actualVector7 = new Vector2D(29.8, 35);

		// Move to (35, 0)
		WorldTestUtils.moveClientPlayerTowardVector(vector1, clientPlayer, serverPlayer);
		Assert.assertEquals(vector1, clientPlayer.getVector2D());

		// Attempt to move to (35, 35)
		// This attempt should be forced to stop one step away from
		WorldTestUtils.moveClientPlayerTowardVector(attemptedVector2, clientPlayer, serverPlayer);
		Assert.assertEquals(actualVector2, clientPlayer.getVector2D());

		WorldTestUtils.moveClientPlayerTowardVector(vector3, clientPlayer, serverPlayer);
		Assert.assertEquals(vector3, clientPlayer.getVector2D());

		WorldTestUtils.moveClientPlayerTowardVector(vector4, clientPlayer, serverPlayer);
		Assert.assertEquals(vector4, clientPlayer.getVector2D());

		WorldTestUtils.moveClientPlayerTowardVector(vector5, clientPlayer, serverPlayer);
		Assert.assertEquals(vector5, clientPlayer.getVector2D());

		WorldTestUtils.moveClientPlayerTowardVector(vector5, clientPlayer, serverPlayer);
		Assert.assertEquals(vector5, clientPlayer.getVector2D());

		WorldTestUtils.moveClientPlayerTowardVector(attemptedVector6, clientPlayer, serverPlayer);
		Assert.assertEquals(actualVector6, clientPlayer.getVector2D());

		WorldTestUtils.moveClientPlayerTowardVector(attemptedVector7, clientPlayer, serverPlayer);
		Assert.assertEquals(actualVector7, clientPlayer.getVector2D());
	}

	/**
	 * Initialize and log the client in.
	 * @throws Exception If there's an exception.
	 */
	private void initWorldClient() throws Exception {
		worldClient = new WorldClient(new File("resources/WorldTestFile.xml"), "localhost", WorldServer.DEFAULT_PORT, "TestAccount01", "testPassword");
		worldClient.blockingStart();
		worldClient.sendBlockingWorldFileRequest();
		worldClient.sendBlockingLoginRequest();

		/* The WorldClientHandler used to test. */
		WorldClientHandler worldClientHandler = worldServer.getClientHandlerByUsername(worldClient.getUsername());
		clientPlayer = worldClient.getPlayer();
		serverPlayer = worldClientHandler.getPlayer();
	}

	/**
	 * Initialize the world and world server.
	 * @throws Exception If there's an exception.
	 */
	private void initWorldServer() throws Exception {
		/* The world SQL handler used to test. */
		WorldSQLHandler worldSQLHandler = new WorldSQLHandler("localhost", "jenjin_test", "jenjin_user", "jenjin_password");
		worldServer = new WorldServer(new WorldFileReader(getClass().getResourceAsStream("/test/jenjinstudios/world/WorldFile01.xml")),
				WorldServer.DEFAULT_UPS, WorldServer.DEFAULT_PORT, WorldClientHandler.class, worldSQLHandler);
		world = worldServer.getWorld();
		worldServer.blockingStart();
	}

}
