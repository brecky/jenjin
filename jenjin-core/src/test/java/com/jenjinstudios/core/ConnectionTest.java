package com.jenjinstudios.core;

import com.jenjinstudios.core.io.*;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the {@code Connection} class.
 *
 * @author Caleb Brinkman
 */
@SuppressWarnings("OverlyCoupledClass")
public class ConnectionTest
{
    private static final MessageRegistry MESSAGE_REGISTRY = MessageRegistry.getInstance();
    private static final int INVALID_MESSAGE_ID = -255;
    private static final long REQUEST_TIME_SPOOF = 123456789L;

	/**
	 * Set up the message registry.
	 */
	@BeforeClass
	public void setUp() {
		MESSAGE_REGISTRY.register("Test Message Registry",
			  getClass().getClassLoader().getResourceAsStream("test/jenjinstudios/core/Messages.xml"));
		MESSAGE_REGISTRY.register("Core Message Registry",
			  getClass().getClassLoader().getResourceAsStream("com/jenjinstudios/core/io/Messages.xml"));
	}

	/**
	 * Clear the message registry.
	 */
	@AfterClass
	public void clearMessageRegistry() {
		MESSAGE_REGISTRY.clear();
	}

	/**
	 * Test the {@code shutdown} method.
	 *
	 * @throws Exception If there's an exception.
	 */
	@Test
	public void testShutDown() throws Exception {
		DataInputStreamMock dataInputStreamMock = new DataInputStreamMock();
		InputStream in = dataInputStreamMock.getIn();
        dataInputStreamMock.mockReadShort((short) INVALID_MESSAGE_ID);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

		MessageInputStream messageInputStream = new MessageInputStream(in);
		MessageOutputStream messageOutputStream = new MessageOutputStream(bos);
		MessageIO messageIO = new MessageIO(messageInputStream, messageOutputStream);
		Connection connection = new Connection(messageIO);
		connection.shutdown();

		Assert.assertTrue(connection.getMessageIO().getOut().isClosed(), "MessageOutputStream should be closed");
	}

	/**
	 * Test the ping request functionality.
	 *
	 * @throws Exception If there's an exception.
	 */
	@Test
	public void testPingRequest() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

        MessageInputStream messageInputStream = mock(MessageInputStream.class);
        MessageOutputStream messageOutputStream = new MessageOutputStream(bos);

        Message pingRequest = MESSAGE_REGISTRY.createMessage("PingRequest");
        pingRequest.setArgument("requestTimeMillis", REQUEST_TIME_SPOOF);

        when(messageInputStream.readMessage()).thenReturn(pingRequest).thenReturn(MESSAGE_REGISTRY.createMessage
              ("BlankMessage"));
		MessageIO messageIO = new MessageIO(messageInputStream, messageOutputStream);
		Connection connection = new Connection(messageIO);
		connection.start();
		Thread.sleep(100);
		connection.getExecutableMessageQueue().runQueuedExecutableMessages();
		connection.shutdown();

		// The connection should execute the InvalidExecutableMessage,
		byte[] bytes = bos.toByteArray();
		MessageInputStream mis = new MessageInputStream(new ByteArrayInputStream(bytes));
		Message msg = mis.readMessage();
        Assert.assertEquals(msg.name, "PingResponse", "Message not PingResponse");
    }

	/**
	 * Test the ping response functionality.
	 *
	 * @throws Exception If there's an exception.
	 */
	@Test
	public void testPingResponse() throws Exception {
		// Spoof an invalid message
		DataInputStreamMock dataInputStreamMock = new DataInputStreamMock();
		dataInputStreamMock.mockReadShort((short) 3);
		dataInputStreamMock.mockReadInt(1);
		dataInputStreamMock.mockReadByte((byte) 0);
		dataInputStreamMock.mockReadShort((short) 2);
		dataInputStreamMock.mockReadLong(System.currentTimeMillis());
		InputStream in = dataInputStreamMock.getIn();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		MessageInputStream messageInputStream = new MessageInputStream(in);
		MessageOutputStream messageOutputStream = new MessageOutputStream(bos);
		MessageIO messageIO = new MessageIO(messageInputStream, messageOutputStream);
		Connection connection = new Connection(messageIO);

		// Create and run the connection.  Normally, we would use connection.start() to spawn a new thread
		// but for testing purposes we want the connection to run in the current thread.
		connection.start();
		Thread.sleep(100);
		// Again, normally an implementation would schedule this, but that's excessive for testing purposes
		connection.getExecutableMessageQueue().runQueuedExecutableMessages();
		connection.shutdown();

		// Ping time should be extremely close to 0, but taking into account wonkiness with tests, I'll allow
		// up to 1000
        Assert.assertEquals(connection.getPingTracker().getAveragePingTime(), 0, 1000, "Ping response too high\n" +
              "this may be a one off, try running again before digging too deeply.");
    }
}
