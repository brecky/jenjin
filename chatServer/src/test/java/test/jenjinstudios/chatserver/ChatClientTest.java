package test.jenjinstudios.chatserver;

import com.jenjinstudios.chatclient.ChatClient;
import com.jenjinstudios.chatserver.ChatClientHandler;
import com.jenjinstudios.jgsf.Server;
import org.junit.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import static org.junit.Assert.assertTrue;

/**
 * Test the chat functionality of the client.
 *
 * @author Caleb Brinkman
 */
public class ChatClientTest
{
	/** The chat server used for testing. */
	private static Server<ChatClientHandler> chatServer;
	/** This client should login successfully. */
	private static ChatClient goodClient01;
	/** This client should login successfully. */
	private static ChatClient goodClient02;

	/**
	 * Set up and run a server for this test.
	 *
	 * @throws SQLException If there is an error connecting to the MySql database.
	 */
	@BeforeClass
	public static void construct() throws SQLException
	{
		chatServer = new Server<>(50, 51019, ChatClientHandler.class);
		chatServer.blockingStart();
	}

	/**
	 * Shut the server down when all is said and done.
	 *
	 * @throws IOException If there is an error shutting down the server.
	 */
	@AfterClass
	public static void destroy() throws IOException
	{
		chatServer.shutdown();
	}

	/** Set up clients for each test. */
	@Before
	public void setUpClients()
	{
		goodClient01 = new ChatClient("localhost", 51019, "TestAccount01");
		goodClient01.blockingStart();

		goodClient02 = new ChatClient("127.0.0.1", 51019, "TestAccount02");
		goodClient02.blockingStart();
	}

	/** Destroy clients after each test. */
	@After
	public void destroyClients()
	{
		goodClient01.shutdown();
		goodClient02.shutdown();
	}

	/**
	 * Test the chat functionality.
	 *
	 * @throws InterruptedException If there is an error during thread sleep.
	 */
	@Test
	public void testSendChatMessage() throws InterruptedException
	{
		String message = "Hello world.";

		goodClient01.sendChatMessage(message);

		// A few lines to block until the second client receives the message.  5 second timeout.
		long startTime = System.currentTimeMillis();
		LinkedList<String> receivedMessages = new LinkedList<>();
		while (receivedMessages.isEmpty() && (System.currentTimeMillis() - startTime) < 5000)
			receivedMessages = goodClient02.getChatMessages();

		String receivedMessage = "Not the correct message.";

		if (!receivedMessages.isEmpty())
		{
			receivedMessage = receivedMessages.pop();
		}

		assertTrue(receivedMessage.contains(message));
	}
}
