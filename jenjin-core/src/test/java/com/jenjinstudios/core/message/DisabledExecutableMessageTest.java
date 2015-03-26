package com.jenjinstudios.core.message;

import com.jenjinstudios.core.Connection;
import com.jenjinstudios.core.io.Message;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

/**
 * Test the DisabledExecutableMessage class.
 *
 * @author Caleb Brinkman
 */
public class DisabledExecutableMessageTest
{

    /**
     * Ensure that the DisabledMessage cannot be invoked.
     */
    @Test(expectedExceptions = IllegalStateException.class)
    public void testMessageExecution() {
        Connection connection = mock(Connection.class);
        Message message = mock(Message.class);

        DisabledExecutableMessage executableMessage = new DisabledExecutableMessage(connection, message);
        executableMessage.runImmediate();
    }
}