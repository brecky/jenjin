package com.jenjinstudios.server.authentication;

import com.jenjinstudios.server.database.DbTable;
import com.jenjinstudios.server.database.sql.TestConnectionFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
		DbTable<BasicUser> table = new UserTable(connection);
		List<BasicUser> users = table.lookup(Collections.singletonMap("username", "TestAccount1"));
		User testAccount1 = users.isEmpty() ? null : users.get(0);
		Assert.assertNotNull(testAccount1, "Test account was null");
		Assert.assertEquals(testAccount1.getUsername(), "TestAccount1", "Incorrect user returned.");
	}

	/**
	 * Test the user lookup functionality with invalid data.
	 *
	 * @throws Exception If there is an exception during testing.
	 */
	@Test
	public void testLookUpFakeUser() throws Exception {
		UserLookup table = new UserTable(connection);
		User user = table.findUser("This User Doesn't Exist.");
		Assert.assertNull(user, "User should not have existed.");
	}

	/**
	 * Test the update functionality.
	 *
	 * @throws Exception If there's an exception.
	 */
	@Test
	public void testUpdate() throws Exception {
		DbTable<BasicUser> table = new UserTable(connection);
		Map<String, Object> where = Collections.singletonMap("username", "TestAccount1");
		List<BasicUser> users = table.lookup(where);
		BasicUser testAccount1 = users.isEmpty() ? null : users.get(0);
		Assert.assertNotNull(testAccount1, "Test account was null");
		testAccount1.setLoggedIn(true);
		table.update(where, testAccount1);
		List<BasicUser> lookup = table.lookup(where);
		User user = lookup.isEmpty() ? null : lookup.get(0);
		Assert.assertNotNull(user, "Test account was null");
		Assert.assertTrue(user.isLoggedIn(), "User not updated");
	}
}