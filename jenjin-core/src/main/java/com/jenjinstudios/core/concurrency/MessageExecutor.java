package com.jenjinstudios.core.concurrency;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageRegistry;
import com.jenjinstudios.core.xml.MessageType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles a thread which executes all messages received by the given Connection.
 *
 * @author Caleb Brinkman
 */
public class MessageExecutor
{
	private static final Logger LOGGER = Logger.getLogger(MessageExecutor.class.getName());
	private final MessageExecutorTask messageExecutorTask;
	private final Timer runTimer;

	/**
	 * Construct a new MessageExecutor which takes messages from the given MessageReader, and constructs and executes
	 * appropriate ExecutableMessages under the given Connection.
	 *
	 * @param threadPool The threadPool under which to execute messages.
	 */
	public MessageExecutor(MessageThreadPool threadPool) {
		this.messageExecutorTask = new MessageExecutorTask(threadPool);
		runTimer = new Timer("MessageExecutor");
	}

	/**
	 * Start executing incoming messages.
	 */
	public void start() {
		runTimer.schedule(messageExecutorTask, 0, 10);
	}

	/**
	 * Stop executing incoming messages.
	 */
	public void stop() {
		runTimer.cancel();
	}

	private static class MessageExecutorTask extends TimerTask
	{
		private final ExecutableMessageFactory exMessageFactory;
		private final MessageThreadPool threadPool;

		protected MessageExecutorTask(MessageThreadPool threadPool) {
			this.threadPool = threadPool;
			exMessageFactory = new ExecutableMessageFactory(this.threadPool);
		}

		@Override
		public void run() {
			Iterable<Message> messages = threadPool.getReceivedMessages();
			messages.forEach(this::executeMessage);
		}

		private void executeMessage(Message message) {
			List<ExecutableMessage> executables = exMessageFactory.getExecutableMessagesFor(message);
			for (ExecutableMessage executable : executables)
			{
				if (executable == null)
				{
					LOGGER.log(Level.WARNING, "Invalid message received from MessageReader");
					Message invalid = generateInvalidMessage(message.getID(), message.name);
					threadPool.enqueueMessage(invalid);
				} else
				{
					Message response = executable.execute();
					if (response != null)
					{
						threadPool.enqueueMessage(response);
					}
				}
			}
		}

		private static Message generateInvalidMessage(short id, String messageName) {
			Message invalid = MessageRegistry.getGlobalRegistry().createMessage("InvalidMessage");
			invalid.setArgument("messageName", messageName);
			invalid.setArgument("messageID", id);
			return invalid;
		}
	}

	private static class ExecutableMessageFactory
	{
		private static final Constructor[] EMPTY_CONSTRUCTOR_ARRAY = new Constructor[0];
		private final MessageThreadPool threadPool;

		/**
		 * Construct an ExecutableMessageFactory for the specified threadPool.
		 *
		 * @param threadPool The threadPool for which this factory will produce ExecutableMessages.
		 */
		private ExecutableMessageFactory(MessageThreadPool threadPool) { this.threadPool = threadPool; }

		/**
		 * Given a {@code Connection} and a {@code Message}, create and return an appropriate {@code
		 * ExecutableMessage}.
		 *
		 * @param message The {@code Message} for which the {@code ExecutableMessage} is being created.
		 *
		 * @return The {@code ExecutableMessage} created for {@code threadPool} and {@code message}.
		 */
		public List<ExecutableMessage> getExecutableMessagesFor(Message message) {
			List<ExecutableMessage> executableMessages = new LinkedList<>();
			Collection<Constructor> execConstructors = getExecConstructors(message);

			for (Constructor constructor : execConstructors)
			{
				if (constructor != null)
				{
					executableMessages.add(createExec(message, constructor));
				} else
				{
					Object[] args = {threadPool.getClass().getName(), message.name};
					String report = "No constructor containing Connection or {0} as first argument type found for {1}";
					LOGGER.log(Level.SEVERE, report, args);
				}
			}
			return executableMessages;
		}

		private Collection<Constructor> getExecConstructors(Message message) {
			Collection<Constructor> constructors = new LinkedList<>();
			MessageType messageType = MessageRegistry.getGlobalRegistry().getMessageType(message.getID());
			for (String className : messageType.getExecutables())
			{
				Constructor[] execConstructors = EMPTY_CONSTRUCTOR_ARRAY;
				try
				{
					Class execClass = Class.forName(className);
					execConstructors = execClass.getConstructors();
				} catch (ClassNotFoundException ex)
				{
					LOGGER.log(Level.WARNING, "Could not find class: " + className, ex);
				}
				constructors.add(getAppropriateConstructor(execConstructors));
			}
			return constructors;
		}

		private ExecutableMessage createExec(Message msg, Constructor constructor) {
			ExecutableMessage executableMessage = null;
			try
			{
				executableMessage = (ExecutableMessage) constructor.newInstance(threadPool, msg);
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e)
			{
				LOGGER.log(Level.SEVERE, "Constructor not correct", e);
			}
			return executableMessage;
		}

		private Constructor getAppropriateConstructor(Constructor... execConstructors) {
			Constructor correctConstructor = null;
			for (Constructor constructor : execConstructors)
			{
				Class<?> firstParam = constructor.getParameterTypes()[0];
				if (firstParam.isAssignableFrom(threadPool.getClass()))
					correctConstructor = constructor;
			}
			return correctConstructor;
		}
	}
}