package com.jenjinstudios.client.message;

import com.jenjinstudios.client.net.Client;
import com.jenjinstudios.client.net.ClientUser;
import com.jenjinstudios.client.net.LoginTracker;
import com.jenjinstudios.core.io.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Tests ExecutableLoginResponse.
 *
 * @author Caleb Brinkman
 */
public class ExecutableLoginResponseTest
{
    /**
     * Test execution of the message.
     */
    @Test
    public void testMessageExecution() {
		Message loginResponse = mock(Message.class);
		when(loginResponse.getArgument("success")).thenReturn(true);
		when(loginResponse.getArgument("loginTime")).thenReturn(12345L);

        ClientUser user = mock(ClientUser.class);
		Client client = mock(Client.class);
		LoginTracker loginTracker = mock(LoginTracker.class);
		when(loginTracker.isLoggedIn()).thenReturn(true);
        when(loginTracker.getLoggedInTime()).thenReturn(12345L);
		when(client.getLoginTracker()).thenReturn(loginTracker);
		when(client.getUser()).thenReturn(user);

		ExecutableLoginResponse response = new ExecutableLoginResponse(client, loginResponse, null);
		response.execute();

        verify(loginTracker).setLoggedInTime(12345L);
        Assert.assertEquals(loginTracker.getLoggedInTime(), 12345L, "Login time was not as expected.");
    }
}
