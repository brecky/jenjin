package com.jenjinstudios.server.database.sql;

import com.jenjinstudios.server.database.Authenticator;
import com.jenjinstudios.server.net.User;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;

/**
 * Test the UserTable class.
 *
 * @author Caleb Brinkman
 */
public class UserTableTest
{
	private Connection connection;

	/**
	 * Create a test connection for use in testing.
	 *
	 * @throws Exception If there is an exception during connection creation.
	 */
	@BeforeClass
	public void createTestConnection() throws Exception {
		connection = new TestConnectionFactory().createTestConnection();
	}

	/**
	 * Close the test connection.
	 *
	 * @throws Exception If there is an exception while closing the test connection.
	 */
	@AfterClass
	public void closeTestConnection() throws Exception {
		connection.close();
	}

	/**
	 * Test the user lookup functionality.
	 *
	 * @throws Exception If there is an exception during testing.
	 */
	@Test
	public void testLookUpUser() throws Exception {
		Authenticator connector = new Authenticator(connection);
		User testAccount1 = connector.lookUpUser("TestAccount1");
		Assert.assertEquals(testAccount1.getUsername(), "TestAccount1", "Incorrect user returned.");

	}

	/**
	 * Test the user lookup functionality with invalid data.
	 *
	 * @throws Exception If there is an exception during testing.
	 */
	@Test
	public void testLookUpFakeUser() throws Exception {
		Authenticator connector = new Authenticator(connection);
		User user = connector.lookUpUser("This User Doesn't Exist.");
		Assert.assertNull(user, "User should not have existed.");
	}
}