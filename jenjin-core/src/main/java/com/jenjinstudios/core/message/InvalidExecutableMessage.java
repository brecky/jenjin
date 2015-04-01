package com.jenjinstudios.core.message;

import com.jenjinstudios.core.Connection;
import com.jenjinstudios.core.concurrency.ExecutableMessage;
import com.jenjinstudios.core.concurrency.MessageContext;
import com.jenjinstudios.core.io.Message;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Invoked when a {@code Connection} reveives notification that an invalid message was received.
 *
 * @author Caleb Brinkman
 */
public class InvalidExecutableMessage extends ExecutableMessage<MessageContext>
{
    private static final Logger LOGGER = Logger.getLogger(InvalidExecutableMessage.class.getName());

    /**
     * Construct a new {@code InvalidExecutableMessage}.
     *
     * @param connection The connection invoking this executable message.
     * @param message The message which caused this executable message to be invoked.
	 * @param context The context in which to execute the message.
	 */
    @SuppressWarnings("WeakerAccess")
	public InvalidExecutableMessage(Connection connection, Message message, MessageContext context) {
		super(connection, message, context);
		LOGGER.log(Level.INFO, "InvalidExecutableMessage created for connection {0}", connection);
    }

    @Override
	public Message execute() {
		String messageName = (String) getMessage().getArgument("messageName");
        short messageID = (short) getMessage().getArgument("messageID");
        String reportMessage = "Connection reported invalid sent message: " + messageName + " (ID:  " + messageID + ')';
        LOGGER.log(Level.SEVERE, reportMessage);
		return null;
	}
}
