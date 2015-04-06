package test.jenjinstudios.integration.integration.message;

import com.jenjinstudios.core.concurrency.ExecutableMessage;
import com.jenjinstudios.core.concurrency.MessageContext;
import com.jenjinstudios.core.io.Message;

/**
 * Used to test message execution.
 * @author Caleb Brinkman
 */
public class ExecutableTest extends ExecutableMessage<MessageContext>
{
	/**
	 * Construct a new ExecutableMessage; this should only ever be invoked reflectively, by a {@code Connection}'s
	 * update cycle.
	 *
	 * @param message The message that caused this {@code ExecutableMessage} to be created.
	 * @param context The context in which to execute the message.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public ExecutableTest(Message message, MessageContext context) {
		super(message, context);
	}

	@Override
	public Message execute() {
		return null;
	}
}
