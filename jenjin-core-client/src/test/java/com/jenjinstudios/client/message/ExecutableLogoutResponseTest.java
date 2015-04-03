package com.jenjinstudios.client.message;

import com.jenjinstudios.client.net.Client;
import com.jenjinstudios.client.net.ClientMessageContext;
import com.jenjinstudios.client.net.LoginTracker;
import com.jenjinstudios.core.io.Message;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the ExecutableLogoutResponse class.
 *
 * @author Caleb Brinkman
 */
public class ExecutableLogoutResponseTest
{
    /**
     * Test the execution of the ExecutableLogoutResponse.
     */
    @Test
    public void testMessageExecution() {
		Message loginResponse = mock(Message.class);
		when(loginResponse.getArgument("success")).thenReturn(true);

		Client client = mock(Client.class);
		ClientMessageContext context = mock(ClientMessageContext.class);
		LoginTracker loginTracker = mock(LoginTracker.class);
		when(context.getLoginTracker()).thenReturn(loginTracker);

		ExecutableLogoutResponse response = new ExecutableLogoutResponse(client, loginResponse, context);
		response.execute();

        verify(loginTracker).setLoggedIn(false);
    }
}
