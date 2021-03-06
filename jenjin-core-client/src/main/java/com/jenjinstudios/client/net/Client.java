package com.jenjinstudios.client.net;

import com.jenjinstudios.core.Connection;
import com.jenjinstudios.core.MessageIO;
import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageRegistry;

import java.io.IOException;
import java.security.KeyPair;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for any client.  This class uses a similar system to the JGSA.
 *
 * @author Caleb Brinkman
 */
public class Client extends Connection
{
    private static final int UPDATES_PER_SECOND = 60;
    /** The list of tasks that this client will execute each update cycle. */
    private final List<Runnable> repeatedTasks;
    /** The timer that manages the update loop. */
    private Timer sendMessagesTimer;
    private final ClientLoop clientLoop = new ClientLoop(this);

    /**
     * Construct a new client and attempt to connect to the server over the specified port.
     *
     * @param messageIO The MessageIO used to send and receive messages.
     */
    protected Client(MessageIO messageIO) {
        super(messageIO);
        repeatedTasks = new LinkedList<>();
    }

    /**
     * Generate a PingRequest message.
     *
     * @return The generated message.
     */
    private static Message generatePingRequest() {
        Message pingRequest = MessageRegistry.getInstance().createMessage("PingRequest");
        pingRequest.setArgument("requestTimeMillis", System.currentTimeMillis());
        return pingRequest;
    }

    /**
     * Add a task to the repeated queue of this client.  Should be called to extend client functionality.
     *
     * @param r The task to be performed.
     */
    public void addRepeatedTask(Runnable r) {
        synchronized (repeatedTasks)
        {
            repeatedTasks.add(r);
        }
    }

    /** Tell the client threads to stop running. */
    @Override
    public void shutdown() {
        super.shutdown();
        if (sendMessagesTimer != null)
        {
            sendMessagesTimer.cancel();
        }
    }

    @Override
    public void start() {
        KeyPair rsaKeyPair = generateRSAKeyPair();
        setRSAKeyPair(rsaKeyPair);

        // Finally, send a ping request to establish latency.
        getMessageIO().queueOutgoingMessage(generatePingRequest());

        sendMessagesTimer = new Timer("Client Update Loop", false);
        int period = 1000 / UPDATES_PER_SECOND;
        sendMessagesTimer.scheduleAtFixedRate(clientLoop, 0, period);

        super.start();
    }

    /** Run the repeated synchronized tasks. */
    protected void runRepeatedTasks() {
        synchronized (repeatedTasks)
        {
            for (Runnable r : repeatedTasks)
                r.run();
        }
    }

    /**
     * Get the average number of updates per second that this client is executing.
     *
     * @return The average number of updates per second that this client is executing.
     */
    public double getAverageUPS() { return 1.0d / clientLoop.getAverageRunTime(); }

    /**
     * The ClientLoop class is essentially what amounts to the output thread.
     *
     * @author Caleb Brinkman
     */

    private static class ClientLoop extends TimerTask
    {
        private static final int MAX_STORED_UPDATE_TIMES = 50;
        private static final int NANOS_TO_SECOND = 1000000000;
        private static final Logger LOGGER = Logger.getLogger(ClientLoop.class.getName());
        /** The Client for this loop. */
        private final Client client;
        private int updateCount;
        private long lastStart = System.nanoTime();
        private final long[] updateTimesNanos = new long[MAX_STORED_UPDATE_TIMES];

        /**
         * Construct a ClientLoop for the given client.
         *
         * @param client The client for this ClientLoop
         */
        ClientLoop(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            updateCount++;
            saveUpdateTime();
            client.runRepeatedTasks();
            client.getExecutableMessageQueue().runQueuedExecutableMessages();
            try
            {
                client.getMessageIO().writeAllMessages();
            } catch (IOException e)
            {
                LOGGER.log(Level.INFO, "Shutting down because of IOException", e);
                client.shutdown();
            }
        }

        private void saveUpdateTime() {
            long newStart = System.nanoTime();
            long timeElapsed = newStart - lastStart;
            lastStart = newStart;
            updateTimesNanos[updateCount % updateTimesNanos.length] = timeElapsed;
        }

        public double getAverageRunTime() {
            double maxIndex = Math.min(updateCount, updateTimesNanos.length);
            double total = 0;
            for (int i = 0; i < maxIndex; i++)
            {
                total += updateTimesNanos[i];
            }
            return total / maxIndex / NANOS_TO_SECOND;
        }

    }
}
