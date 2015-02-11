package com.jenjinstudios.core;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageRegistry;
import com.jenjinstudios.core.io.MessageTypeException;
import com.jenjinstudios.core.message.ExecutableMessage;
import com.jenjinstudios.core.message.ExecutableMessageFactory;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to continuously read {@code Message} objects from a {@code MessageInputStream}, invoke the
 * appropriate {@code ExecutableMessage}, and store it so that the {@code runeDelayed} method may be called later.
 *
 * @author Caleb Brinkman
 */
public class RunnableMessageReader implements Runnable
{
    private static final int MAX_INVALID_MESSAGES = 10;
    private static final Logger LOGGER = Logger.getLogger(RunnableMessageReader.class.getName());
    private final Connection connection;
    private int invalidMsgCount;

    /**
     * Construct a new {@code RunnableMessageReader} working for the given Connection.
     *
     * @param connection The {@code Connection} managing this reader.
     */
    public RunnableMessageReader(Connection connection) {
        this.connection = connection;
    }

    /**
     * Generate an InvalidMessage message for the given invalid ID and message name.
     *
     * @param id The ID of the invalid message.
     * @param name The Name of the invalid message.
     *
     * @return The generated InvalidMessage object.
     */
    private static Message generateInvalidMessage(short id, String name) {
        Message invalid = MessageRegistry.getInstance().createMessage("InvalidMessage");
        invalid.setArgument("messageName", name);
        invalid.setArgument("messageID", id);
        return invalid;
    }

    @Override
    public void run() {
        boolean success = true;
        while ((invalidMsgCount < MAX_INVALID_MESSAGES) && success)
        {
            try
            {
                Message currentMessage = connection.getMessageIO().getIn().readMessage();
                executeMessage(currentMessage);
            } catch (MessageTypeException e)
            {
                reportInvalidMessage(e);
            } catch (EOFException | SocketException e)
            {
                LOGGER.log(Level.FINER, "Connection closed: " + connection.getName(), e);
                success = false;
            } catch (IOException e)
            {
                LOGGER.log(Level.FINE, "IOException when attempting to read from stream.", e);
                success = false;
            }
        }
    }

    void executeMessage(Message message) {
        ExecutableMessageFactory messageFactory = new ExecutableMessageFactory(connection);
        Collection<ExecutableMessage> execs = messageFactory.getExecutableMessagesFor(message);
        for (ExecutableMessage exec : execs)
        {
            if (exec != null)
            {
                processExecutableMessage(exec);
            } else
            {
                processInvalidMessage(message);
            }
        }
    }

    private void processInvalidMessage(Message message) {
        Message invalid = generateInvalidMessage(message.getID(), message.name);
        connection.getMessageIO().queueOutgoingMessage(invalid);
    }

    private void processExecutableMessage(ExecutableMessage exec) {
        exec.runImmediate();
        connection.getExecutableMessageQueue().queueExecutableMessage(exec);
    }

    void reportInvalidMessage(MessageTypeException e) {
        LOGGER.log(Level.WARNING, "Input stream reported invalid message receipt.");
        Message unknown = generateInvalidMessage(e.getId(), "Unknown");
        connection.getMessageIO().queueOutgoingMessage(unknown);
        invalidMsgCount++;
    }
}
