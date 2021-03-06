package com.jenjinstudios.server.net;

import com.jenjinstudios.server.sql.Authenticator;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

/**
 * @author Caleb Brinkman
 */
@SuppressWarnings("unchecked")
public class AuthServerTest
{
	@Test
	public void testConstructor() throws Exception {
		ServerInit serverInit = new ServerInit();
		Authenticator authenticator = mock(Authenticator.class);

		AuthServer authServer = new AuthServer(serverInit, authenticator);
		authServer.shutdown();

		Assert.assertEquals(authServer.getAuthenticator(), authenticator);
	}
}
